package sailpoint.idn.rest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class BaseRESTService {

  protected ConfigurationConfig getConfig() throws SQLException, NamingException {
    
    Connection dbCon = getDBConnection();
    
    Map retval=new HashMap<String,String>();
    Statement stmt=dbCon.createStatement();
    ResultSet rs=stmt.executeQuery("SELECT NAME, VALUE FROM IDNCONFIG");
    while (rs.next()) {
      String name=rs.getString("NAME");
      String value=rs.getString("VALUE");
      retval.put(name,  value);
    }
    ConfigurationConfig conf=new ConfigurationConfig();
    conf.org=(String)retval.get("org");
    conf.clientID=(String)retval.get("clientID");
    conf.secret=(String)retval.get("secret");
    
    return conf;
  }
  
  protected class ConfigurationConfig {
    String org;
    String clientID;
    String secret;
  }
  
  protected Connection getDBConnection() throws NamingException, SQLException {
    Context initContext = new InitialContext();
    Context webContext = (Context)initContext.lookup("java:/comp/env");

    DataSource ds = (DataSource) webContext.lookup("jdbc/configDB");
    Connection dbCon = ds.getConnection();
    return dbCon;
  }
  

}
