'use strict'

angular.module('skyfleet.controllers').controller(
  'addMemberModalController',
  function ($scope, profileSettingFactory, sessionFactory, $q, $rootScope, rootScopeFactory, usersFactory, ngDialog, bikeFleetFactory, notify, _, $timeout) {
    $scope.domain = {}

    $scope.addDomain = function () {
      if ($scope.domain.memberform && $scope.domain.memberform.domain.$valid && !$scope.domain.memberform.domain.$pristine) {
        $scope.domainList.unshift({domain_name: $scope.domain.id})
        $scope.domain.memberform.domain.$setPristine()
        $scope.domain.id = ''
      }
    }

    if (rootScopeFactory.getData('fleetId')) {
      fetchDomainsAndCSVFile(rootScopeFactory.getData('fleetId'))
    }

    $rootScope.$on('fleetChange', function (event, fleetId) {
      fetchDomainsAndCSVFile(fleetId)
    })

    function fetchDomainsAndCSVFile (fleetId) {
      $q.all([usersFactory.getDomains({fleet_id: fleetId}),
        usersFactory.getMemberCsv(fleetId)]).then(function (response) {
        if (response[0].payload) {
          $scope.domainList = angular.copy(response[0].payload)
          $scope.domainListCopy = angular.copy(response[0].payload)
        }

        if (response[1].payload) {
          $scope.CSVFile = angular.copy(response[1].payload.file_location)
          $scope.CSVFile === null ? $scope.csvFile = null : $scope.csvFile = $scope.CSVFile.split('/').pop()
        }
      })
    }

    $scope.delDomain = function (domainId) {
      $scope.domainList = _.reject($scope.domainList, function (num) {
        return num.domain_id === domainId
      })
    }

    $scope.delCsv = function () {
      $scope.csvFile = null
      $scope.deleteClicked = true
    }

    $scope.memberNext = function () {
      $scope.newDomains = []
      $scope.delDomains = []
      _.each($scope.domainList, function (element) {
        if (_.where($scope.domainListCopy, {domain_name: element.domain_name}).length === 0) {
          $scope.newDomains.push(element.domain_name)
        }
      })
      _.each($scope.domainListCopy, function (element) {
        if (_.where($scope.domainList, {domain_name: element.domain_name}).length === 0) {
          $scope.delDomains.push(element.domain_id)
        }
      })
      if ($scope.newDomains.length) {
        $rootScope.showLoader = true
        usersFactory.clearMemberCSVCache()
        bikeFleetFactory.addMemberDomain({
          fleet_id: rootScopeFactory.getData('fleetId'),
          operator_id: sessionFactory.getCookieId(),
          domain_name: $scope.newDomains
        }, function (response) {
          if (response && response.status === 200) {
            $rootScope.showLoader = false
            $scope.showStatus = true
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            notify({message: 'Domain has been added successfully', duration: 2000, position: 'right'})
            $scope.domain.memberform.domain.$setPristine()
            $scope.domain.id = ''
            $scope.showStatus = false
            ngDialog.closeAll()
          } else if (response && response.status === 500) {
            $rootScope.showLoader = false
            notify({message: 'This domain has been added already', duration: 2000, position: 'right'})
          }
        })
      }

      if ($scope.delDomains.length) {
        $rootScope.showLoader = true
        usersFactory.clearMemberCSVCache()
        usersFactory.removeDomain({
          domain_id: $scope.delDomains,
          fleet_id: rootScopeFactory.getData('fleetId'),
          operator_id: sessionFactory.getCookieId()
        }, function (response) {
          if (response && response.status === 200) {
            $rootScope.showLoader = false
            $scope.showStatus = true
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            notify({
              message: 'Domain has been removed successfully',
              duration: 2000,
              position: 'right'
            })
            $scope.domain.memberform.domain.$setPristine()
            $scope.domain.id = ''
            $scope.showStatus = false
            ngDialog.closeAll()
          } else if (response && response.status === 500) {
            $rootScope.showLoader = false
            notify({message: 'Failed to delete the domain', duration: 2000, position: 'right'})
          }
        })
      }

      if ($scope.deleteClicked) {
        $rootScope.showLoader = true
        usersFactory.deleteCSV(function (response) {
          if (response && response.status === 200) {
            $rootScope.showLoader = false
            $timeout(function () {
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            }, 50)
            notify({
              message: 'User CSV file has been deleted successfully',
              duration: 2000,
              position: 'right'
            })
            $scope.deleteClicked = false
            ngDialog.closeAll()
          } else {
            $rootScope.showLoader = false
            notify({message: 'Failed to delete member CSV', duration: 2000, position: 'right'})
          }
        })
      }

      if (_.isObject($scope.membercsv)) {
        $rootScope.showLoader = true
        let member = new FormData()
        member.append('member_csv', $scope.membercsv)
        member.append('fleet_id', rootScopeFactory.getData('fleetId'))
        usersFactory.uploadCSV(member, function (res) {
          if (res && res.status === 200) {
            $timeout(function () {
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            }, 50)
            $rootScope.showLoader = false
            notify({
              message: 'User CSV file has been uploaded successfully',
              duration: 2000,
              position: 'right'
            })
            $scope.membercsv = null
            ngDialog.closeAll()
          } else {
            $rootScope.showLoader = false
            notify({message: 'Failed to upload member CSV', duration: 2000, position: 'right'})
          }
        })
      }
    }

    $scope.memberCancel = function () {
      ngDialog.closeAll()
    }
  })
