import os
import sys
import getopt
import argparse
import re

import xml.etree.ElementTree as ET
import time
import datetime
import calendar

# python BackupIIQObjects.py All26SmallNotClean.xml 20141123 000000 Gilai

def newexport(dt, dir):
    if os.path.isfile(dir+"/"+dt+".xml"):
        sys.exit("\nThe Export has already been launched with the exact same date and time. Re-launch with a different date and/or time.")
    return;

def inputexists(file):
    if not os.path.isfile(file):
        sys.exit("\nThe Input file does not exist or the Input file name is incorrect")
    return;

def dateformat(date):
    regexpattern = re.compile('\d\d\d\d\d\d\d\d')
    if not re.match(regexpattern, date):
        sys.exit("\nThe Input date format is incorrect, please use YYYYmmDD (ex. 20141201 for 1rst of december 2014)")
    return;

def timeformat(time):
    regexpattern = re.compile('\d\d\d\d\d\d')
    if not re.match(regexpattern, time):
        sys.exit("\nThe Input time format is incorrect, please use HHmmSS (ex. 074430 for 7AM 44min and 30sec)")
    return;

def validdate(y, m, d):
    try:
        d = datetime.date(y, m, d)
    except ValueError:
        sys.exit("\nThe date is incorrect (although the format YYYYmmDD used is correct).") # raise ValueError(
    return;

def validtime(h, m, s):
    try:
        d = datetime.time(h, m, s)
    except ValueError:
        sys.exit("\nThe time is incorrect (although the format HHmmSS used is correct).") # raise ValueError(
    return;

def directoryexists(dir):
    if not os.path.isdir(dir):
        os.makedirs(dir)
        print "\nCreating director ",dir
    return;

def getdoctype(file, root):
    doctype = ''
    with open(file) as reader:
        for line in reader.readlines():
            if line.startswith("<"+root):
                break;
            else:
                doctype = doctype + line # rstrip('\n')
    return doctype;

def search(node, javatime):
    boolean = False
    for elements in node.iter():
        if elements.attrib.has_key('modified'):
            if float(elements.get('modified')[:10]) >= float(javatime):
				boolean = True
        if elements.attrib.has_key('created'):
            if float(elements.get('created')[:10]) >= float(javatime):
				boolean = True
    return boolean;

def clean(node):
    for elements in node.iter():
        if elements.attrib.has_key('modified'):
            del elements.attrib["modified"]
        if elements.attrib.has_key('created'):
            del elements.attrib["created"]
        if elements.attrib.has_key('id'):
            del elements.attrib["id"]
    return node;

def save(rootdir, nodename, rootname, dtd, dtc, cleanednode):
    nodedir = rootdir + '/' + nodename
    if not os.path.isdir(nodedir):
        os.makedirs(nodedir)
        # print 'Creating director ',nodedir
    nodefile = nodedir + '/' + str(dtc) + '.xml'
    if not os.path.isfile(nodefile):
        fh = open(nodefile,"w")
        fh.write(dtd)
        fh.write("<"+rootname+">\n")
        fh.write(ET.tostring(cleanednode))
        fh.close()
    else:
        fh = open(nodefile,"a")
        fh.write(ET.tostring(cleanednode))
        fh.close()

def summary(dictionnary):
    print "\nSummary:"
    print "\nObjects".ljust(40)+"Saved"
    print "---------------------------------------------"
    for key in sorted(dictionnary):
        print  key.ljust(40), dictionnary[key]
        # AuthenticationQuestion
    print "---------------------------------------------"
		
def close(dictionnary, rootdir, rootname, dtc):
    for key in dictionnary:
        nodedir = rootdir + '/' + key
        nodefile = nodedir + '/' + str(dtc) + '.xml'
        fh = open(nodefile,"a")
        fh.write("</"+rootname+">\n")
        fh.close()

def importer(dictionnary, rootdir, rootname, dtc):
    importerfile = rootdir + '/' + str(dtc) + '.xml'
    fh = open(importerfile,"w")
    fh.write(dtd)
    fh.write("<"+rootname+">\n")
    for key in dictionnary:
        fh.write("\t<ImportAction name=\'include\' value=\'WEB-INF/config/"+ rootdir +"/"+ key +"/"+ str(dtc) +".xml\'/>\n")
    fh.write("</"+rootname+">\n")
    fh.close()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    # ... declaring the arguments ...
    parser.add_argument("File", type=str, help="IIQ export file: the result of the IIQ Export command in console mode, for example \"export all.xml\" (without -clean).")
    parser.add_argument("Date", type=str, help="Date: save objects created or modified since this date.")
    parser.add_argument("Time", type=str, help="Time: save objects created or modified since this time.")
    parser.add_argument("Directory", type=str, help="Directory: directory where to store objects (if it doesn't exist it will be created).")

    args = parser.parse_args()

    # ... has this export already been launched ...
    dtc = args.Date + '-' + args.Time
    newexport(dtc, args.Directory)

    # ... testing the arguments ...
    inputexists(args.File)
    dateformat(args.Date)
    timeformat(args.Time)
    directoryexists(args.Directory)

    # ... validating date ...
    year = int(args.Date[:4])
    month = int(args.Date[4:4+2])
    day = int(args.Date[-2:])
    # print (year, month, day)
    validdate(year, month, day)
	
    # ... validating time ...
    hour = int(args.Time[:2])
    minute = int(args.Time[2:2+2])
    second = int(args.Time[-2:])
    # print (hour, minute, second)
    validtime(hour, minute, second)
	
    # ... starting parsing Export file ...
    tree = ET.parse(args.File)
    root = tree.getroot()
    # print 'Root of Export file is ',root.tag

    # ... getting doc type ...
    dtd = getdoctype(args.File, root.tag)
    # print 'Doc Type is ',dtd

    # ... playing with time Java Epoch UTC ...
    offset = (datetime.datetime.now() - datetime.datetime.utcnow()).total_seconds()
    utctime = (datetime.datetime(year, month, day, hour, minute, second) - datetime.datetime(1970,1,1,0,0,0)).total_seconds() - offset
    # print 'Jave Time search for is ',utctime

    # ... declaring results dictionnary ...
    results = {};

    # ... searching in xml ...
    for node in root.getchildren():
        if search(node, utctime):
            results[node.tag] = results.get(node.tag, 0) + 1 
            cleanednode = clean(node)
            save(args.Directory, node.tag, root.tag, dtd, dtc, cleanednode)

    print "\nDone..."
	
    close(results, args.Directory, root.tag, dtc)
    importer(results, args.Directory, root.tag, dtc)
    summary(results)

    print "\nTo reload those objects, copy the directory to WEB-INF/config and System Setup > Import from File\n"
    