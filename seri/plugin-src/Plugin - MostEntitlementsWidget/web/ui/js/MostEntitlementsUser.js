'use strict';

angular.module('sailpoint.home.widget').

/**
 * A DirectReport is a DTO representing an identity that reports to a manager and includes
 * information about the identity, as well as which actions the manager can execute on the
 * identity.
 */
factory('MostEntitlementUser', function() {

    /**
     * Constructor.
     *
     * @param {Object} data  The raw direct report data.
     *
     * @throws If the data is null or is missing the id, name, or displayName.
     */
	function MostEntitlementUser() {
	    //console.log("MEU Constructor");
	 }
	
    function MostEntitlementUser(data) {
        if (!data) {
            throw 'data is required';
        }
        if (!data.count) {
            throw 'count is required';
        }
        if (!data.displayName) {
            throw 'displayName is required';
        }
        if (!data.id) {
            throw 'id is required';
        }

         this.count = data.count;

        /**
         * @property {String} The display name of the identity.
         */
        this.displayName = data.displayName;

        /**
         * @property {Array<String>} The actions that can be performed on the identity.
         */
        this.id = data.id;
    }



    return MostEntitlementUser;
});
