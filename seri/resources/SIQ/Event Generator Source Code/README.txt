--------
Settings
--------
Personal settings for file paths can be set in the application .config file,
make sure to edit these to the correct specificaitions:
	LDAP Path
	CSV Path- a log directory will be created at this path that will be globally accessible.
	Log Path
	AD Address
	FS Domain

Also be sure to edit the CSV file.

-------------
Functionality
-------------
Initially the Parser class will take in a header-defined CSV file reading
line by line and creating a unique Task object for each individual line.
The backbone of the code is done within the Task Class in the Task object 
there exists a Timer class which executes a static method from the FilySys
or ActiveDirectory class.


---
API
---

Active Directory:
	public static void createUser(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, targetPass, time, action

	public static void deleteUser(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, time, action

	public static void modifyPass(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, targetPass, time, action, param1[newTargetPass]
	
	public static void modifyProp(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, targetPass, time, action, param1[propName], param2[property]
	
	public static void createGroup(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[groupName]
	
	public static void deleteGroup(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[groupName]

	public static void addMember(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, time, action, param1[groupName]
	
	public static void removeMember(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, targetUser, time, action, param1[groupName]

CSV:
	CSV Format
	+----+----------+----------+------------+------------+------+--------+---------+--------+--------+
	| id | bindUser | bindPass | targetUser | targetPass | time | action |  param1 | param2 | param3 |
	+----+----------+----------+------------+------------+------+--------+---------+--------+--------+

FileSys:
	public static void createFile(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath]

	public static void deleteFile(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath]

	public static void readFile(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath]

	public static void writeFile(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath], param2[writeText]

	public static void moveFile(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath], param2[newFilePath]

	public static void fileAccess(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[filePath], param2[accessType*], param3[accessSetting**]
		* accessType defined as a case sensitive string from FileSystemRightsEnumeration 
			Found here: https://msdn.microsoft.com/en-us/library/system.security.accesscontrol.filesystemrights(v=vs.110).aspx
		* accessSetting defined as a case sensitive string from AccessControlType (eg. "Allow")
			Found here: https://msdn.microsoft.com/en-us/library/w4ds5h86(v=vs.110).aspx

	public static void createDir(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[dirPath]

	public static void deleteDir(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[dirPath]

	public static void readDir(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[dirPath]

	public static void moveDir(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[dirPath], param2[newDirPath]

	public static void dirAccess(Dictionary<string,string> paramDict)
		-Parameters used: id, bindUser, bindPass, time, action, param1[dirPath], param2[accessType*], param3[accessSetting**]
		* accessType defined as a case sensitive string from FileSystemRightsEnumeration 
			Found here: https://msdn.microsoft.com/en-us/library/system.security.accesscontrol.filesystemrights(v=vs.110).aspx
		* accessSetting defined as a case sensitive string from AccessControlType (eg. "Allow")
			Found here: https://msdn.microsoft.com/en-us/library/w4ds5h86(v=vs.110).aspx

Impersonate:
	public Impersonator(string userName, string domainName, string password){}

Log:
	public static void createLog()

	public static void Write(string s)

Parser: 
	**Contains the main method**
	
	public static ArrayList parse(String filePath)

Task: 
	public Task(Dictionary<string,string> dict)

	private static int setTime(string t)

	private void TaskRunner(object sender, ElapsedEventArgs e)

	public void startTask()
