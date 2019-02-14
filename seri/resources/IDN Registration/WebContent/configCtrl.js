angular.module('idnRegApp')
  .controller('configCtrl', ['$scope', 'ConfigService', 'IdnRegService', function ($scope, ConfigService, IdnRegService) {
    $scope.org= "";
    $scope.clientID= "";
    $scope.secret = "";
    $scope.username = "";
    $scope.password = "";
    $scope.source = "";
    $scope.sources= [];
    $scope.schema=[];
    $scope.currentSource="";
    $scope.fields=[];
    $scope.appSchema=null;

    $scope.successMessage="";
    $scope.errorMessage="";
  
    $scope.init=function() {
    	ConfigService.readConfig().then( function(response) {
    		console.log(response);
    		$scope.org=response.data.org;
    		$scope.clientID=response.data.clientID;
    		$scope.secret=response.data.secret;
    		if (!($scope.clientID === undefined || $scope.secret === undefined )) {
    			$scope.sources=$scope.getSourceList();
    		}
    		$scope.currentSource=response.data.source;
// This is no use until we have a schema REST endpoint in the v2 api    		
    		if ($scope.currentSource!=null) {
    			ConfigService.getAccountSchema($scope.currentSource);
    		}
    	});
    	IdnRegService.getFields().then( function(response) {
    		if (response.data.fields===undefined) {
    			$scope.fields=[ {"name":"id", "label":"ID Attribute", "required":true}, {"name":"firstname", "label":"First Name", "required":true}, {"name":"lastname", "label":"Last Name", "required":true}, {"name":"phone", "label":"Phone Number", "required":false}, {"name":"location", "label":"Location"} ]
    		} else {
    			$scope.fields=response.data.fields;
    		}
    	})
    	
    };
    
    $scope.addField=function() {
    	var newField={
    			name: "",
    			label: "",
    			required: false
    	};
    	$scope.fields.push(newField);
    }

    $scope.save=function() {
    	payload={
    			"org": $scope.org,
    			"clientID": $scope.clientID,
    			"secret": $scope.secret,
    			"source": $scope.source,
    			"fields": $scope.fields
    	};
    	ConfigService.writeConfig(payload, function(successMessage) {
      	  $scope.successMessage=successMessage;
        }, function(errorMessage) {
      	  $scope.errorMessage=errorMessage;
        });
    };
    
    $scope.getClientSecret=function() {
    	
    	ConfigService.generateClientSecret($scope.org, $scope.username, $scope.password).then( function(response) {
    		console.log(response);
    		$scope.clientID=response.data.clientID;
    		$scope.secret=response.data.secret;
    	});
    };
    
    $scope.getOAuthToken=function() {
    	ConfigService.generateOAuthToken($scope.org, $scope.username, $scope.password).then( function(response) {
    		$scope.oauth=response.data.oauth;
    	})
    }
    
  $scope.$watch("source", function() {
	  console.log("selected="+$scope.source);
  });

    
    $scope.getSourceList=function() {
    	ConfigService.getSourceList($scope.org, $scope.clientID, $scope.secret).then( function(response) {
    		$scope.sources=[];
    		angular.forEach(response.data, function(value, key) {
    			console.log("adding "+value.id+" , "+value.name);
    			$scope.sources.push( { "id":value.id, "name":value.name} );
    		});
    		console.log("setting selected to "+$scope.currentSource);
    		$scope.source=$scope.currentSource;
    	});    	
    }
    
    $scope.getAccountSchema=function(source) {
    	IdnRegService.getAccountSchema(source).then( function(response) {
    		$scope.appSchema=response.attributes;
    	});
    };
    
    
    $scope.init();
    //$scope.$watch("source", $scope.updateSchema);
  }
  ])
;