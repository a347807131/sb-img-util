#!/bin/bash

NAME="sb-img-util"

VERSION=$(grep -i "version: " "target/classes/application.yml" | sed 's/^.*: //')
echo "current version: $VERSION"
VENDOR="JGatsby， Civin@bupt.cn"
JDK_PATH_UNIX="$2"
OUTPUT_PATH="build"

if [ -z "$JDK_PATH_UNIX" ]; then
   JDK_PATH_UNIX=${JAVA_HOME}
fi
export JAVA_HOME=$JDK_PATH_UNIX

rm -rf "build"

JPKGCMD="${JAVA_HOME}/bin/jpackage"
$JPKGCMD --type app-image --name "$NAME" --input target \
--main-jar "${NAME}-${VERSION}.jar"  --icon logo.ico --win-console  \
 --arguments "--spring.profiles.active=prod" --dest "build"

cp Licence.txt "$OUTPUT_PATH/$NAME"
cp logo.ico "$OUTPUT_PATH/$NAME"
cp "README.MD" "$OUTPUT_PATH/$NAME"
#cp -r pyApi "$OUTPUT_PATH/$NAME"

echo "-------------处理完成--------------"

#
#$JPKGCMD --type msi --name "$NAME" --app-image $OUTPUT_PATH --license-file Licence.txt \
#--vendor "$VENDOR"  --app-version "$VERSION" --win-upgrade-uuid "$UPGRADE_UID"  --dest ./build
##--win-menu-group $NAME --win-menu --win-dir-chooser \

