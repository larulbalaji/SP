angular.module('sailpoint.plugin.updateViewAttributesPlugin')
.controller('SortableCTRL', function ($scope, $location, $http){

  $scope.currentItems=[];
  $scope.availableItems=[];

	$scope.sortableOptions = {
	  containment: '#sortable-container'
	};
	
	function getItems($scope) {
	    $http.get(PluginHelper.getPluginRestUrl("updateviewattributes/currentViewAttributes"))
	    .then(function (msg) {
	        msg.data.current.forEach( function(item) {
	          $scope.currentItems.push({"name": item.name, "displayName": item.displayName});
	        });
	        console.info('done '+$scope.currentItems.length);
	        msg.data.available.forEach( function(item) {
	          $scope.availableItems.push({"name": item.name, "displayName": item.displayName});
	        });
	   });
	}
	
	$scope.dragControlCurrentListeners = {
	    orderChanged: function (event) {
	        console.log('orderChanged : ', event.source.index, 'to', event.dest.index);
	    },
	    dragStart: function () {
	        console.clear();
	        console.log('dragStart : ', arguments)
	    },
	    dragEnd: function () {
	        console.log('dragEnd : ', arguments)
	    }
	};
	
	$scope.dragControlAvailableListeners = {
	    orderChanged: function (event) {
	        console.log('orderChanged : ', event.source.index, 'to', event.dest.index);
	    },
	    dragStart: function () {
	        console.clear();
	        console.log('dragStart : ', arguments)
	    },
	    dragEnd: function () {
	        console.log('dragEnd : ', arguments)
	    }
	};
	
	$scope.cancelEdit=function() {
	  //$location.url('/iiq7/systemSetup/index.jsf'); doesn't work; documentation says use low-level API
	  window.location.href=SailPoint.CONTEXT_PATH+'/systemSetup/index.jsf';
	};
	$scope.saveEdit=function() {
	    console.info("selected="+$scope.currentItems);
	    var msg="";
	    var first=true;
	    $scope.currentItems.forEach(function (item){
	    	if(first) first=false;
	    	else msg+=",";
	    	msg+=item.name;
	    	console.info("item.name="+item.name);
	    });
	    var theData='{ "newAttrs" : "'+msg+'"}';
	    console.log("theData="+theData);
	    
	    $http.put(
	      PluginHelper.getPluginRestUrl("updateviewattributes/currentViewAttributes"),
	      theData)
	    .then(function (msg) {
	        console.info("save done");        
	    });
	  window.location.href=SailPoint.CONTEXT_PATH+'/systemSetup/index.jsf';
	};
	$scope.isSaving=function() {
	  console.log('isSaving');
	  return false;
	};

	// Handle the window sizing for scrollbars within each list of attributes
	$(document).ready(function(){
	    resizeColumns();
	    getItems($scope);
	});
	
	window.onresize = function(event) {
	    resizeColumns();
	}

function resizeColumns() {
    // Resize to (window height - y position) - footer size
    vpw = $(window).width();
    vph = $(window).height();
    console.info("resize "+vph);

    myLocation=$("#currentAttrs").position();

    toSubtract=myLocation.top+$(".sp-footer").height();

    $("#currentAttrs").css({"height": vph-myLocation.top});
    $("#availableAttrs").css({"height": vph-myLocation.top});
}

});

