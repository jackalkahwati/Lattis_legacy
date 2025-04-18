(function () {
  'use strict'

  angular
    .module('angular-nicescroll', [])
    .directive('ngNicescroll', ngNicescroll)

  ngNicescroll.$inject = ['$rootScope']

  /* @ngInject */
  function ngNicescroll ($rootScope) {
    // Usage:
    //
    // Creates:
    //
    var directive = {
      link: link
    }
    return directive

    function link (scope, element, attrs, controller) {
      var niceOption = scope.$eval(attrs.niceOption)

      var niceScroll = $(element).niceScroll(niceOption)
      // niceScroll.onscrollend = function (data) {
      //     if (data.end.y >= this.page.maxh) {
      //         if (attrs.niceScrollEnd) scope.$evalAsync(attrs.niceScrollEnd);
      //
      //     }
      //
      // };
      niceScroll.onscrollend = function (data) {
        if (this.newscrolly >= this.page.maxh) {
          if (attrs.niceScrollEnd) scope.$evalAsync(attrs.niceScrollEnd)
        }
        if (this.newscrolly === 0) {
          // at top
          if (attrs.niceScrollTopEnd) scope.$evalAsync(attrs.niceScrollTopEnd)
        }
      }
    }
  }
})()
