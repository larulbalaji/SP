''''''''
''''''''
'creates the demodata in AD if given a seed ou to start in (see vars in main)

'requires three files - groups, users and memberships (path set in main)

'must be run as domain administrator on a box in the domain

'the groups have _AD appended on the end by default to avoid collisions.
'this is on by default.

'The group and user uniquefier's should be set and changed if you create
'a second demo environment in the same domain.  The value is appended to the last name
'of each user and the name of each group.




'Functions
'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub CreateAUser(theAttrList)
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

    Dim objContainer
    Dim objNewUser
    Dim adjustedDN

    'File headings should be:
    ' employeeId;firstName;lastName;department;region;location;isContractor;managerBaseDN;sAMAccountName

    employeeId=theAttrList(0)
    first=theAttrList(1)
    last=theAttrList(2)
    department=theAttrList(3)
    region=theAttrList(4)
    location=theAttrList(5)
    isContractor=theAttrList(6)
    managerBaseDN=theAttrList(7)
    ' Optional field
    sAMAccountName=theAttrList(8)    
    
    ' Bind to Active Directory
    'we want to create the user in the container that matches their region and location

    Set objContainer = GetObject("LDAP://" & sServer &_
                                    "/OU=" & location &_
                                    ",ou=" & region &_
                                    ",ou=" & sBaseContainer &_
                                       "," & sLDAPDomain)
    ' Build the actual User.
    If (Len(sAMAccountName) > 0) Then
		'WScript.Echo "Override cn: " & sAMAccountName & " in container " & "OU=" & location &_
         '                           ",ou=" & region &_
          '                          ",ou=" & sBaseContainer &_
           '                            "," & sLDAPDomain
        Set objNewUser = objContainer.Create("User", "cn=" & sAMAccountName)
	Else
        Set objNewUser = objContainer.Create("User", "cn=" & first & " " &_
                                               last & sUserUniquifier)	
	End If
	 'On Error Resume Next
    objNewUser.Put "givenName", first
    objNewUser.Put "sn", last & sUserUniquifier

    If (Len(sAMAccountName) > 0) Then
	   'WScript.Echo "Override SAM: " & sAMAccountName
       objNewUser.Put "sAMAccountName", sAMAccountName
    Else 
       objNewUser.Put "sAMAccountName", first & "." & last & sUserUniquifier
    End If

	' Getting kind of hacky now; bypass regular account processing if sAMAccountName 
	' is specified in input file (this allows us to seed orphans and service accounts)
    If Not (Len(sAMAccountName) > 0) Then
        objNewUser.Put "displayName", first & " " & last & sUserUniquifier
        objNewUser.Put "userPrincipalName", first & "." &_
                        last & sUserUniquifier & "@" & sMSDomain
        objNewUser.Put "mail", first & "." & last & sUserUniquifier &_
                       "@" & sMSDomain
    End If
	
    If Not employeeId = "NULL" Then
        objNewUser.Put "employeeId", employeeId
    End If

    objNewUser.Put "department", department

    If Not managerBaseDN = "NULL" Then   'skip if string null from file
      
      If sUserUniquifier="" Then         'just use what we're given
        objNewUser.Put "manager", managerBaseDN &_
                       ",ou=" & sBasecontainer &_
                       "," & sLDAPDomain

      Else          'need to append the unique string to lastname in the DN
        sCN = split (managerBaseDN,",ou=",2,1)
        theDN = sCN(0) & sUserUniquifier & ",ou=" & sCN(1)
        objNewUser.Put "manager", theDN &_
                       ",ou=" & sBasecontainer &_
                       "," & sLDAPDomain
                     End If

    End If

    objNewUser.SetInfo

    '512 is normal, but we need a password to meet policy
    'add 32 for PASSWD_NOTREQD flag as appears you have to exist to set password 
    objNewUser.Put "userAccountControl", "544"
    objNewUser.SetInfo

    'now that we exist, set the password and set to 512
    objNewUser.setPassword "ADpass1$"
    objNewUser.Put "userAccountControl", "512"
    objNewUser.SetInfo

End Sub



'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub CreateTheUsers
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objTextFile = objFSO.OpenTextFile _
    (sUsersFile, ForReading)

