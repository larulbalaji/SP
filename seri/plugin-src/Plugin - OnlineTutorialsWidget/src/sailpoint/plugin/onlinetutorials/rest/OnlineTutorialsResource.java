package sailpoint.plugin.onlinetutorials.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.object.DashboardContent;
import sailpoint.object.Attributes;
import sailpoint.plugin.onlinetutorials.rest.OnlineTutorialsDTO;
import sailpoint.tools.Message;

@RequiredRight(value = "onlineTutorialsRESTAllow")
@Path("onlineTutorials")
public class OnlineTutorialsResource extends BasePluginResource {
    public static final Log  log = LogFactory.getLog(OnlineTutorialsResource.class);

    private SailPointContext context; // = this.getContext();

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult getTutorials() {
        log.debug("Getting Online Tutorials");
        
        try {
            context = SailPointFactory.getCurrentContext();
            
        } catch (Exception ex) {
            log.debug("Error creating Context");
        }
        List<OnlineTutorialsDTO> listDTO = new ArrayList<OnlineTutorialsDTO>();
               
        try {
        	    DashboardContent  dash = (DashboardContent) context.getObjectByName(DashboardContent.class, "Online Tutorials");

        	    if( null != dash ) {
        	    	
        	    	Attributes attributes = dash.getArguments();
            	    
        	    	if( null != attributes ) {
        	    		List<Map> tutList = (List<Map>) attributes.get("tutorials");

        	    		for (Map temp : tutList ) {
        	    			OnlineTutorialsDTO t = new OnlineTutorialsDTO();
                	        t.setDescription( getMessage( (String)temp.get("description_key") ));
        	    			t.setPage( (String)temp.get("page"));
        	    			t.setTitleKey( getMessage( (String) temp.get("title_key")));
        	    			listDTO.add(t);
                		}
        	    	}
        	    	
        	    }

        } catch (Exception ex) {
            log.debug("Exception in Online Tutorials Widget");
            //ex.printStackTrace();
        }

        int total = listDTO.size();
        log.debug("Size of return = " + total);
        return new ListResult(listDTO, total);
    }

    private String getMessage( String messageKey ) {
        Message message = new Message( messageKey );
        String localizedMessage = message.getLocalizedMessage( Locale.getDefault(), TimeZone.getDefault() );
        return localizedMessage;
    }

       
    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public String testConnection() {
        log.debug("GET test");
        
        String ret = "Success!";
        log.debug(ret);
        return ret;
    }

    @Override
    public String getPluginName() {
        return "onlinetutorials";
    }

}
