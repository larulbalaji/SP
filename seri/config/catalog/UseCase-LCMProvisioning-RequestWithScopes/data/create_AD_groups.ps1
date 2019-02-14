Import-Module ActiveDirectory 
#Import CSV 
$path     = Split-Path -parent $MyInvocation.MyCommand.Definition  
$newpath  = $path + "\groups_def.csv" 
$csv      = @() 
$csv      = Import-Csv -Path $newpath -Delimiter ','
 
#Get Domain Base 
$searchbase = "DC=seri,DC=sailpointdemo,DC=com"; 
 
#Loop through all items in the CSV 
ForEach ($line In $csv) 
{ 
  #Check if the OU exists
  $ouPath = $line.GroupLocation + "," + $searchbase
  Write-Host "Checking if $ouPath exists"
  $check = [ADSI]::Exists("LDAP://$ouPath")
   
  If ($check -eq $True) 
  { 
    Try 
    { 
      #Check if the Group already exists 
      $exists = Get-ADGroup $line.GroupName 
      Write-Host "Group $($line.GroupName) already exists! Group creation skipped!" 
    } 
    Catch 
    { 
      #Create the group if it doesn't exist 
      $create = New-ADGroup -Name $line.GroupName -GroupScope $line.GroupScope -Path ($($line.GroupLocation)+","+$($searchbase)) 
      Write-Host "Group $($line.GroupName) created!" 
    } 
  } 
  Else 
  { 
    Write-Host "Target OU can't be found! Group creation skipped!" 
  } 
}