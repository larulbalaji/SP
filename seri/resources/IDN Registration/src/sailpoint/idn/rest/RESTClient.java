package sailpoint.idn.rest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class RESTClient {
  
  // THIS IS COPIED FROM THE IIQ DEPLOYMENT ACCELERATOR
  // TODO: MAINTAIN THIS SOMEWHERE ELSE CENTRALLY (Maybe whole REST Client, who knows..)
  
  private CookieStore cookieStore;

  private HttpClientContext localContext;

  private static final Log log = LogFactory.getLog(RESTClient.class);
  
  private int timeout=10000; // TODO: put this in the config UI
  
  public enum HTTPMethod {
    GET,
    POST,
    PUT,
    PATCH;
  };

  public RESTClient(String url, String user, String pass) {

//    this.iUrl=url;
//    this.iUsername=user;
//    this.iPassword=pass;
  }
  
  public Object doIDNRestCall(HTTPMethod method, String url, Map<String,String> headers, HttpEntity entity, String username, String password, Class returnType) throws Exception {
  
      if (returnType==null) returnType=Map.class; // default to Map (like a JSON structure)
      
      URI hostUri=null;
      try {
        hostUri=new URI(url);
      } catch (URISyntaxException ue) {
        throw new Exception("Invalid URI: "+url);
      }
  
  
      CloseableHttpClient httpclient = getPreEmptiveClient(hostUri, username, password);
  
      HttpUriRequest request=null;
      switch (method) {
        case GET:
          request=new HttpGet(hostUri);
          break;
        case POST:
          request=new HttpPost(hostUri);
          break;        
        case PUT:
          request=new HttpPut(hostUri);
          break;        
        case PATCH:
          request=new HttpPatch(hostUri);
          break;
        default:
          throw new Exception("Unknown method '"+method.toString()+" : "+url);
      }
  
      // Set any headers
      if (headers!=null) {
        for (Entry<String,String> itm: headers.entrySet()) {
          request.setHeader(itm.getKey(), itm.getValue());
        }
      }
  
      // some kind of payload
      if (entity!=null) {
        ((HttpEntityEnclosingRequest)request).setEntity(entity);
      }
      // make sure we have some kind of context to hold the cookies. Mmmmmm, cooookies
      if (localContext==null) {
        localContext = HttpClientContext.create();
      }
      if (cookieStore!=null) {
        localContext.setCookieStore(this.cookieStore);
      }
  
      try {
        CloseableHttpResponse response = httpclient.execute(request, localContext);
        cookieStore=localContext.getCookieStore();
        System.out.println("Cookies: "+cookieStore);
        if(log.isDebugEnabled()) {
          log.debug("response="+response.getStatusLine());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
  
        String output="";
        if(log.isDebugEnabled()) {
          log.debug("Output from Server .... \n");
        }
        String line;
        while ((line= br.readLine()) != null) {
          output+=line+"\n";
        }
        if(log.isDebugEnabled()) {
          log.debug(output);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode>=200&&statusCode<=299) {
          if (response.getEntity().getContentType().getValue().startsWith("application/json")) {
            // Deserialize will not allow us to return JSON as a string - so for this special
            // case we'll skip the deserializing
            Object retMap=output;
            if (returnType!=String.class) {
              JSONDeserializer deserializer = new JSONDeserializer();
              // deserializer
              retMap= deserializer.deserialize(output);
              // check return type?
  //            if (!(retMap.getClasses()==returnType)){
  //              throw new Exception(url+" - Expected return "+returnType.getName()+", got "+retMap.getClass().getName());
  //            }
            }
            return retMap;            
          } else if (response.getEntity().getContentType().getValue().startsWith("text/html")) {
            return output;
          } else {
            throw new Exception("Unexpected content type "+response.getEntity().getContentType());
          }
        } else {
          System.out.println("fail: payload=\n"+output);
          throw new Exception(method.toString() + " Failed: reason="+statusCode);
        }
  
      } catch (ClientProtocolException e) {
        throw new Exception("Connection failed - "+e, e);
      } catch (IOException e) {
        throw new Exception("Connection failed - "+e, e);
      }
  
    }
  
  private CloseableHttpClient getPreEmptiveClient(URI hostUri, String username, String password) {

    CredentialsProvider credsProvider = null;
    HttpHost target=null;

    if (username!=null && password !=null) {
      target=new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());    

      credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
          new AuthScope(target.getHostName(), target.getPort()),
          new UsernamePasswordCredentials(username, password));
    }


    FileInputStream f=null;
    SSLConnectionSocketFactory sslsf=null;
    try {

      KeyStore keystore=KeyStore.getInstance(KeyStore.getDefaultType());
      f=new FileInputStream(System.getProperty("java.home")+"/lib/security/cacerts");
      keystore.load(f, "changeit".toCharArray());

      SSLContext mSSLContextInstance = SSLContext.getInstance("TLS");
      TrustManager trustManager = new  IIQDATrustManager(keystore);
      TrustManager[] tms = new TrustManager[] { trustManager };
      mSSLContextInstance.init(null, tms, new SecureRandom());

      sslsf = new SSLConnectionSocketFactory(mSSLContextInstance);

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } finally {
      try {
        f.close();
      } catch (Exception e) {}
    }

    //    X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
    RequestConfig config = RequestConfig.custom()
        .setSocketTimeout(timeout)
        .setConnectTimeout(timeout)
        .build();

    HttpClientBuilder bldr=HttpClients.custom()
        .setSSLSocketFactory(sslsf)
        .setDefaultRequestConfig(config)
        .setRedirectStrategy(new LaxRedirectStrategy());
    //      .setHostnameVerifier(hostnameVerifier)
    if (credsProvider!=null) {
      bldr.setDefaultCredentialsProvider(credsProvider);
    }
    CloseableHttpClient httpclient = bldr.build(); 

    if (target!=null) {
      // Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local
      // auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(target, basicAuth);

      localContext = HttpClientContext.create();
      localContext.setAuthCache(authCache);
    }

    return httpclient;
  }
}

