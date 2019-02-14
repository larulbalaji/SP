#!/bin/bash

#########################################################################################
#
# templateChanger.sh is used to modify the color coding and URL 
# for multiple email templates at once
#
# History:
#
# Version:				0.1 
# Modification date: 	23-08-2013
# Modified by:			Hans-Robert Vermeulen
# Modification:		
#
#
# Version:				0.2 
# Modification date: 	28-08-2013
# Modified by:			Hans-Robert Vermeulen
# Modification:			More templates, added "test" capability
#
#
# Version:				0.3 
# Modification date: 	29-08-2013
# Modified by:			Hans-Robert Vermeulen
# Modification:			Added "body tables" markup
#
#
# Version:				0.4 
# Modification date: 	5-11-2013
# Modified by:			Hans-Robert Vermeulen
# Modification:			SERI-alized for 6.2
#
#
# Version:				0.5 
# Modification date: 	3-10-2014
# Modified by:			Hans-Robert Vermeulen
# Modification:			Added Setup.xml generation
#
#
#########################################################################################

ver="0.5"

function validate_inputFile {
	if [ -e "$confFile" ] ; then
		confFileCheck="ok"
	else
		clear
		echo -e "\nSorry, the file "$confFile" was not found!\n\nPlease enter the name of the customer file to use.\n"
		read confFile
		if [ -e "confFile" ] ; then
			confFileCheck="ok"
		fi
	fi
	return
}

# Determine customer input file
if [ -n "$1" ] ; then
	confFile="$1"
else
	clear
	echo -e "\nPlease enter the name of the customer config file to use, or use \"./templateChange.sh <customer_file>\"\nEither use a file in the current folder, or use the full path to the customer file.\n"
	read confFile
fi

while test -z $confFileCheck
do
	validate_inputFile $confFile
done


################
## Here we go ##
################

# Source in settings from customer file
. "$confFile"


# Start the real work
clear

