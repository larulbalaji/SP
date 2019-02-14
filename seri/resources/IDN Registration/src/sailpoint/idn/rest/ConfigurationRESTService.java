package sailpoint.idn.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import sailpoint.idn.rest.RESTClient.HTTPMethod;

@Path("configuration")
public class ConfigurationRESTService extends BaseRESTService {

  private static final Log log = LogFactory.getLog(ConfigurationRESTService.class);

  
  @GET
  @Produces("application/json")
  public Response getConfiguration() {
    
    /*
     * Get all the rows in the configuration table and return them as a map
     */
    try {
      Connection dbCon = getDBConnection();
      
      Map retval=new HashMap<String,String>();
      Statement stmt=dbCon.createStatement();
      ResultSet rs=stmt.executeQuery("SELECT NAME, VALUE FROM IDNCONFIG WHERE NAME='org' OR NAME='clientID' or NAME='secret' or NAME='source'");
      while (rs.next()) {
        String name=rs.getString("NAME");
        String value=rs.getString("VALUE");
        retval.put(name,  value);
      }
      dbCon.close();
      
      return Response.ok().entity(retval).build();
    } catch (NamingException ne) {
      return Response.serverError().entity("Naming Exception "+ne).build();
    } catch (SQLException e) {
      return Response.serverError().entity("Database problem: See server for details. ("+e+")").build();
    }
  }

  @POST
  @Produces("application/json")
  public Response setConfiguration(Map<String,Object> config) {
    
    try {
      Connection dbCon = getDBConnection();
      
      PreparedStatement stmt=dbCon.prepareStatement("REPLACE INTO IDNCONFIG(NAME, VALUE) VALUES(?, ?)");
      for (String key: config.keySet()) {
        Object value = config.get(key);

        String sValue=null;
        if (value instanceof String) {
          sValue=(String)value;
        } else {
          // some other kind of object; serialize it
          Gson gson=new Gson();
          sValue=gson.toJson(value);
        }
        
        stmt.setString(1, key);
        stmt.setString(2, sValue);
        stmt.execute();
      }
      if (!dbCon.getAutoCommit()) {
        dbCon.commit();
      }
      dbCon.close();
      
      Map resp=new HashMap();
      resp.put("result", "Update successful");
      return Response.ok().entity(resp).build();
    } catch (NamingException ne) {
      return Response.serverError().entity("Naming Exception "+ne).build();
    } catch (SQLException e) {
      return Response.serverError().entity("Database problem: See server for details. ("+e+")").build();
    }
    
  }

  @GET
  @Path("fields")
  @Produces("application/json")
  public Response getFields() {
    
    /*
     * Get all the rows in the configuration table and return them as a map
     */
    try {
      Connection dbCon = getDBConnection();
      
      String retval="{}";
      Statement stmt=dbCon.createStatement();
      ResultSet rs=stmt.executeQuery("SELECT VALUE FROM IDNCONFIG WHERE NAME='fields'");
      while (rs.next()) {
        String fields=rs.getString("VALUE");
        if (fields==null) fields="[]";
        retval="{ \"fields\": "+fields+" }";
      }
      dbCon.close();
      
      return Response.ok().entity(retval).build();
    } catch (NamingException ne) {
      return Response.serverError().entity("Naming Exception "+ne).build();
    } catch (SQLException e) {
      return Response.serverError().entity("Database problem: See server for details. ("+e+")").build();
    }
  }
  
  @GET
  @Path("sourcelist")
  @Produces("application/json")
  public Response getSourceList() {
    
    try {
      ConfigurationConfig configuration=getConfig();
      if (configuration.org==null) {
        return Response.serverError().entity("No IDN Org configured").build();        
      }
      
      String url = "https://"+configuration.org+".identitynow.com/api/source/list";
      RESTClient rc=new RESTClient(configuration.org, configuration.clientID, configuration.secret);
      
      String ret=(String)rc.doIDNRestCall(HTTPMethod.GET, url, null, null, configuration.clientID, configuration.secret, String.class);
      
      log.debug(ret);
      
      return Response.ok(ret).build();
      
    } catch (Exception e) {
      return Response.serverError().entity("Error making IDN REST Call: See server for details. ("+e+")").build();
    }
  }
  
  @GET
  @Path("accountschema/{source}")
  @Produces("application/json")
  public Response getAccountSchema(@PathParam("source") String source) {
    
    try {
      ConfigurationConfig configuration=getConfig();
      if (configuration.org==null) {
        return Response.serverError().entity("No IDN Org configured").build();        
      }
      
      String url = "https://"+configuration.org+".identitynow.com/api/source/getAccountSchema/"+source;
      log.debug(url);
      RESTClient rc=new RESTClient(configuration.org, configuration.clientID, configuration.secret);
      
      String ret=(String)rc.doIDNRestCall(HTTPMethod.GET, url, null, null, configuration.clientID, configuration.secret, String.class);
      
      log.debug(ret);
      
      return Response.ok(ret).build();
      
    } catch (Exception e) {
      return Response.serverError().entity("Error making IDN REST Call: See server for details. ("+e+")").build();
    }
  }
  
}
