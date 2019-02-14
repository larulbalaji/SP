$User = 'Willie.Gomez'
$badpassword = 'abcdef'
#$Credential = Get-Credential -UserName $User -Message 'Enter account with incorrect password.'
Write-Verbose -Message "Locking User $($User)" -Verbose
1..6 | ForEach-Object {
    try {
	    (new-object directoryservices.directoryentry "",$User,$badpassword).psbase.name #-ne $null
    } catch [System.UnauthorizedAccessException] {
        Write-Warning -Message "Access is denied."
    } catch {
        Write-Warning -Message "Unknown Error." 
    }
}

If ((Get-ADUser -id $User -Properties LockedOut).LockedOut -eq $true) {
    Write-Verbose -Message 'Account is locked.' -Verbose
} ElseIf ((Get-ADUser -id $User -Properties LockedOut).LockedOut -eq $false) {
    Write-Verbose -Message 'Account is unlocked.' -Verbose
}