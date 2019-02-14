'use strict';
(function() {
//console.log("CTRL Test");
/* jshint maxparams:10 */
         
function OnlineTutorialsWidgetDirectiveCtrl(SearchData, PageState, PagingData, $q, $timeout, configService, onlineTutorialsService, navigationService, $location, ListResultDTO, ListResultCache) {
    //'ngInject';
    console.log("Ctrl");
    var me = this, 
    // The number of items to load into the cache at once.
    CACHE_CHUNK_SIZE = 40, 
    // The search term used the list time the data was loaded.
    lastSearchTerm, 
    // A cache of all of the results that have been loaded.
    cache = new ListResultCache(), 
    // Flag indicating that we are fetching results to go to the previous page.
    // See description in previousPage().
    fetchForPreviousPage = false, 
    // A timeout promise that will refresh the list results after going to the previous page.
    // See description in previousPage().
    previousPageRefreshPromise;
    ////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR
    //
    ////////////////////////////////////////////////////////////////////////
    OnlineTutorialsWidgetDirectiveCtrl._super.call(this, SearchData, $q, $timeout, configService, new PageState(new PagingData(5)));
    ////////////////////////////////////////////////////////////////////////
    //
    // AbstractListCtrl methods
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Return the requested items from the cache wrapped in a promise.
     *
     * @param {Number} startIdx  The start index to retrieve.
     * @param (Number) numItems  The number of items to retrieve.
     *
     * @return {Promise<ListResultDTO>} A promise that resolves with a ListResultDTO with the
     *     requested items, or null if the cache does not contain these items.
     */
    function getFromCache(startIdx, numItems) {
        var cached = cache.get(startIdx, numItems);
        return (cached) ? $q.when(cached) : null;
    }
    /**
     * Reset the cache and current page information if the search term differs from the previous
     * search.
     *
     * @param {String} newSearchTerm  The current search term.
     */
    function resetIfSearchChanged(newSearchTerm) {
        if (lastSearchTerm !== newSearchTerm) {
            // Clear the cache
            cache.reset();
            // Save the new search term to remember it for our next fetch.
            lastSearchTerm = newSearchTerm;
            // Reset the displayed page number back to 0.
            me.displayedPageNumber = 0;
        }
    }
    /**
     * Search for the direct reports.
     *
     * @param {String} searchTerm  Ignored.
     * @param {Object} filterValues  Ignored.
     * @param {Number} startIdx  The zero-based start index.
     * @param {Number} itemsPerPage  The number of items to display per page.
     * @param {SortOrder} sortOrder The SortOrder holding sorts. May be null or undefined.
     *
     * @return {Promise<ListResult>} A promise that will resolve to a ListResult
     *     with the requested items.
     */
    this.doSearch = function (searchTerm, filterValues, startIdx, itemsPerPage, sortOrder) {
        // First, reset stuff if the search term is changing.
        resetIfSearchChanged(searchTerm);
        // If we are going to the previous page we need to keep extra results in the list so
        // that they are displayed while the animation is sliding up.  Four pages worth is enough
        // of a buffer to quickly click the previous button a number of times and not see any
        // blank rows scrolling by.
        if (fetchForPreviousPage) {
            itemsPerPage = itemsPerPage * 4;
        }
        // We're starting at index 0 because of the animation that we are using for the list.
        // The list will always include all items up to the end of the last page.  We show the
        // appropriate page by sliding the list up and down.  Since we are hiding the overflow,
        // only the desired window of information is shown.
        var listStart = 0, listEnd = startIdx + itemsPerPage, results;
        // First try to load the results from the cache.
        results = getFromCache(listStart, listEnd);
        // If not found in the cache, hit the server.
        if (!results) {
            results = loadIntoCache(searchTerm, startIdx).then(function () {
                // Now that its in the cache, get the requested stuff back from the cache.
                return getFromCache(listStart, listEnd);
            });
        }
        // Wrap the result in an HTTP Promise looking thing.
        return results.then(function (listResult) {
            return {
                data: listResult
            };
        });
    };
    /**
     * Load the next chunk of data into the cache, starting at the given index.
     *
     * @param {String} searchTerm  The search typed into the filter input box.
     * @param {Number} startIdx  The start index to load into the cache.
     *
     * @return {Promise<ListResultDTO>} A promise that resolves with the data that was loaded.
     */
    function loadIntoCache(searchTerm, startIdx) {
    	//console.log("CTRL Loading into Cache");
        return onlineTutorialsService.getOnlineTutorials(searchTerm, startIdx, CACHE_CHUNK_SIZE).then(function (users) {
            // Add the loaded data to the cache.
            cache.add(users, startIdx, CACHE_CHUNK_SIZE);
            return users;
        });
    }
    /**
     * We don't support filters - just return an empty list.
     */
    this.doLoadFilters = function () {
        // No-op - just return an empty list.
        return $q.when([]);
    };
    /**
     * Helper method to handle focusing and animations when changing page
     * @param {Function} changePageFunc Super method to call to actually change the page
     */
    function changePage(changePageFunc) {
        return changePageFunc().then(function () {
            // Current page is 1-based, so subtract 1 to get a zero-based index.
            me.displayedPageNumber = me.pageState.pagingData.currentPage - 1;
            // This must be in a timeout or else the watch in the focus snatcher doesn't get the correct values.
            $timeout(function () {
                me.focusOnList = true;
            });
        }).then(function () {
            // After changing the page, preload more data into the cache if we need to.
            preloadCache();
        });
    }
    /**
     * If we are near the end of our cached data, load some more.
     */
    function preloadCache() {
        var loadedSize = me.pageState.pagingData.getStart() + me.pageState.pagingData.itemsPerPage;
        // If we're within a couple pages of the end of the cache, go ahead and load more data.
        if (loadedSize >= cache.size - 10) {
            loadIntoCache(me.pageState.searchData.searchTerm, cache.size);
        }
    }
    /**
     * Extend nextPage() to focus on the list after the data is loaded, set data required by
     * animations, and preload data into the cache if needed.
     */
    this.nextPage = function () {
        // Don't need to do a previous page refetch since we're moving the list again.
        cancelPreviousPageRefetch();
        // Do the paging.
        changePage(OnlineTutorialsWidgetDirectiveCtrl._super.prototype.nextPage.bind(me));
    };
    /**
     * If there is a pending request to refetch a previous page, cancel it.
     */
    function cancelPreviousPageRefetch() {
        if (previousPageRefreshPromise) {
            $timeout.cancel(previousPageRefreshPromise);
            previousPageRefreshPromise = null;
        }
    }
    /**
     * Extend previousPage() to focus on the list after the data is loaded, set data required by
     * animations, and preload data into the cache if needed.
     */
    this.previousPage = function () {
        // Set this to true so we keep some extra rows in the list while the animation is happening.
        fetchForPreviousPage = true;
        changePage(OnlineTutorialsWidgetDirectiveCtrl._super.prototype.previousPage.bind(me))["finally"](function () {
            // Cancel the existing request to refetch if there is one.
            cancelPreviousPageRefetch();
            // Wait a couple of seconds and then trim the results to the actual desired size once
            // the transition is complete.
            previousPageRefreshPromise = $timeout(function () {
                me.fetchItems();
            }, 2000);
        });
        // Set this back to false for the next request.
        fetchForPreviousPage = false;
    };
    ////////////////////////////////////////////////////////////////////////
    //
    // 
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Go to the View Identity page for the given directReport.
     *
     * @param {DirectReport} directReport The direct report.
     */    
    this.viewOnlineTutorial = function (page) {
    	//console.log("Calling viewOnlineTutorial");
    	//console.log(page);
    	
    	// navigationService just redirects.
        //navigationService.go({
        //    outcome: 'tutorials/' + page
        //});
    	var target = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/identityiq/tutorials/" + page;
        // wants to open a new window!
        var popupWindow = window.open( target );
    };
    ////////////////////////////////////////////////////////////////////////
    //
    // INITIALIZATION
    //
    ////////////////////////////////////////////////////////////////////////
    /**
     * @property {Array<Object>}  Config objects with information about the buttons to render.
     */
    this.buttonConfigs = [{
            action: 'requestAccess',
            onClick: this.requestAccess,
            iconCls: 'fa-key',
            tooltip: 'quicklink_request_access',
            srText: 'direct_report_request_access_sr'
        }, {
            action: 'managePasswords',
            onClick: this.managePasswords,
            iconCls: 'fa-unlock-alt',
            tooltip: 'quicklink_manage_passwords',
            srText: 'direct_report_manage_passwords_sr'
        }, {
            action: 'manageAccounts',
            onClick: this.manageAccounts,
            iconCls: 'fa-folder',
            tooltip: 'quicklink_manage_accounts',
            srText: 'direct_report_manage_accounts_sr'
        }];
    /**
     * @property {Boolean}  A flag that gets set to true after paging to draw the focus back to
     *     the top of the list.
     */
    this.focusOnList = false;
    /**
     * @property {Number}  The zero-based number of the page that is being displayed.
     */
    this.displayedPageNumber = 0;
    // We need a truthy columnConfigs value for AbstractListCtrl to work even though we aren't
    // using them.
    this.columnConfigs = [];
    // Initialize when the controller is constructed.
    this.initialize();
}
OnlineTutorialsWidgetDirectiveCtrl.$inject = ['SearchData', 'PageState', 'PagingData', '$q', '$timeout', 'configService', 'OnlineTutorialsService', 'navigationService', '$location','ListResultDTO', 'ListResultCache'];

SailPoint.extend(OnlineTutorialsWidgetDirectiveCtrl, AbstractListCtrl);
angular.module('sailpoint.home.widget').controller('OnlineTutorialsWidgetDirectiveCtrl', OnlineTutorialsWidgetDirectiveCtrl);
})();
