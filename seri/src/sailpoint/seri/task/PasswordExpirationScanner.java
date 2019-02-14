package sailpoint.seri.task;

import sailpoint.task.AbstractTaskExecutor;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.api.SailPointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.tools.Util;
import sailpoint.tools.GeneralException;
import sailpoint.object.Application;
import sailpoint.object.Identity;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
//import sailpoint.object.AuditEvent;

import sailpoint.object.TaskItem;

import java.util.List;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
//import javax.naming.directory.Attribute;
import javax.naming.NamingEnumeration;

import sailpoint.object.EmailTemplate;
import sailpoint.object.EmailOptions;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by jbounds on 7/10/2014.
 * Updated by jbounds on 7/5/2015
 */
public class PasswordExpirationScanner extends AbstractTaskExecutor {

  private SailPointContext _ctx;
  public static final String ARG_APPLICATION_ID = "appId";
  public static final String ARG_TEST_MODE = "testMode";
  public static final String ARG_EXPIRED = "expired";
  public static final String ARG_RANGE = "range";
  public static final String ARG_USER = "user";
  public static final String ARG_PASSWORD = "password";
  public static final String ARG_SEARCHBASE = "searchBase";
  public static final String ARG_ROOTSEARCHBASE = "rootSearchBase";
  public static final String ARG_EXPIRINGEMAILTEMPLATE = "expiringEmailTemplate";
  public static final String ARG_EXPIREDEMAILTEMPLATE = "expiredEmailTemplate";

  private static Log _log = LogFactory.getLog(PasswordExpirationScanner.class);

  // Used for Active Directory Time Conversion
  // (Current Time * ONE_HUNDRED_NANO) + EPOCHOFFSET = WINDOWS TIME
  private static long EPOCHOFFSET = 11644473600000L;
  private static long ONE_HUNDRED_NANO = 10000L;
  private static long ONE_DAY_NANO = 864000000000L;

  private String _appId;
  private Application _app;
  private boolean _checkExpired;
  private String _expiringTemplate;
  private String _expiredTemplate;

  private String _user; // If not set in taskdefinition, then get from
                        // Application definition
  private String _password; // If not set in taskdefinition, then get from
                            // Application definition
  private String _searchBase;
  private String _rootSearchBase;

  private String _range; // CSV list of day range
  private int pwdcount = 0; // Used for reporting, the total number of passwords
                            // about to expire
  private int expiredCount = 0; // Used for reporting, the number of expired
                                // passwords
  private int rangecount = 0; // Used for reporting, the number of password
                              // about to expire for each day in range

  // Used if LDAP does NOT have a password expiration value. Use this value to
  // determine how long a password is
  // valid. We will use this like a MaxPwdAge in LDAP
  // private long passwordExpirationDays; //Used if the LDAP does NOT have a
  // password expiration time

  private HashMap<String, Object> _iiqApplicationMap;

  // Allow the script to be run in "testMode". In "testMode" we only output the
  // details for who has invalid passwords and do not send out email
  // notifications.
  // When testMode is false we send out email notifications to the users.
  private boolean _testMode;

  public boolean terminate() {
    return false;
  }

