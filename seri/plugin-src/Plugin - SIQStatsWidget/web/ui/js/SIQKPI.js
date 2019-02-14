'use strict';

angular.module('sailpoint.home.widget').

/**
 * A DirectReport is a DTO representing an identity that reports to a manager and includes
 * information about the identity, as well as which actions the manager can execute on the
 * identity.
 */
factory('SIQKPI', function() {

    /**
     * Constructor.
     *
     * @param {Object} data  The raw direct report data.
     *
     * @throws If the data is null or is missing the id, name, or displayName.
     */
	function SIQKPI() {
	   
	 }
	
    function SIQKPI(data) {
        if (!data) {
            throw 'data is required';
        }
        if (!data.statName) {
            throw 'name is required';
        }
        if (!data.statScore) {
            throw 'statScore is required';
        }
        if (!data.statValue) {
            throw 'statValue is required';
        }
        
        if (!data.statColor){
        	throw 'statColor is required';
        }

        this.statValue = data.statValue;
     
        this.statName = data.statName;

        this.statScore = data.statScore;
        
        this.statColor = data.statColor;
        
    }



    return SIQKPI;
});
