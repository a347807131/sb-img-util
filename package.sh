#!/bin/bash

NAME="sb-img-util"
VERSION="2.2.1"
VENDOR="JGatsby， Civin@bupt.cn"
#JAVA_HOME="C:/Users/Gatsby/scoop/apps/openjdk17/17.0.2-8"
JPKGCMD="${JAVA_HOME}/bin/jpackage"
$JPKGCMD --type app-image --name "$NAME" --input target \
--main-jar "${NAME}-${VERSION}".jar --win-console --icon logo.ico  \
 --arguments "--spring.profiles.active=prod" --dest ./build

#$JPKGCMD --type msi --name "$NAME" --license-file Licence.txt \
#--vendor "$VENDOR"  --app-version "$VERSION" \
#--win-menu-group $NAME --win-menu --win-dir-chooser --app-image "build/$NAME" --dest ./build
