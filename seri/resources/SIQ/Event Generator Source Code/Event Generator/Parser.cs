using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Configuration;

namespace EventGenerator{
    public class Parser{

        // Static parsing method, parses line by line creating a new task for each line
        public static ArrayList parse(String filePath){
            ArrayList taskList = new ArrayList();
            string line;
            string[] vars;
            try {
                StreamReader file = new StreamReader(@ConfigurationManager.AppSettings["CSV Path"]);
                Logger.Write("Reading actions from file " + filePath);
                string[] header = file.ReadLine().Split(',');
                while ((line = file.ReadLine()) != null) {
                    Logger.Write("Adding Line " + line);
                    var dict = new Dictionary<string, string>();
                    vars = line.Split(',');
                    if (vars.GetUpperBound(0) != header.GetUpperBound(0))
                    {
                        Logger.Write("Invalid line found, skipping - line was: ");
                        continue;
                    }
                    for (int i = 0; i < header.Length; i++) {
                        dict.Add(header[i], vars[i]);
                    }
                    Logger.Write("Adding New Task to TaskList:" + line);
                    taskList.Add(new Task(dict));
                }
            }
            catch (Exception e) {
                Console.WriteLine("Parse failed.\n" + e);
                Logger.Write("Parse failed.\n" + e);
            }
            return taskList;
        }

        public static void Main(string[] args) {
            Logger.createLog();
            Console.WriteLine("Beginning Event Generation.");
            Logger.Write("Beginning Event Generation.");
            ArrayList x = parse(ConfigurationManager.AppSettings["CSV Path"]);
            foreach (Task y in x) {
                y.startTask();
            }
            Logger.Write("Tasks executing. Waiting for console keypress to finish.");
            Console.ReadKey();
        }
    }
}
