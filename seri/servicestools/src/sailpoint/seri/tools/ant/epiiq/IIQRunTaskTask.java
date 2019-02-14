package sailpoint.seri.tools.ant.epiiq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import flexjson.JSONDeserializer;

public class IIQRunTaskTask extends Task{


  /*
   * 	 gets an oauth token
   */	

  private String iiqURL;
  private String username;
  private String password;
  private String task;

  public static void main(String[] args) {

    IIQRunTaskTask task=new IIQRunTaskTask();

    task.setIiqURL("http://localhost:8080/identityiq");
    task.setUsername("spadmin");
    task.setPassword("admin");
    task.setTask("Setup Demo");
    task.execute();

  }


  public void execute() throws BuildException {
    // TODO: Check for public key; if so encrypt password with public key
    // for passthrough auth

    if (iiqURL==null) {
      throw new BuildException("IIQ URL must be specified");
    }
    if (username==null) {
      throw new BuildException("username must be specified");
    }
    if (password==null) {
      throw new BuildException("password must be specified");
    }		
    if (task==null) {
      throw new BuildException("task name must be specified");
    }

    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "runTask");
    args.put("taskName", task);
    String response=doPost(args);
    if(!(response instanceof String)) {
      throw new BuildException("getObjects:\nExpected: String\nGot: "+response.getClass().getName());
    }

    System.out.println("IIQRunTaskTask.execute: "+response);

    //	return (String)resp.get("access_token");
  }


  public String getIiqURL() {
    return iiqURL;
  }


  public void setIiqURL(String iiqURL) {
    this.iiqURL = iiqURL;
  }


  public String getTask() {
    return task;
  }


  public void setTask(String task) {
    this.task = task;
  }


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  public String getPassword() {
    return password;
  }


  public void setPassword(String password) {
    this.password = password;
  }

  @SuppressWarnings("unchecked")
  private String doPost(Map<String,String> args) throws BuildException {
    URI hostUri=null;
    try {
      hostUri=new URI(iiqURL+"/");
      hostUri=hostUri.resolve("rest/workflows/Importer/launch");
      System.out.println("POST to: "+hostUri.toString());
    } catch (URISyntaxException ue) {
      throw new BuildException("Invalid URI: "+iiqURL);
    }

    HttpHost target=new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());    

    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
        new org.apache.http.auth.AuthScope(target.getHostName(), target.getPort()),
        new org.apache.http.auth.UsernamePasswordCredentials(username, password));
    HttpPost post=new HttpPost(hostUri);

    StringEntity entity=null;

    CloseableHttpClient httpclient = HttpClients.custom()
        .setDefaultCredentialsProvider(credsProvider).build();


    try {
      WorkflowArgsPayload payload=new WorkflowArgsPayload(args);
      entity=payload.getEntity();
      post.setEntity(entity);
      post.setHeader("accept", "application/json");
    } catch (UnsupportedEncodingException ue) {
      throw new BuildException("Unsupported Encoding");
    }
    HttpClientContext localContext = HttpClientContext.create();
    try {
      CloseableHttpResponse response = httpclient.execute(post, localContext);
      System.out.println("response="+response.getStatusLine());
      if(response.getStatusLine().getStatusCode()==200) {
        BufferedReader br = new BufferedReader(new InputStreamReader(
            (response.getEntity().getContent())));

        String output="";
        //        System.out.println("Output from Server .... \n");
        String line;
        while ((line= br.readLine()) != null) {
          output+=line+"\n";
        }
        //        System.out.println(output);
        JSONDeserializer<Map<String,Object>> deserializer = new JSONDeserializer<Map<String,Object>>();
        Map<String,Object> retMap= deserializer.deserialize(output);
        // We need the attributes.result string.
        // if it's 'failure', we throw a ConnectionException
        // if it's 'success', we return attributes.payload
        Map<String,Object> attributes=(Map<String,Object>)retMap.get("attributes");
        if(attributes==null) {
          List<String> errors=(List<String>)retMap.get("errors");          
          throw new BuildException("No 'attributes' entry in response");          
        }
        String result=(String)attributes.get("result");
        if(result==null) {
          throw new BuildException("No 'result' entry in response");          
        }
        String payload = (String)attributes.get("payload");
        switch(result) {
          case "failure":
            throw new BuildException("POST failed: "+payload);
          case "success":
            return payload;
          default:
            throw new BuildException("POST failed: Unexpected result value '"+result+"'");
        }
      } else {
        throw new BuildException("POST Failed: reason="+response.getStatusLine().getStatusCode());
      }

    } catch (ClientProtocolException e) {
      throw new BuildException("Connection failed - "+e, e);
    } catch (IOException e) {
      throw new BuildException("Connection failed - "+e, e);
    }
  }
}
