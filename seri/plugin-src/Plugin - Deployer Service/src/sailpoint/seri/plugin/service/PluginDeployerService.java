package sailpoint.seri.plugin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Plugin;
import sailpoint.plugin.PluginInstaller.PluginInstallationResult;
import sailpoint.server.BasePluginService;
import sailpoint.server.Environment;
import sailpoint.service.plugin.PluginsService;
import sailpoint.service.plugin.PluginsService.PluginInstallData;
import sailpoint.tools.GeneralException;


public class PluginDeployerService extends BasePluginService {

    private static final Log log = LogFactory.getLog(PluginDeployerService.class);

    private static HashMap<String, String> localPluginState = null;
    private static HashMap<String, String> databasePluginState = null;
    private static boolean isApplicationServerStarting = true;

    private String monitorFolder;

    private boolean isRelative;

    /**
     * Query database and find all plugins.
     * <p/>
     * Install from UI mode. Database is authoritative.
     * 1)Scan local plugin installs and cache md5 hash
     * 2)Pull md5 hashes from database and compare
     * 3)Any plugins found locally not in database will be removed.
     * 4)Any plugins found in the database with different MD5 or new will be installed.
     * <p/>
     * SSB compatibility mode. File system is authoritative and only runs once on server start.
     * 1)Check for first execution
     * 2)Generate MD5 for SSB plugins
     * 3)For each plugin
     * a)Check database for plugin with matching MD5.
     * b)If found for install or upgrade case abort.
     * c)If not found or uninstall / obtain transaction lock on PluginConfig
     * d)Attempt uninstall or install/upgrade.
     * e)write plugin to database.
     * f)release lock.
     */
    
    public void configure(SailPointContext sailPointContext) throws GeneralException {
      log.debug("Configure");
      this.monitorFolder = this.getSettingString("monitorFolder");
      this.isRelative = this.getSettingBool("isRelative");
    }
    
