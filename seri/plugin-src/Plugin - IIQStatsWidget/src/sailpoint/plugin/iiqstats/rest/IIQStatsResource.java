package sailpoint.plugin.iiqstats.rest;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;

import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.plugin.iiqstats.rest.IIQStatsDTO;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.object.Identity;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.IdentityRequest;
import sailpoint.object.PolicyViolation;
import sailpoint.object.Bundle;
import sailpoint.object.Certification;
import sailpoint.object.QueryOptions;
import sailpoint.object.Filter;
import sailpoint.tools.JdbcUtil;


@RequiredRight(value = "iiqstatsRESTAllow")
@Path("iiqstats")
public class IIQStatsResource extends BasePluginResource {
    public static final Log  log = LogFactory.getLog(IIQStatsResource.class);

    private SailPointContext context; // = this.getContext();

    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult collectStatistics() {
        log.debug("Getting Stats");
        
        try {
            context = SailPointFactory.getCurrentContext();
            
        } catch (Exception ex) {
            log.debug("Error creating Context");
        }

        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusWeeks(1L);
        List<IIQStatsDTO> listDTO = new ArrayList<IIQStatsDTO>();
        try {

            // int limit = this.getSettingInt("limit");

            // Get Identity Total
            if (this.getSettingBool("showidentity")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                int identitycount = context.countObjects(Identity.class, null);
                stats.setStatName("Identities");
                stats.setStatCount(Integer.toString(identitycount));
                stats.setStatUrl("define/identity/identities.jsf");
                
                //Will be used to query SIQ
                if (this.getSettingBool("showsiqusers")) {
                    try {
                        log.debug("showsiqusers");
                        //JdbcUtil jdbcutil = new JdbcUtil();
                        ResultSet rs = null;
                        Map<String,String> connectionMap = new HashMap<String,String>();
                        connectionMap.put("driverClass","net.sourceforge.jtds.jdbc.Driver");
                        connectionMap.put("password","Sailp0!nt");
                        connectionMap.put("user","Administrator");
                        connectionMap.put("url","jdbc:jtds:sqlserver://seri.sailpointdemo.com:1433/SecurityIQDB_SERI;useNTLMv2=true;domain=SERI;");
                        Connection conn = JdbcUtil.getConnection(connectionMap);
       
                        Statement stmt = conn.createStatement();
                        String sql = "SELECT count(user_name) from whiteops.ra_user;";
                        rs = stmt.executeQuery(sql);
                        int siqusercount = 0;
                        while (rs.next()) {
                            siqusercount = rs.getInt(1);
                        }
                        stats.setStatName("IIQ Identities/SIQ Identities");
                        stats.setStatCount(Integer.toString(identitycount) + "/" + Integer.toString(siqusercount));
                    } catch (Exception ex) {
                        log.debug("Error in showsiqusers");
                        //ex.printStackTrace();
                    }
            
                    listDTO.add(stats);
                }
                listDTO.add(stats);
            }
            
            if (this.getSettingBool("showbundles")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                int bundlecount = context.countObjects(Bundle.class, null);
                stats.setStatName("Roles");
                stats.setStatCount(Integer.toString(bundlecount));
                stats.setStatUrl("define/roles/roleTabs.jsf?forceLoad=true&tabState:tabPanelId=roleTabPanel");
                listDTO.add(stats);
            }
            
            if (this.getSettingBool("showunusedbundles")) {
                TreeSet<String> roles = new TreeSet<String>();
                TreeSet<String> usedroles = new TreeSet<String>();
                
                log.debug("ShowUnusedBundles");
                
                ArrayList<String> colsToRead = new ArrayList<String>();  
                colsToRead.add("name");  
                Iterator bundles = context.search(Bundle.class, null,colsToRead);
                while ( (null != bundles) && (bundles.hasNext()) ) {
                    Object [] thisRecord = (Object[]) bundles.next();  
                    
                    String name = (String) thisRecord[0];  
                    roles.add(name);
                }
                sailpoint.tools.Util.flushIterator(bundles);
                log.debug("***********");
                log.debug("Number of Roles = " + roles.size());
                
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions usedRoleQO = new QueryOptions();
                usedRoleQO.addFilter(Filter.eq("name", "assignedRoles"));
                ArrayList<String> usedColsToRead = new ArrayList<String>();
                usedColsToRead.add("value");
                Iterator usedRoles = context.search(IdentityEntitlement.class, usedRoleQO, usedColsToRead);
                while ( (null != usedRoles) && (usedRoles.hasNext()) ) {
                    Object [] record = (Object[]) usedRoles.next();  
                    
                    String value = (String) record[0];  
                    usedroles.add(value);
                }
                sailpoint.tools.Util.flushIterator(usedRoles);
                log.debug("Number of used roles = " + usedroles.size());
                int unusedroles = roles.size() - usedroles.size();
                log.debug("Number of Unused Roles = " + unusedroles);                

                //int bundlecount = context.countObjects(Bundle.class, null);
                stats.setStatName("Unused Roles");
                stats.setStatCount(Integer.toString(unusedroles));
                listDTO.add(stats);
            }
            
            if (this.getSettingBool("showmanagedattributes")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                int macount = context.countObjects(IdentityEntitlement.class, null);
                stats.setStatName("ManagedAttributes");
                stats.setStatCount(Integer.toString(macount));
                stats.setStatUrl("define/groups/accountGroups.jsf?forceLoad=true");
                listDTO.add(stats);
            }

            // Get Identity Entitlements Total
            if (this.getSettingBool("showentitlements")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                int identityentitlementscount = context.countObjects(IdentityEntitlement.class, null);
                stats.setStatName("Entitlements");
                stats.setStatCount(Integer.toString(identityentitlementscount));
                stats.setStatUrl("define/groups/accountGroups.jsf?forceLoad=true");
                listDTO.add(stats);
            }

            // Get PwdChange total
            if (this.getSettingBool("showpasswordchanges")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions pwdCQO = new QueryOptions();
                pwdCQO.addFilter(Filter.eq("type", "PasswordsRequest"));
                int pwdchange = context.countObjects(IdentityRequest.class, pwdCQO);
                stats.setStatName("Password Changes");
                stats.setStatCount(Integer.toString(pwdchange));
                stats.setStatUrl("manage/accessRequest/myAccessRequests.jsf");
                listDTO.add(stats);
            }

            if (this.getSettingBool("showrecentpasswordresets")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions pwdCLastWeekQO = new QueryOptions();
                pwdCLastWeekQO.addFilter(Filter.eq("type", "PasswordsRequest"));
                pwdCLastWeekQO.addFilter(Filter.gt("created", lastWeek.get(ChronoField.MILLI_OF_SECOND)));
                int pwdChangesLast7Days = context.countObjects(IdentityRequest.class, pwdCLastWeekQO);
                stats.setStatName("Recent Password Changes");
                stats.setStatCount(Integer.toString(pwdChangesLast7Days));
                stats.setStatUrl("manage/accessRequest/myAccessRequests.jsf");
                stats.setStatArrow("UP");
                listDTO.add(stats);
            }

            // Get PwdReset Total
            if (this.getSettingBool("showpasswordresets")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions pwdRQO = new QueryOptions();
                pwdRQO.addFilter(Filter.eq("type", "ForgotPassword"));
                int pwdreset = context.countObjects(IdentityRequest.class, pwdRQO);
                stats.setStatName("Password Changes");
                stats.setStatCount(Integer.toString(pwdreset));
                stats.setStatUrl("manage/accessRequest/myAccessRequests.jsf");
                listDTO.add(stats);
            }

            /*
             * QueryOptions pwdRLastWeekQO = new QueryOptions();
             * pwdRLastWeekQO.addFilter(Filter.eq("type", "ForgotPassword"));
             * pwdRLastWeekQO.addFilter(Filter.lt("created",
             * lastWeek.get(ChronoField.MILLI_OF_SECOND))); int
             * pwdResetsBeforeLastWeek =
             * context.countObjects(IdentityRequest.class, pwdRLastWeekQO); //
             * stats.setPwdResetsLastWeek(pwdResetsBeforeLastWeek); if (pwdreset
             * > pwdResetsBeforeLastWeek) { stats.setPwdResetArrow("Up"); } else
             * if (pwdreset < pwdResetsBeforeLastWeek) {
             * stats.setPwdResetArrow("Down"); }
             */

            // Get Policy Violations
            if (this.getSettingBool("showpolicyviolations")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                int policyviolationcount = context.countObjects(PolicyViolation.class, null);
                stats.setStatName("Policy Violations");
                stats.setStatCount(Integer.toString(policyviolationcount));
                stats.setStatUrl("manage/policyViolations/policyViolations.jsf?reset=true");
                listDTO.add(stats);
            }

            // Get AccessRequest Total
            if (this.getSettingBool("showaccessrequests")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions qo = new QueryOptions();
                qo.addFilter(Filter.eq("type", "AccessRequest"));
                int accessrequestcount = context.countObjects(IdentityRequest.class, qo);
                stats.setStatName("Access Requests");
                stats.setStatCount(Integer.toString(accessrequestcount));
                stats.setStatUrl("manage/accessRequest/myAccessRequests.jsf");
                listDTO.add(stats);
            }

            // Get Total Certification
            if (this.getSettingBool("showcertifications")) {
                IIQStatsDTO stats = new IIQStatsDTO();
                QueryOptions certQO = new QueryOptions();
                certQO.addFilter(Filter.eq("phase", "active"));
                int activecertcount = context.countObjects(Certification.class, certQO);
                stats.setStatName("Certifications");
                stats.setStatCount(Integer.toString(activecertcount));
                stats.setStatUrl("monitor/scheduleCertifications/viewAndEditCertifications.jsf?resetTab=true");
                listDTO.add(stats);
            }
            /*
                certQO.addFilter(Filter.eq("complete", true));
                int completedcertcount = context.countObjects(Certification.class, certQO);
                stats.setCompletedCertifications(completedcertcount);

                float percentcomplete = completedcertcount / activecertcount;
                if (percentcomplete > .75)
                    stats.setCompletedPercentColor("Green");
                else if (percentcomplete > .35)
                    stats.setCompletedPercentColor("Yellow");
                else
                    stats.setCompletedPercentColor("Red");
            }*/

        } catch (Exception ex) {
            log.debug("Exception in IIQStats Widget");
            //ex.printStackTrace();
        }

        int total = listDTO.size();
        log.debug("Size of return = " + total);
        return new ListResult(listDTO, total);
    }

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public String testConnection() {
        log.debug("GET test");
        
        String ret = "Success!";
        log.debug(ret);
        return ret;
    }

    @Override
    public String getPluginName() {
        return "iiqstats";
    }

}
