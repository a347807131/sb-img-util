#!/bin/bash

APP_NAME="sb-img-util"

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
$JPKGCMD --type app-image --name "$APP_NAME" --input target \
--main-jar "${APP_NAME}-${VERSION}.jar"  --icon logo.ico --win-console  \
 --arguments "--spring.profiles.active=prod" --dest "build"

cp Licence.txt "$OUTPUT_PATH/$APP_NAME"
cp logo.ico "$OUTPUT_PATH/$APP_NAME"
cp "README.MD" "$OUTPUT_PATH/$APP_NAME"
#cp -r pyApi "$OUTPUT_PATH/$NAME"

APP_JAR_DIR="$OUTPUT_PATH/$APP_NAME/app"
rm -rf ${APP_JAR_DIR}/classes ${APP_JAR_DIR}/generated-sources  ${APP_JAR_DIR}/generated-test-sources ${APP_JAR_DIR}/maven-archiver ${APP_JAR_DIR}/maven-status ${APP_JAR_DIR}/test-classes

echo "-------------处理完成--------------"

#
#$JPKGCMD --type msi --name "$NAME" --app-image $OUTPUT_PATH --license-file Licence.txt \
#--vendor "$VENDOR"  --app-version "$VERSION" --win-upgrade-uuid "$UPGRADE_UID"  --dest ./build
##--win-menu-group $NAME --win-menu --win-dir-chooser \

