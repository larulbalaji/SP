using System;
using System.DirectoryServices;
using System.Collections.Generic;
using System.Configuration;

namespace EventGenerator {
    public class ActiveDirectory {

        // Function that creates a User in Active Directory with the specified credentials
        public static void createUser(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry newUser = c.Children.Add("CN=" + paramDict["targetUser"], "user");
                newUser.Properties["sAMAccountName"].Add(paramDict["targetUser"]);
                newUser.Properties["displayName"].Add(paramDict["targetUser"]);
                newUser.Properties["distinguishedName"].Add("cn=" + paramDict["targetUser"] + "cn=Users,dc=SP,dc=local");
                newUser.CommitChanges();
                newUser.Invoke("SetPassword", new object[] { paramDict["targetPass"] });
                newUser.CommitChanges();
                newUser.Properties["userAccountControl"][0] = 0x200;
                newUser.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " created by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " created by " + c.Username);
            }
            catch (Exception e) {
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to create.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to create.\n" + e);
            }
        }

        // Function that deletes a User from Active Directory with the specified credentials
        public static void deleteUser(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry delUser = c.Children.Find("cn=" + paramDict["targetUser"], "user");
                c.Children.Remove(delUser);
                c.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " deleted by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " deleted by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to delete.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to delete.\n" + e);
            }
        }

        // Function that modifies a User's Password in Active Directory with the specified new Password
        public static void modifyPass(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry modUser = c.Children.Find("cn=" + paramDict["targetUser"], "user");
                modUser.Invoke("ChangePassword", new object[] { paramDict["targetPass"], paramDict["param1"] });
                modUser.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " modified password by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " modified password by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to modify password.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " failed to modify password.\n" + e);
            }
        }

        // Function that modifies a User's Property in Active Directory with the specified Property and value
        public static void modifyProp(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry modUser = c.Children.Find("cn=" + paramDict["targetUser"], "user");
                if (modUser.Properties.Contains(paramDict["param1"]))
                    modUser.Properties[paramDict["param1"]][0] = paramDict["param2"];
                else
                    modUser.Properties[paramDict["param1"]].Add(paramDict["param2"]);
                modUser.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " modified " + paramDict["param1"] + " by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] User " + paramDict["targetUser"] + " modified " + paramDict["param1"] + " by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " " + paramDict["param1"] + " not successfully modified.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " " + paramDict["param1"] + " not successfully modified.\n" + e);
            }
        }

        // Function that creates a User Group in Active Directory with specified name
        public static void createGroup(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try { 
                DirectoryEntry newGroup = c.Children.Add("cn=" + paramDict["param1"], "group");
                newGroup.Properties["samAccountName"].Value = paramDict["param1"];
                newGroup.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] Group " + paramDict["param1"] + " created by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] Group " + paramDict["param1"] + " created by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["param1"] + " not successfully created.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["param1"] + " not successfully created.\n" + e);
            }
        }

        // Function that deletes a User Group in Active Directory with specified name
        public static void deleteGroup(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry deleteGroup = c.Children.Find("cn=" + paramDict["param1"], "group");
                c.Children.Remove(deleteGroup);
                c.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] Group " + paramDict["param1"] + " deleted by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] Group " + paramDict["param1"] + " deleted by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["param1"] + " not successfully created.\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["param1"] + " not successfully created.\n" + e);
            }
        }

        // Function that adds a User to a Group in Active Directory
        public static void addMember(Dictionary<string,string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry group = c.Children.Find("cn=" + paramDict["param1"], "group");
                DirectoryEntry member = c.Children.Find("cn=" + paramDict["targetUser"], "user");
                group.Properties["member"].Add(member.Properties["distinguishedName"].Value);
                group.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] Member " + paramDict["targetUser"] + " added to " + paramDict["param1"] + " by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] Member " + paramDict["targetUser"] + " added to " + paramDict["param1"] + " by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " not successfully added to " + paramDict["param1"] + ".\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " not successfully added to " + paramDict["param1"] + ".\n" + e);
            }
        }

        // Function that removes a User from a Group in Active Directory
        public static void removeMember(Dictionary<string, string> paramDict) {
            DirectoryEntry c = new DirectoryEntry(ConfigurationManager.AppSettings["LDAP Path"], paramDict["bindUser"] + ConfigurationManager.AppSettings["AD Address"], paramDict["bindPass"]);
            c.AuthenticationType = AuthenticationTypes.Secure;
            try {
                DirectoryEntry group = c.Children.Find("cn=" + paramDict["param1"], "group");
                DirectoryEntry member = c.Children.Find("cn=" + paramDict["targetUser"], "user");
                group.Properties["member"].Remove(member.Properties["distinguishedName"].Value);
                group.CommitChanges();

                Console.WriteLine("[Task " + paramDict["id"] + "] Member " + paramDict["targetUser"] + " removed from " + paramDict["param1"] + " by " + c.Username);
                Logger.Write("[Task " + paramDict["id"] + "] Member " + paramDict["targetUser"] + " removed from " + paramDict["param1"] + " by " + c.Username);
            }
            catch (Exception e){
                Console.WriteLine("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " not successfully removed from " + paramDict["param1"] + ".\n" + e);
                Logger.Write("[Task " + paramDict["id"] + "] " + paramDict["targetUser"] + " not successfully removed from " + paramDict["param1"] + ".\n" + e); 
            }
        }
    }
}
