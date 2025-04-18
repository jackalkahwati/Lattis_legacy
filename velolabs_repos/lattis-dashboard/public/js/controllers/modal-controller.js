'use strict'

angular.module('skyfleet.controllers')
  .controller('modalController',
    function ($scope, $state, bikeFleetFactory, utilsFactory, sessionFactory, rootScopeFactory, $timeout, $rootScope, myFleetFactory, notify, _, profileSettingFactory, $q, usersFactory, lattisErrors) {
      $scope.modal = {}
      $scope.profileLogo = null
      $scope.maintanenceList = rootScopeFactory.getData('maintanenceList')
      $scope.sendToActiveFleet = function () {
        bikeFleetFactory.toActiveFleet({
          bikes: rootScopeFactory.getData('selectedBikes'),
          operator_id: sessionFactory.getCookieId()
        }, function (response) {
          if (response.status_code === 200) {
            modalClose('Bike has send to active fleet', 'activeFleetModal')
          }
        })
      }
      $scope.disableInputs = true
      $scope.newuser = {}
      $scope.newuser.email = ''

      $scope.$watch('newuser.email', checkOperator)

      function checkOperator () {
        $scope.mailtrue = false
        if ($scope.newuser.email) {
          var operator = $scope.newuser.email.toString().match(/^[-a-zA-Z0-9][-._!#$%&*+/=?`{|~}a-zA-Z0-9]*@[-.a-zA-Z0-9]+(\.[-.a-zA-Z0-9]+)*\.(com|edu|info|gov|int|mil|net|org|biz|name|museum|coop|aero|pro|io|[a-zA-Z]{2})$/g)
          if (operator && operator.length) {
            usersFactory.checkOperator($scope.newuser.email, sessionFactory.getCookieId(), function (response) {
              if (response.payload.operator === null) {
                $scope.disableInputs = false
                $scope.newuser.first_name = ''
                $scope.newuser.last_name = ''
              } else {
                $scope.newuser.operator_id = response.payload.operator.operator_id
                $scope.disableInputs = true
                $scope.mailtrue = true
              }
              if (response.payload) {
                $scope.fleetList = response.payload.fleets
                if (response.payload.fleets) {
                  $scope.fleetNameModel = response.payload.fleets[0]
                  $scope.newuser.first_name = response.payload.operator.first_name
                  $scope.newuser.last_name = response.payload.operator.last_name
                }
              }
            })
          }
        } else {
          $scope.disableInputs = true
        }
      }

      bikeFleetFactory.getMaintenanceList().then(function (response) {
        $scope.maintanenceList = response.payload
        _.each($scope.maintanenceList, function (element) {
          element['maintenance_category'] = utilsFactory.startCapitalize(utilsFactory.underscoreToSpace(element['maintenance_category']))
        })
      })

      $scope.sendToLive = function () {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.toActiveFleet({
            operator_id: sessionFactory.getCookieId(),
            bikes: rootScopeFactory.getData('selectedBikes')
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Bikes returned to Live successfully', duration: 2000, position: 'right'})
              jQuery('#activeFleetModal').modal('hide')
              rootScopeFactory.setData('selectedBikes', null)
              $rootScope.$broadcast('clearBikeDataCache')
              $rootScope.$emit('clearBikeDataCache')
              $rootScope.$emit('disableBikeOptions')
              $rootScope.$broadcast('disableBikeOptions')
            } else {
              notify({message: 'Failed to send bikes to Live', duration: 2000, position: 'right'})
              jQuery('#activeFleetModal').modal('hide')
              rootScopeFactory.setData('selectedBikes', null)
            }
          })
        }
      }
      $scope.mainTypes = [
        {name: 'Report Damage', value: 'damage_reported', disabled: false},
        {name: 'Maintenance due', value: 'service_due', disabled: false},
        {name: 'Report Theft', value: 'reported_theft', disabled: false},
        {name: 'Parked outside parking area', value: 'parking_outside_geofence', disabled: false}
      ]

      $scope.ticketTypeChanged = function (type) {
        if (!type.disabled) {
          $scope.category = type
        }
      }

      $scope.category = $scope.mainTypes[0]
      $scope.$on('uploadCompleted', function (event, data) {
        $scope.maintenceticket.loadimage = data[0]
      })

      $scope.maintenceticket = [{
        name: 'Cool lion',
        loadimage: '',
        category: '',
        notes: ''
      }]

      $scope.genericTicket = function () {
        let ticket = new FormData()
        ticket.append('category', $scope.category.value)
        ticket.append('notes', $scope.maintenceticket.maintenanceNotes)
        if ($scope.maintenceticket.ticketImage) {
          ticket.append('photo', $scope.maintenceticket.ticketImage)
        }
        ticket.append('fleet_id', rootScopeFactory.getData('fleetId'))
        ticket.append('operator_id', sessionFactory.getCookieId())
        ticket.append('customer_id', rootScopeFactory.getData('customerId'))
        ticket.append('bike_id', $scope.selectedBikeId)
        ticket.append('reported_by_operator_id', sessionFactory.getCookieId())
        if ($scope.selectedBikeId) {
          $rootScope.showLoader = true
          bikeFleetFactory.createTicket(ticket, function (res) {
            if (res || res.status === 200) {
              jQuery('#createTicketModal').modal('hide')
              $scope.maintenceticket = {}
              myFleetFactory.clearTicketsCache()
              bikeFleetFactory.clearBikeDataCache()
              $scope.ticketImage = null
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
              jQuery('#createTicketModal').modal('hide')
              notify({message: 'Failed to create ticket', duration: 2000, position: 'right'})
              $rootScope.showLoader = false
            }
          })
        } else {
          notify({message: 'No bike has been selected to create ticket', duration: 2000, position: 'right'})
          $rootScope.showLoader = false
        }
      }

      $scope.ticketCancel = function () {
        $scope.maintenceticket = {}
        $rootScope.$broadcast('clearUpload')
        $rootScope.$emit('clearUpload')
      }

      $scope.maintenanceTicket = function () {
        let ticketform = new FormData()
        ticketform.append('category', 'damage_reported')
        ticketform.append('notes', $scope.maintenceticket.notes)
        if ($scope.maintenceticket.damageImage) {
          ticketform.append('operator_photo', $scope.maintenceticket.damageImage)
        }
        ticketform.append('fleet_id', rootScopeFactory.getData('fleetId'))
        ticketform.append('operator_id', sessionFactory.getCookieId())
        ticketform.append('customer_id', rootScopeFactory.getData('customerId'))
        ticketform.append('bike_id', rootScopeFactory.getData('selectedBikes')[0])
        ticketform.append('reported_by_operator_id', sessionFactory.getCookieId())
        $rootScope.showLoader = true
        bikeFleetFactory.sendToService(ticketform, function (res) {
          if (res && res.status === 200) {
            notify({message: 'Ticket successfully created', duration: 2000, position: 'right'})
            jQuery('#maintenanceTicketModal').modal('hide')
            $scope.maintenceticket = {}
            $scope.damageImage = null
            $rootScope.$broadcast('clearUpload')
            $rootScope.$emit('clearUpload')
            rootScopeFactory.setData('selectedBikes', null)
            $timeout(function () {
              $rootScope.$broadcast('clearBikeDataCache')
              $rootScope.$emit('clearBikeDataCache')
            }, 30)
            $rootScope.$emit('disableBikeOptions')
            $rootScope.$broadcast('disableBikeOptions')
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.showLoader = false
          } else {
            notify({message: 'Failed to send bike to service', duration: 2000, position: 'right'})
            jQuery('#maintenanceTicketModal').modal('hide')
            rootScopeFactory.setData('selectedBikes', null)
            $rootScope.$emit('disableBikeOptions')
            $rootScope.$broadcast('disableBikeOptions')
            $rootScope.showLoader = false
          }
        })
      }
      $scope.sendWorkshop = function () {
        bikeFleetFactory.bikeToWorkshop({
          bikes: rootScopeFactory.getData('selectedBikes'),
          maintenance_notes: $scope.modal.maintenanceNotes,
          maintenance_category: utilsFactory.startLowerCase(utilsFactory.spaceToUnderscore($scope.modal.maintenanceSelect.maintenance_category)),
          fleet_id: rootScopeFactory.getData('fleetId'),
          operator_id: sessionFactory.getCookieId()
        }, function (response) {
          if (response.status_code === 200) {
            $scope.modal.maintenanceNotes = ''
            $scope.modal.maintenanceSelect = null
            modalClose('Bike has been sent to workshop', 'workshopModal')
          }
        })
      }

      $scope.$on('sendToService', function () {
        $scope.selectedBike = _.where($scope.bikesList, {bike_id: parseInt(rootScopeFactory.getData('selectedBikes')[0])})
      })
      $scope.bikesIdList = []
      function fetchAllData (fleetId) {
        $q.all([bikeFleetFactory.getBikesData({fleet_id: fleetId}),
          bikeFleetFactory.getBikeDescriptionAndImage({fleet_id: fleetId}),
          profileSettingFactory.getOnCall(fleetId)]).then(function (response) {
          if (response[0].payload) {
            $scope.bikesList = angular.copy(response[0].payload.bike_data)
            $scope.operatorData = response[2].payload
            $scope.bikesIdList.splice(0, $scope.bikesIdList.length)
            _.each(response[0].payload.bike_data, function (element) {
              $scope.bikesIdList.push({value: element.bike_name, id: element.bike_id})
            })
          }
          if (response[1].payload) {
            $scope.bikeDescriptionList = angular.copy(response[1].payload.bike_descriptions)
            $scope.bikePhotoList = angular.copy(response[1].payload.bike_images)
            _.each($scope.bikeDescriptionList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
            _.each($scope.bikePhotoList, function (element) {
              element['date'] = utilsFactory.dateStringToDate(element.date)
            })
          }

          _.each($scope.operatorData, function (element) {
            element['full_name'] = element.first_name + ' ' + element.last_name
          })
        })
        usersFactory.getDomains(fleetId, function (response) {
          if (response.payload) {
            $scope.domainList = response.payload
            $scope.domainListCopy = angular.copy(response.payload)
          }
        })

        usersFactory.getCsvFile(fleetId, function (response) {
          if (response.payload) {
            $scope.CSVFile = response.payload[0].member_csv
            $scope.CSVFile === null ? $scope.csvFile = null : $scope.csvFile = $scope.CSVFile.split('/').pop()
          }
        })
      }

      $scope.$on('fleetChange', function (event, fleetId) {
        fetchAllData(fleetId)
      })

      $scope.$on('ticketSelected', function (event, ticketId) {
        $scope.selectedTicketId = ticketId
      })

      $scope.assignTicket = function () {
        if ($scope.selectedTicketId) {
          bikeFleetFactory.assignTicket({
            ticket_id: $scope.selectedTicketId,
            assignee: $scope.dataInfo.operatorModel.operator_id
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Ticket assigned successfully', duration: 2000, position: 'right'})
              myFleetFactory.clearTicketsCache()
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
              jQuery('#assignTicketModal').modal('hide')
            } else {
              notify({
                message: 'Failed to assignee ticket to operator',
                duration: 2000,
                position: 'right'
              })
              jQuery('#assignTicketModal').modal('hide')
            }
          })
        }
      }

      $scope.delDomain = function (domainId) {
        $scope.domainList = _.reject($scope.domainList, function (num) {
          return num.domain_id === domainId
        })
      }

      $scope.dataInfo = {}
      $scope.selectPhoto = function () {
        $rootScope.$broadcast('photoSelected', $scope.dataInfo.selectedphoto)
        $rootScope.$emit('photoSelected', $scope.dataInfo.selectedphoto)
        modalClose(null, 'bikeuploadModal')
      }

      $scope.selectDesc = function () {
        $rootScope.$broadcast('descriptionSelected', $scope.dataInfo.selectedBikeDescription)
        modalClose(null, 'exitDescriptionModal')
      }

      $scope.sendToArchive = function () {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.sendToArchive({
            operator_id: sessionFactory.getCookieId(),
            bikes: rootScopeFactory.getData('selectedBikes'),
            status: $scope.dataInfo.archiveStatus
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Bikes sent to Archive successfully', duration: 2000, position: 'right'})
              jQuery('#sendToArchive').modal('hide')
              rootScopeFactory.setData('selectedBikes', null)
              $timeout(function () {
                $rootScope.$broadcast('clearBikeDataCache')
                $rootScope.$emit('clearBikeDataCache')
              }, 30)
            } else if (response && response.code === lattisErrors.Unauthorized) {
              notify({message: 'Bike has active booking', duration: 2000, position: 'right'})
              jQuery('#sendToArchive').modal('hide')
              rootScopeFactory.setData('selectedBikes', null)
            } else {
              notify({message: 'Failed to send bikes to Archive', duration: 2000, position: 'right'})
              jQuery('#sendToArchive').modal('hide')
              rootScopeFactory.setData('selectedBikes', null)
            }
          })
        }
      }

      if (rootScopeFactory.getData('fleetId')) {
        fetchAllData(rootScopeFactory.getData('fleetId'))
      }

      $scope.$on('onAutocompleteSelect', function (event, id) {
        $scope.selectedBikeId = id
      })

      $scope.deleteBike = function () {
        bikeFleetFactory.deleteBikes({
          operator_id: sessionFactory.getCookieId(),
          bikes: rootScopeFactory.getData('selectedBikes')
        }, function (response) {
          if (response.status_code === 200) {
            modalClose('Bike has been deleted', 'deleteModal')
          }
        })
      }
      $scope.uploadLogo = function () {
        $rootScope.$broadcast('logoSaved')
        $rootScope.$emit('logoSaved')
      }

      $scope.cancelLogo = function () {
        $rootScope.$broadcast('clearUpload')
        $rootScope.$emit('clearUpload')
      }

      function modalClose (msg, id) {
        jQuery('#' + id).modal('hide')
        if (msg) {
          notify({message: msg, duration: 2000, position: 'right'})
        }
        $rootScope.$broadcast('bikeFleetMenuClear')
        $rootScope.$broadcast('clearBikeDataCache')
      }

      $scope.$on('damageId', function (event, damageData) {
        $scope.damageData = damageData
        $scope.damageData[0].damageNotes = damageData[0].operator_notes ||
                    damageData[0].maintenance_notes || damageData[0].rider_notes
        $scope.damageData[0].damagePhoto = damageData[0].operator_photo ||
                    damageData[0].user_photo
      })

      $scope.domain = {}
      $scope.addDomain = function () {
        if ($scope.domain.memberform && $scope.domain.memberform.domain.$valid && !$scope.domain.memberform.domain.$pristine) {
          $scope.domainList.unshift({domain_name: $scope.domain.id})
          $scope.domain.memberform.domain.$setPristine()
          $scope.domain.id = ''
        }
      }

      $scope.memberCancel = function () {
        $scope.domainList = $scope.domainListCopy
        $scope.uploadedCSV = ''
      }

      $scope.$on('uploadCompletedDup', function (event, file) {
        $scope.uploadedCSV = file
      })

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
          bikeFleetFactory.addMemberDomain({
            fleet_id: rootScopeFactory.getData('fleetId'),
            operator_id: sessionFactory.getCookieId(),
            domain_name: $scope.newDomains
          }, function (response) {
            if (response && response.status === 200) {
              $scope.showStatus = true
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
              notify({message: 'Domain has been added successfully', duration: 2000, position: 'right'})
              $scope.domain.memberform.domain.$setPristine()
              $scope.domain.id = ''
              $scope.showStatus = false
              modalClose(null, 'addmemberModal1')
            } else if (response && response.status === 500) {
              notify({message: 'This domain has been added already', duration: 2000, position: 'right'})
            }
          })
        }

        if ($scope.delDomains.length) {
          usersFactory.removeDomain($scope.delDomains, function (response) {
            if (response && response.status === 200) {
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
              modalClose(null, 'addmemberModal1')
            } else if (response && response.status === 500) {
              notify({message: 'Failed to delete the domain', duration: 2000, position: 'right'})
            }
          })
        }

        if ($scope.deleteClicked) {
          usersFactory.deleteCSV(function (response) {
            if (response && response.status === 200) {
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
              modalClose(null, 'addmemberModal1')
            } else {
              notify({message: 'Failed to delete member CSV', duration: 2000, position: 'right'})
            }
          })
        }

        if (_.isObject($scope.uploadedCSV)) {
          let member = new FormData()
          member.append('member_csv', $scope.uploadedCSV)
          member.append('fleet_id', rootScopeFactory.getData('fleetId'))
          usersFactory.uploadCSV(member, function (res) {
            if (res && res.status === 200) {
              $timeout(function () {
                $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
                $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
              }, 50)
              notify({
                message: 'User CSV file has been uploaded successfully',
                duration: 2000,
                position: 'right'
              })
              $scope.uploadedCSV = null
              modalClose(null, 'addmemberModal1')
            } else {
              notify({message: 'Failed to upload member CSV', duration: 2000, position: 'right'})
            }
          })
        }
      }

      // Add User fleet
      // $scope.selectedFleets = [];

      $scope.newuser = {}
      $scope.newuser.access_staff = 'Select user'
      $scope.customerList = [{
        customer_name: 'Primary administrator', value: 'admin'
      },
      {customer_name: 'Fleet Coordinator', value: 'coordinator'},
      {customer_name: 'Fleet Technician', value: 'maintenance'}]

      $scope.selectRole = function (role) {
        if (!role.disabled) {
          $scope.newuser.access_staff = role.customer_name
        }
      }
      $scope.adduser = function () {
        $scope.newuser.customer_id = rootScopeFactory.getData('customerId')
        profileSettingFactory.addOperator($scope.newuser, function (response) {
          if (response && response.status === 200) {
            notify({
              message: 'Users have been added to the fleet successfully',
              duration: 2000,
              position: 'right'
            })
            $scope.newuser = {}
            $scope.fleetList = []
            profileSettingFactory.clearOnCallCache()
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            modalClose(null, 'addUserModal')
          } else if (response && response.status === 409) {
            notify({
              message: $scope.domain.id + 'is already an registered operator',
              duration: 2000,
              position: 'right'
            })
          } else {
            notify({message: 'Failed to create users', duration: 2000, position: 'right'})
          }
        })
      }

      $scope.cancelUser = function () {
        $scope.newuser = {}
        $scope.fleetList = []
        $scope.newuser.email = null
        $scope.newuser.access_staff = 'admin'
      }
    })