  public HashMap<String, Object> getApplicationCredentials(String appId) {
    _log.trace("Entering getApplicationCredentials");
    HashMap<String, Object> map = new HashMap<String, Object>();
    try {
      _app = _ctx.getObjectById(Application.class, appId);

      if (_user == null) {
        _user = _app.getStringAttributeValue("user");

      }

      if (_password == null) {
        String encPwd = _app.getStringAttributeValue("password");
        _password = _ctx.decrypt(encPwd);
      }

      if (_searchBase == null) {
        String searchBase = _app.getStringAttributeValue("searchDN");
        
        //searchDN does not exist. Grab the first SearchDN in searchDNs
        if (searchBase == null) {
          List<HashMap> searchDNs = _app.getListAttributeValue("searchDNs");
          if (searchDNs != null) {
            HashMap searchDNMap = searchDNs.get(0);
            if (searchDNMap != null) {
              searchBase = (String) searchDNMap.get("searchDN");
            }
          }
        }
        _searchBase = searchBase;
      }
      map.put("host", _app.getStringAttributeValue("host"));
      map.put("port", _app.getStringAttributeValue("port"));
      map.put("appName", _app.getName());
      _log.debug("getApplicationCredentials " + Util.mapToString(map));

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    _log.trace("Exiting getApplicationCredentials");
    return map;
  }

  public Hashtable<String, String> setupSearchEnvironment(
      HashMap<String, Object> credentials) {
    _log.trace("Entering setupSearchEnvironment");
    // Creates the JNDI Environment Context
    Hashtable<String, String> environment = new Hashtable<String, String>();
    // _log.debug("setupSearchEnvironment incoming credentials = " +
    // Util.mapToString(credentials));
    environment.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory");
    environment.put(Context.PROVIDER_URL, "ldap://" + credentials.get("host")
        + ":" + credentials.get("port"));
    environment.put(Context.SECURITY_PRINCIPAL, _user);
    environment.put(Context.SECURITY_CREDENTIALS, _password);
    environment.put(Context.SECURITY_AUTHENTICATION, "simple");
    environment.put(Context.REFERRAL, "follow");
    _log.trace("setupSearchEnvironment outgoing environment = "
        + Util.mapToString(environment));
    _log.trace("Exiting setupSearchEnvironment");
    return environment;
  }

  public long queryActiveDirectoryMaxPasswordAge() throws Exception {
    _log.trace("Entering queryActiveDirectoryMaxPasswordAge");
    String maxpwdageFilter = "(objectClass=domain)";
    String maxpwdageSearchBase;
    // String maxpwdageSearchBase = "dc=seri,dc=sailpointdemo,dc=com";
    if (_rootSearchBase != null) {
      maxpwdageSearchBase = _rootSearchBase;
    } else {
      throw new GeneralException("No rootSearchBase");
    }
    _log.debug("Using maxpwdageSearchBase = " + maxpwdageSearchBase);

    Long maxPwdAgeLong = 0L;

    // LDAP search controls
    SearchControls searchCtlsAD = new SearchControls();
    searchCtlsAD.setSearchScope(SearchControls.OBJECT_SCOPE);

    // Using standard Port, check your installation
    Hashtable<String, String> environment = setupSearchEnvironment(_iiqApplicationMap);

    LdapContext ctxGC = new InitialLdapContext(environment, null);

    /********************************************************/
    /* maxPwdAge Day Check */
    /********************************************************/
    // MaxPwdAge is stored on cn=domain
    // MaxPwdAge is a negative value that represents the number of 100ns before
    // a pwd expires.
    // A MaxPwdAge of 45 days would display as -38880000000000
    // ctxGC.search
    NamingEnumeration mpaResults = ctxGC.search(maxpwdageSearchBase,
        maxpwdageFilter, searchCtlsAD);
    // _log.debug("Executed Query");
    String maxPwdAge;
    if (!mpaResults.hasMore()) {
      _log.debug("Don't have any results");
      _log.debug(mpaResults);
    }

    while (mpaResults.hasMore()) {
      // _log.debug("Got Query");
      SearchResult mpaResult = (SearchResult) mpaResults.next();

      javax.naming.directory.Attributes attributes = mpaResult.getAttributes();
      maxPwdAge = (String) attributes.get("maxPwdAge").get();
      // _log.debug("Queried maxPwdAge from AD = " + maxPwdAge);

      // maxPawdAge is a negative value. If not found, just exit.
      if (maxPwdAge != null) {
        maxPwdAgeLong = Long.parseLong(maxPwdAge);
        _log.debug("MaxPasswordAge in Win32Time = " + maxPwdAgeLong.toString());
        _log.debug("In English that is "
            + Math.abs(maxPwdAgeLong / ONE_DAY_NANO) + " days");
      } else {

        throw new GeneralException("No MaxPasswordAge");
        // return "No PasswordAge";
      }
    }
    // } catch (Exception ex) {
    // _log.debug(ex.getMessage());
    // ex.printStackTrace();
    // }
    _log.trace("Exiting queryMaxPasswordAge");
    return maxPwdAgeLong;
  }

  public NamingEnumeration queryActiveDirectoryForUsersWithExpiringPassword(
      long pwdUpperRange, long pwdLowerRange) {

    _log.trace("Entering queryActiveDirectoryForUsersWithExpiringPassword");
    Hashtable<String, String> environment = setupSearchEnvironment(_iiqApplicationMap);

    NamingEnumeration results = null;

    // LDAP search controls
    SearchControls searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    // Using standard Port, check your installation

    try {

      // Connect to the LDAP Environment
      LdapContext ctxGC = new InitialLdapContext(environment, null);

      // Only get users who have pwdLastSet set and within a certain range.
      // Also, ignore users who's passwords don't expire
      String pwdExpiringFilter = "(&(objectClass=person)(pwdLastSet=*)(pwdLastSet>="
          + pwdLowerRange
          + ")(pwdLastSet<="
          + pwdUpperRange
          + ")(!(userAccountControl:1.2.840.113556.1.4.803:=65536)))";
      // (!userAccountControl=65536)
      _log.debug("Expiring PWD Filter = " + pwdExpiringFilter);
      // _log.trace("Looking for users with Password last set >= " +
      // pwdLowerRange);
      // _log.trace("And Password Last Set <= " + pwdUpperRange);

      // Execute an LDAP search and Loop through the results
      results = ctxGC.search(_searchBase, pwdExpiringFilter, searchCtls);

    } catch (Exception ex) {
      _log.debug(ex.getMessage());
      ex.printStackTrace();
    }
    _log.trace("Exiting queryActiveDirectoryForUsersWithExpiringPassword");
    return results;
  }

  public NamingEnumeration queryActiveDirectoryForUsersWithExpiredPassword(
      long pwdWillExpire) {
    _log.trace("Entering queryActiveDirectoryForUsersWithExpiredPassword");
    NamingEnumeration results = null;

    // LDAP search controls
    SearchControls searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    // Using standard Port, check your installation

    Hashtable<String, String> environment = setupSearchEnvironment(_iiqApplicationMap);

    try {

      // Connect to the LDAP Environment
      LdapContext ctxGC = new InitialLdapContext(environment, null);

      // Only get users who have pwdLastSet set and within a certain range.
      // Also, ignore users who's passwords don't expire
      String pwdExpiredFilter = "(&(objectClass=person)(pwdLastSet=*)(pwdLastSet<="
          + pwdWillExpire + ")(!(userAccountControl=65536)))";
      _log.debug("Expired PWD Filter = " + pwdExpiredFilter);
      // (!userAccountControl=65536)
      // _log.debug("pwdLastSetFilter = " + pwdLastSetFilter);
      // _log.trace("Looking for users with Expired Password last set >= " +
      // pwdWillExpire);
      // _log.debug("And Password Last Set <= " + pwdUpperRange);

      // Used to find out how many users are affected.

      // Execute an LDAP search and Loop through the results
      results = ctxGC.search(_searchBase, pwdExpiredFilter, searchCtls);

    } catch (Exception ex) {
      _log.debug(ex.getMessage());
      ex.printStackTrace();
    }
    _log.trace("Exiting queryActiveDirectoryForUsersWithExpiredPassword");
    return results;
  }

  public void processADUsersWithExpiringPasswords(long pwdUpperRange,
      long pwdLowerRange, long dayRangelong) {
    _log.trace("Entering processADUsersWithExpiringPasswords");
    try {
      NamingEnumeration results = queryActiveDirectoryForUsersWithExpiringPassword(
          pwdUpperRange, pwdLowerRange);

      while (results.hasMore()) {
        _log.trace("LDAP Search for Users with Expiring Passwords");
        pwdcount++;
        rangecount++;

        SearchResult searchresult = (SearchResult) results.next();

        javax.naming.directory.Attributes attributes = searchresult
            .getAttributes();
        String dn = (String) attributes.get("distinguishedName").get();
        String pwdLastSet = (String) attributes.get("pwdLastSet").get();

        _log.debug("-------------------------------------------");
        _log.debug("dn: " + dn);
        _log.debug("pwdLastSet: " + pwdLastSet);
        _log.debug("-------------------------------------------");

        // Try to find a corresponding Identity to send an email to.
        QueryOptions qo = new QueryOptions();
        qo.addFilter(Filter.and(
            Filter.eq("links.application.name", "Active Directory"),
            Filter.eq("links.nativeIdentity", dn)));

        Iterator idResult = _ctx.search(Identity.class, qo);
        // Link link = _ctx.getObjectByName(Link.class,dn);

        // Assume we only get one result back!

        if (idResult.hasNext()) {
          Identity identity = (Identity) idResult.next();
          _log.debug("User " + identity.getName()
              + " password is about to expire in " + dayRangelong + " day(s)");
          if (!_testMode) {
            // If testmode is not set, then we send the email
            sendEmailExpiration(identity, Long.valueOf(dayRangelong).toString());
          } else {
            _log.debug("testMode: Skipping email for " + identity.getName());
          }
        } else {
          _log.debug("Found Password Expiration but no corresponding IdentityIQ user");
        }

        /*
         * if (link != null) { Identity identity = (Identity)
         * link.getIdentity(); _log.debug("User " + identity.getName() +
         * " password is about to expire in " + dayRangelong + " day(s)"); if
         * (!_testMode) { //If testmode is not set, then we send the email
         * sendEmailExpiration(identity, Long.valueOf(dayRangelong).toString());
         * } else { _log.debug("testMode: Skipping email for " +
         * identity.getName()); } } else { _log.debug(
         * "Found Password Expiration but no corresponding IdentityIQ user"); }
         */

      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    _log.trace("Exiting processADUsersWithExpiringPasswords");

  }

  public void processADUsersWithExpiredPasswords(long pwdWillExpire) {
    _log.trace("Entering processADUsersWithExpiredPasswords");
    try {
      NamingEnumeration expiredResults = queryActiveDirectoryForUsersWithExpiredPassword(pwdWillExpire);

      while (expiredResults.hasMore()) {
        SearchResult expiredResult = (SearchResult) expiredResults.next();
        javax.naming.directory.Attributes attributes = expiredResult
            .getAttributes();
        String dn = (String) attributes.get("distinguishedName").get();
        String pwdLastSet = (String) attributes.get("pwdLastSet").get();

        _log.debug("-------------------------------------------");
        _log.debug("dn: " + dn);
        _log.debug("pwdLastSet: " + pwdLastSet);
        _log.debug("-------------------------------------------");
        expiredCount++;

        QueryOptions qo = new QueryOptions();
        qo.addFilter(Filter.and(
            Filter.eq("links.application.name", "Active Directory"),
            Filter.eq("links.nativeIdentity", dn)));

        Iterator idResult = _ctx.search(Identity.class, qo);
        // Assuming we only get one result back!
        if (idResult.hasNext()) {
          Identity identity = (Identity) idResult.next();
          if (!_testMode) {
            sendEmailExpiration(identity, "expired");
          } else {
            _log.debug("testMode: Skipping email for " + identity.getName());
          }
        } else {
          _log.debug("Found Password Expiration but no corresponding user");
        }

        if (expiredResults.hasMore()) {
          _log.debug("Moving to next expired User");
        } else {
          _log.debug("No more users");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    _log.trace("Exiting processADUsersWithExpiredPasswords");
  }

  public HashMap<String, Long> getImportantADTimes() throws Exception {
    _log.trace("Entering getImportantADTime");
    HashMap<String, Long> importantTimes = new HashMap<String, Long>();

    long maxpwdage = queryActiveDirectoryMaxPasswordAge();
    _log.debug("1. MaxPwdAge = " + maxpwdage);
    importantTimes.put("maxpwdage", maxpwdage);

    // Get the current time in Millis
    Calendar nowCalendar = Calendar.getInstance();
    long nowUnixEpoch = nowCalendar.getTimeInMillis();
    _log.debug("2. Current Time in Millis = " + nowUnixEpoch);
    importantTimes.put("nowUnixEpoch", nowUnixEpoch);

    // Unix Epoch is number of seconds since Jan 1, 1970
    // Java Calendar & Date give you the number of milliseconds since Jan 1,
    // 1970
    // Windows Epoch is number of 100 nano seconds since Jan 1, 1601
    // Add the number of Milliseconds between Jan 1, 1601 and Jan 1, 1970
    long nowWindowsOffset = (nowUnixEpoch + EPOCHOFFSET);
    _log.debug("3. Millis since Jan 1, 1601 = " + nowWindowsOffset);
    importantTimes.put("nowWindowsOffset", nowWindowsOffset);

    // Now multiply to get 100 nanoseconds
    long nowWindowslong = (nowWindowsOffset * ONE_HUNDRED_NANO);
    _log.debug("4. Current Windows Time = " + nowWindowslong);
    importantTimes.put("nowWindowslong", nowWindowslong);

    // Add Current time to MaxPwdAge. MaxPwdAge is negative value.
    // This gives us the win32time for when passwords expire and will be a
    // smaller number that nowWindowsLong
    long pwdWillExpire = nowWindowslong + maxpwdage;
    _log.debug("5. Passwords have expired if less than this date pwdWillExpire = "
        + pwdWillExpire);
    importantTimes.put("pwdWillExpire", pwdWillExpire);

    _log.trace("Exiting getImportantADTime");
    return importantTimes;
  }

  public void execute(SailPointContext ctx, TaskSchedule sched,
      TaskResult result, sailpoint.object.Attributes<String, Object> args)
      throws Exception {
    _log.trace("Enter Execute");
    _ctx = ctx;
    _appId = args.getString(ARG_APPLICATION_ID);
    _range = args.getString(ARG_RANGE);
    _testMode = args.getBoolean(ARG_TEST_MODE);
    _checkExpired = args.getBoolean(ARG_EXPIRED);
    _user = args.getString(ARG_USER);
    _password = args.getString(ARG_PASSWORD);
    _searchBase = args.getString(ARG_SEARCHBASE);
    _rootSearchBase = args.getString(ARG_ROOTSEARCHBASE);
    _expiringTemplate = args.getString(ARG_EXPIRINGEMAILTEMPLATE);
    _expiredTemplate = args.getString(ARG_EXPIREDEMAILTEMPLATE);

    // _iiqApplicationMap = new HashMap<String,Object>();

    _iiqApplicationMap = getApplicationCredentials(_appId);

    _log.debug("Print TaskDefinition Variables");
    _log.debug("_appName = " + _app.getName());
    _log.debug("_appId = " + _appId);
    _log.debug("_range = " + _range);
    _log.debug("_testMode = " + _testMode);
    _log.debug("_checkExpired = " + _checkExpired);
    _log.debug("_user = " + _user);
    _log.debug("_password = " + _password);
    _log.debug("_searchBase = " + _searchBase);
    _log.debug("_rootSearchBase = " + _rootSearchBase);
    // _log.debug("host = " + )

    HashMap<String, Object> rangecountMap = new HashMap<String, Object>();

    // Application app = (Application) applicationMap.get("app");
    if (_app.getType().equals("Active Directory - Direct")) {
      _log.trace("Process Active Directory");
      HashMap<String, Long> importantTimes = getImportantADTimes();
      List<String> rangeList = Util.csvToList(_range);
      for (String rl : rangeList) {

        rangecount = 0; // Reset this variable for each iteration
        _log.debug("CSV Day Range value of " + rl);
        long dayRangeLong = Long.parseLong(rl);
        // long pwdUpperRange = importantTimes.get("pwdWillExpire") +
        // (ONE_DAY_NANO * dayRangeLong);

        // long pwdWouldExpire = importantTimes.get("nowWindowslong") -
        // importantTimes.get("maxpwdage");
        long pwdWillExpire = importantTimes.get("pwdWillExpire");
        long WillExpireLongUpper = pwdWillExpire
            + (ONE_DAY_NANO * (dayRangeLong + 1L));
        long WillExpireLongLower = WillExpireLongUpper - ONE_DAY_NANO;
        _log.debug("***********************************************");
        _log.debug("pwdWouldExpire      =   " + pwdWillExpire);
        _log.debug("WillExpireLongUpper =   " + WillExpireLongUpper);
        _log.debug("WillExpireLongLower =   " + WillExpireLongLower);
        _log.debug("***********************************************");

        // _log.debug("6a. Pwd will expire if less than pwdUpperRange  = " +
        // pwdUpperRange);
        // long pwdLowerRange = pwdUpperRange - (ONE_DAY_NANO);
        // _log.debug("6b. Pwd will expire if greater than pwdLowerRange = " +
        // pwdLowerRange);
        // processUsersWithExpiringPasswords(pwdUpperRange,pwdLowerRange,dayRangeLong);
        processADUsersWithExpiringPasswords(WillExpireLongUpper,
            WillExpireLongLower, dayRangeLong);

        _log.debug("Users with Passwords expiring in " + dayRangeLong
            + " day(s) is " + rangecount);
        rangecountMap.put("Expires in " + dayRangeLong + " day(s) count",
            rangecount);

      }

      if (_checkExpired) {
        processADUsersWithExpiredPasswords(importantTimes.get("pwdWillExpire"));
        _log.debug("Number of Users with expired password: " + expiredCount);
        result.setAttribute("expiredCount", expiredCount);
      }
    } else {
      _log.info("Application Type not supported");
      throw new GeneralException("Unsupported Application Type");
    }

    result.setAttribute("rangeCount", rangecountMap);
    _log.debug("Number of Users to Notify of Upcoming Password Expiration: "
        + pwdcount);
    result.setAttribute("pwdCount", pwdcount);
    _log.trace("Exiting Execute");
    return;
  }

  public void sendEmailExpiration(Identity identity, String numofDays) {

    String emailTemplateName;
    _log.trace("Entering sendEmailExpiration");
    String toAddress = identity.getEmail();
    if (null == toAddress) {
      toAddress = "noemail@sailpointdemo.com";
    }

    Map<String, Object> mailArgs = new HashMap<String, Object>();
    mailArgs.put("identityName", identity.getName());
    mailArgs.put("displayName", identity.getDisplayName());
    mailArgs.put("numofDays", numofDays);

    _log.debug("\nsendEmailExpiration for " + identity.getName() + " of type "
        + numofDays);
    // String emailTemplateName = "Password Notification-HTML";

    if (numofDays.equals("expired")) {
      _log.debug("sendEmailExpiration :: Type = expired");
      // .emailTemplateId = (String)
      // _app.getStringAttributeValue("expiredEmailTemplate");
      emailTemplateName = _expiredTemplate;

    } else {
      _log.debug("sendEmailExpiration :: Type != expired");
      // emailTemplateId = (String)
      // _app.getStringAttributeValue("expiringEmailTemplate");
      emailTemplateName = _expiringTemplate;
    }

    try {
      EmailTemplate template = _ctx.getObjectById(EmailTemplate.class,
          emailTemplateName);
      if (template == null) {
        _log.error("sendEmailExpiration :: Email Template not found");
        return;
      }

      EmailOptions mailopts = new EmailOptions(toAddress, mailArgs);

      _ctx.sendEmailNotification(template, mailopts);
      _log.debug("sendEmailExpiration :: Email Sent");
    } catch (GeneralException ex) {
      _log.debug(ex.getMessage());
      ex.printStackTrace();
    }
    _log.trace("Exiting sendEmailNotification");
  }

}