angular.module('EllipseInfoUpdate').controller(
    'registrationController',
    [
        '$scope',
        '$http',
        '$location',
        'updateService',
        function($scope, $http, $location, updateService) {
            // private vars
            var user = null;
            var currentMode = 'email';

            // public vars
            $scope.showButton = false;
            $scope.inputValues = {
                email: null,
                code: null
            };

            // public methods
            $scope.init = function() {
                user = updateService.getUser();
                if (user) {
                    $scope.inputValues.email = user.email;
                    shouldShowButton();
                }
            };

            $scope.submitEmail = function() {
                updateService.setNewEmail($scope.inputValues.email);
                currentMode = 'code';
                $scope.showButton = false;
            };

            $scope.submitCode = function() {
                updateService.setEnteredCode($scope.inputValues.code);
                updateService.verifyUser(function(verifiedString) {
                    if (verifiedString === 'verified') {
                        $location.path('/confirm/address1').replace();
                    } else if (verifiedString === 'not_verified') {
                        $location.path('/register/register_deny').replace();
                    } else if (verifiedString === 'already_verified') {
                        $location.path('/register_verified').replace();
                    } else {
                        // TODO: This is where and error has occurred on the server. If we
                        // have time, we can handle this.
                    }
                });
            };

            $scope.checkEmail = function() {
                shouldShowButton();
            };

            $scope.checkCode = function() {
                shouldShowButton();
            };

            // private methods
            var shouldShowButton = function() {
                if (currentMode === 'email' && $scope.inputValues.email) {
                    var atIndex = $scope.inputValues.email.indexOf('@');
                    var periodIndex = $scope.inputValues.email.lastIndexOf('.');
                    $scope.showButton = (atIndex !==  -1 && periodIndex  !== -1 && periodIndex > atIndex);
                } else if (currentMode === 'code' && $scope.inputValues.code) {
                    $scope.showButton = $scope.inputValues.code.length === 4;
                } else {
                    $scope.showButton = false;
                }
            }
        }
    ]
);
