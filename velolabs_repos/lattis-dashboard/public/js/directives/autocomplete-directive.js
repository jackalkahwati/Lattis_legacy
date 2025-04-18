'use strict'

angular.module('skyfleet')
  .directive('autoComplete', ['$state', '_', '$rootScope', function ($state, _, $rootScope) {
    return {
      restrict: 'A',
      scope: {
        source: '=',
        single: '='
      },
      link: function (scope, element, attr) {
        function doubleFilter (request, response) {
          function hasMatch (s) {
            return s.toString().toLowerCase().indexOf(request.term.toString().toLowerCase()) !== -1
          }
          var i, l, obj, matches = []

          if (request.term === '') {
            response([])
            return
          }
          for (i = 0, l = scope.source.length; i < l; i++) {
            obj = scope.source[i]
            if (hasMatch(obj.value) || hasMatch(obj.lable)) {
              matches.push(obj)
            }
          }
          response(matches)
        }
        scope.$watch('source', function() {
          if (scope.single == true) {
            element.autocomplete({
              minLength: 0,
              source: scope.source,
              focus: function (event, ui) {
                element.val(ui.item.value)
                return false
              },
              select: function (event, ui) {
                element.val(ui.item.value)
                if (ui.item.id) {
                  $rootScope.$broadcast('onAutocompleteSelect', ui.item.id)
                  $rootScope.$emit('onAutocompleteSelect', ui.item.id)
                }
                return false
              }
            })
              .autocomplete('instance')._renderItem = function (ul, item) {
                return $("<li class = 'autocomplete-listitem'>")
                  .append('<p>' + item.value + '</p>')
                  .appendTo(ul)
              }
          } else if (scope.single == false) {
            element.autocomplete({
              minLength: 0,
              source: doubleFilter,
              focus: function (event, ui) {
                element.val(ui.item.value)
                return false
              },
              select: function (event, ui) {
                element.val(ui.item.value)
                return false
              }
            })
              .autocomplete('instance')._renderItem = function (ul, item) {
                return $("<li class = 'autocomplete-listitem'>")
                  .append('<p>' + item.value + ' - ' + item.lable + '</p>')
                  .appendTo(ul)
              }
          } else {
            element.autocomplete({
              minLength: 0,
              source: lightwell,
              focus: function (event, ui) {
                element.val(ui.item.value + ',' + ui.item.location)
                return false
              },
              select: function (event, ui) {
                element.val(ui.item.value + ',' + ui.item.location)
                element.trigger('input')
                return false
              }
            })
              .autocomplete('instance')._renderItem = function (ul, item) {
                return $("<li class = 'autocomplete-listitem'>")
                  .append('<div>' + '<span>' + item.value + ',' + '</span>' + item.location + '</div>')
                  .appendTo(ul)
              }
          }
        });
      }
    }
  }])
