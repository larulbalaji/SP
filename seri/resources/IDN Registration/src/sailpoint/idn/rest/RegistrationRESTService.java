package sailpoint.idn.rest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;

import sailpoint.idn.rest.RESTClient.HTTPMethod;

@Path("/registration")
public class RegistrationRESTService extends BaseRESTService {

  private static final Log log = LogFactory.getLog(RegistrationRESTService.class);

  @POST
  @Path("/register")
  @Produces("application/json")
  public Response registerUser(Map payload) {

    System.out.println("payload="+payload);
    String ret=null;
    try {
      ConfigurationConfig configuration=getConfig();
      if (configuration.org==null) {
        return Response.serverError().entity("No IDN Org configured").build();        
      }
      
      String url = "https://"+configuration.org+".api.identitynow.com/v2/identities?sourceId="+getSource()+"&org="+configuration.org;
      RESTClient rc=new RESTClient(configuration.org, configuration.clientID, configuration.secret);
      
      Gson gson=new Gson();
      String json=gson.toJson(payload);
      StringEntity se=new StringEntity(json);
      System.out.println("json="+json);
      
      Map<String,String> headers=new HashMap<String,String>();
      headers.put("Content-Type", "application/json");
      ret=(String)rc.doIDNRestCall(HTTPMethod.POST, url, headers, se, configuration.clientID, configuration.secret, String.class);
      
      log.debug(ret);
      
      return Response.ok(ret).build();
      
    } catch (Exception e) {
      System.out.println(ret);
      e.printStackTrace();
      return Response.serverError().entity("Error making IDN REST Call: See server for details. ("+e+")").build();
    }
  }
  
  private String getSource() throws Exception {
    String src=null;
    try {
      Connection dbCon = getDBConnection();
      
      Map retval=new HashMap<String,String>();
      Statement stmt=dbCon.createStatement();
      ResultSet rs=stmt.executeQuery("SELECT VALUE FROM IDNCONFIG WHERE NAME='source'");
      while (rs.next()) {
        String value=rs.getString("VALUE");
        src=value;
      }
      dbCon.close();
      
    } catch (NamingException ne) {
      throw new Exception("Naming Exception "+ne);
    } catch (SQLException e) {
      throw new Exception("Database problem: See server for details. ("+e+")");
    }
    return src;
  }
}