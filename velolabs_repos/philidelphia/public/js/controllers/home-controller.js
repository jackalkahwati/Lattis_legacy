angular.module('EllipseInfoUpdate').controller(
    'homeController',
    [
        '$scope',
        '$element',
        '$http',
        '$location',
        '$window',
        'updateService',
        function($scope, $element, $http, $location, $window, updateService) {
            // Public Methods
            $scope.init = function() {
                var code = getUpdateCode();
                if (code) {
                    updateService.getUserForCode(code, function(success) {
                        if (success) {
                            // TODO: check if the user has already been verified and
                            // display the appropriate UI.
                        } else {
                            // TODO: Present failure message/page here.
                        }
                    });
                } else {
                    // TODO: handle case where the cannot be retieved from the url.
                }
                //$window.location.href = 'http://localhost:8976/
            };

            // Private Methods
            var getUpdateCode = function() {
                var target = 'code';
                var url = $location.absUrl();
                var index = url.indexOf(target);
                if (index !== -1) {
                    index += target.length;
                    var startIndex = -1;
                    var endIndex = -1;
                    while (index < url.length && endIndex === -1) {
                        if (startIndex === -1 && url[index - 1] === '=') {
                            startIndex = index;
                        }

                        if (endIndex === -1 && (url[index] === '#' || url[index] === '&')) {
                            endIndex = index;
                        }

                        index += 1;
                    }

                    if (startIndex !== -1 && endIndex !== -1) {
                        return url.substring(startIndex, endIndex);
                    }
                }

                return null
            };
        }
    ]
);