Utility-XMLExportTask v 1.0.0
08/24/2017 Initial Release originating from ssdv4

Contact: achim.reckeweg@sailpoint.com, paul.wheeler@sailpoint.com
************************
* Library dependencies *
************************
sailpoint.services.standard.task.ExportXML

*************
* Execution *
*************
Create a new task with "Setup/Tasks/New Task/XML Object Exporter"

**************
* Background *
**************
Especially if not possible to use eclipse with the plugin it was not possible to export
the artifacts of a POC/Project using the UI. 

The XML Object Exporter is a custom IdentityIQ task for quick and easy export of XML 
objects such as applications, rules, workflows, configurations and any other objects 
that are managed in IIQ. The task has the following features:
- Exports XML objects to the same filesystem structure used by the Services Standard Build (SSB)
- Allows the user to specify which classes get exported
- Can optionally export only the objects that have been created or modified after a given date
- Can optionally remove IDs and timestamps from the exported objects
- Allows definition of a naming format for the exported XML files
- Can optionally produce tokenized XML files ready for use in the SSB build, using reverse-lookup
  to the tokens in a target.properties file
- Can add CDATA sections and unescape BeanShell code and other escaped elements
• Can create “merge” files by comparing the XML of objects being exported with previously
  exported base versions of the objects (for supported object types).
  The tool is designed to help developers working in an IdentityIQ sandbox or development 
  environment, particularly when working directly within IIQ rather than working on 
  external master files and importing those. 
  When working directly within IIQ it can be easy to lose track of what was changed and when, 
  making it difficult to keep the build up-to-date. 
  The Object Exporter provides the following advantages over other export methods:
  - Allows the developer to easily export everything they have been working on over a 
    given period, or since the start of a project, removing the need to maintain scripts 
    used to export objects from the console.
  - By maintaining a standard naming format for the resulting files it also helps to 
    prevent the creation of multiple copies of the same object XML when there is more 
    than one developer working on an IdentityIQ project.
  - Avoids the need to tediously replace text in the XML files with the tokens used in the 
    SSB or eclipse plugin config files
  - Avoids the need to create the SSB folder structure when exporting objects.
  - The resulting XML files can be added directly to the SSB since they are in the correct 
    folder structure and have the correct tokens.
  - The simple process also makes it easy to provide exported files that can be uploaded 
    to a code repository or just stored outside of IIQ as a quick and simple backup of 
    everything that has changed.
  - Fixes issues that the console checkout process has with exporting IDs rather than names 
    for objects referenced by certain classes such as IdentityTrigger and Scope.
  - The use of CDATA sections with unescaped code aids the readability of the code when viewed
    offline.
  - Allows the export of “merge” files that contain only the additions and changes in certain types of
    objects when compared with base exports.
      
**************************
* Potential Enhancements *
**************************
--

***************
* Limitations *
***************


****************
* To Configure *
****************
The XML Object Exporter consists of a Java class (ExportXML.class) and a Task Definition

After setting up standard SERI
Import the setup.xml or add Utility-XMLExportTask to myDemo


******************
* To Demonstrate *
******************
To access the XML Object Exporter in IdentityIQ, go to "Setup/Tasks/New Task. Create a new 
task of type “XML Object Exporter”. The task has several user-configurable options:
- Base path for export
  Enter the path to which you want to export the XML files. This must be a path that can be 
  accessed by the server that runs the task.
- Remove IDs from exported XML
  Defines whether the exported XML files will include the IDs and created/modified 
  timestamps for the objects in the environment that you are exporting from. 
  In most cases this should be set to True.
- Add CDATA sections
  Encloses code in CDATA sections and unescapes the code for ease of reading in 
  the exported file. This works for BeanShell code in rules and scripts, HTML formatting 
  in email templates and SOAP messages in IntegrationConfig objects.
- Classes to export
  This should be a comma-separated lists of object types to export. If left blank it will 
  export all object types. If set to “default” it will export only the types of objects 
  that are commonly exported during an IdentityIQ project. It can also be a combination of 
  “default” and other classes – for example you could use “default,Scope”.
- Only include objects updated or created after this date
  If enabled, this option allows the user to enter a date and time which defines the 
  earliest creation and modification timestamps by which we will filter the objects to 
  be included in the export. If disabled, creation and modification timestamps will be ignored.       
- Naming format
  Allows the user to define a format for naming the exported XML files. 
  There are two optional variables that can be used:
  - $Name$ The name of the XML object
  - $Class$ The name of the object class, e.g. Application, Rule etc.
  Using these variables, you can build a custom naming format for the exported files. 
  For example, “MyCorp_$Class$_$Name$” would export an application called “ActiveDirectory” 
  with the file name “MyCorp_Application_ActiveDirectory.xml”. If this option is left blank 
  the XML file will be named as the object name with a “.xml” extension.
  Illegal characters (anything that is not an alphanumeric character, dot or hyphen) and 
  spaces will be replaced with underscores in the file names.
- target.properties file for reverse-tokenization for SSB
  Optionally defines the path to a target.properties file that provides token values for 
  the sandbox/development environment in the usual SSB format. 
  The task performs a “reverse-lookup” on any text it finds in the XML that matches a 
  token value found in the target.properties file, and replaces it with the corresponding 
  token. If this option is left blank, no tokenization of the export files will take place. 
  Note that tokens in the target.properties file that have values of “true” or “false” will 
  be ignored in the current version of the Object Exporter since these values are present 
  in many objects and replacing them all with a token is not usually the intention. 
  The same applies to token values that are blank or just whitespace.
- Directory containing original XML files for comparison when creating merge files
  Optionally defines the location under which the “base” XML files can be found for use when 
  creating “merge” files. 
  These are files that contain only the XML entries that have been added or changed when
  compared with the base files and will be exported with the ImportAction property set to 
  “merge”. The task compares the current XML being exported with the base files and creates 
  merge files based on the differences. When imported into IdentityIQ, the entries in 
  these files will be merged with an existing object that has the same name, rather than 
  overwriting it. Merge files should be used for certain types of objects (such as 
  Configuration and UIConfig objects) where an upgrade to IdentityIQ may result in the 
  addition of new entries in these objects, which would otherwise get removed when 
  importing a full object that does not have an ImportAction of “merge”.
  At the start of a project it is useful to export all the objects that support merging 
  to obtain the base files that are referenced here and place these in a single location. 
  When looking for merge files in the provided location, the task will recursively search 
  in subfolders for any files that have .xml extensions and use these as the base files.
  The task currently supports objects of the following types for merging:
  - Configuration • UIConfig
  - ObjectConfig • AuditConfig
  - Dictionary
After selecting the desired options, click “Save and Execute”. 
The task will start running and its progress can be monitored in the Task Results. 
When completed, the results will show a summary of the number of XML files exported for 
each class and a total of all objects exported.

*******************
* Version History *
*******************
08/24/2017 
Initial Release originating from ssdv4

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Utility-XMLExportTask

Instructions for how to make a new tag for your contribution and 
create a "Testing Status" issue on Github: https://harbor.sailpoint.com/docs/DOC-21317