Do Until objTextFile.AtEndOfStream
    strNextLine = objTextFile.Readline
    attrList = Split(strNextLine , ";")

    'WScript.Echo "Create:" & attrList(1) & " " & attrList(2) & sUserUniquifier
    CreateAUser attrList

Loop

End Sub



'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub AddUsersToGroups
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

Dim sUserDN
Dim sGroupDN
Const ADS_PROPERTY_APPEND = 3

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objTextFile = objFSO.OpenTextFile _
    (sGrpAssignmentsFile, ForReading)

Do Until objTextFile.AtEndOfStream
    sAssignment = objTextFile.Readline
    theAttrList = Split(sAssignment , ";")

    'build the group and user DN's.
    sGroupDN = "cn="  & theAttrList(0)   &_
                        sGroupUniquifier &_
               ",ou=" & sGroupOU         &_
               ",ou=" & sBaseContainer   &_
               ","    & sLDAPDomain
    

    'TODO
    If sUserUniquifier="" Then         'just use what we're given
      sUserDN = theAttrList(1)          &_
                ",ou=" & sBasecontainer &_
                ","    & sLDAPDomain
    Else                                'need to uniquify this
      newCN = split (theAttrList(1),",ou=",2,1)
      sUserDN = newCN(0) & sUserUniquifier &_
                ",ou=" & newCN(1) &_
                ",ou=" & sBaseContainer   &_
                ","    & sLDAPDomain
    End If
    
    'WScript.Echo "groupDN:" & sGroupDN & " , " & "userDN:" & sUserDN

    'all right let's add it
    Set objTheGroup = GetObject("LDAP://" & sServer & "/" & sGroupDN)
	'On Error Resume Next
    objTheGroup.PutEx ADS_PROPERTY_APPEND, "member", Array(sUserDN) 
    objTheGroup.SetInfo

Loop

End Sub



'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub CreateTheOUs
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

  'just hard code the ones we need, doesn't need to be pretty

  ' TODO: Delete and recreate objects?

  Set objBaseOU = GetObject("LDAP://" & sServer & "/" & sLDAPDomain)
 On Error Resume Next
  Set objBaseOU = objBaseOU.Create("organizationalUnit", "ou=" & sBaseContainer)
  objBaseOU.SetInfo

  'create the 3 regions that will contain the locations and 1 for groups
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Americas")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Europe")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Asia-Pacific")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=" & sGroupOU)
  objOU.SetInfo

  'create the US locations
  Set objBaseOU = GetObject("LDAP://" & sServer & "/ou=Americas,OU=" &_
                             sBaseContainer & "," & sLDAPDomain)
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Austin")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Brazil")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=San Jose")
  objOU.SetInfo

  'create the Europe locations
  Set objBaseOU = GetObject("LDAP://" & sServer & "/ou=Europe,OU=" &_
                             sBaseContainer & "," & sLDAPDomain)
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Brussels")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Munich")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=London")
  objOU.SetInfo

  'create the Asia-Paciifc locations
  Set objBaseOU = GetObject("LDAP://" & sServer & "/ou=Asia-Pacific,OU=" &_
                             sBaseContainer & "," & sLDAPDomain)
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Singapore")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Tokyo")
  objOU.SetInfo
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=Taipei")  
  objOU.SetInfo

  'create the Disabled Users OU
  Set objBaseOU = GetObject("LDAP://" & sServer & "/ou=" & sBaseContainer & "," & sLDAPDomain)
  Set objOU = objBaseOU.Create("organizationalUnit", "ou=DisabledUsers")
  objOU.SetInfo
  'create SIQ OUs.
    Set objOU = objBaseOU.Create("organizationalUnit", "ou=SecurityIQ,ou=Groups")
    objOU.SetInfo
    Set objOU = objBaseOU.Create("organizationalUnit", "ou=SharePoint,ou=Groups")
    objOU.SetInfo

End Sub



'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub CreateTheGroups
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objTextFile = objFSO.OpenTextFile _
    (sGroupsFile, ForReading)

Do Until objTextFile.AtEndOfStream
    strGroupDefn = objTextFile.Readline
    theAttrList = Split(strGroupDefn , ";")
    'WScript.Echo "Creating: " & theAttrList(0)
    CreateAGroup theAttrList(0), theAttrList(1), theAttrList(2)

