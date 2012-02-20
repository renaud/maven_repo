#!/bin/sh

# http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/v2.0/DOC/esoap_java_help.html
# ftp://ftp.ncbi.nlm.nih.gov/pub/eutils/soap/v2.0/java/axis2_1.5.2_jdk_6.0.12/

FILE=eutils-2.0.jar
GROUP_ID=gov.nih.nlm.ncbi
ARTIFACT_ID=eutils
VERSION=2.0

REPO=snapshot-repo
URL=file:/Users/richarde/dev/sources/maven_repo/snapshots/
REMOTE_URL=https://github.com/renaud/maven_repo/raw/master/snapshots

mvn deploy:deploy-file -Dfile=$FILE  -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion=$VERSION -Dpackaging=jar -DrepositoryId=$REPO -Durl=$URL

echo '\n\n<dependency>\n<groupId>'$GROUP_ID'</groupId>\n<artifactId>'$ARTIFACT_ID'</artifactId>\n<version>'$VERSION'</version>\n</dependency>\n\n'
echo '\n<repositories>\n<repository>\n<id>'$REPO'</id>\n<url>'$REMOTE_URL'</url>\n</repository>\n</repositories>\n\n'

echo 'cd to '$URL