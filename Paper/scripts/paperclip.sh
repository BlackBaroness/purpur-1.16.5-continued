#!/usr/bin/env bash

(
set -e
basedir="$(cd "$1" && pwd -P)"
workdir="$basedir/work"
mcver=$(cat "$workdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
paperjar="$basedir/Paper-Server/target/paper-$mcver.jar"
vanillajar="$workdir/Minecraft/$mcver/$mcver.jar"

(
    cd "$workdir/Paperclip"
    sed -i 's|https://papermc.io/repo/repository/maven-releases/|https://repo.papermc.io/repository/maven-releases/|' assembly/pom.xml
    mvn -U clean package "-Dmcver=$mcver" "-Dpaperjar=$paperjar" "-Dvanillajar=$vanillajar"
)
cp "$workdir/Paperclip/assembly/target/paperclip-${mcver}.jar" "$basedir/paperclip.jar"

echo ""
echo ""
echo ""
echo "Build success!"
echo "Copied final jar to $(cd "$basedir" && pwd -P)/paperclip.jar"
) || exit 1
