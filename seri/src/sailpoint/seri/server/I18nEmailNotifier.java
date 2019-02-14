package sailpoint.seri.server;

import java.util.List;

import sailpoint.api.SailPointContext;
import sailpoint.object.Configuration;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.server.SMTPEmailNotifier;
import sailpoint.tools.EmailException;
import sailpoint.tools.GeneralException;

public class I18nEmailNotifier extends SMTPEmailNotifier {

	@Override
  public void sendEmailNotification(SailPointContext context,
      EmailTemplate src, EmailOptions options) throws GeneralException,
      EmailException {
	  System.out.println("I18nEmailNotifier.sendEmailNotification");
	  
	  // Get the to: address from the EmailTemplate
	  String to=src.getTo();
	  // Look up the Identity
	  QueryOptions qo=new QueryOptions();
	  qo.add(Filter.eq("email", to));
	  
	  List<Identity> identities=context.getObjects(Identity.class, qo);
	  if(identities!=null) {
	  	// TODO: skip if we find more than one identity
	  	Identity iden=identities.get(0);
	  	// If we find it, get the 'locale' attribute
	  	String locale=iden.getStringAttribute("locale");
	  	if(locale!=null) {
	  		// If we have it, look up <template name>_<locale>
	  		EmailTemplate template=context.getObjectByName(EmailTemplate.class, src.getName()+"_"+locale);
	  		// if we find it, substitute the body and the subject
	  	  if(template!=null) {
	  	  	src.setBody(template.getBody());
	  	  	src.setSubject(template.getSubject());
	  	  	
	  	  	// Damn, have to do this again - it was done in InternalContext before the EmailTemplate was passed to us
	  	  	Configuration conf=Configuration.getSystemConfig();
	  	  	src = src.compile(context, conf, options);
	  	  }
	  	}
	  }
	  // then do the original thing
	  super.sendEmailNotification(context, src, options);
  }

	@Override
  public Boolean sendImmediate() {
	  System.out.println("I18nEmailNotifier.sendImmediate");
	  return Boolean.TRUE;
  }

	
	
	
}
