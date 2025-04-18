'use strict'

angular.module('skyfleet.controllers').controller(
  'createTicketModalController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, _, myFleetFactory, $location) {
    $scope.bikesIdList = []

    $q.all([bikeFleetFactory.getBikesData({fleet_id: rootScopeFactory.getData('fleetId')})]).then(function (response) {
      if (response[0].payload) {
        // automatically set bike when creating ticket under bike section
        let absUrl = $location.absUrl();
        const bikeId = parseInt(absUrl.substring(absUrl.lastIndexOf('/') + 1))
        if (bikeId) {
          const [selectedBike] = response[0].payload.bike_data.filter(bike => bike.bike_id === bikeId)
          $scope.ticketBike = selectedBike
          $scope.$emit('onAutocompleteSelect', selectedBike.bike_id)
        }
        $scope.bikesIdList.splice(0, $scope.bikesIdList.length)
        _.each(response[0].payload.bike_data, function (element) {
          $scope.bikesIdList.push({value: element.bike_name, id: element.bike_id})
        })
      }
    })

    $scope.mainTypes = [
      {name: 'Report Damage', value: 'damage_reported', disabled: false},
      {name: 'Maintenance due', value: 'service_due', disabled: false},
      {name: 'Report Theft', value: 'reported_theft', disabled: false},
      {name: 'Parked outside parking area', value: 'parking_outside_geofence', disabled: true}
    ]

    $scope.ticketTypeChanged = function (type) {
      if (!type.disabled) {
        $scope.category = type
      }
    }

    $scope.category = $scope.mainTypes[0]

    $scope.createTicket = function () {
      let ticket = new FormData()
      ticket.append('category', $scope.category.value)
      ticket.append('notes', $scope.serviceNotes)
      if ($scope.serviceImage) {
        ticket.append('photo', $scope.serviceImage)
      }
      ticket.append('fleet_id', rootScopeFactory.getData('fleetId'))
      ticket.append('operator_id', sessionFactory.getCookieId())
      ticket.append('customer_id', rootScopeFactory.getData('customerId'))
      ticket.append('bike_id', $scope.selectedBikeId)
      ticket.append('reported_by_operator_id', sessionFactory.getCookieId())
      if($scope.serviceImage && $scope.serviceImage.size > 2097152) {
        notify({
          message: 'Image cannot be bigger than 2 MB.',
          duration: 2000,
          position: 'right'
        })
        $rootScope.showLoader = false
      } else if ($scope.selectedBikeId) {
        $rootScope.showLoader = true
        bikeFleetFactory.createTicket(ticket, function (res) {
          if (res && res.status === 200) {
            ngDialog.closeAll()
            myFleetFactory.clearTicketsCache()
            bikeFleetFactory.clearBikeDataCache()
            $rootScope.$broadcast('clearUpload')
            $rootScope.$emit('clearUpload')
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            notify({
              message: 'Ticket has been created successfully',
              duration: 2000,
              position: 'right'
            })
            $rootScope.showLoader = false
          } else {
            ngDialog.closeAll()
            notify({message: 'Failed to create ticket', duration: 2000, position: 'right'})
            $rootScope.showLoader = false
          }
        })
      } else {
        notify({message: 'No bike has been selected to create ticket', duration: 2000, position: 'right'})
        $rootScope.showLoader = false
      }
    }

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $scope.$on('onAutocompleteSelect', function (event, id) {
      $scope.selectedBikeId = id
    })
  })
