angular.module('idnRegApp')
  .controller('idnRegCtrl', ['$scope', 'IdnRegService', function ($scope, IdnRegService) {
    $scope.displayName= $scope.givenName+" "+$scope.familyName;

    $scope.successMessage="";
    $scope.errorMessage="";
    $scope.fields=[];
    
    $scope.init=function() {
    	IdnRegService.getFields().then( function(response) {
    		console.log("payload=");
    		console.dir(response.data.fields);
    		$scope.fields=response.data.fields;
    	});
    }

    $scope.updateDisplayName=function() {
      displayName=$scope.givenName;
      if (displayName.length>0) {
        displayName+=" ";
      }
      displayName+=$scope.familyName;
      $scope.displayName=displayName;
    };
//    $scope.$watch("givenName", $scope.updateDisplayName);
//    $scope.$watch("familyName", $scope.updateDisplayName);

    $scope.register=function() {
      console.log("Register");
      console.log("valid? "+$scope.regForm.$valid);

      var payload={};
      $scope.fields.forEach(function (field) {
    	  if (field.value!=null && field.value.length>0) {
    		payload[field.name]=field.value;
    	  }
      });
      

      console.dir(payload);
      $scope.errorMessage="";
      $scope.successMessage="";
      IdnRegService.register(payload, function(successMessage) {
    	  $scope.successMessage=successMessage;
      }, function(errorMessage) {
    	  $scope.errorMessage=errorMessage;
      });
      
    };
    
    $scope.init();

  }
  ])
;