'use strict';

angular.module('sailpoint.home.widget').
directive('spSensitiveDataUserWidget', function() {
	   //console.log("Directive");
	    return {
	        restrict: 'E',
	        scope: {
	            widget: '=spWidget'
	        },
	        controller: 'SensitiveDataUserWidgetDirectiveCtrl',
	        controllerAs: 'ctrl',
	        bindToController: true,
	    
	        template:
	            '<div class="seri-widget">' +
	            '  <div class="panel-body" sp-loading-mask="ctrl.items">' +
	            '    <div tabindex="50" class="list-group reports-list"' +
	            '         sp-focus-snatcher="ctrl.focusOnList"' +
	            '         sp-focus-snatcher-element="#seriWidgetRow{{ ctrl.getPageState().pagingData.getStart() }}"' +
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
	            '      <div class="list-group-item" ng-repeat="sensUser in ctrl.items track by $index">' +
	            '        <div class="row" tabindex="-1" id="seriWidgetRow{{ $index }}">' +
	            '          <div class="col-sm-7">' +
	            '            <a href tabindex="50" ng-click="ctrl.viewIdentity(sensUser)" ' +
	            '              <span class="text-ellipsis">{{ ::sensUser.displayName }}</span>' +
	            '            </a>' +
	            '          </div>' +
	            '          <div class="col-sm-5 text-right" ng-if="sensUser.riskStatus"> ' +
	            '             <span ng-switch on="sensUser.riskStatus">' +
	            '                <img ng-switch-when="risk_band_lowest" src="images/icons/risk_indicator_lowest.png">' + 	            
	            '                <img ng-switch-when="risk_band_low" src="images/icons/risk_indicator_low.png">' +
	            '                <img ng-switch-when="risk_band_medium_low" src="images/icons/risk_indicator_medium_low.png">' +
	            '                <img ng-switch-when="risk_band_medium" src="images/icons/risk_indicator_medium.png">' +
	            '                <img ng-switch-when="risk_band_medium_high" src="images/icons/risk_indicator_medium_high.png">' +
	            '                <img ng-switch-when="risk_band_high" src="images/icons/risk_indicator_high.png">' +
	            '                <img ng-switch-when="risk_band_highest" src="images/icons/risk_indicator_highest.png">' +
	            '                <img ng-switch-default src="images/icons/risk_indicator_low.png">' +
	            '             </span>' + 
	            '          </div>' +
	        
	            '        </div>' +
	            '      </div>' +
	            '    </div>' +
	            '  </div>' +
	            '  <div class="panel-footer">' +
	            '    <div class="row">' +
	            '      <div class="col-xs-4">' +
	            '        <p class="m-t-xs m-b-xs">' +
	            '          <b id="seriWidgetsWidgetTotal">{{ ctrl.pageState.pagingData.getTotal() }}</b>' +
	            '          {{ \'ui_total\' | spTranslate }}' +
	            '        </p>' +
	            '      </div>' +
	            '      <div id="seriWidgetsPageInfo" class="col-xs-8 text-right"' +
	            '           ng-if="ctrl.pageState.pagingData.hasMultiplePages()">' +
	            '        <p class="inline m-t-xs m-b-xs m-r-sm">' +
	            '          <sp-current-page-info ng-model="ctrl.pageState.pagingData"' +
	            '                                sp-hide-total="true" />' +
	            '        </p>' +
	            '        <div class="inline">' +
	            '          <div class="btn-group" role="group">' +
	            '            <button ng-click="ctrl.previousPage()"' +
	            '               id="seriWidgetsPrevBtn"' +
	            '               class="btn btn-sm btn-white"' +
	            '               tabindex="50"' +
	            '               ng-disabled="!ctrl.pageState.pagingData.hasPrevious()">' +
	            '              <i class="fa fa-chevron-up" role="presentation"></i>' +
	            '              <span class="sr-only">{{ \'direct_report_pager_prev\' | spTranslate }}</span>' +
	            '            </a>' +
	            '            <button ng-click="ctrl.nextPage()"' +
	            '               id="seriWidgetsNextBtn"' +
	            '               class="btn btn-sm btn-white"' +
	            '               tabindex="50"' +
	            '               ng-disabled="!ctrl.pageState.pagingData.hasNext()">' +
	            '              <i class="fa fa-chevron-down" role="presentation"></i>' +
	            '              <span class="sr-only">{{ \'direct_report_pager_next\' | spTranslate }}</span>' +
	            '            </button>' +
	            '          </div>' +
	            '        </div>' +
	            '      </div>' +
	            '    </div>' +
	            '  </div>' +
	            '</div>'
	    };
});
