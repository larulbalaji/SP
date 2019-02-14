#  Program: getPerms.ps1
#  Purpose: Collect NTFS Permissions from a Windows File System.
# Language: Windows PowerShell
#   Author: Steve Kendall
#  Version: 2
#   Useage: .\getPerms.ps1 X:\Data | Export-Csv X:\Corporate Data\getPerms_Audit.csv
#   Output: 
# TYPE System.Security.AccessControl.FileSystemAccessRule
# "Path","FileSystemRights","AccessControlType","IdentityReference","IsInherited","InheritanceFlags","PropagationFlags"
# "X:\","ReadAndExecute, Synchronize","Allow","Everyone","False","None","None"
# "X:\","268435456","Allow","CREATOR OWNER","False","ContainerInherit, ObjectInherit","InheritOnly"
# "X:\","FullControl","Allow","NT AUTHORITY\SYSTEM","False","ContainerInherit, ObjectInherit","None"
# "X:\","FullControl","Allow","BUILTIN\Administrators","False","ContainerInherit, ObjectInherit","None"
# "X:\","AppendData","Allow","BUILTIN\Users","False","ContainerInherit","None"
# "X:\","CreateFiles","Allow","BUILTIN\Users","False","ContainerInherit","InheritOnly"
# "X:\","ReadAndExecute, Synchronize","Allow","BUILTIN\Users","False","ContainerInherit, ObjectInherit","None"
#

function Get-PathPermissions {
 
param ( [Parameter(Mandatory=$true)] [System.String]${Path} )
 
    begin {
    $root = Get-Item $Path
    ($root | get-acl).Access | Add-Member -MemberType NoteProperty -Name "Path" -Value $($root.fullname).ToString() -PassThru
    }
    process {
    $containers = Get-ChildItem -path $Path -recurse | ? {$_.psIscontainer -eq $true}
    if ($containers -eq $null) {break}
        foreach ($container in $containers)
        {
        (Get-ACL $container.fullname).Access | ? { $_.IsInherited -eq $false } | Add-Member -MemberType NoteProperty -Name "Path" -Value $($container.fullname).ToString() -PassThru
        }
    }
}
Get-PathPermissions $args[0]