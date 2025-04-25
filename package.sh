#!/bin/bash

APP_NAME="sb-img-util"

VERSION=$(grep -i "version: " "target/classes/application.yml" | sed 's/^.*: //')
echo "current version: $VERSION"
VENDOR="JGatsby， Civin@bupt.cn"
JDK_PATH_UNIX="$2"
OUTPUT_PATH="build/${APP_NAME}"

if [ -z "$JDK_PATH_UNIX" ]; then
   JDK_PATH_UNIX=${JAVA_HOME}
fi
export JAVA_HOME=$JDK_PATH_UNIX

rm -rf "${OUTPUT_PATH}"
sh ./mvnw clean package -Dmaven.test.skip=true

JPKGCMD="${JAVA_HOME}/bin/jpackage"
$JPKGCMD --type app-image --name "$APP_NAME" --input target \
--main-jar "${APP_NAME}-${VERSION}.jar"  --icon logo.ico  \
 --arguments "--spring.profiles.active=prod" --dest "build"

cp Licence.txt "${OUTPUT_PATH}"
cp logo.ico "$OUTPUT_PATH"
cp "README.MD" "$OUTPUT_PATH"

APP_JAR_DIR="$OUTPUT_PATH/app"
rm -rf ${APP_JAR_DIR}/classes ${APP_JAR_DIR}/generated-sources  ${APP_JAR_DIR}/generated-test-sources ${APP_JAR_DIR}/maven-archiver ${APP_JAR_DIR}/maven-status ${APP_JAR_DIR}/test-classes

echo "-------------处理完成--------------"

#
#$JPKGCMD --type msi --name "APP_NAME" --app-image $OUTPUT_PATH --license-file Licence.txt \
#--vendor "$VENDOR"  --app-version "$VERSION" --win-upgrade-uuid "$UPGRADE_UID"  --dest ./build1
##--win-menu-group $NAME --win-menu --win-dir-chooser \

