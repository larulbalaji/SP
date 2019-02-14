    HelpDesk Verify 2.0.1
	13 Nov 2014
    Contact: jeff.bounds@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependecies.

    *************
    * Execution *
    *************
    This creates a quicklink called "HelpDesk Verify".   Click the quicklink, select the user, then enter that user's Authentication Answers that match to their Authentication Questions.


    ************
    * Background *
    **************
	A common use case for HelpDesk password change is for the HelpDesk user to "verify" the caller's identity.   Currently, the HelpDesk User would either have to independently verify the caller's identity or use
	"View Identity" to use those identity attributes for verification.   This simple UseCase allows a HelpDesk person to select the identity and then fill in the Authentication Answers as if the caller was answering the questions.
	If the answers are correct, then the normal "Change Password" workflow is called.

    **************************
    * Potential Enhancements *
    **************************
    1.  Remove Question1 from Question2's list
    2.  Check to make sure user has answered questions.


    ***************
    * Limitations *
    ***************
    1.  User MUST have authentication questions answered.   If a user with no authenticationquestions is selected, then the workflow exits.
    2.  Current Validation is done in a validationscript.   This tells the HelpDesk which question was answered incorrectly.
    3.  The current form does not "remove" the first question from the second list.  So, the same question could be answered twice.   When I make the field dynamic, the display of the question reverts to the messagecatalog entry name (ie. Auth_Question_2)
    4.  This  usecase uses a hack to call the "Change Password" workflow.   We display a button that is actually an HTML deeplink to "Change Password"

    ****************
    * To Configure *
    ****************
    Import all the artifacts via setup.xml


    ******************
    * To Demonstrate *
    ******************
    1. Click the "Verify Identity" quicklink on the dashboard.
    2. Select a User that has AuthenticationQuestions set
    3. Select the two AuthenticationQuestions you wish to answer.
    4. Fill in the corresponding AuthenticationAnswers and click submit.
    5. Once the user is verified, select the "Change Password" link
    6. Follow normal "Change Password" use case.

    *******************
    * Version History *
    *******************
    16 Sept 2014
    Version 1.0.0 contributed to SERI

    24 Sept 2014
    Version 2.0.0 Added suggestions from John Singleton
    Renamed Quicklink to "Verify Identity"
    Added the identityName to the Forms so we know which user we are verifying.
    Fixed a few typos
    
    13 Nov 2014
    Wasn't seeing HelpDeskVerify in seri/develop.   I had accidently auto-merged my pull request.  Sean had backed that out.  Might have affected the normal pull
    Touched every file to make sure it updates correctly.   Also changed a few descriptions.
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-HelpDeskVerifyIdentity
