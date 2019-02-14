package sailpoint.plugin.siqstats.rest;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;

import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.tools.GeneralException;
import sailpoint.tools.JdbcUtil;
import sailpoint.tools.Pair;

@RequiredRight(value = "siqstatsRESTAllow")
@Path("siqstats")
public class SIQStatsResource extends BasePluginResource {
    public static final Log  log        = LogFactory.getLog(SIQStatsResource.class);

    private SailPointContext context;                                               // =
    private String           ownerid    = null;
    private String           owner      = null;
    private String           ownerscore = null;
    private String           dbuser     = "Administrator";
    private String           dbpassword = "Sailp0!nt";
    private String           dbhost     = "seri.sailpointdemo.com:1433";
    private String           dbname     = "SecurityIQDB_SERI";
    private String           dbdomain   = "SERI";

    private SIQStatsDTO calculateKPIS(Connection conn, String kpiName, int kpiID, int resourceid) {
        log.debug("Calculating KPIS via stored procedures");
        SIQStatsDTO stats = new SIQStatsDTO();
        try {
            Statement kpiStmt = conn.createStatement();
            ResultSet kpiRS = null;
            String sql = "DECLARE @RC int;  DECLARE @resource_ids [whiteops].[id_table_type]; DECLARE @kpi_id bigint;  INSERT @resource_ids (id) VALUES ("
                    + resourceid + "); SET @kpi_id = " + kpiID + "; EXECUTE @RC = [whiteops].[" + kpiName + "] @resource_ids, @kpi_id;";
            kpiRS = kpiStmt.executeQuery(sql);
            //int resource_id = 0;
            //int kpi_id = 0;
            //int kpi_value = 0;
            //float kpi_score = 0;
            //log.debug(kpiRS.toString());

            while (kpiRS.next()) {

                String name = "";
                //String name = kpiRS.getString(1);
                switch (kpiID) {
                case 1: name = "Overexposed Folders";
                break;
                case 2: name = "Overexposed Sensitive Folders";
                break;
                case 3: name = "Users with Stale Permissions";
                break;
                case 4: name = "Stale Data";
                break;
                default: name = "Doh";
                }
                String value = kpiRS.getString(3);
                float score = kpiRS.getFloat(4);
                stats.setStatName(name);
                stats.setStatScore(Float.toString(score));
                if ("Stale Data".equals(name))
                    stats.setStatValue(value + " GB");
                else 
                    stats.setStatValue(value);
                
                //stats.setStatScore(Integer.toString(kpi_score));
                if (score > 8.0)
                    stats.setStatColor("Green");
                else if (score > 3.0)
                    stats.setStatColor("Orange");
                else
                    stats.setStatColor("Red");
            }
            kpiRS.close();
            kpiStmt.close();
        } catch (Exception ex) {

        } finally {

        }
        log.debug("\nReturn value for " + kpiName);
        log.debug(stats.toString());
        return stats;
    }

    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult collectStatistics() {
        log.debug("\n*******************CollectStatistics*****************");
        log.debug("Getting Stats");
        List<SIQStatsDTO> listDTO = new ArrayList<SIQStatsDTO>();

       // boolean online = false;
        String resource = null;

        try {
            context = SailPointFactory.getCurrentContext();
            //online = this.getSettingBool("online");
            resource = this.getSettingString("resource");
            dbuser = this.getSettingString("dbuser");
            dbpassword = this.getSettingString("dbpassword");
            dbhost = this.getSettingString("dbhost");
            dbname = this.getSettingString("dbname");
            dbdomain = this.getSettingString("dbdomain");
        } catch (Exception ex) {
            log.debug("Error creating Context");
        }

        log.debug("Resource = " + resource);
 

            // Connect to SIQ and query values from Stored Procedures.
            //boolean validConnection = false;
            log.debug("Attempting to connect to SIQ DB - No Stored Procs");

            try {

                ResultSet rs = null;
                Map<String, String> connectionMap = new HashMap<String, String>();
                connectionMap.put("driverClass", "net.sourceforge.jtds.jdbc.Driver");
                connectionMap.put("password", dbpassword);
                connectionMap.put("user", dbuser);
                String url = "jdbc:jtds:sqlserver://" + dbhost + "/" + dbname + ";useNTLMv2=true;domain=" + dbdomain + ";"; 
                log.debug("Connection URL = " + url);        
                connectionMap.put("url", url);
                Connection conn = JdbcUtil.getConnection(connectionMap);

                log.debug("Got a valid Connection " + conn);


                Statement stmt = conn.createStatement();
                int resourceid = 0;
                String resourceidSQL = "select id from whiteops.business_service WHERE full_path='" + resource + "';";
                log.debug("About to execute resourceid query.  This gives us an id rather than the string from the config. \n\t" + resourceidSQL);
                rs = stmt.executeQuery(resourceidSQL);
                if (!rs.isBeforeFirst()) {
                    throw new GeneralException("No SIQ Resource Defined.   Make sure SIQ is installed and configured.");
                } else {
                    while (rs.next()) {
                        resourceid = rs.getInt("id");
                    }
                }
                log.debug("Queried ResourceID from resouce : " + resourceid);

                String kpiresourceresultSQL = "select type.name,rresult.kpi_value,rresult.kpi_score from whiteops.kpi_resource_result rresult INNER JOIN whiteops.kpi_type type ON type.id = rresult.kpi_id WHERE rresult.resource_id="
                        + Integer.valueOf(resourceid) + ";";
                log.debug("About to execute kpiQuery.  If this table is populated. \n\t" + kpiresourceresultSQL);
                rs = stmt.executeQuery(kpiresourceresultSQL);
                if (!rs.isBeforeFirst()) {
                    log.debug("No data, Will use stored procedures instead to calculate the values");
                    
                    // Have to call Stored Procedures
                    SIQStatsDTO overexposedDTO = calculateKPIS(conn,"kpi_calc_overexposed_folders", 1, resourceid);
                    SIQStatsDTO overexposedSensitiveDTO = calculateKPIS(conn,"kpi_calc_overexposed_sensitive_folders", 2, resourceid);
                    SIQStatsDTO stalepermissionsDTO = calculateKPIS(conn,"kpi_calc_users_with_stale_permissions", 3, resourceid);
                    SIQStatsDTO staledataDTO = calculateKPIS(conn,"kpi_calc_stale_data", 4, resourceid);
                    listDTO.add(overexposedDTO);
                    listDTO.add(overexposedSensitiveDTO);
                    listDTO.add(stalepermissionsDTO);
                    listDTO.add(staledataDTO);

                } else {
                    while (rs.next()) {
                        log.debug("Adding stats for each KPI from kpi_resource_result");
                        SIQStatsDTO stats = new SIQStatsDTO();
                        String statname = rs.getString(1);
                        String statvalue = rs.getString(2);
                        
                        
                        
                        float statscore = rs.getFloat(3);
                        stats.setStatName(statname);
                        if ("Stale Data".equals(statname))
                            stats.setStatValue(statvalue + " GB");
                        else 
                            stats.setStatValue(statvalue);
                        
                        
                        stats.setStatScore(Float.toString(statscore));
                        if (statscore > 8.0)
                            stats.setStatColor("Green");
                        else if (statscore > 3.0)
                            stats.setStatColor("Orange");
                        else
                            stats.setStatColor("Red");
                        // log.debug("Stats = " + stats.toString());
                        listDTO.add(stats);
                    }
                }

                rs.close();
                stmt.close();
                conn.close();

            } catch (Exception ex) {
                //validConnection = false;
                log.debug("Exception in IIQStats Widget");
                // ex.printStackTrace();
            }


        
        int total = listDTO.size();
        log.debug("Size of return = " + total);

        return new ListResult(listDTO, total);
    }

