# OMORI for Android

![spacexh_happy](.github/assets/spacexh_happy.png)

Android WebView + [nwcompat](https://github.com/fifomori/nwcompat)

# Prerequisites

- [OMORI](https://store.steampowered.com/app/1150690/OMORI)

# Installing

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

# OneLoader

To use OneLoader, enable it in `OMORI - Settings` app and install [OneLoader](https://mods.one/mod/oneloader) to your OMORI directory

**IMPORTANT**: see [chromori](https://github.com/fifomori/chromori#oneloader-)

# Building

1. Build [nwcompat](https://github.com/fifomori/nwcompat)
1. Build in Android Studio

# Credits

See [nwcompat](https://github.com/fifomori/nwcompat#credits)