# Create destination folder and copy raw templates
mkdir "$outputFolderName"
cp templates/* "$outputFolderName"/


# Substitute variables for all files found in folder

FILES="$outputFolderName"/*
for f in $FILES
do
  echo "Processing $f file..."
  # take action on each file. $f store current file name
  sed -i .bak s,%%primaryLogoURL%%,"$primaryLogoURL",g "$f"
  sed -i .bak s/%%primaryLogoHeight%%/"$primaryLogoHeight"/g "$f"
  sed -i .bak s,%%secondaryLogoURL%%,"$secondaryLogoURL",g "$f"
  sed -i .bak s/%%secondaryLogoHeight%%/"$secondaryLogoHeight"/g "$f"
  sed -i .bak s/%%mainBackgroundColor%%/"$mainBackgroundColor"/g "$f"
  sed -i .bak s/%%mainBorderColor%%/"$mainBorderColor"/g "$f"
  sed -i .bak s/%%mainBorderWidthTop%%/"$mainBorderWidthTop"/g "$f"
  sed -i .bak s/%%mainBorderWidthSide%%/"$mainBorderWidthSide"/g "$f"
  sed -i .bak s/%%mainBorderWidthBottom%%/"$mainBorderWidthBottom"/g "$f"  
  sed -i .bak s/%%topBannerColor%%/"$topBannerColor"/g "$f"
  sed -i .bak s/%%mainBannerTitleFont%%/"$mainBannerTitleFont"/g "$f"
  sed -i .bak s/%%mainBannerTitleFontSize%%/"$mainBannerTitleFontSize"/g "$f"
  sed -i .bak s/%%mainBannerTitleColor%%/"$mainBannerTitleColor"/g "$f"
  sed -i .bak s/%%mainBannerSubtitleFont%%/"$mainBannerSubtitleFont"/g "$f"
  sed -i .bak s/%%mainBannerSubtitleFontSize%%/"$mainBannerSubtitleFontSize"/g "$f"
  sed -i .bak s/%%mainBannerSubtitleColor%%/"$mainBannerSubtitleColor"/g "$f"
  sed -i .bak s/%%contentBackground%%/"$contentBackground"/g "$f"
  sed -i .bak s/%%contentBorder%%/"$contentBorder"/g "$f"
  sed -i .bak s/%%mainTextColor%%/"$mainTextColor"/g "$f"
  sed -i .bak s/%%mainTextFont%%/"$mainTextFont"/g "$f"
  sed -i .bak s/%%mainTextSize%%/"$mainTextSize"/g "$f"
  sed -i .bak s/%%supportLinksColor%%/"$supportLinksColor"/g "$f"
  sed -i .bak s/%%supportLinksFont%%/"$supportLinksFont"/g "$f"
  sed -i .bak s/%%supportLinksFontSize%%/"$supportLinksFontSize"/g "$f"
  sed -i .bak s/%%thankYouStatementFontColor%%/"$thankYouStatementFontColor"/g "$f"
  sed -i .bak s/%%thankYouStatementFontColorSize%%/"$thankYouStatementFontColorSize"/g "$f"
  sed -i .bak s/%%thankYouStatementFont%%/"$thankYouStatementFont"/g "$f"  
  sed -i .bak s/%%bottomBannerColor%%/"$bottomBannerColor"/g "$f"
  sed -i .bak s/%%supportLinksSeparatory%%/"$OtherColorThingy"/g "$f"
  sed -i .bak s,%%iiqLink%%,"$iiqLink",g "$f"
  sed -i .bak s,%%iiqShortLink%%,"$iiqShortLink",g "$f"
  sed -i .bak s/%%mainSubtableHeaderFont%%/"$mainSubtableHeaderFont"/g "$f"
  sed -i .bak s/%%mainSubtableHeaderFontSize%%/"$mainSubtableHeaderFontSize"/g "$f"  
  sed -i .bak s/%%mainSubtableHeaderFontColor%%/"$mainSubtableHeaderFontColor"/g "$f"
  sed -i .bak s/%%mainSubtableHeaderBackground%%/"$mainSubtableHeaderBackground"/g "$f"
  sed -i .bak s/%%mainSubtableFont%%/"$mainSubtableFont"/g "$f"
  sed -i .bak s/%%mainSubtableFontColor%%/"$mainSubtableFontColor"/g "$f"  
  sed -i .bak s/%%mainSubtableFontSize%%/"$mainSubtableFontSize"/g "$f"  
  sed -i .bak s/%%mainSubtableBackground%%/"$mainSubtableBackground"/g "$f"  
  sed -i .bak s/%%mainSubtableBorderColor%%/"$mainSubtableBorderColor"/g "$f"  
  sed -i .bak s/%%mainSubtableBorderSize%%/"$mainSubtableBorderSize"/g "$f"   
  sed -i .bak s/%%mainTextLinkColor%%/"$mainTextLinkColor"/g "$f"     
  sed -i .bak s/%%buttonTextLinkColor%%/"$buttonTextLinkColor"/g "$f"
  sed -i .bak s/%%buttonTextFontSize%%/"$buttonTextFontSize"/g "$f"
  sed -i .bak s/%%buttonTextFont%%/"$buttonTextFont"/g "$f"
  sed -i .bak s/%%actionButtonBackground%%/"$actionButtonBackground"/g "$f"
  sed -i .bak s/%%actionButtonBorder%%/"$actionButtonBorder"/g "$f"
  sed -i .bak s,%%helpLink%%,"$helpLink",g "$f"
  sed -i .bak s,%%faqLink%%,"$faqLink",g "$f"
  sed -i .bak s,%%supportLink%%,"$supportLink",g "$f"
#  sed -i .bak s/%%%%/"$"/g "$f"
#  sed -i .bak s/%%%%/"$"/g "$f"
#  sed -i .bak s/%%%%/"$"/g "$f"
done
rm "$outputFolderName"/*.bak

# Lets see the results..... set test to TRUE to test or anything else to skip....
if [ "$test" = "TRUE" ] ; then
	# Copy to a (iiq?)folder to test and
	# open all in a browser to test (will display the xml as well, but you get the idea...)
	CWD = $(pwd)
	cd "$outputFolderName"

	if [ ! -d "$testDir" ]; then
		echo "it's not there"
		mkdir "$testDir"
	fi

	TESTall=*.xml
	for f in $TESTall
		do
			cp "$f" "$testDir"/"$f".html
		done
	# Tomcat needs a bit of time.... so doing this in two steps
	for f in $TESTall
		do
			open -a firefox $testURL/$f.html
		done	
	cd "$CWD"
fi

# Create a setup.xml file

cd "$outputFolderName"
echo \<?xml version=\'1.0\' encoding=\'UTF-8\'?\> > setup.xml
echo \<!DOCTYPE sailpoint PUBLIC \'sailpoint.dtd\' \'sailpoint.dtd\'\> >> setup.xml
echo \<sailpoint\> >> setup.xml
FILES=*
for f in $FILES
do
  echo "Processing $f file..."
  # take action on each file. $f store current file name
  if [ $f == "setup.xml" ]
	then
		echo "Skiping $f file..."
		continue  # read next file and skip this one
	fi
	if [ $f == $testDir ]
	then
		echo "Skiping $f file..."
		continue  # read next file and skip this one
	fi
  echo \<ImportAction name=\'include\' value=\'xxxx\'/\> >> setup.xml 
  sed -i .bak s,xxxx,"$setupXmlPath$f",g "setup.xml"
done
echo \</sailpoint\> >> setup.xml
