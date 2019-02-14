#     Program: createFolders.ps1
#       Title: SIQ Folder Creator
# Description: Creates a folder structure under X:\Data on the SERI-FS-RI
#              Permissions get set on the folders, AD Groups and specific users.
#              Files get created containing key words relating to Data Classification policies  
# Author:      Steve Kendall - Tech Specialist
#
Import-Module ServerManager
Add-WindowsFeature RSAT-AD-PowerShell
Import-Module ActiveDirectory
Import-Module NTFSSecurity

#
# Drive
$drive = "X:\Corporate Data"

# Date
$t = Get-Date -format D
# Top Level Folders
$topLevel = @("BoardOfDirectors","Departments","Projects","TestData","Users","Share")
foreach ($top in $topLevel) {
   New-Item -ItemType Directory -Force -Path ${drive}\${top}
   if ( $top = "Departments"){
     Add-NTFSAccess -Path ${drive}\${top} -Account 'SERI\aaron.nichols' -AccessRights FullControl
   }
   if ($top = "Share" ){
     $folder = "${drive}\${top}"
     $acl = Get-ACL -Path $folder
     $acl.SetAccessRuleProtection($True, $True)
     Set-Acl -Path $folder -AclObject $acl
   }
}

# secondLevel Folders
# BoardOfDirectors
     $secondLevel = @("BoardMeetingMinutes","EBITDA_results")
     foreach ($second in $secondLevel) {
	   New-Item -ItemType Directory -Force -Path ${drive}\BoardOfDirectors\${second}
	   if ($second = "BoardMeetingMinutes") {
	     Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\HR_All' -AccessRights FullControl
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\HR_Internal' -AccessRights FullControl
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\BenefitCommittee' -AccessRights FullControl
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\CompCommittee' -AccessRights FullControl
		 
         $dc = "$t - SecurityIQ 5.0 - Confidential HR"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\EmployeeDismissalPolicy.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential Finance"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\ExpenseReporting.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\mergePlanMeeting.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\notesFromPaulMeeting.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\pub_relationsMinutes.txt
	   }
	   if ($second = "EBITDA_results") {
	     Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\FinanceUsers' -AccessRights FullControl
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\TaxAccounting' -AccessRights FullControl
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\TaxAuthority' -AccessRights FullControl
         $dc = "$t - SecurityIQ 5.0 - Confidential Finance Tax"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\ebitdaQ1_FY2016.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential Finance Tax"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\ebitdaQ2_FY2016.txt
         $dc = "$t - SecurityIQ 5.0 - Confidential Finance Tax"
	     $dc | Set-Content ${drive}\BoardOfDirectors\${second}\ebitdaQ3_FY2015.txt
	   }	   
	   
	 }
	

# Departments

     $secondLevel = @("Accounting","Audit","Development","Finance","FolderToMove","Human Resources","Inventory","Operations")
     foreach ($second in $secondLevel) {
       New-Item -ItemType Directory -Force -Path ${drive}\Departments\${second}
       if ($second = "Accounting"){
	     #Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SYSTEM' -AccessRights FullControl
		 #Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SIQ1\Administrators' -AccessRights FullControl
	     Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SERI\AccountingGeneral' -AccessRights ReadAndExecute
	   }
       if ($second = "Audit"){
	     Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SERI\AccountingGeneral' -AccessRights ReadAndExecute
		 Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SERI\DataArchive' -AccessRights ReadAndExecute
		 $dc = "$t - SecurityIQ 5.0 - Audit"
	     $dc | Set-Content ${drive}\Departments\${second}\audit_schedule_USWest.txt
		 $dc = "$t - SecurityIQ 5.0 - Audit"
	     $dc | Set-Content ${drive}\Departments\${second}\auditRecs.txt
		 $dc = "$t - SecurityIQ 5.0 - Audit"
	     $dc | Set-Content ${drive}\Departments\${second}\internalAudit_FY2016.txt
		 $dc = "$t - SecurityIQ 5.0 - Audit"
	     $dc | Set-Content ${drive}\Departments\${second}\materialFindings_jan16.txt
	   }
       if ($second = "Development"){
         New-Item -ItemType Directory -Force -Path ${drive}\Departments\${second}\test1
       }
       if ($second = "Finance"){
         New-Item -ItemType Directory -Force -Path ${drive}\Departments\${second}\Loans
	     $dc = "$t - SecurityIQ 5.0 - Finance"
	     $dc | Set-Content ${drive}\Departments\${second}\capital_depr.txt
		 $dc = "$t - SecurityIQ 5.0 - Finance"
	     $dc | Set-Content ${drive}\Departments\${second}\district7capital.txt
		 $dc = "$t - SecurityIQ 5.0 - Finance"
	     $dc | Set-Content ${drive}\Departments\${second}\refi_loans.txt
       }	
       if ($second = "FolderToMove"){
	     $dc = "$t - SecurityIQ 5.0 `r`nDave Visa 4111-1111-1111-1111"
	     $dc | Set-Content ${drive}\Departments\${second}\TextTest.txt
       }
       if ($second = "Human Resources"){
         New-Item -ItemType Directory -Force -Path "${drive}\Departments\${second}\Comp Plans"
		 New-Item -ItemType Directory -Force -Path ${drive}\Departments\${second}\Employees
		 New-Item -ItemType Directory -Force -Path ${drive}\Departments\${second}\Interviews
		 New-Item -ItemType Directory -Force -Path "${drive}\Departments\${second}\Performance Reviews"
		 Add-NTFSAccess -Path ${drive}\BoardOfDirectors\${second} -Account 'SERI\HR_Internal' -AccessRights FullControl
         $dc = "$t - SecurityIQ 5.0 - HR `r`nPassport Nos. `r`n761325119 Peter Duncan `r`n752678299 Jeremy Smith"
	     $dc | Set-Content ${drive}\Departments\${second}\Benefits.txt 
       }	   
	 } 
