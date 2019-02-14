/**
 * Created by ray.shea on 3/23/14.
 */

/**
 * A service which sets the XSRF-TOKEN cookie to the token value retrieved from the bean. Angular will automatically
 * copy this value into the X-XSRF-TOKEN header on every request.
 */
angular.module('sailpoint.csrf').
    factory('csrfCookieInitializerService', function($browser) {
        var service = {},
            XSRF_TOKEN_NAME = 'XSRF-TOKEN';
        
        /**
         * Set the XSRF-TOKEN cookie
         *
         * Use $browser.cookies for now.
         * TODO: Use cookie service when we get to 1.4
         *
         * https://github.com/angular/angular.js/issues/7631
         */
        service.initializeXsrfToken = function() {
            $browser.cookies()[XSRF_TOKEN_NAME] = SailPoint.XSRF_TOKEN;
        };

        return service;
    }
    );
