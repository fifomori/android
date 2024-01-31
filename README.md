Languages: [русский](README_ru.md)

# OMORI for Android

![spacexh_happy](.github/assets/spacexh_happy.png)

Android WebView + [nwcompat](https://github.com/fifomori/nwcompat)

# Prerequisites

- [OMORI](https://store.steampowered.com/app/1150690/OMORI)

# Installing

1. Get and install APK
2. Copy installed OMORI from Steam to your phone
3. Run app on your phone for first time, select game directory (из пункта 2) and [enter key](#get-a-key)
4. Now you can run game normally

# Get a key

1. Launch OMORI from Steam
2. Launch cmd (Win+R -> `cmd`)
3. Paste and run this command

```cmd
wmic process where caption='OMORI.exe' get commandline | findstr .*--6
```

4. Key is the string after `--` (not including `--`)

# OneLoader

To use OneLoader, [install](https://mods.one/mod/oneloader) it to your OMORI directory and enable it in settings

**IMPORTANT**: see [chromori](https://github.com/fifomori/chromori#oneloader-)

# Building

1. Build [nwcompat](https://github.com/fifomori/nwcompat)
2. Build in Android Studio

# [Credits](https://github.com/fifomori/nwcompat#credits)
