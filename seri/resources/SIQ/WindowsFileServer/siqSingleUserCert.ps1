param(
[string]$identity
)
$url="http://seri.sailpointdemo.com:8080/identityiq/rest/workflows/SecurityIQ-SingleUserCert/launch"
$username="spadmin"
$password="admin"

$body=@{
  workflowArgs=@{
    background='false'
     identityName=$identity
  }
}

$jsonBody=$body | ConvertTo-Json

$params = @{
             uri = $url;
             Method = 'Post';
             Headers = @{
               Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($username):$($password)"));
             }
             Body = $jsonBody;
             ContentType='application/json';
           }

Invoke-RestMethod @params