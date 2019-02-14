using System;
using System.IO;
using System.Configuration;
using System.Security.AccessControl;

namespace EventGenerator {
    public class Logger {
        private Logger() { }

        private static readonly object locker = new object();

        // Static method used to create the log directory and the log file
        public static void createLog() {
            Console.WriteLine("Creating Event Logfile at: " + @ConfigurationManager.AppSettings["Log Path"] + @"\log\log.txt");
            if (!Directory.Exists(@ConfigurationManager.AppSettings["Log Path"] + @"\log"))
            {
                Directory.CreateDirectory(@ConfigurationManager.AppSettings["Log Path"] + @"\log");
            }
            DirectoryInfo dInfo = new DirectoryInfo(@ConfigurationManager.AppSettings["Log Path"]);
            DirectorySecurity dSecurity = dInfo.GetAccessControl();
            dSecurity.AddAccessRule(new FileSystemAccessRule("everyone", FileSystemRights.FullControl, InheritanceFlags.ObjectInherit | InheritanceFlags.ContainerInherit, PropagationFlags.InheritOnly, AccessControlType.Allow));
            dInfo.SetAccessControl(dSecurity);
        }

        // Static method used to write to the log, also includes a timestamp
        public static void Write(string s) {
            lock (locker) {
                StreamWriter stream;
                stream = File.AppendText(@ConfigurationManager.AppSettings["Log Path"] + @"\log\log.txt");
                string time = string.Format("{0:yyyy-MM-dd_hh-mm-ss-tt}", DateTime.Now);
                stream.WriteLine("[" + time + "] " + s);
                stream.Close();
            }
        }
    }
}
