#!/bin/bash

# Путь к Android SDK и Java
ANDROID_SDK_ROOT=$ANDROID_HOME
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

# Путь к необходимым инструментам
AAPT=$ANDROID_SDK_ROOT/build-tools/30.0.3/aapt
DX=$ANDROID_SDK_ROOT/build-tools/30.0.3/dx
APKBUILDER=$ANDROID_SDK_ROOT/tools/lib/apkbuilder.jar
ANDROID_JAR=$ANDROID_SDK_ROOT/platforms/android-30/android.jar

# Имя сборки
NAME=csgo

# Создание необходимых директорий
mkdir -p gen
mkdir -p bin
mkdir -p bin/classes

# Пакетирование ресурсов и генерация R.java
$AAPT package -m -J gen/ -M AndroidManifest.xml -I $ANDROID_JAR

# Компиляция Java файлов
$JAVA_HOME/bin/javac -d bin/classes -source 1.8 -target 1.8 -cp $ANDROID_JAR src/org/libsdl/app/*.java src/com/nvidia/*.java src/com/valvesoftware/ValveActivity.java

# Генерация .dex файла
$DX --dex --output=bin/classes.dex bin/classes/

# Пакетирование APK файла
$AAPT package -f -M AndroidManifest.xml -I $ANDROID_JAR -F bin/$NAME.apk.unaligned

# Сборка APK файла
java -jar $APKBUILDER bin/$NAME.apk -u -nf libs/ -rj libs -f bin/classes.dex -z bin/$NAME.apk.unaligned

# Подпись APK файла
java -jar $ANDROID_SDK_ROOT/build-tools/30.0.3/lib/signapk.jar $ANDROID_SDK_ROOT/build-tools/30.0.3/lib/testkey.x509.pem $ANDROID_SDK_ROOT/build-tools/30.0.3/lib/testkey.pk8 bin/$NAME.apk bin/$NAME-signed.apk