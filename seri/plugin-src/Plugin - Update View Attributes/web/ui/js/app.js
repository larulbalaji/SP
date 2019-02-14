angular.module('sailpoint.plugin.updateViewAttributesPlugin',
  [
   'ui.sortable',
   'sailpoint.csrf'
  ]
).config(
        ['$locationProvider',
            function ($locationProvider) {
                $locationProvider.html5Mode({
                  enabled: true,
                  requireBase: false
                });
            }
        ]
);