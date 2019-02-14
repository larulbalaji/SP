package sailpoint.seri.reporting.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.LiveReport;
import sailpoint.object.QueryOptions;
import sailpoint.object.Sort;
import sailpoint.reporting.datasource.AbstractDataSource;
import sailpoint.reporting.datasource.JavaDataSource;
import sailpoint.tools.GeneralException;


public class ADNestedGroupsDataSource extends AbstractDataSource implements JavaDataSource {

  private static final Log log = LogFactory.getLog(ADNestedGroupsDataSource.class);

  private SailPointContext ctx;
  private Iterator<Record> resultIterator;
  private ArrayList<Record> results = new ArrayList<ADNestedGroupsDataSource.Record>();
  private Record currentRecord;

  @Override
  public String getBaseHql() {
    log.debug("getBaseHql: ");
    return null;
  }

  @Override
  public QueryOptions getBaseQueryOptions() {
    log.debug("getBaseQueryOptions: ");
    return null;
  }

  @Override
  public Object getFieldValue(String arg0) throws GeneralException {
    //log.debug("getFieldValue: ("+arg0+")");
    switch(arg0) {
      case "identity": return currentRecord.getIdentityName();
      case "application": return currentRecord.getApplicationName();
      case "group": return currentRecord.getGroupName();
      case "viagroup" : return currentRecord.getViaGroupName();
      case "direct" : return currentRecord.isDirect();
      case "account" : return currentRecord.getAccountName();
    }
    return "";
  }

  @Override
  public int getSizeEstimate() throws GeneralException {
    return 0;
  }

  @Override
  public Object getFieldValue(JRField arg0) throws JRException {
    log.debug("getFieldValue (JRValue): ");
    return null;
  }
  
  @Override
  public void setLimit(int arg0, int arg1) {
    log.debug("setLimit: ");

  }

  @Override
  public boolean next() throws JRException {
    boolean hasNext = resultIterator.hasNext();
    log.debug("next: "+hasNext);
    if(hasNext) currentRecord=resultIterator.next();
    return hasNext;
  }
  