    public void execute(SailPointContext context) throws GeneralException {

      log.debug("Execute!");
      
      PluginsService srv=new PluginsService(context);
      
      // Only executes on startup of the application server. Only one attempt will be made regardless of success
      //or failure.

      //  SSB mode
      Environment environment = Environment.getEnvironment();
      boolean runSqlScripts = environment.getPluginsConfiguration().isRunSqlScripts();
      boolean importObjects = environment.getPluginsConfiguration().isImportObjects();
      boolean cache = false; // TODO: Is there a global setting for this somewhere?
      PluginsService pluginsService = new PluginsService(context);

      File folder=null;
      // 
      if (isRelative) {
        // getResource is relative to the WEB-INF/classes folder. So to go relative to the root of the WAR we need
        // to drop back two levels
        String fldr="/../../"+this.monitorFolder;
        log.debug("folder="+fldr);
        URL url=this.getClass().getResource(fldr);
        if (url==null) {
          log.debug("Folder "+fldr+" not found");
          return;
        }
        log.debug("url="+url.toString());
        try {
          folder = new File(url.toURI());
        } catch(URISyntaxException e) {
          folder = new File(url.getPath());
        }
      } else {
        folder=new File(this.monitorFolder);
      }
      log.debug("Absolute folder path: "+folder.getAbsolutePath());
      File[] installOrUpgradeZips = folder.listFiles();
      if(installOrUpgradeZips!=null) {
        log.debug("Found "+installOrUpgradeZips.length+" zips to install/upgrade");
      }
//            File[] uninstallZips = PluginRegistry.getSSBUninstallDir().listFiles();
      if (installOrUpgradeZips != null) {
          for (File zip : installOrUpgradeZips) {
              try {
                FileInputStream fileInputStream = new FileInputStream(zip);
                log.debug("Found file: "+zip.getName());
                

                PluginInstallData installData = new PluginInstallData(
                    zip.getName(),
                    fileInputStream,
                    cache,
                    runSqlScripts,
                    importObjects
                );

                PluginInstallationResult result = pluginsService.installPlugin(installData, environment.getPluginsCache());
                log.debug("Installation Result="+result.toString());
                Plugin plugin = result.getPlugin();
              } catch (Exception e) {
                  log.warn("Could not install plugin, it may already be installed", e);
              }
              //remove the file regardless. The SSB installs are commands
              FileUtils.deleteQuietly(zip);
          }
      }
      /* No uninstall for now 
       * if (uninstallZips != null) {
          for (File zip : uninstallZips) {
              try {
                  Plugin plugin = PluginRegistry.getPluginFromArchive(zip.getAbsolutePath());

                  if (plugin != null) {
                      String deployPath = PluginRegistry.getPluginsRootPath().getAbsolutePath() + "/" +
                              plugin.getUniqueName();

                      PluginRegistry.uninstall(plugin.getUniqueName());
                  } else {
                      log.error("Unable to read plugin archive.");
                  }
              } catch (Exception e) {
                  log.warn("Could not uninstall plugin, it may already be uninstalled", e);
              }
                //remove the file regardless. The SSB uninstalls are commands
                FileUtils.deleteQuietly(zip);
            }
        }
          */
    }

//    protected byte[] pullFileFromDatabase(SailPointContext context, String pluginName) throws GeneralException {
//        PersistedFileInputStream inputStream = null;
//        byte[] readBytes = null;
//
//        QueryOptions ops = new QueryOptions();
//        ops.add(Filter.eq("contentType", Constants.PLUGIN_CONTENT_TYPE));
//        ops.add(Filter.eq("name", pluginName));
//
//        try {
//            List<PersistedFile> files = context.getObjects(PersistedFile.class, ops);
//            PersistedFile file = files.get(0);
//            inputStream = new PersistedFileInputStream(context, file);
//            readBytes = new byte[(int) file.getContentLength()];
//            inputStream.read(readBytes);
//        } catch (IOException e) {
//            throw new GeneralException(e);
//        } finally {
//            if (inputStream != null)
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    log.error(e);
//                }
//        }
//        return readBytes;
//    }

//    //fast query to snap database state and indicate if it has changed since last read.
//    protected boolean hasDatabaseChanged(SailPointContext context) throws GeneralException {
//        boolean databaseChanged = false;
//
//        //The state is rebuilt every time because we need to check for modifications and deletes :(
//        HashMap<String, String> newState = new HashMap<>();
//        List<String> props = new ArrayList<>();
//        props.add("name");
//        props.add("description");
//
//        QueryOptions ops = new QueryOptions();
//        ops.add(Filter.eq("contentType", Constants.PLUGIN_CONTENT_TYPE));
//
//        try {
//            Iterator<Object[]> it = context.search(PersistedFile.class, ops, props);
//            int count = 0;
//            while (it.hasNext()) {
//                count++;
//                Object[] row = it.next();
//                String name = (String) row[0];
//                String md5 = (String) row[1];
//                newState.put(name, md5);
//                if (localPluginState == null || (localPluginState.containsKey(name) && !localPluginState.get(name).equalsIgnoreCase(md5))) {
//                    databaseChanged = true;
//                }
//            }
//            if (localPluginState != null && count != localPluginState.size())
//                databaseChanged = true;
//        } catch (Exception e) {
//            log.error("Error while updating database plugin state", e);
//        }
//        databasePluginState = newState;
//        return databaseChanged;
//    }

    protected HashMap<String, String> calcLocalMD5state(File folder) {
        File[] zips = folder.listFiles();
        HashMap<String, String> state = null;
        FileInputStream inputStream = null;
        if (zips != null) {
            state = new HashMap<>(zips.length);
            for (File zip : zips) {
                try {
                    inputStream = new FileInputStream(zip);
                    String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(inputStream);
                    state.put(zip.getName().replaceAll(".zip", ""), md5);
                } catch (IOException e) {
                    log.error("Could not read plugin archive. ", e);
                } finally {
                    if (inputStream != null)
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            log.error("Can't close zip file input stream.", e);
                        }
                }
            }
        }
        return state;
    }

    @Override
    public String getPluginName() {
      return "Deployer";
    }
}