    private Pair<String, String> findResourceOwner(Connection conn, int resourceid) {

        Pair<String, String> pair = null;
        try {

            Statement ownerStmt = conn.createStatement();
            ResultSet ownerRS = null;
            String sql = "select user1.user_display_name, user1.id from whiteops.ra_user user1 inner join whiteops.business_service_owner bizowner on bizowner.ra_user_id = user1.id inner join whiteops.business_service biz on biz.id =  bizowner.business_service_id where biz.id = "
                    + resourceid + ";";
            ownerRS = ownerStmt.executeQuery(sql);

            if (ownerRS.isBeforeFirst()) {
                while (ownerRS.next()) {
                    String owner = ownerRS.getString(1);
                    String id = ownerRS.getString(2);
                    pair = new Pair<String, String>(owner, id);
                }
            } else {
                log.debug("No Owner found");
                pair = new Pair<String, String>("undefined", "");
            }
            ownerStmt.close();
        } catch (Exception ex) {

        }

        return pair;
    }

    @GET
    @Path("ownerdata")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getOwnerData() {
        log.debug("\n*******************GetOwnerData*****************");
        HashMap<String, String> map = new HashMap<String, String>();
        ResultSet rs = null;
        Connection conn = null;
        Statement stmt = null;
        try {

            rs = null;
            Map<String, String> connectionMap = new HashMap<String, String>();
            connectionMap.put("driverClass", "net.sourceforge.jtds.jdbc.Driver");
            connectionMap.put("password", dbpassword);
            connectionMap.put("user", dbuser);
            String url = "jdbc:jtds:sqlserver://" + dbhost + "/" + dbname + ";useNTLMv2=true;domain=" + dbdomain + ";"; 
            log.debug("Connection URL = " + url);        
            connectionMap.put("url", url);
            conn = JdbcUtil.getConnection(connectionMap);

            log.debug("Got Connection " + conn);
            // if (conn != null) {
            // validConnection = conn.isValid(0);
            // log.debug("Got Valid Connection");
            // }
            String resource = this.getSettingString("resource");

            stmt = conn.createStatement();
            int resourceid = 0;
            // String sql = "SELECT count(user_name) from
            // whiteops.ra_user;";
            String resourceidSQL = "select id from whiteops.business_service WHERE full_path='" + resource + "';";
            log.debug("About to execute resourceid query \n\t" + resourceidSQL);
            rs = stmt.executeQuery(resourceidSQL);
            if (!rs.isBeforeFirst()) {
                throw new GeneralException("Resource is not valid");
            } else {
                while (rs.next()) {
                    resourceid = rs.getInt("id");
                }
            }
            log.debug("Queried ResourceID from resouce : " + resourceid);

            Pair<String, String> pair = this.findResourceOwner(conn, resourceid);
            owner = pair.getFirst();
            ownerid = pair.getSecond();
            log.debug("Found Resource Owner " + owner + " : " + ownerid);

            if (!"undefined".equals(owner)) {
                
                String ownerresultSQL = "select owner_score from whiteops.kpi_owner where ra_user_id=" + Integer.valueOf(ownerid);

                log.debug("About to execute ownerQuery \n\t" + ownerresultSQL);
                rs = stmt.executeQuery(ownerresultSQL);
                if (!rs.isBeforeFirst()) {
                    log.debug("No data, owner not defined or KPI task not executed yet");

                    ownerscore = "-";
                } else {
                    while (rs.next()) {
                        ownerscore = rs.getString(1);
                    }
                }
            } else {
                log.debug("Owner was undefined so we are setting the score to a blank value");
                ownerscore = "-";
            }

            map.put("owner", owner);
            map.put("ownerscore", ownerscore);
            map.put("resource", resource);
            log.debug("Owner info for " + ownerid);
            log.debug("\t" + map.toString());
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return map;
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
        return "siqstats";
    }

}
