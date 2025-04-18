'use strict'

angular.module('skyfleet.controllers').controller(
  'headerController',
  [
    '$scope',
    'sessionFactory',
    'rootScopeFactory',
    'authFactory',
    '$state',
    'utilsFactory',
    'myFleetFactory',
    '$rootScope',
    function ($scope, sessionFactory, rootScopeFactory, authFactory, $state, utilsFactory, myFleetFactory, $rootScope) {
      $scope.name = sessionFactory.getCookieUser()
      $rootScope.$on('nameChange', function () {
        $scope.name = sessionFactory.getCookieUser()
      })
      $rootScope.$on('customerName', function (event, data) {
        $rootScope.customer_Name = data
      })
      // $('body').bind('mousewheel', function () {
      //     $("body").niceScroll({ autohidemode: false, zindex: 999 }).resize();
      // });

      $scope.menu_Items = [{
        id: 'item1',
        title: 'HOME',
        href: 'skysharehome',
        active: 'home'
      }, {
        id: 'item3',
        title: 'REPORTS',
        href: 'reports.details',
        active: 'reports'

      }, {
        id: 'item6',
        title: 'MY FLEETS',
        href: 'myfleet.listview',
        active: 'myfleet'

      }]

      $scope.visible = false
      $scope.toggle = function () {
        $scope.visible = !$scope.visible
      }

      // Logout
      $scope.logout = function () {
        authFactory.logout(function (error, response) {
          // call logout from service
          sessionFactory.clearCookieData()
          myFleetFactory.clearFleetCache()
          $rootScope.showAfterLoad = false
          localStorage.removeItem('acl')
          rootScopeFactory.clearAll()
          sessionStorage.clear()
          localStorage.clear()
          $state.go('login')
        })
      }
      $scope.notification_show = false
      $scope.notify = function () {
        $scope.notifyanimation = 'slideInLeft'
        $scope.notification_show = !$scope.notification_show
      }
    }
  ])
