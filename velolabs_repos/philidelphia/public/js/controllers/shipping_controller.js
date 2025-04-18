'use strict';

angular.module('EllipseInfoUpdate').controller(
    'shippingController',
    [
        '$http',
        '$scope',
        '$location',
        'updateService',
        function($http, $scope, $location, updateService) {
            // private vars
            var user = null;
            var page = null;

            // public vars
            $scope.inputValues = {
                address1: null,
                address2: null,
                city: null,
                state:null,
                zip: null,
                country: null
            };

            // public methods
            var getNextPage = function() {
                var nextPage;
                if (page === 'address1')   {
                    nextPage = 'address2';
                } else if (page === 'address2') {
                    nextPage = 'city';
                } else if (page === 'city') {
                    nextPage = 'state';
                } else if (page === 'state') {
                    nextPage = 'zip';
                } else if (page === 'zip') {
                    nextPage = 'country';
                }

                return nextPage;
            };

            // public methods
            $scope.init = function() {
                user = updateService.getUser();
                if (!user) {
                    // TODO: present UI for the case where there is no user or no address.
                    // Another solution is to store the token in local storage. We can then
                    // pull the token out here and re-fetch the user's information.
                    return;
                }

                $scope.inputValues.address1 = user.address.address1 || '';
                $scope.inputValues.address2 = user.address.address2 || '';
                $scope.inputValues.city = user.address.city || '';
                $scope.inputValues.state = user.address.state || '';
                $scope.inputValues.zip = user.address.zip || '';
                $scope.inputValues.country = user.address.country || '';

                page = 'address1';
            };

            $scope.nextButtonClicked = function() {
                var fieldValue = $scope.inputValues[page];
                var isValid = true;
                if (page === 'address1' ||
                    page === 'city' ||
                    page === 'state' ||
                    page === 'zip' ||
                    page === 'country')
                {
                    isValid = (fieldValue && fieldValue !== '');
                }

                if(isValid) {
                    page = getNextPage();
                    $scope.invalidText = '';
                    if (page) {
                        var path = '/confirm/' + page;
                        $location.path(path).replace();
                    } else {
                        user.address.address1 = $scope.inputValues.address1;
                        user.address.address2 = $scope.inputValues.address2;
                        user.address.city = $scope.inputValues.city;
                        user.address.state = $scope.inputValues.state;
                        user.address.zip = $scope.inputValues.zip;
                        user.address.country = $scope.inputValues.country;

                        $location.path('/final_intro').replace()
                    }
                } else {
                    $scope.invalidText = 'Please Input Field';
                }
            }
        }
    ]
);

