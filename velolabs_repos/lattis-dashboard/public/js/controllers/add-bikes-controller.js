'use strict'

angular.module('skyfleet.controllers')
  .controller('addbikeController',
    function ($scope, $rootScope, $state, bikeFleetFactory, sessionFactory, rootScopeFactory, $timeout, utilsFactory, $window, notify, _, ngDialog) {
      $scope.Reusedescreiprion = function () {
        $state.go('')
      }
      $scope.addBikes = true
      $scope.addDetails = false
      $scope.bike = {}
      $scope.bikeImage = null
      $scope.descriptionMaxLength = 400

      $scope.validateFields = function (data) {
        data.$setDirty()
      }

      $scope.distancePreference = JSON.parse(localStorage.getItem('currentFleet')).distance_preference

      function toggleValidation () {
        $scope.form.make.$setDirty()
        $scope.form.model.$setDirty()
        $scope.form.description.$setDirty()
      }

      $scope.customerRadio = function () {
        ngDialog.open({
          template: '../../html/modals/select-existing-bike-image.html',
          controller: 'selectExistingBikeImageController'
        })
      }

      $scope.existingDescription = function () {
        ngDialog.open({
          template: '../../html/modals/select-existing-bike-description.html',
          controller: 'selectExistingBikeDescriptionController'
        })
      }

      // Back To Staging
      $scope.backtoStagingClick = function () {
        $window.history.back()
      }

      $scope.mobile = {
        bikeType: 'Bike',
        bikeDesc: 'Bikes in this network are black and white, ' +
                'and have a UHBikes sticker placed on the crossbar.',
        bikeImageSRC: '../images/bike-placeholder.png'
      }
      let bikeSRC
      $rootScope.$on('uploadCompleted', function (event, data) {
        bikeSRC = data[0]
      })

      $scope.$on('photoSelected', function (event, data) {
        bikeSRC = data
      })

      $scope.updatePreview = function () {
        if (bikeSRC) {
          $scope.mobile.bikeImageSRC = bikeSRC
        }
        if ($scope.selectedBikeDescription) {
          $scope.mobile.bikeDesc = $scope.selectedBikeDescription
        }
        $scope.mobile.bikeType = $scope.bikeType
        if ($scope.bikeType.toLowerCase() === 'electric'){
          $scope.mobile.bikeType = 'Electric Bike'
        }

        if ($scope.bikeType.toLowerCase() === 'regular'){
          $scope.mobile.bikeType = 'Bike'
        }
      }

      if (rootScopeFactory.getData('fleetId')) {
        bikeFleetFactory.getBikeDescriptionAndImage({fleet_id: rootScopeFactory.getData('fleetId')}).then(function (res) {
          if (res.payload) {
            $scope.bikeDescriptionList = angular.copy(res.payload.bike_descriptions)
            $scope.bikePhotoList = angular.copy(res.payload.bike_images)
            _.each($scope.bikeDescriptionList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
            _.each($scope.bikePhotoList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
          } else {
            $scope.bikeDescriptionList = true
            $scope.bikePhotoList = true
          }
        })

        bikeFleetFactory.fetchPreDefinedBikes(rootScopeFactory.getData('customerId'), function (res) {
          if (res && res.status === 200) {
            $scope.predifinedBikes = res.payload
          }
        })
      }

      $scope.bikeSelected = function () {
        if ($scope.selectedOption) {
          $scope.bikeMake = $scope.selectedOption.make
          $scope.bikeModel = $scope.selectedOption.model
          $scope.bikeType = $scope.selectedOption.type
          $scope.descriptionSelector = 'exsistingDescription'
          $scope.selectedBikeDescription = $scope.selectedOption.description
          $scope.photoUpload = 'useOldPhoto'
          $scope.oldPhotoURL = $scope.selectedOption.pic
          $scope.iotModuleType = $scope.selectedOption.iot_module_type
          bikeSRC = $scope.selectedOption.pic
          $scope.updatePreview()
        } else {
          $scope.bikeMake = null
          $scope.bikeModel = null
          $scope.bikeType = 'regular'
          $scope.descriptionSelector = 'newDescription'
          $scope.selectedBikeDescription = null
          $scope.photoUpload = 'newPhoto'
          $scope.oldPhotoURL = null
          $scope.iotModuleType = null
          $scope.updatePreview()
        }
      }

      $scope.$on('fleetChange', function (event, id) {
        bikeFleetFactory.getBikeDescriptionAndImage({fleet_id: id}).then(function (res) {
          if (res.payload) {
            $scope.bikeDescriptionList = angular.copy(res.payload.bike_descriptions)
            $scope.bikePhotoList = angular.copy(res.payload.bike_images)
            _.each($scope.bikeDescriptionList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
            _.each($scope.bikePhotoList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
          } else {
            $scope.bikeDescriptionList = true
            $scope.bikePhotoList = true
          }
        })

        bikeFleetFactory.fetchPreDefinedBikes(rootScopeFactory.getData('customerId'), function (res) {
          if (res && res.status === 200) {
            $scope.predifinedBikes = res.payload
          }
        })
      })

      $scope.saveProfile = function () {
        toggleValidation()
        if ($scope.form.$dirty && !_.keys($scope.form.$error).length > 0) {
          $rootScope.showLoader = true
          let fd = new FormData()
          fd.append('fleet_id', rootScopeFactory.getData('fleetId'))
          fd.append('make', $scope.bikeMake)
          fd.append('model', $scope.bikeModel)
          fd.append('type', $scope.bikeType)
          fd.append('iot_module_type', $scope.iotModuleType || null)
          fd.append('bikes_to_be_added', rootScopeFactory.getData('bikesToAdd'))
          fd.append('description', $scope.selectedBikeDescription)
          fd.append('customer_id', rootScopeFactory.getData('customerId'))
          fd.append('operator_id', sessionFactory.getCookieId())
          $scope.distancePreference === 'miles' ? fd.append('maintenance_schedule', utilsFactory.getMeters($scope.mainSchedule)) : fd.append('maintenance_schedule', utilsFactory.kmToMeter($scope.mainSchedule))
          if ($scope.photoUpload === 'newPhoto' && $scope.uploadedFile) {
            fd.append('pic', $scope.uploadedFile, $scope.uploadedFile.name)
          } else if ($scope.photoUpload === 'useOldPhoto') {
            fd.append('bike_image', $scope.oldPhotoURL)
          }
          if ($scope.bikeType === 'electric' && $scope.selectedOption) {
            fd.append('pre_defined_bike', $scope.selectedOption.pre_defined_bike)
          }
          bikeFleetFactory.addBikesToFleet(fd, function (res) {
            if (res && res.status === 200) {
              $rootScope.showLoader = false
              notify({
                message: 'Rides added to the fleet successfully',
                duration: 2000,
                position: 'right'
              })
              bikeFleetFactory.clearBikeDataCache()
              bikeFleetFactory.clearDescriptionCache()
              $state.go('staged-bikes')
            } else {
              $rootScope.showLoader = false
              notify({message: 'Failed to add rides to the fleet', duration: 2000, position: 'right'})
            }
          })
        }
      }

      $scope.nextBikes = function () {
        if ($scope.bike.no && $scope.bike.no !== '') {
          rootScopeFactory.setData('bikesToAdd', $scope.bike.no)
          $scope.addBikes = false
          $scope.addDetails = true
        }
      }
      $scope.backorderBikes = function () {
        $scope.addBikes = true
        $scope.addDetails = false
      }

      $scope.$on('photoSelected', function (event, photoURL) {
        $scope.oldPhotoURL = photoURL
      })

      $scope.checkValue = function () {
        if ($scope.descriptionSelector === 'newDescription') {
          $scope.selectedBikeDescription = ''
        }
      }

      $scope.$on('descriptionSelected', function (event, desc) {
        $scope.selectedBikeDescription = desc
      })

      $rootScope.$on('uploadCompleted', function (event, imageData) {
        $scope.photoUpload = 'newPhoto'
        $scope.descriptionSelector = 'newDescription'
        $scope.uploadedFile = imageData[1]
      })
    })
