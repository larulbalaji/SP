angular.module('idnRegApp')
    .factory('IdnRegService', [ '$http', 'ConfigService', 'Base64', function ($http, ConfigService, Base64) {
        var idnService = {};

        idnService.register = function(payload, successFn, errorFn) {
        	
        	configPromise=ConfigService.readConfig().then(function (response) {
        	
        		var org=response.data.org;
        		var clientID=response.data.clientID;
        		var secret=response.data.secret;
        		var source=response.data.source;
        	
	            $http({
	                method: 'POST',
	                url:  "rest/registration/register",
	                data: payload,
	                headers: {
	                	'Authorization': 'Basic '+Base64.encode(clientID+':'+secret),
	                    'Content-Type': 'application/json'
	                    	
	                }
	            }).then(function(successResponse) {
	            	successFn("User Registered Successfully");
	            }, function(errorResponse) {
	            	console.log("error");
	            	console.log(errorResponse);
	            	errorFn("Status "+errorResponse.status+" : "+errorResponse.data.message);
	            });
        	});
        };
        
        idnService.getFields = function() {
        	return $http.get("rest/configuration/fields");
        }
        
        return idnService;
    }]);
