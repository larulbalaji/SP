package sailpoint.plugin.sensitivedata.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.plugin.sensitivedata.rest.SensitiveDataUserDTO;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.QueryOptions;
import sailpoint.object.ScoreBandConfig;
import sailpoint.object.ScoreConfig;


@RequiredRight(value = "SensitiveDataUserRESTAllow")
@Path("sensitivedata")
public class SensitiveDataResource extends BasePluginResource {
    public static final Log  log = LogFactory.getLog(SensitiveDataResource.class);
    
    private SailPointContext context;

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public sailpoint.integration.ListResult getUsers() {
        log.debug("GET getUsers");
        sailpoint.integration.ListResult ret = null;
       
        
        int limit = this.getSettingInt("limit");
        String tag = this.getSettingString("dctag");
                   
        log.debug("PluginConfig Limit = " + limit);
        log.debug("PluginConfig DataClassification = " + tag);

        try {
            log.debug("Getting SailPointContext");
            context = SailPointFactory.getCurrentContext(); //this.getContext();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<SensitiveDataUserDTO> listDTO = new ArrayList<SensitiveDataUserDTO>();

        Iterator<ManagedAttribute> it = null;
        Iterator<IdentityEntitlement> itID = null;
        
        try {
            Set<String> ids = new TreeSet<String>();
            if (tag.contains("|")) {
               log.debug("Does not currently support multiple data classification tags");   
            }
            QueryOptions qo = new QueryOptions();
            if ("ALL".equals(tag))
                qo.addFilter(Filter.notnull("extended1"));
            else 
                qo.addFilter(Filter.like("extended1",tag));
            it = context.search(ManagedAttribute.class, qo);
            while ((null != it) && (it.hasNext())) {
                ManagedAttribute ma = (ManagedAttribute) it.next();
                QueryOptions idQO = new QueryOptions();
                idQO.addFilter(Filter.eq("value", ma.getValue()));
                idQO.setResultLimit(limit);
                itID = context.search(IdentityEntitlement.class, idQO);
                while (itID.hasNext()) {
                    IdentityEntitlement ie = (IdentityEntitlement) itID.next();
                    Identity id = ie.getIdentity();
                    ids.add(id.getName());
                    log.debug("User " + id.getName() + " has access to " + ma.getValue());
                    //context.decache(id);
                }
                context.decache(ma);
            }
            sailpoint.tools.Util.flushIterator(it);
            sailpoint.tools.Util.flushIterator(itID);
            
            log.debug("Users that have tag " + ids.size());
            
            
            //Now loop through and put them in a ListResult.
            log.debug("Putting users into ListResult");
            Iterator<String> idsIT = ids.iterator();
            while (idsIT.hasNext()) {
              String myId = (String) idsIT.next();
              Identity identity = context.getObjectByName(Identity.class, myId);
              SensitiveDataUserDTO sdu = new SensitiveDataUserDTO();
              sdu.setDisplayName(identity.getDisplayableName());
              sdu.setId(identity.getId());
              String riskBand = determineRiskBand(identity);
              sdu.setRiskStatus(riskBand);
              log.debug(identity.getDisplayableName() + " " + riskBand);
              listDTO.add(sdu);
              context.decache(identity);
            }
            sailpoint.tools.Util.flushIterator(idsIT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int total = listDTO.size();
        log.debug("Size of return = " + total);
        return new ListResult(listDTO, total);
    }

    public String determineRiskBand(Identity identity) {
        log.debug("\tDetermine RiskBand");
        String riskBand = null;
        int score = identity.getScore();
        log.debug("\tUser Score = " + score);
        try {
            ScoreConfig scoreConfig = context.getObjectByName(ScoreConfig.class, "ScoreConfig");
            List<ScoreBandConfig> scoreBandList = scoreConfig.getBands();
            for (ScoreBandConfig sbc : scoreBandList) {
                if (score > sbc.getLowerBound() && score < sbc.getUpperBound()) {
                    riskBand = sbc.getLabel();
                    break;
                }
            }
            log.debug("\tUser " + identity.getName() + "has label of " + riskBand + " with score of " + identity.getScore());
            if (riskBand == null) {
                if (identity.isDisabled()) {
                    //User is disabled so doesn't have a risk score
                    int maxBand = scoreConfig.getMaximumNumberOfBands();
                    ScoreBandConfig maxBandConfig = scoreBandList.get(maxBand);
                    riskBand = maxBandConfig.getLabel();
                }
            }
        
        } catch (Exception ex) {
            log.debug("\t" + ex.toString());
            ex.printStackTrace();
        }
        
        
        return riskBand;
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
        return "sensitivedata";
    }
    


}
