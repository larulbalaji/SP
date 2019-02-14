'use strict';

angular.module('sailpoint.home.widget').
directive('spSIQStatsWidget', function() {
	   //console.log("Directive");
	    return {
	        restrict: 'E',
	        scope: {
	            widget: '=spWidget'
	        },
	        controller: 'SIQKPIDirectiveCtrl',
	        controllerAs: 'ctrl',
	        bindToController: true,
	    
	        template:
	            '<div class="direct-reports-widget">' +
	            '  <div class="panel-body" sp-loading-mask="ctrl.items">' +
	            '    <div tabindex="50" class="list-group reports-list"' +
	            '         sp-focus-snatcher="ctrl.focusOnList"' +
	            '         sp-focus-snatcher-element="#directReportRow{{ ctrl.getPageState().pagingData.getStart() }}"' +
	            '         sp-focus-snatcher-wait="1250"' +
	            /*
	             * To page the results with a sliding effect we are using CSS transitions.  The top property
	             * has a transition.  When the page is changed we scroll the page up 250 more pixels.  The
	             * overflow is hidden so the part that gets scrolled up is not seen and the next 5 entries
	             * come into the window.
	             */
	            '         ng-style="{ \'top\': -250 * ctrl.displayedPageNumber + \'px\' }">' +
	            '      <div class="list-group-item text-center empty-widget"' +
	            '           ng-if="ctrl.items.length === 0">' +
	            '        <p class="h4 text-muted">{{ \'ui_widget_no_data\' | spTranslate }}</p>' +
	            '      </div>' +
	            '      <div class="list-group-item" ng-repeat="kpi in ctrl.items track by $index">' +
	            '        <div class="row" tabindex="-1" id="directReportRow{{ $index }}">' +
	            '          <div class="col-sm-8">' +
	            '              <span class="text-ellipsis"><b>{{ ::kpi.statValue }} {{ ::kpi.statName }}</b></span>' +
	            '          </div>' +
	            '          <div class="col-sm-4 text-right">' +
	      
	            '             <span ng-switch on="kpi.statColor">' +
	            '                <span ng-switch-when="Green" class="text-ellipsis" style="color:Green">{{ ::kpi.statScore }} score</span>' +
	            '                <span ng-switch-when="Orange" class="text-ellipsis" style="color:Orange">{{ ::kpi.statScore }} score</span>' +
	            '                <span ng-switch-when="Red" class="text-ellipsis" style="color:Red">{{ ::kpi.statScore }} score</span>' +
	            '             </span>' + 
	         
	            '          </div>' +
	            '        </div>' +
	            '      </div>' +
	            '    </div>' +
	            '  </div>' +
	            '  <div class="panel-footer">' +
	            '    <div class="row">' +
	            '      <div class="col-xs-9">' +
	            '        <p class="m-t-xs m-b-xs">' +
	            '          <span class="text-ellipsis" id="siqOwnerData">{{ ctrl.resource }}   <b>Owner:</b>{{ ctrl.owner }}</span>' + 
	            '        </p>' +
	            '      </div>' +
	            '      <div id="directReportsPageInfo" class="col-xs-3 text-right">' +
	            '        <p class="inline m-t-xs m-b-xs m-r-sm">' +
	            '          (Score:{{ ctrl.ownerscore }})' +
	            '        </p>' +
	            '      </div>' +
	            '    </div>' +
	            '  </div>' +
	            '</div>'
	    };
});
