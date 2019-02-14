Artifact Importer v1.0.0
27 Jan 2014

Contact: kevin.james@sailpoint.com

**************
* Background *
**************

At the Sales Kickoff 2014, It was made clear that we don't do the "standard demo" any more. Once a discovery meeting has
been performed, and the prospect's requirements collected, a targeted demo is performed.

Also, when at a POC, you want all the SERI artifacts to be available without having been pre-imported - this would mean
having to delete applications, identities, certifications etc. We want a clean demo environment with the useful artifacts
easily available.

This QuickLink/Workflow combo allows the user to see a list of all the importable combinations of artifacts (Resources,
Use Cases etc.), select one or more from the list, and import them. This allows the simple creation of a targeted demo
or POC base.

**************************
* Potential Enhancements *
**************************
- Currently there is no way to know which artifacts have already been imported. Add a configuration object to store a list
of artifacts currently imported, and add a small merge artifact to each resource/use case etc. that will register it in 
the configuration object.
- Add a target to ant, 'demobase', which will just import this artifact. Then, when you login to IdentityIQ, you are all
ready to build your demo.

***************
* Limitations *
***************
Currently no way to know what we already have (see Potential Enhancements).
No way to unimport an object


****************
* To Configure *
****************

From IIQ Console import the setup.xml in this directory.

******************
* To Demonstrate *
******************
Hit the quicklink

*******************
* Version History *
*******************

27 Jan 2014
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-ArtifactImporter
