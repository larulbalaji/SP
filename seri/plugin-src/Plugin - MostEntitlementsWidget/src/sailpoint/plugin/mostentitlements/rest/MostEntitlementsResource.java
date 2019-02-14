package sailpoint.plugin.mostentitlements.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.plugin.mostentitlements.rest.MostEntitlementsUserDTO;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;


@RequiredRight(value = "mostEntitlementsRESTAllow")
@Path("mostEntitlements")
public class MostEntitlementsResource extends BasePluginResource {
    public static final Log  log = LogFactory.getLog(MostEntitlementsResource.class);
    
    private SailPointContext context;

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public sailpoint.integration.ListResult getGetUsers() {
        log.debug("Enter GetUsers");
        sailpoint.integration.ListResult ret = null;
       
        int limit = this.getSettingInt("limit");
        log.debug("Limit value set to " + limit);

        try {
            log.debug("Getting SailPointContext");
            context = SailPointFactory.getCurrentContext(); //this.getContext();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<MostEntitlementsUserDTO> listDTO = new ArrayList<MostEntitlementsUserDTO>();

        try {
            String hqlQuery = "select ident.name,ident.id,count(ent.id) AS numEntitlements from Identity ident "
                    + "inner join ident.identityEntitlements ent group by ident.name order by 3 desc";
            
            QueryOptions qo = new QueryOptions();
            qo.setOrderBy("2");
            qo.setOrderAscending(false);

            log.debug("Using HQL Query of : " + hqlQuery);
            log.debug("Setting return limit to : " + limit);
            qo.setResultLimit(limit);
            // qo.setResultLimit(15);
            log.debug("Querying IdentityEntitlements");
            Iterator it = context.search(hqlQuery, null, qo);
            if ( (null !=it) && (it.hasNext())){
                log.debug("Got IdentityEntitlements");
            }
            while ((null !=it) && (it.hasNext())) {
                Object[] results = (Object[]) it.next();
                String identityName = (String) results[0];
                String numEntitlements = "" + results[2];
                String id = (String) results[1];
                log.debug(identityName + " " + numEntitlements);
                MostEntitlementsUserDTO meuDTO = new MostEntitlementsUserDTO();

                meuDTO.setDisplayName(identityName);
                meuDTO.setCount(numEntitlements);
                meuDTO.setId(id);
                listDTO.add(meuDTO);
            }
            sailpoint.tools.Util.flushIterator(it);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int total = listDTO.size();
        log.debug("Size of return = " + total);
        return new ListResult(listDTO, total);
    }

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public String testConnection() {
        log.debug("GET test");
        String ret = "Success!";
        return ret;
    }

    @Override
    public String getPluginName() {
        return "mostentitlements";
    }
    


}