  ///////////////////////////////////////////////////////
  // Main section
  ///////////////////////////////////////////////////////
  @Override
  public void initialize(SailPointContext context, LiveReport rpt,
      Attributes<String, Object> attrs, String groupBy, List<Sort> sort)
          throws GeneralException {

      this.ctx = context;
      
     // dump arguments 
     for (String attr : attrs.keySet()) {
         log.debug("Argument: "+ attr + ", value: "+ attrs.get(attr));
     }
    
     // Check arguments
     boolean noIdentity = false;
     boolean noManagedAttr = false;
     ArrayList<ManagedAttribute> ADgroups = new ArrayList<ManagedAttribute>();
     ArrayList<Identity> identities = new ArrayList<Identity>();
     
     // if no identities, we will use all identities having the entitlements
     List<String> identitiesId = (List<String>) attrs.get("identities");
     if (null == identitiesId || identitiesId.size() == 0) {
         log.warn("No identities passed to the report. Defaulting to all");
         noIdentity = true;
     } else {
         for (String identityId : identitiesId) {
             Identity anIdentity = context.getObjectById(Identity.class, identityId);
             log.debug("Found identity: "+ anIdentity.getDisplayableName());
             identities.add(anIdentity);
         }
     }
     
     // if no managed attributes are passed, we use all AD managed attributes
     List<String> maNames = (List<String>) attrs.get("entitlements");
     if (null == maNames || maNames.size() == 0) {
         log.warn("No entitlements passed to the report. Defaulting to all AD managed attributes");
         noManagedAttr = true;
     } else {
         for (String ADgroup : maNames) {
             QueryOptions qo = new QueryOptions();
             qo.add(Filter.eq("value", ADgroup));
             Iterator<ManagedAttribute> groups = context.search(ManagedAttribute.class, qo);
             while (groups.hasNext()) {
                 ManagedAttribute ma = groups.next();
                 ADgroups.add(ma);
                 log.debug("Found AD group: "+ ma.getAttribute("cn"));
             }
         }
     }
     
     // if no entitlements, fetch all entitlements of type Active Directory
     if (noManagedAttr || ADgroups.isEmpty()) {
         QueryOptions qo = new QueryOptions();
         qo.add(Filter.eq("application.type", "Active Directory - Direct"));
         
         Iterator<ManagedAttribute> allGroups = context.search(ManagedAttribute.class, qo);
         while (allGroups.hasNext()) {
             ADgroups.add(allGroups.next());
         }
     }
     log.debug("Loaded " + ADgroups.size() + " AD Groups");
     
  // Check if some groups in the selected list are inherited from other groups
     HashMap<ManagedAttribute, ArrayList<ManagedAttribute>> indirectGroups = new HashMap<ManagedAttribute, ArrayList<ManagedAttribute>>();
     for (ManagedAttribute adGroup : ADgroups) {
         ArrayList<ManagedAttribute> grpChildren = fetchInheritingGroups(adGroup);
         log.debug("List of AD groups fetched for " + adGroup.getAttribute("cn"));
         for (ManagedAttribute child : grpChildren) {
             log.debug("Group name:" + child.getAttribute("cn"));
         }
         if(grpChildren != null && !grpChildren.isEmpty()) {
             indirectGroups.put(adGroup, grpChildren);
         }
     }
     
     // for each AD group, find which identities have this group either directly or indirectly granted
     for (ManagedAttribute ma : ADgroups) {
        log.debug("Calling getRecords for :" + ma.getValue());
        getRecordsForGroup(ma, ma, indirectGroups, identities, results);
     }
     

     for (Record r : results) {
         log.debug("Record:" + r.toString());
     }
     resultIterator = results.iterator();
     
     
  }


  
  private void getRecordsForGroup(ManagedAttribute topGrp, ManagedAttribute viaGrp, HashMap<ManagedAttribute, ArrayList<ManagedAttribute>> grpWithChildren, ArrayList<Identity> id, ArrayList<Record> resultsRecurse) 
  throws GeneralException {
      
      
   // Find identities directly connected to this group
      log.debug("Finding identities for group:" + viaGrp.getValue());
      log.debug("Top group:" + topGrp.getValue());
      
      // if this group has children, get the records for each child
      if (grpWithChildren.containsKey(viaGrp) && grpWithChildren.get(viaGrp) != null) {
          for (ManagedAttribute childGrp : grpWithChildren.get(viaGrp)) {
              getRecordsForGroup(topGrp, childGrp, grpWithChildren, id, resultsRecurse);
          }
      }
      
      QueryOptions ieqo = new QueryOptions();
      if (id != null && !id.isEmpty()) {
          ieqo.add(
                  Filter.and(
                          Filter.eq("application.name", viaGrp.getApplication().getName()), 
                          Filter.eq("value", viaGrp.getValue())),
                          Filter.in("identity", id)
                          );
      } else {
          ieqo.add(
                  Filter.and(
                          Filter.eq("application.name", viaGrp.getApplication().getName()), 
                          Filter.eq("value", viaGrp.getValue()))
                          );
      }
      Iterator<IdentityEntitlement> entls = ctx.search(IdentityEntitlement.class, ieqo);
      if (entls != null) {
          while(entls.hasNext()) {
              IdentityEntitlement ie = entls.next();
           
              log.debug("---- found identity with this group:" + ie.getIdentity().getName());
              if (topGrp.getValue().compareTo(viaGrp.getValue()) == 0) {
                  Record newR = new Record(ie.getIdentity(), ie.getNativeIdentity(), topGrp, viaGrp, true);                   
                  resultsRecurse.add(new Record(ie.getIdentity(), ie.getNativeIdentity(), topGrp, viaGrp, true));
              } else {
                  Record newR = new Record(ie.getIdentity(), ie.getNativeIdentity(), topGrp, viaGrp, false); 
                  if (!resultsRecurse.contains(newR)) {
                      log.debug("ADD - results does not contain this record" + newR.toString());
                      resultsRecurse.add(newR);
                  }
              }
          }
      }      
  }
  
