'use strict';

angular.module('sailpoint.home.widget').

/**
 */
factory('OnlineTutorials', function() {

    /**
     * Constructor.
     *
     * @param {Object} data  The raw direct report data.
     *
     * @throws If the data is null or is missing the id, name, or displayName.
     */
	function OnlineTutorials() {
	    console.log("OnlineTutorials Constructor");
	 }
	
    function OnlineTutorials(data) {
        if (!data) {
            throw 'data is required';
        }
        //console.log( data );
        if (!data.description) {
            throw 'description is required';
        }
        if (!data.page) {
            throw 'page is required';
        }
        if( ! data.titleKey ) {
            throw 'titleKey is required';
       }

       this.description = data.description;
       this.page = data.page;
       this.titleKey = data.titleKey;
    }



    return OnlineTutorials;
});
