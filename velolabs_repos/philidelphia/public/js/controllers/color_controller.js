'use strict';

angular.module('EllipseInfoUpdate').controller(
    'colorController',
    [
        '$scope',
        '$element',
        '$http',
        '$location',
        'updateService',
        function($scope, $element, $http, $location, updateService) {
            var user = null;
            $scope.displayColors = null;

            $scope.init = function() {
                user = updateService.getUser();
                if (!user) {
                    return;
                }

                if (!user.colors) {
                    user.colors = [];
                }

                $scope.displayColors = {};

                for (var i=0; i < user.quantity; i++) {
                    var color;
                    if (i < user.colors.length) {
                        color = user.colors[i];
                    } else {
                        color = 'grey';
                        user.colors.push(color);
                    }

                    $scope.displayColors[i.toString()] = {
                        color: displayColorFromColor(color),
                        className: classNameForColor(color)
                    };
                }
            };

            $scope.conformationTitle = function() {
                var ellipseCount = numberOfEllipses();
                return 'Color' + (ellipseCount > 1 ? 's' : '') +  ' Confirmation:'
                    + ellipseCount.toString() + ' Ellipse' + (ellipseCount > 1 ? 's' : '');
            };

            $scope.ellipseColors = function() {
                if (!user) {
                    return [];
                }

                return user.colors;
            };

            $scope.chooseColor = function(color, index) {
                user.colors[index] = color;
                var indexString = index.toString();
                $scope.displayColors[indexString].color = displayColorFromColor(color);
                $scope.displayColors[indexString].className = classNameForColor(color);
            };

            $scope.submitColor = function() {
                updateService.updateUser(function(success) {
                    if (success) {
                        $location.path('/confirmed').replace()
                    } else {
                        // TODO: Need UI for this update failure.
                    }
                });
            };

            // private methods
            var numberOfEllipses = function() {
                if (!user || !user.colors) {
                    return 0;
                }

                return user.colors.length;
            };

            var displayColorFromColor = function(color) {
                var displayColor;
                if (color === 'grey') {
                    displayColor = 'Charcoal Grey';
                } else if (color === 'blue') {
                    displayColor = 'Midnight Blue';
                } else {
                    displayColor = 'Pearl White';
                }

                return displayColor;
            };

            var updateInfoTextForColor = function(color) {
                var text;
                if (color === 'white') {
                    text = 'Note: Pearl White locks will begin shipping in December';
                } else if (color === 'blue') {
                    text = 'Note: Midnight Blue locks will begin shipping in November';
                } else {
                    text = 'Note: Charcoal Grey locks will begin shipping in late October';
                }

                return text;
            };

            var classNameForColor = function(color) {
                return 'chosen_color_' + color;
            };
        }
    ]
);