  //
  // Recursively get list of groups inheriting from passed group 
  // 
  private ArrayList<ManagedAttribute> fetchInheritingGroups(ManagedAttribute parent) throws GeneralException
  {
      
      ArrayList<ManagedAttribute> children = new ArrayList<ManagedAttribute>();
 
      ArrayList<ManagedAttribute> criteria = new ArrayList<ManagedAttribute>();
      criteria.add(parent);

      QueryOptions qo = new QueryOptions();
      qo.add(Filter.containsAll("inheritance", criteria));

      Iterator<ManagedAttribute> it = ctx.search(ManagedAttribute.class, qo);
      // if no children, return an empty list
      if (!it.hasNext()) {
          log.debug("fetchInheritingGroups - no children for "+parent);
          return new ArrayList<ManagedAttribute>();
      }
      
      while (it.hasNext()) {
          ManagedAttribute ag = (ManagedAttribute) it.next();
          log.debug(ag.getAttribute("cn") + " is a member of " + parent.getAttribute("cn"));
          children.addAll(fetchInheritingGroups(ag));
          children.add(ag);
      }
      
      return children;
  }

  // internal class to represent a record in the result set
  
  private class Record {
      String groupName;
      String viaGroupName;
      String accountName;
      String applicationName;
      String identityName;
      boolean direct;
    
    public Record(Identity i, String accountName, ManagedAttribute ma, ManagedAttribute viaGrp, boolean isDirect) {
        groupName = ma.getValue();
        viaGroupName = viaGrp.getValue();
        applicationName = ma.getApplication().getName();
        identityName = i.getName();
        this.accountName = accountName;
        direct = isDirect;
    }
    
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getApplicationName() {
        return applicationName;
    }
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    public String getIdentityName() {
        return identityName;
    }
    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }
    public boolean isDirect() {
        return direct;
    }
    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public String getViaGroupName() {
        return viaGroupName;
    }

    public void setViaGroupName(String viaGroup) {
        this.viaGroupName = viaGroup;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        return "Record [groupName=" + groupName + ", viaGroupName="
                + viaGroupName + ", applicationName=" + applicationName
                + ", identityName=" + identityName + ", direct=" + direct + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getOuterType().hashCode();
        result = prime * result
                + ((accountName == null) ? 0 : accountName.hashCode());
        result = prime * result
                + ((applicationName == null) ? 0 : applicationName.hashCode());
        result = prime * result + (direct ? 1231 : 1237);
        result = prime * result
                + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result
                + ((identityName == null) ? 0 : identityName.hashCode());
        result = prime * result
                + ((viaGroupName == null) ? 0 : viaGroupName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Record other = (Record) obj;
        if (!getOuterType().equals(other.getOuterType()))
            return false;
        if (accountName == null) {
            if (other.accountName != null)
                return false;
        } else if (!accountName.equals(other.accountName))
            return false;
        if (applicationName == null) {
            if (other.applicationName != null)
                return false;
        } else if (!applicationName.equals(other.applicationName))
            return false;
        if (direct != other.direct)
            return false;
        if (groupName == null) {
            if (other.groupName != null)
                return false;
        } else if (!groupName.equals(other.groupName))
            return false;
        if (identityName == null) {
            if (other.identityName != null)
                return false;
        } else if (!identityName.equals(other.identityName))
            return false;
        if (viaGroupName == null) {
            if (other.viaGroupName != null)
                return false;
        } else if (!viaGroupName.equals(other.viaGroupName))
            return false;
        return true;
    }

    private ADNestedGroupsDataSource getOuterType() {
        return ADNestedGroupsDataSource.this;
    }
      
  }
  
}
