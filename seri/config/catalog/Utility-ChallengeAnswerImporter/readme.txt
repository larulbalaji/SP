Challenge Answer Importer v1.0.0
10 July 2013

Contact: kevin.james@sailpoint.com

**************
* Background *
**************

This is a utility to import the challenge-response answers for users from a csv file

****************
* To Configure *
****************

This is a Task-based utility. The task takes a CSV file as input (file name specified in the Task Definition)

The CSV file must be in the format
identity,question,answer

The question text *must match exactly* the question text already in IdentityIQ. For the case of the out of the box questions, since they are
Internationalized they are stored internally with the text as the key that is looked up against the message catalog.

For example:
Stored in the question object: auth_question_mothers_maiden_name
In locale US: What is your mother's maiden name?
In locale FR: Quel est le nom de jeune fille de votre m�re ?

*******************
* Version History *
*******************
10 July 2013
Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-ChallengeAnswerImporter
