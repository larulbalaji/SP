package sailpoint.idn.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("rest")
public class RESTApplication extends Application {

  public Set<Class<?>> getClasses() {

    System.out.println("-------------------");
    System.out.println("--  Get Classes  --");
    System.out.println("-------------------");

    Set<Class<?>> s = new HashSet<Class<?>>();
    s.add(ConfigurationRESTService.class);
    s.add(RegistrationRESTService.class);
    return s;
  }

}
