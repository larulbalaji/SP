using System;
using System.IO;
using System.Configuration;
using System.Collections.Generic;
using System.Security.AccessControl;

namespace EventGenerator {
    class FileSys {
        // Function that creates a file for the specified targetUser at the specified file path
        public static void createFile(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (File.Exists(@paramDict["param1"]))
                        File.Delete(@paramDict["param1"]);
                    var file = File.Create(@paramDict["param1"]);
                    file.Close();

                    Console.WriteLine("[Task " + paramDict["id"] + "] File Created: " + paramDict["param1"]);
                    Logger.Write("[Task " + paramDict["id"] + "] File Created: " + paramDict["param1"]);
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] CreateFile failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that deletes a file for the specified targetUser at the specified file path
        public static void deleteFile(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (File.Exists(@paramDict["param1"])) {
                        File.Delete(@paramDict["param1"]);

                        Console.WriteLine("[Task " + paramDict["id"] + "] File Deleted: " + paramDict["param1"]);
                        Logger.Write("[Task " + paramDict["id"] + "] File Created: " + paramDict["param1"]);
                    }
                    else {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Delete failed, file does not exist.");
                        Logger.Write("[Task " + paramDict["id"] + "] Delete failed, file does not exist.");
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] DeleteFile failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that reads a file for the specified targetUser at the specified file path
        public static void readFile(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (File.Exists(@paramDict["param1"])) {
                        var file = File.Open(@paramDict["param1"], FileMode.Open);
                        file.Close();

                        Console.WriteLine("[Task " + paramDict["id"] + "] File Read: " + paramDict["param1"]);
                        Logger.Write("[Task " + paramDict["id"] + "] File Read: " + paramDict["param1"]);
                    }
                    else {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Read failed, file does not exist.");
                        Logger.Write("[Task " + paramDict["id"] + "] Read failed, file does not exist.");
                    }
                }
            }
            catch(Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] ReadFile failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that writes to a file for the specified targetUser at the specified file path
        public static void writeFile(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"]))
                {
                    LogImpersonation(paramDict);
                    if (File.Exists(@paramDict["param1"]))
                    {
                        using (StreamWriter writer = File.AppendText(@paramDict["param1"]))
                        {
                            writer.WriteLine(@paramDict["param2"]);
                            Console.WriteLine("[Task " + paramDict["id"] + "] File Written to: " + paramDict["param1"]);
                            Logger.Write("[Task " + paramDict["id"] + "] File Written to: " + paramDict["param1"]);
                        }
                    }
                    else {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Write failed, file " + @paramDict["param1"] + " does not exist.");
                        Logger.Write("[Task " + paramDict["id"] + "] Write failed, file " + @paramDict["param1"] + " does not exist.");
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] Write failed on file " + @paramDict["param1"]+ ", error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that moves a file for a specified targetUser to the specified file path
        public static void moveFile(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    try {
                        File.Move(paramDict["param1"], paramDict["param2"]);

                        Console.WriteLine("[Task " + paramDict["id"] + "] File Moved to: " + paramDict["param2"]);
                        Logger.Write("[Task " + paramDict["id"] + "] File Moved to: " + paramDict["param2"]);
                    }
                    catch (Exception e) {
                        Console.WriteLine("[Task " + paramDict["id"] + "] File Move failed, new path is not able to be reached. " + e);
                        Logger.Write("[Task " + paramDict["id"] + "] File Move failed, new path is not able to be reached. " + e);
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] moveFile failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that allows/denies ACL settings from a file for a specified user.
        public static void fileAccess(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                try {
                    FileSystemRights right = new FileSystemRights();
                    Type typeRight = right.GetType();

                    AccessControlType setting = new AccessControlType();
                    Type typeSetting = setting.GetType();

                    var access = new FileSystemAccessRule(paramDict["targetUser"],
                                    (FileSystemRights)Enum.Parse(typeRight, paramDict["param2"]),
                                    (AccessControlType)Enum.Parse(typeSetting, paramDict["param3"]));

                    var fInfo = new FileInfo(paramDict["param1"]);
                    FileSecurity fSecurity = fInfo.GetAccessControl();
                    fSecurity.AddAccessRule(access);
                    fInfo.SetAccessControl(fSecurity);

                    Console.WriteLine("[Task " + paramDict["id"] + "] File access control changed successfully.");
                    Logger.Write("[Task " + paramDict["id"] + "] File access control changed successfully.");
                }
                catch (Exception e) {
                    Console.WriteLine("[Task " + paramDict["id"] + "] File access control failed.\n" + e);
                    Logger.Write("[Task " + paramDict["id"] + "] File access control failed.\n" + e);
                }
            }
        }

        // Function that creates a directory for a specified targetUser at the specified directory path
        public static void createDir(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (!Directory.Exists(paramDict["param1"])) {
                        Directory.CreateDirectory(paramDict["param1"]);

                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory Created: " + paramDict["param1"]);
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Created: " + paramDict["param1"]);
                    }
                    else {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory Create failed, the directory already exists.");
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Create failed, the directory already exists.");
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] CreateDir failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that deletes a directory for a specified targetUser at the specified directory path
        public static void deleteDir(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (paramDict["param2"].ToLower() == "true") {
                        Directory.Delete(paramDict["param1"], true);

                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory Deleted: " + paramDict["param1"]);
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Delted: " + paramDict["param1"]);
                    }
                    else {
                        try {
                            Directory.Delete(paramDict["param1"]);
                            Console.WriteLine("[Task " + paramDict["id"] + "] Directory Deleted: " + paramDict["param1"]);
                            Logger.Write("[Task " + paramDict["id"] + "] Directory Delted: " + paramDict["param1"]);
                        }
                        catch (Exception e) {
                            Console.WriteLine("[Task " + paramDict["id"] + "] Directory Delete failed.\n" + e);
                            Logger.Write("[Task " + paramDict["id"] + "] Directory Delete failed.\n" + e);
                        }
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] DeleteDir failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that reads the contents of a directory for a specified targetUser at the specified directory path
        public static void readDir(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    if (Directory.Exists(paramDict["param1"])) {
                        string[] files = Directory.GetFileSystemEntries(paramDict["param1"]);

                        Console.Write("[Task " + paramDict["id"] + "] Directory Files: [");
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Files: [");

                        for (int i = 0; i < files.Length; i++) {
                            if (i != files.Length - 1) {
                                Console.Write(files[i] + ", ");
                                Logger.Write(files[i] + ", ");
                            }
                            else {
                                Console.Write(files[i] + "]\n");
                                Logger.Write(files[i] + "]\n");
                            }
                        }
                    }
                    else {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory read failed, the directory does not exist.");
                        Logger.Write("[Task " + paramDict["id"] + "] Directory read failed, the directory does not exist.");
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] ReadDir failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that moves a directory for a specified targetUser from a specified directory path to the a new path.
        // param1 when set to "true" will delete all the contents of the directory upon deletion of the directory and
        // must be set true if the directory to be deleted contains files
        public static void moveDir(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            try
            {
                using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                    try {
                        Directory.Move(paramDict["param1"], paramDict["param2"]);

                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory Moved to: " + paramDict["param2"]);
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Moved to: " + paramDict["param2"]);
                    }
                    catch (Exception e) {
                        Console.WriteLine("[Task " + paramDict["id"] + "] Directory Move failed, new path is not able to be reached.\n" + e);
                        Logger.Write("[Task " + paramDict["id"] + "] Directory Move failed, new path is not able to be reached.\n" + e);
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("[Task " + paramDict["id"] + "] MoveDir failed, error was: " + exc.Message);
                Logger.Write("Exception occurred, error was: " + exc.Message);
                throw exc;
            }
        }

        // Function that allows/denies ACL settings from a directory for a specified user.
        public static void dirAccess(Dictionary<string, string> paramDict) {
            LogExecuteMethod(paramDict);
            using (new Impersonator(paramDict["bindUser"], ConfigurationManager.AppSettings["FS Path"], paramDict["bindPass"])) {
                try {
                    FileSystemRights right = new FileSystemRights();
                    Type typeRight = right.GetType();

                    AccessControlType setting = new AccessControlType();
                    Type typeSetting = setting.GetType();

                    var access = new FileSystemAccessRule(paramDict["targetUser"],
                                    (FileSystemRights)Enum.Parse(typeRight, paramDict["param2"]),
                                    (AccessControlType)Enum.Parse(typeSetting, paramDict["param3"]));

                    var dInfo = new DirectoryInfo(paramDict["param1"]);
                    DirectorySecurity dSecurity = dInfo.GetAccessControl();
                    dSecurity.AddAccessRule(access);
                    dInfo.SetAccessControl(dSecurity);

                    Console.WriteLine("[Task " + paramDict["id"] + "] Directory access control changed successfully.");
                    Logger.Write("[Task " + paramDict["id"] + "] Directory access control changed successfully.");
                }
                catch (Exception e) {
                    Console.WriteLine("[Task " + paramDict["id"] + "] Directory access control failed.\n" + e);
                    Logger.Write("[Task " + paramDict["id"] + "] Directory access control failed.\n" + e);
                }
            }
        }

        private static void LogExecuteMethod(Dictionary<string, string> paramDict)
        {
            Logger.Write("Executing Method" + paramDict["action"]);
        }

        private static void LogImpersonation(Dictionary<string, string> paramDict)
        {
            Logger.Write("Impersonation Successful - impersonating user: " + paramDict["bindUser"]);
            Logger.Write("FS Path is: " + ConfigurationManager.AppSettings["FS Path"]);
            Logger.Write("Bind Password is: " + paramDict["bindPass"]);
        }
    }
}
