'use strict'

angular.module('skyfleet.controllers').controller(
  'assignTicketController',
  function ($scope, profileSettingFactory, sessionFactory, $q, $rootScope, rootScopeFactory, myFleetFactory, ngDialog, bikeFleetFactory, notify) {
    $q.all([profileSettingFactory.getOnCall(rootScopeFactory.getData('fleetId'))]).then(function (response) {
      $scope.operatorData = response[0].payload
    })

    $scope.assignOperator = function (user) {
      $scope.operatorInfo = user
    }
    $scope.operatorInfo = {}
    $scope.operatorInfo.first_name = 'Select an operator'
    $scope.assignTicket = function () {
      if (rootScopeFactory.getData('ticketSelected')) {
        bikeFleetFactory.assignTicket({
          ticket_id: rootScopeFactory.getData('ticketSelected'),
          assignee: $scope.operatorInfo.operator_id
        }, function (response) {
          if (response && response.status === 200) {
            notify({message: 'Ticket assigned successfully', duration: 2000, position: 'right'})
            myFleetFactory.clearTicketsCache()
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            ngDialog.closeAll()
            rootScopeFactory.setData('ticketSelected', null)
          } else {
            notify({
              message: 'Failed to assignee ticket to operator',
              duration: 2000,
              position: 'right'
            })
            rootScopeFactory.setData('ticketSelected', null)
            ngDialog.closeAll()
          }
        })
      }
    }
    $scope.closeModal = function () {
      ngDialog.closeAll()
    }
  })
