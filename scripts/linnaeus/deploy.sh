#!/bin/sh

FILE=linnaeus-2.0.jar
GROUP_ID=hu.u_szeged.rgai.bio.uima
ARTIFACT_ID=linnaeus
VERSION=2.0

REPO=snapshot-repo
URL=file:/Users/ren/dev/sources/maven_repo/snapshots/
REMOTE_URL=http://github.com/renaud/maven_repo/raw/master/snapshots


mvn deploy:deploy-file -Dfile=$FILE  -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion=$VERSION -Dpackaging=jar -DrepositoryId=$REPO -Durl=$URL

echo '\n\n<dependency>\n<groupId>'$GROUP_ID'</groupId>\n<artifactId>'$ARTIFACT_ID'</artifactId>\n<version>'$VERSION'</version>\n</dependency>\n\n'
echo '\n<snapshotRepository>\n<id>'$REPO'</id>\n<url>'$REMOTE_URL'</url>\n</snapshotRepository>\n\n'

echo 'cd to '$URL