angular.module('sailpoint.csrf', []).
    run(function(csrfCookieInitializerService) {
        csrfCookieInitializerService.initializeXsrfToken();
});