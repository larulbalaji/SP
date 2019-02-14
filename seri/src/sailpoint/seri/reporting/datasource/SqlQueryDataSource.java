package sailpoint.seri.reporting.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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


public class SqlQueryDataSource extends AbstractDataSource implements JavaDataSource  {

  private static final Log log = LogFactory.getLog(SqlQueryDataSource.class);

  private SailPointContext ctx;
  private ResultSet resultIterator;
  private int currentLine = 0;


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
      Object fieldValue = null;
      try {
          if (arg0.equals("id")) {
              fieldValue = new Integer(currentLine).toString();
          } else {
              fieldValue =  resultIterator.getObject(arg0);
          }
      }
      catch (SQLException se) {
          throw new GeneralException(se);
      }
      return fieldValue;
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
    boolean hasNext = false;
    try {
        hasNext = resultIterator.next();
        currentLine++;
    }
    catch (SQLException se) {
        throw new JRException(se);
    }
    log.debug("next: "+hasNext);
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
     String sqlQuery = (String)attrs.get("normalisedQuery");
     
     // get a connection to the DB to execute the query
     try {
         Connection conn = context.getJdbcConnection();
         PreparedStatement sqlCommand = conn.prepareStatement(sqlQuery);
         resultIterator = sqlCommand.executeQuery();
     }
     catch (SQLException se) {
         throw new GeneralException(se);
     }
  }

    private SqlQueryDataSource getOuterType() {
        return SqlQueryDataSource.this;
    }
      
  }
  

