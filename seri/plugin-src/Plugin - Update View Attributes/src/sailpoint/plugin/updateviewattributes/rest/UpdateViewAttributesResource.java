package sailpoint.plugin.updateviewattributes.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.ObjectAttribute;
import sailpoint.object.ObjectConfig;
import sailpoint.object.UIConfig;
import sailpoint.plugin.updateviewattributes.UpdateViewAttributesDTO;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

/**
 * @author kevin.james
 */
@RequiredRight("UpdateViewAttributesPluginRestServiceAllow")
@Path("updateviewattributes")
public class UpdateViewAttributesResource extends BasePluginResource {
    private static final Log log = LogFactory.getLog(UpdateViewAttributesResource.class);

    public UpdateViewAttributesResource() {
    }

    /**
     * Returns the current enables (and disabled) attributes
     */
    @GET
    @Path("currentViewAttributes")
    @Produces(MediaType.APPLICATION_JSON)
    public UpdateViewAttributesDTO
    getCurrentAttributes() throws GeneralException {

        log.debug("getCurrentAttributes 3");
      
        UpdateViewAttributesDTO viewAttrsDTO = new UpdateViewAttributesDTO();

        // Get the available Identity Attributes
        Map<String,String> attrs=getAllAttributes();
        
        // Now get the list of currently displayed attributes
        String[] currentAttrs=getCurrentViewAttributes();
        
        List<Map<String,String>> current=new ArrayList<Map<String,String>>();
        List<Map<String,String>> available=new ArrayList<Map<String,String>>();
        
        // Now go through currentAttrs, and add those to the 'current' list
        for (String aCurrentAttr: currentAttrs) {
          String aDisplayName=attrs.get(aCurrentAttr);
          Map<String,String> anAttr=new HashMap<String,String>();
          anAttr.put("name",  aCurrentAttr);
          anAttr.put("displayName", localize(aDisplayName));
          current.add(anAttr);
          attrs.remove(aCurrentAttr);
        }
        
        // Now add the remaining attributes to the 'available' list
        for (Map.Entry<String,String> entry: attrs.entrySet()) {
          Map<String,String> anAttr=new HashMap<String,String>();
          anAttr.put("name",  entry.getKey());
          anAttr.put("displayName", localize(entry.getValue()));
          available.add(anAttr);
        }
        
        
        
        viewAttrsDTO.setCurrent(current);
        viewAttrsDTO.setAvailable(available);

        return viewAttrsDTO;
    }

    /**
     * Updates the current enabled attributes
     */
    @PUT
    @Path("currentViewAttributes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setCurrentAttributes(Map<String, Object> viewAttrsDTO) throws GeneralException {

      log.debug("setCurrentAttributes");
      int status=200;
      String msg="ok";
      
      try {
        SailPointContext ctx=getContext();
        UIConfig idenConfig=ctx.getObjectByName(UIConfig.class, "UIConfig");
        
        String newAttrs=(String)viewAttrsDTO.get("newAttrs");
        log.debug("newAttrs="+newAttrs);
        // TODO: some sanity checking here?
        idenConfig.put("identityViewAttributes", newAttrs);
        
        ctx.saveObject(idenConfig);
        ctx.commitTransaction();
        
        log.debug("all ok");
      } catch (GeneralException ge) {
        log.error("setCurrentAttributes: GeneralException "+ge);
        status=500;
        msg="not ok: "+ge;
      }
      
      String output = "Received message= " + msg;

      return Response.status(status).entity(output).build();
      
    }
    
    private String[] getCurrentViewAttributes() throws GeneralException {

      SailPointContext ctx=getContext();
      
      UIConfig idenConfig=ctx.getObjectByName(UIConfig.class, "UIConfig");
      
      String currentViewAttributes=idenConfig.get("identityViewAttributes");
      log.debug("currentViewAttributes="+currentViewAttributes);
      String[] list=currentViewAttributes.split(",");
      
      return list;
      
    }

    private Map<String, String> getAllAttributes() throws GeneralException {
      
      SailPointContext ctx=getContext();
      
      ObjectConfig idenConfig=ctx.getObjectByName(ObjectConfig.class, "Identity");
      
      log.debug("Got "+idenConfig.getObjectAttributes().size()+" available Identity attributes");
      
      Map<String,String> attrs=new HashMap<String,String>();
      for (ObjectAttribute attr: idenConfig.getObjectAttributes()) {
        attrs.put(attr.getName(), attr.getDisplayName());
      }
      return attrs;
    }

    @Override
    public String getPluginName() {
      // TODO Auto-generated method stub
      return "updateViewAttributes";
    }

}