# Projects
     $secondLevel = @("project_DAG","project_IAM","project_IGA","project_X","project_Y","project_Z")
     foreach ($second in $secondLevel) {
       New-Item -ItemType Directory -Force -Path ${drive}\Projects\${second} 
	   
	   if ($second = "project_X"){
	     #Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SYSTEM' -AccessRights FullControl
		 #Add-NTFSAccess -Path ${drive}\Departments\${second} -Account 'SIQ1\Administrators' -AccessRights FullControl
	     Add-NTFSAccess -Path ${drive}\Projects\${second} -Account 'SERI\Training' -AccessRights ReadAndExecute
	   }
	   if ($second = "project_DAG") {
	     New-Item -ItemType Directory -Force -Path ${drive}\Projects\${second}\YE_2016
	     $dc = "$t - SecurityIQ 5.0 - Governance"
	     $dc | Set-Content ${drive}\Projects\${second}\YE_2016\January16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\February16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\March16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\April16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\May16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\June16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\July16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\August16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\September16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\October16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\November16.txt
		 $dc | Set-Content ${drive}\Projects\${second}\YE_2016\December16.txt
	   }
	   if ($second = "project_IGA") {
	     $dc = "$t - SecurityIQ 5.0 - Customers`r`nAPX1-100024 Our Customer`r`nAPX1-102402 Another Customer`r`nAPX1-101902 Third Customer`r`nAPX1-104650 Ninth Customer`r`nAPX1-111332 Big Customer`r`nAPX1-103244 Small Customer`r`nAPX1-199999 ANew Customer`r`nAPX1-123739 Middle Customer"
	     $dc | Set-Content ${drive}\Projects\${second}\CustomerIDs.txt
	   
	   }
	 }
 
# Share
     $secondLevel = @("exec_team","finance","hr")
     foreach ($second in $secondLevel) {
       New-Item -ItemType Directory -Force -Path ${drive}\Share\${second}
	   if ($second = "exec_team") {
         New-Item -ItemType file -Path ${drive}\Share\${second}\2016-Strategy-SWOT-docx
		 New-Item -ItemType file -Path ${drive}\Share\${second}\2017-FY-Forecast.xls
       }
       if ($second = "finance") {
         New-Item -ItemType Directory -Force -Path ${drive}\Share\${second}\TimeSheetAdmins-Reports
		 New-Item -ItemType file -Path ${drive}\Share\${second}\FY2016-BudgetSummit.docx
		 New-Item -ItemType file -Path ${drive}\Share\${second}\GeneralLedger-Export.csv
       }
      if ($second = "hr") {
		 New-Item -ItemType file -Path ${drive}\Share\${second}\2014-payroll-analysis.xls
       }	   
	   
	 }

	 