Loop

End Sub



'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub CreateAGroup (groupToCreate, description, manager)
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
  '  Set objRecordSet = objCommand.Execute

  ' Bind to Active Directory
  'create the group in the container we created
  Set objBaseGroupOU = GetObject("LDAP://" & sServer & "/ou=" & sGroupOU &_
                                 ",ou=" & sBaseContainer & "," & sLDAPDomain)
						 
  'WScript.Echo "Creating: " & groupToCreate & sGroupUniquifier
Err.Clear  
  'On Error Resume Next
  Set objGroup = objBaseGroupOU.Create("Group",_
                                       "cn=" & groupToCreate & sGroupUniquifier)
If Err.Number <> 0 Then
    'WScript.Echo "Creating: " & groupToCreate
    WScript.Echo "Error: " & Err.Number
    WScript.Echo "Error (Hex): " & Hex(Err.Number)
    WScript.Echo "Source: " &  Err.Source
    WScript.Echo "Description: " &  Err.Description
    Err.Clear
End If
		
objGroup.Put "sAMAccountName", groupToCreate & sGroupUniquifier


  if (Len(description) > 0) Then
      'WScript.Echo "Setting descr: [" & description & "]" 
      objGroup.description = description
  end if
  
  if (Len(manager) > 0) Then
      'WScript.Echo "Setting managedBy: [" & manager & "]" 
      objGroup.managedBy = manager
  end if

  objGroup.SetInfo
	

End Sub


'~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub ChangeAllPasswordsInOU (OU,passwd)
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

  Dim objOU, objUser, objRootDSE
  Dim strContainer, strDNSDomain

  ' Bind to Active Directory Domain
  Set objRootDSE = GetObject("LDAP://RootDSE")
  strDNSDomain = objRootDSE.Get("DefaultNamingContext")
  strContainer = "OU=" & OU & ", " & strDNSDomain

  ' Loop through OU=, setting passwords for all users
  set objOU =GetObject("LDAP://" & strContainer )
  For each objUser in objOU
    If objUser.class="user" then
      objUser.SetPassword Sailp0int
      objUser.SetInfo
    End If
  Next

End Sub



'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'Main
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Const ForReading = 1

Dim sLDAPDomain     'use for the ldap DN building      
Dim sMSDomain       'use for email address building
Dim sBaseContainer
Dim sServer
Dim sGroupsFile
Dim sUsersFile
Dim sGrpAssignmentsFile
Dim sGroupOU
Dim sGroupUniquifier

sLDAPDomain = "dc=seri,dc=sailpointdemo,dc=com"
sMSDomain = "sailpointdemo.com"
sBaseContainer = "Demo"
sServer = "localhost"
sGroupOU = "Groups"

'Need something to prevent dupe group names in an entire domain
'we'll add this to the end of each group name
'sGroupUniquifier = "_AD"

'if you want more than one set on the same domain
'this value can be set and will be appended to the
'last name of all hte users

'sUserUniquifier = "1"


'path to the 3 files we need
sGroupsFile = "C:\SailPoint\SERI\data\AD\createDemoData\DemoDataADGroups.txt"
sUsersFile =  "C:\SailPoint\SERI\data\AD\createDemoData\DemoDataADUsers.txt"
sGrpAssignmentsFile = "C:\SailPoint\SERI\data\AD\createDemoData\DemoDataADGroupAssignments.txt"


'Setup
'we need to create some ou's for region and location
WScript.Echo "Creating the OU's....."
CreateTheOUs


'create all the users
'With SERI 1.5, we are setting "managedBy" on the groups, so we 
'have to create users before groups
WScript.Echo "OU's created." & vbNewLine & "Creating the Users....."
CreateTheUsers


'Create the groups in the groups OU
WScript.Echo "Users created." & vbNewLine & "Creating the Groups....."
CreateTheGroups


'Assign the users to their groups
WScript.Echo "Groups created." & vbNewLine & "Setting the Group Memberships....."
AddUsersToGroups
WScript.Echo "Group Memberships created."

WScript.Echo "Tasks Complete."

WScript.Quit 



