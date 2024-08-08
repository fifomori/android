# OMORI для Android

![spacexh_happy](.github/assets/spacexh_happy.png)

Android WebView + [nwcompat](https://github.com/fifomori/nwcompat)

# Требования

- [OMORI](https://store.steampowered.com/app/1150690/OMORI)

# Установка

## [Видео](https://youtu.be/vewM9YLIpB0)

1. Скачайте и установите APK
2. Скопируйте на телефон папку `www` из установленной копии OMORI
3. Запустите приложение в первый раз, выберите папку установки OMORI (из пункта 2) и [введите ключ](#получение-ключа)
4. Теперь вы можете запускать игру

# Получение ключа

1. Запустите OMORI через Steam
2. Откройте командную строку (Win+R -> `cmd`)
3. Выполните эту команду

```cmd
wmic process where caption='OMORI.exe' get commandline | findstr .*--6
```

4. Ключ - строка после `--` (не включая `--`)

# OneLoader

Чтобы использовать OneLoader, [установите](https://mods.one/mod/oneloader) его в папку установки OMORI и включите его в настройках

![warning](.github/assets/warning.gif) **ВАЖНО**: ознакомьтесь с [chromori](https://github.com/fifomori/chromori#oneloader-)

# Сборка

1. Соберите [nwcompat](https://github.com/fifomori/nwcompat)
2. Соберите проект в Android Studio

# Благодарности

- Наэкранные кнопки геймпада сделаны с помощью [Xelu's FREE Controller Prompts](https://thoseawesomeguys.com/prompts/) и оптимизированы инструментов из [этого ответа на stackoverflow](https://stackoverflow.com/a/74330757/22076815)

- Дополнительно в [nwcompat](https://github.com/fifomori/nwcompat#credits)
