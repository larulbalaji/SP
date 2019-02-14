'use strict';

angular.module('sailpoint.home.widget').

/**
 * A DirectReport is a DTO representing an identity that reports to a manager and includes
 * information about the identity, as well as which actions the manager can execute on the
 * identity.
 */
factory('IIQStats', function() {

    /**
     * Constructor.
     *
     * @param {Object} data  The raw direct report data.
     *
     * @throws If the data is null or is missing the id, name, or displayName.
     */
	function IIQStats() {
	    //console.log("MEU Constructor");
	 }
	
    function IIQStats(data) {
        if (!data) {
            throw 'data is required';
        }
        if (!data.statName) {
            throw 'Name is required';
        }
        if (!data.statCount) {
            throw 'count is required';
        }
        if (!data.statArrow) {
            //throw 'id is required';
        }

         this.statCount = data.statCount;

        /**
         * @property {String} The display name of the identity.
         */
        this.statName = data.statName;

        /**
         * @property {Array<String>} The actions that can be performed on the identity.
         */
        this.statArrow = data.statArrow;
        
        this.statUrl = data.statUrl;
    }



    return IIQStats;
});
