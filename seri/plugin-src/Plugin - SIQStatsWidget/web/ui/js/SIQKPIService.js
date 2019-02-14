'use strict';

angular.module('sailpoint.home.widget').

/**
 * The directReportService provides methods to retrieve direct reports for the widget.
 */
factory('siqKPIService', ['SP_CONTEXT_PATH', 'ListResultDTO', 'SIQKPI', '$http', function(SP_CONTEXT_PATH, ListResultDTO, SIQKPI, $http) {

    var svc = {};
    
    /**
     * Return a ListResultDTO with the direct reports for the logged in user.
     *
     * @param {String} searchTerm  The name to filter by.
     * @param {Number} start  The start index.
     * @param {Number} limit  The max number of results to return.
     *
     * @return {Promise<ListResultDTO<DirectReport>>} A promise that will be resolved with a list
     *     result containing the DirectReports.
     */
    svc.getSIQKPIS = function(searchTerm, start, limit) {
        var params = {
            start: start,
            limit: limit
        };

        // Only send the search term if it was specified.
        if (searchTerm) {
            params.query = searchTerm;
        }
        
        return $http.get(SP_CONTEXT_PATH + '/plugin/rest/siqstats/stats', {
            params: params
        }).then(function(response) {
            // Convert the objects in the response into DirectReports.
            var transformed = angular.copy(response.data);
            //console.log(transformed);
            transformed.objects = transformed.objects.map(function(siqkpi) {
                return new SIQKPI(siqkpi);
            });

            return new ListResultDTO(transformed);
        });
    };

    svc.getOwnerScore = function() {
        //console.log("GetOwnerScore Function");      
        return $http.get(SP_CONTEXT_PATH + '/plugin/rest/siqstats/ownerdata').then(function(response) {
        	
            var ownerdata = angular.copy(response.data);
            //console.log(transformed);
            
            return ownerdata;
        });
    };
    
    return svc;
}]);