# thirdLevel Folders
# Accounting
    $thirdLevel = @("Acct_2013","Acct_2014","Acct_2015","Tax")
	$link = "Departments\Accounting"
	foreach ($third in $thirdLevel) {
	       if ($third = "Acct_2013") {
		     New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q1_13
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q1_13 -Account 'EVERYONE' -AccessRights FullControl
			 $dc = "$t - SecurityIQ 5.0 - Accounts"
	         $dc | Set-Content ${drive}\$link\${third}\Q1_13\q1_findings.txt
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q2_13
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q2_13 -Account 'EVERYONE' -AccessRights FullControl
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q3_13
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q3_13 -Account 'EVERYONE' -AccessRights FullControl
			 $dc = "$t - SecurityIQ 5.0 - Accounts"
	         $dc | Set-Content ${drive}\$link\${third}\Q3_13\accountingPrinc.txt
	         $dc | Set-Content ${drive}\$link\${third}\Q3_13\findingsFIN.txt
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q4_13
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q4_13 -Account 'EVERYONE' -AccessRights FullControl
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q4_13 -Account 'SERI\aaron.nichols' -AccessRights Modify
		   }
		   if ($third = "Acct_2014") {
		     New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q1_14
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q1_14 -Account 'EVERYONE' -AccessRights FullControl
			 $dc = "$t - SecurityIQ 5.0 - Accounts"
	         $dc | Set-Content ${drive}\$link\${third}\Q1_14\Q1_paymentSchedule.txt
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q2_14
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q2_14 -Account 'EVERYONE' -AccessRights FullControl
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q2_14 -Account 'SERI\aaron.nichols' -AccessRights ReadAndExecute
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q3_14
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q3_14 -Account 'EVERYONE' -AccessRights FullControl
			 $dc = "$t - SecurityIQ 5.0 - Accounts"
	         $dc | Set-Content ${drive}\$link\${third}\Q3_14\Q3_paymentSchedule.txt
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q4_14
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q4_14 -Account 'EVERYONE' -AccessRights FullControl
			 $dc = "$t - SecurityIQ 5.0 - Accounts"
	         $dc | Set-Content ${drive}\$link\${third}\Q4_14\Q4_paymentSchedule.txt
		   }
		   if ($third = "Acct_2015") {
		     New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q1_15
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q1_15 -Account 'EVERYONE' -AccessRights FullControl
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q2_15
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q2_15 -Account 'EVERYONE' -AccessRights FullControl
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q3_15
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q3_15 -Account 'EVERYONE' -AccessRights FullControl
			 New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}\Q4_15
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q4_15 -Account 'EVERYONE' -AccessRights FullControl
			 Add-NTFSAccess -Path ${drive}\$link\${third}\Q4_15 -Account 'SERI\aaron.nichols' -AccessRights FullControl
		   }
		   if ($third = "Tax") {
	         New-Item -ItemType Directory -Force -Path ${drive}\$link\${third}
			 Add-NTFSAccess -Path ${drive}\$link\${third} -Account 'SERI\TaxAccounting' -AccessRights Modify
			 Add-NTFSAccess -Path ${drive}\$link\${third} -Account 'SERI\TaxAuthority' -AccessRights ReadAndExecute
	       }
	}
	
# Users
#$users = get-aduser -SearchBase "ou=demo,dc=seri,dc=sailpointdemo,dc=com" -Filter * | select -ExpandProperty SamAccountName

$users = Get-aduser -SearchBase "dc=seri,dc=sailpointdemo,dc=com" -Filter * | select -ExpandProperty SamAccountName
$homedir = "$drive\Users"

foreach ($user in $users) {
  New-Item -ItemType Directory -Force -Path ${drive}\Users\${user}  
  New-Item -ItemType Directory -Force -Path "${drive}\Departments\Human Resources\Interviews\${user}"
  New-Item -ItemType Directory -Force -Path "${drive}\Departments\Human Resources\Performance Reviews\${user}"
  Add-NTFSAccess -Path ${drive}\Users\${user} -Account SERI\${user} -AccessRights Modify
  Add-NTFSAccess -Path "${drive}\Departments\Human Resources\Interviews\${user}" -Account SERI\${user} -AccessRights Modify
  Add-NTFSAccess -Path "${drive}\Departments\Human Resources\Performance Reviews\${user}" -Account SERI\HR_All -AccessRights ReadAndExecute
  Add-NTFSAccess -Path "${drive}\Departments\Human Resources\Performance Reviews\${user}" -Account SERI\HR_Internal -AccessRights Modify
  
}










