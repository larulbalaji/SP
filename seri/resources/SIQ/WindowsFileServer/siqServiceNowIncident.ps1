param ( [string]$UserAndFiles = "Default Input")

#Uncomment to check proper values are being passed in
#Write-Output "Input=" $UserAndFiles | Out-File C:\SailPoint\Tools\snowps.log -Append

$arr = $UserAndFiles.Split('''')
$user = $arr[1]
$action = $arr[3]
$path = $arr[5]
$object = $arr[7]

#Additional logging to check that the param is split correctly
#Write-Output "User=" $user | Out-File C:\SailPoint\Tools\snowps.log -Append
#Write-Output "action=" $action | Out-File C:\SailPoint\Tools\snowps.log -Append
#Write-Output "path=" $path | Out-File C:\SailPoint\Tools\snowps.log -Append
#Write-Output "object=" $object | Out-File C:\SailPoint\Tools\snowps.log -Append

#Change Values to match ServiceNow Enpoint if needed
$uri = "https://ven01309.service-now.com/x_sapo_iiq_sim_incident.do?SOAP"
$username = 'spadmin'
$password = 'Sailp0!nt'

$
ml = [xml]@"
  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:x="http://www.service-now.com/x_sapo_iiq_sim_incident">
				  	    <soapenv:Header></soapenv:Header>
						<soapenv:Body>
						  <x:insert>
						    <opened_by>spadmin</opened_by>
							<category>request</category>
							<assignment_group>Service Desk</assignment_group>
							<contact_type>email</contact_type>
							<impact>3</impact>
							<urgency>3</urgency>
							<incident_state>1</incident_state>
							<short_description>SecurityIQ File Access Alert by User $($User)</short_description>
							<description>
User : $($User) 
Action Taken on File : $($action)
File Acted Upon : $($path)/$($object)
                            </description>
						  </x:insert>
		                </soapenv:Body>
		              </soapenv:Envelope>
"@

$header = @{"Authorization" = "Basic "+[System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($username+":"+$password))}
$post = Invoke-WebRequest -Uri $uri -Headers $header -Method Post -Body $xml -ContentType "text/xml"