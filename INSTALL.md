# Install

1. Get and install APK
1. Copy installed OMORI from Steam to your phone
1. Run OMORI on your phone for first time
   - Select OMORI directory and [enter key](#get-a-key)
1. Now you can run OMORI normally

# Get a key

1. Launch OMORI from Steam
2. Launch cmd (Win+R -> `cmd`)
3. Paste and run this command

```cmd
wmic process where caption='OMORI.exe' get commandline | findstr .*--6
```

4. Key is the string after `--` (not including `--`)