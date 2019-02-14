cls
$searcher = New-Object System.DirectoryServices.DirectorySearcher([ADSI]'')

# get domain password policy (max pw age)
$D = [System.DirectoryServices.ActiveDirectory.Domain]::GetCurrentDomain()
$Domain = [ADSI]"LDAP://$D"
$MPA = $Domain.maxPwdAge.Value
$MinPA = $Domain.minPwdAge.Value
# Convert to Int64 ticks (100-nanosecond intervals).
$lngMaxPwdAge = $Domain.ConvertLargeIntegerToInt64($MPA)
$lngMinPwdAge = $Domain.ConvertLargeIntegerToInt64($MinPA)
$MaxPwdAge = -$lngMaxPwdAge/(600000000 * 1440)
$MinPwdAge = -$lngMinPwdAge/(600000000 * 1440)
Write-Host "Max Password Age : " $MaxPwdAge
Write-Host "Min Password Age : " $MinPwdAge
