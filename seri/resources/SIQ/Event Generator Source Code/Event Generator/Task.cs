using System;
using System.Timers;
using System.Reflection;
using System.Collections.Generic;

namespace EventGenerator { 

    // Constructor for Task object, contains a Timer object
    public class Task{
        Dictionary<string, string> paramDict;
        Timer timer;

        public Task(Dictionary<string,string> dict) {
            paramDict = dict;
            timer = new Timer(setTime(paramDict["time"]));
            timer.AutoReset = false;
            timer.Elapsed += (sender, e) => TaskRunner(sender, e);
        }

        // Helper method to type-cast string to int and convert seconds to milliseconds
        private static int setTime(string t) {
            int temp = 0;
            try {
                temp = 1000 * Convert.ToInt32(t);
            }
            catch (Exception) {
                Console.WriteLine("Time input is invalid int.");
                Logger.Write("Time input is invalid int.");
            }
            return temp;
        }

        // Method that defines the task to be run upon timer interval tick
        // also stops the timer after the first tick and closes
        private void TaskRunner(object sender, ElapsedEventArgs e) {
            object[] param = new object[1];
            param[0] = paramDict;
            Type type;
            MethodInfo method;
            Logger.Write("Adding new Task of type: " + paramDict["action"].Substring(0, 2) + " to execute method: " + paramDict["action"].Substring(2));
            try
            {
                if (paramDict["action"].Substring(0,2) == "AD") {
                    ActiveDirectory x = new ActiveDirectory();
                    type = x.GetType();
                    method = type.GetMethod(paramDict["action"].Substring(2));              
                    method.Invoke(null, param);
                } else if(paramDict["action"].Substring(0,2) == "FS") {
                    FileSys x = new FileSys();
                    type = x.GetType();
                    method = type.GetMethod(paramDict["action"].Substring(2));
                    method.Invoke(null, param);
                }
            }
            finally
            {
                timer.Stop();
                timer.Close();
            }
        }

        // Method that starts the timer for the task to be run
        public void startTask() {
            timer.Start();
        }
    }
}
