package sailpoint.seri.reporting.datasource;

import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.object.LiveReport;
import sailpoint.object.QueryOptions;
import sailpoint.object.Sort;
import sailpoint.reporting.datasource.AbstractDataSource;
import sailpoint.reporting.datasource.JavaDataSource;
import sailpoint.tools.GeneralException;

public class EntitlementUsageDataSource extends AbstractDataSource implements JavaDataSource {

  private static final Log log = LogFactory.getLog(Identity.class);

  private Iterator<Object[]> resultIterator;
  private Object[] row;

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
    log.debug("getFieldValue: ("+arg0+")");
    switch(arg0) {
      case "entitlement": return row[0];
      case "bundles": return row[2];
    }
    return arg0;
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
  public void initialize(SailPointContext context, LiveReport rpt,
      Attributes<String, Object> attrs, String groupBy, List<Sort> sort)
          throws GeneralException {

    // The HQL query for this is:@
    // select b.name from Bundle b left join b.profiles as p left join p.constraints as cons
    //    where cons like '%CN=All_Users,OU=Groups,OU=Demo,DC=seri,DC=sailpointdemo,DC=com%'
    
    // Get the managed entitlements that were specified
    // find the Roles (Bundles) that use them
    String hqlBase="select ma.displayName, ma.value, b.name from ManagedAttribute ma, Bundle b left join b.profiles as p left join p.constraints as cons";

    // append our where clauses
    Object obj=attrs.get("entitlements");
    List<String> lObj=(List<String>)obj;
    StringBuilder sb=new StringBuilder();
    sb.append(" where ");
    boolean first=true;
    for(String ma: lObj) {
      // where ma.displayName='All_Users' and cons like concat('%',ma.value,'%')
      if(first) first=false;
      else sb.append(" or ");
      sb.append("( ma.value='");
      sb.append(ma);
      sb.append("' and cons like concat('%',ma.value,'%') )");                   
    }
//    serilog.debug("query="+query);
//    serilog.debug("appending: "+sb.toString()+" <<");
    String hql=hqlBase+sb.toString();
      
    resultIterator = context.search(hql, null, null);

  }

  @Override
  public void setLimit(int arg0, int arg1) {
    log.debug("setLimit: ");

  }

  @Override
  public boolean next() throws JRException {
    boolean hasNext = resultIterator.hasNext();
    log.debug("next: "+hasNext);
    if(hasNext) row=resultIterator.next();
    return hasNext;
  }

}
