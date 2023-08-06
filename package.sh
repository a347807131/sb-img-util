#!/bin/bash

NAME="sb-img-util"
VERSION="2.2.2"
VENDOR="JGatsby， Civin@bupt.cn"
#JAVA_HOME="C:/Users/Gatsby/scoop/apps/openjdk17/17.0.2-8"

rm -rf "build/$NAME"

JPKGCMD="${JAVA_HOME}/bin/jpackage"
$JPKGCMD --type app-image --name "$NAME" --input target \
--main-jar "${NAME}-${VERSION}.jar" --win-console --icon logo.ico  \
 --arguments "--spring.profiles.active=prod" --dest ./build

cp Licence.txt "build/$NAME"
cp logo.ico "build/$NAME"
cp "README.MD" "build/$NAME"

#$JPKGCMD --type msi --name "$NAME" --app-image "build/$NAME" --license-file Licence.txt \
#--vendor "$VENDOR"  --app-version "$VERSION" --win-upgrade-uuid "$UPGRADE_UID"  --dest ./build
##--win-menu-group $NAME --win-menu --win-dir-chooser \

