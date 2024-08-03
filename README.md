Languages: [русский](README_ru.md)

# OMORI for Android

![spacexh_happy](.github/assets/spacexh_happy.png)

Android WebView + [nwcompat](https://github.com/fifomori/nwcompat)

# Prerequisites

- [OMORI](https://store.steampowered.com/app/1150690/OMORI)

# Installing

## [Video tutorial](https://youtu.be/vewM9YLIpB0)

1. Get and install APK
2. Copy `www` folder from your OMORI installation to your phone
3. Run app on your phone for first time, select game directory (step 2) and [enter key](#get-a-key)
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

# Credits

- Onscreen gamepad buttons are made using [Xelu's FREE Controller Prompts](https://thoseawesomeguys.com/prompts/) and optimized using a tool from [this stackoverflow answer](https://stackoverflow.com/a/74330757/22076815)

- For more credits see [nwcompat](https://github.com/fifomori/nwcompat#credits)
