#!/bin/bash
#
# Syntax: iiq-increaseIndexes.sh <sourceWAR>
#

#Change these numbers to the number of extended, indexed attributes for each type

ApplicationExtendedAttributes=20
BundleExtendedAttributes=20
CertItemExtendedAttributes=20
IdentityExtendedAttributes=20
LinkExtendedAttributes=20
ManagedAttributeExtendedAttributes=10

#Function to regenerate hbm.xml files

function makeHbmXml() {

  # $1=target filename
  # $2=number of records
  # $3=index prefix (e.g. spt_bundle)

  hbmloc="WEB-INF/classes/sailpoint/object"

  filename=$hbmloc/$1.hbm.xml

  mv "$filename" "$filename.old"

  for i in `seq 1 $2`;
  do
    echo "<property name=\"extended$i\" type=\"string\" length=\"450\" index=\"$3_extended${i}_ci\"/>" >> $filename
  done 

}

function appendManyToOne() {

  # $1=target filename
  # $2=number of records

  filename=$hbmloc/$1.hbm.xml

  for i in `seq 1 $2`;
  do
    echo "<many-to-one name=\"extendedIdentity$i\" class=\"sailpoint.object.Identity\"/>" >> $filename
  done 

}

function appendLinkKey() {

  # $1=target filename
  # $2=number of records

  filename=$hbmloc/$1.hbm.xml

  for i in `seq 1 $2`;
  do
    echo "<property name=\"key$i\" type=\"string\" length=\"450\" index=\"spt_link_key${i}_ci\"/>" >> $filename
  done 

}


if [ "$1" = "" ]; then
  echo Syntax: $0 sourceWAR
  exit 0
fi
sourceWar=`readlink -f $1`

type -P jar > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo Unable to find jar command on PATH
  exit -1
fi

type -P mktemp > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo Unable to find mktemp command on PATH
  exit -1
fi

tempdir=`mktemp -d`
echo Using temp Dir: $tempdir

# Check if we're in Cygwin
# if so we have to convert paths for jar.exe
if [ "`uname -o`" = "Cygwin" ]; then
  echo cygwin=1
  cygwin=1
else
  echo cygwin=0
  cygwin=0
fi

if [ $cygwin -eq 1 ]; then
  echo updating sourceWar for cygwin
  sourceWar=`cygpath -wp $sourceWar`
  echo sourceWar now is $sourceWar
fi

currentDir=`pwd`

pushd $tempdir >/dev/null 2>&1

echo Using WAR: $sourceWar

jarcmd="jar xvf \"$sourceWar\""
eval $jarcmd
rm WEB-INF/database/*{db2,mysql,oracle,sqlserver}

# Regenerate hbm.xml files

echo Generating new hbm.xml files

makeHbmXml "ApplicationExtended" $ApplicationExtendedAttributes "spt_app"
makeHbmXml "BundleExtended" $BundleExtendedAttributes "spt_bundle"
makeHbmXml "CertificationItemExtended" $CertItemExtendedAttributes "spt_certitem"
makeHbmXml "IdentityExtended" $IdentityExtendedAttributes "spt_identity"
appendManyToOne "IdentityExtended" 5
makeHbmXml "LinkExtended" $LinkExtendedAttributes "spt_link"
appendLinkKey "LinkExtended" 4
makeHbmXml "ManagedAttributeExtended" $ManagedAttributeExtendedAttributes "spt_managed_attr"

echo ..Done

echo Regenerating DDL files
cd WEB-INF/bin
./iiq schema
cd ../..
echo ..Done

echo Fixing iiq.properties
cd WEB-INF/classes
mv iiq.properties iiq.properties.old
sed -e s/identityiq55p/identityiq/g < iiq.properties.old > iiq.properties
cd ../..

echo creating new War File
mvcmd="mv \"$sourceWar\" \"$sourceWar.old\""
eval $mvcmd

jarcmd="jar cvf \"$sourceWar\" \"*\""
eval $jarcmd

echo Done. Updated War file now at $sourceWar

echo Removing temporary directory
popd
rm -r $tempdir
