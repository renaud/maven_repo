#!/bin/sh

FILE=stanford-parser-2011-04-17.jar
GROUP_ID=edu.stanford.nlp
ARTIFACT_ID=parser
VERSION=2011-04-17

REPO=snapshot-repo
URL=file:/Users/ren/dev/sources/maven_repo/snapshots/
REMOTE_URL=https://github.com/renaud/maven_repo/raw/master/snapshots

mvn deploy:deploy-file -Dfile=$FILE  -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion=$VERSION -Dpackaging=jar -DrepositoryId=$REPO -Durl=$URL

echo '\n\n<dependency>\n<groupId>'$GROUP_ID'</groupId>\n<artifactId>'$ARTIFACT_ID'</artifactId>\n<version>'$VERSION'</version>\n</dependency>\n\n'
echo '\n<repositories>\n<repository>\n<id>'$REPO'</id>\n<url>'$REMOTE_URL'</url>\n</repository>\n</repositories>\n\n'

echo 'cd to '$URL