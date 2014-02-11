#!/bin/sh

#https://pubchemdb.googlecode.com/svn-history/r32/trunk/PubChemDB/PubChemDB-apps/src/repository/gov/nih/nlm/ncbi/ncbi-eutils-axis2/2.0/ncbi-eutils-axis2-2.0.pom

FILE=ncbi-eutils-axis2-2.0.jar
GROUP_ID=gov.nih.nlm.ncbi
ARTIFACT_ID=ncbi-eutils-axis2
VERSION=2.0

REPO=snapshot-repo
URL=file:/Volumes/HDD2/ren_data/dev_hdd/sources/maven_repo/snapshots/
REMOTE_URL=https://github.com/renaud/maven_repo/raw/master/snapshots

mvn deploy:deploy-file -Dfile=$FILE  -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion=$VERSION -Dpackaging=jar -DrepositoryId=$REPO -Durl=$URL

echo '\n\n<dependency>\n<groupId>'$GROUP_ID'</groupId>\n<artifactId>'$ARTIFACT_ID'</artifactId>\n<version>'$VERSION'</version>\n</dependency>\n\n'
echo '\n<repositories>\n<repository>\n<id>'$REPO'</id>\n<url>'$REMOTE_URL'</url>\n</repository>\n</repositories>\n\n'

echo 'cd to '$URL


# <dependency>
# <groupId>gov.nih.nlm.ncbi</groupId>
# <artifactId>ncbi-eutils-axis2-2.0</artifactId>
# <version>2.0</version>
# </dependency>



# <repositories>
# <repository>
# <id>snapshot-repo</id>
# <url>https://github.com/renaud/maven_repo/raw/master/snapshots</url>
# </repository>
# </repositories>
