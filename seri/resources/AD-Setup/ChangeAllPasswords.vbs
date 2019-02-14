'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
Sub ChangeAllPasswordsInOU (OU,passwd)
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

  Dim objOU, objUser, objRootDSE
  Dim strContainer, strDNSDomain
  Dim intUAC

  Const ADS_UF_DONT_EXPIRE_PASSWD = &H10000

  'WScript.Echo "Inside ChangeAllPasswords..."

  ' Bind to Active Directory Domain
  Set objRootDSE = GetObject("LDAP://RootDSE")
  strDNSDomain = objRootDSE.Get("DefaultNamingContext")
  strContainer = OU & "," & strDNSDomain

  'WScript.Echo "...using container: " & strContainer

  ' Loop through OU=, setting passwords for all users
  set objOU = GetObject("LDAP://" & strContainer )
  For each objUser in objOU
    'WScript.Echo "...examinging object: " & objUser.displayName
    If objUser.class="user" then
      'WScript.Echo "...setting password for: " & objUser.displayName
      objUser.SetPassword passwd
      intUAC = objUser.Get("userAccountControl")
      ' Check if "Password Never Expires" already set.
      If (ADS_UF_DONT_EXPIRE_PASSWD AND intUAC) = 0 Then
         ' Set bit for "Password Never Expires".
         objUser.Put "userAccountControl", intUAC OR ADS_UF_DONT_EXPIRE_PASSWD
      End If
      objUser.SetInfo
      'WScript.Echo "...set password for: " & objUser.displayName
    End If
  Next

End Sub



'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'Main
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

WScript.Echo "Starting password changes..."

ChangeAllPasswordsInOU "OU=Austin,OU=Americas,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Brazil,OU=Americas,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=San Jose,OU=Americas,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Singapore,OU=Asia-Pacific,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Tokyo,OU=Asia-Pacific,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Taipei,OU=Asia-Pacific,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Brussels,OU=Europe,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=London,OU=Europe,OU=Demo", "ADpass1$"
ChangeAllPasswordsInOU "OU=Munich,OU=Europe,OU=Demo", "ADpass1$"

WScript.Echo "Done!"

WScript.Quit 