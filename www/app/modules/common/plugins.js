
angular.module('openspecimen')
  .factory('PluginReg', function($http, angularLoad) {
    /**
     * Structure
     * {
     *   plugin_name: {
     *     view_name: {
     *       section_name: {
     *         template: template.html,
     *         // there could be other props in future 
     *       },
     *
     *       // more sections within same view
     *     },
     *
     *     // more views within same plugin
     *   },
     * }
     */

     var pluginViews = {};

     var activePlugins = [];

     function registerViews(pluginName, views) {
       pluginViews[pluginName] = views;
     }

     function getViews(pluginName) {
       return pluginViews[pluginName] || {};
     }

     function getTmpls(viewName, secName, defaultTmpl) {
       var tmpls = [];
       angular.forEach(pluginViews, function(pViews, pName) {
         if (activePlugins.indexOf(pName) == -1) {
           //
           // plugin is not used
           //
           return; 
         }

         angular.forEach(pViews, function(pViewTmpls, pViewName) {
           if (viewName !== pViewName) {
             return;
           }

           angular.forEach(pViewTmpls, function(pViewSecTmpl, pViewSecName) {
             if (pViewSecName !== secName) {
               return;
             }

             tmpls.push(pViewSecTmpl.template);
           });
         });
       });

       if (tmpls.length == 0 && !!defaultTmpl) {
         tmpls.push(defaultTmpl);
       }

       return tmpls;
     }

     function usePlugins(pluginNames) {
       activePlugins = [].concat(pluginNames); 

       var pluginUrl = 'plugin-ui-resources/'; 
       angular.forEach(activePlugins, function(pluginName) {
         $http.get(pluginUrl + pluginName + '/def.json').then(
           function(result) {
             angular.forEach(result.data.styles, function(style) {
               angularLoad.loadCSS(pluginUrl + style);
             });

             angular.forEach(result.data.scripts, function(script) {
               angularLoad.loadScript(pluginUrl + script);
             });
           }
         );
       });
     }

     return {
       registerViews: registerViews,

       getViews: getViews,

       getTmpls: getTmpls,

       usePlugins: usePlugins
     };
  })

  .directive('osPluginHooks', function(PluginReg) {
    return {
      restrict: 'AE',

      replace: true,

      scope: {
        viewName: '=',

        secName: '=',

        defaultTmpl: '='
      },

      template: '<ng-include src="tmpl" ng-repeat="tmpl in hookTmpls"></ng-include>',

      link: function(scope, element, attrs) {
        scope.hookTmpls = [];
        scope.$watchGroup(['viewName', 'secName', 'defaultTmpl'], function(newVals) {
          scope.hookTmpls = PluginReg.getTmpls(scope.viewName, scope.secName, scope.defaultTmpl);
        });
      }
    };
  });
