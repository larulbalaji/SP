#  Program: setPerms.ps1
#  Purpose: Set NTFS Permissions on a Windows File System.
# Language: Windows PowerShell
#   Author: Steve Kendall
#  Version: 2.0
#    Usage: .\setPerms.ps1 
#    Input:  Output CSV file of getPerms.ps1 script
# TYPE System.Security.AccessControl.FileSystemAccessRule
#"Path","FileSystemRights","AccessControlType","IdentityReference","IsInherited","InheritanceFlags","PropagationFlags"
#"X:\Corporate Data\Example\subDir1","ReadAndExecute","Allow","SERI\Jery.Bennett","False","None","None"
#"X:\Corporate Data\Example\subDir1","FullControl","Allow","SERI\Randy.Knight","False","None","None"
#

Import-Module ServerManager
Add-WindowsFeature RSAT-AD-PowerShell
Import-Module ActiveDirectory
Import-Module NTFSSecurity

#
# Drive
$drive = "X:\Data\"

# Date
$t = Get-Date -format D

# CSV Headers = 
#TYPE System.Security.AccessControl.FileSystemAccessRule
#"Path","FileSystemRights","AccessControlType","IdentityReference","IsInherited","InheritanceFlags","PropagationFlags"

$csv = Import-Csv DB-SERIWithBoxPerms.csv
$count = 0
foreach ($line in $csv) {
  $path = $line.Path
  $rights = $line.FileSystemRights
  $access = $line.AccessControlType
  $identity = $line.IdentityReference
  $inherited = $line.IsInherited
  $inheritanceFlag = $line.InheritanceFlags
  $propogationFlags = $line.PropogationFlags
  
  Write-Host "Path:  $path  Rights:  $rights  Access:  $access  Identity:  $identity  Inherited:  $inherited  InheritanceFlag:  $inheritanceFlag  propogationFlag:  $propogationFlags"
  if(!(Test-Path -Path $path )){
    New-Item -ItemType Directory  -Name ${identity} -Path ${path} 
    Add-NTFSAccess -Path $path -Account ${identity} -AccessRights $rights
  } else {
    Add-NTFSAccess -Path $path -Account ${identity} -AccessRights $rights
  }
  $count++
  echo $count
}
exit
