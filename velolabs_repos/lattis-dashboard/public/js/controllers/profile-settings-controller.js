'use strict'

angular.module('skyfleet.controllers').controller(
  'profileSettingController',
  function ($scope, sessionFactory, $rootScope, usersFactory, $state, profileSettingFactory, _, utilsFactory, $timeout, myFleetFactory,
    rootScopeFactory, notify, paymentFactory, timezoneConstants, ngDialog, bikeFleetFactory, addFleetFactory, ngIntlTelInput, lattisConstants,
    iotModuleConstants, regexPatterns, $http
  ) {
    $scope.billingAddress = {}
    $scope.pricingdata = {}
    $scope.fleetType = {}
    $scope.scoutIntegrated = false
    $scope.geotabIntegrated = false
    $scope.maxFleetNameLength = 25
    let bike_available_start = 0
    let bike_available_end = 720
    $scope.inputs = [];
    $scope.showModules = [
      'scout',
      'geotab',
      'linka',
      'duckt',
      'tapkey',
      'ACTON',
      'Teltonika',
      'Omni IoT',
      'kisi',
      'sas',
      'ParcelHive',
      'Edge'
    ]
    const [patterns] = regexPatterns
    $scope.emailRegexPatterns = patterns.email
    $scope.inner_menu_Items = [
      {
        id: 1,
        title: "Fleet Info",
      },
      {
        id: 3,
        title: "Users",
      },
      {
        id: 4,
        title: "Fleet Schedule",
      },
      {
        id: 7,
        title: "App Setting",
      },
      {
        id: 8,
        title: "Integrations",
      },
    ];
    $scope.rideDeposit = [
      {
        name: "Mins",
        id: "1",
        mulVal: 1,
      },
      {
        name: "Hours",
        id: "2",
        mulVal: 60,
      },
      {
        name: "Days",
        id: "2",
        mulVal: 1440,
      },
      {
        name: "Weeks",
        id: "3",
        mulVal: 10080,
      },
      {
        name: "Months",
        id: "4",
        mulVal: 43200,
      },
    ];
    $scope.newOnCall = {}
    $scope.newOnCall.full_name = 'Select user'
    $scope.userSelectedItem = function (user) {
      $scope.newOnCall = user
    }
    $scope.innertab =  localStorage.getItem('innertab') || 1
    localStorage.removeItem('innertab')
    $scope.cardField = false
    var temp
    $scope.pricingdata.restriction = 'Yes'
    $scope.unlimitedSetting = function () {
      temp = $scope.restrictionLimit
      if ($scope.pricingdata.restriction === 'Yes') {
        $scope.restrictionLimit = null
      } else {
        $scope.restrictionLimit = temp
      }
    }

    $scope.fleetTypes = [
      { name: 'Private', value: 'private' },
      { name: 'Public', value: 'public' }
    ]
    $scope.phoneNumberIsValid = false
    window.onload = function () {
      const phoneInput = document.querySelector('#phone')
      let iti = window.intlTelInput(phoneInput, {
        hiddenInput: 'full_phone',
        utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.15/js/utils.js",
      });
      phoneInput.addEventListener('blur', function () {
        $scope.phoneNumberIsValid = !!iti.isValidNumber();
        $scope.fleetDetails.contact_phone = iti.getNumber()
        $scope.$apply();
      })
    }
    $scope.fieldChanged = false

    $scope.reservation_settings = {}
    $scope.status = {
      reservationsEnabled: false
    }

    $scope.newuser = {}
    $scope.customerList = [
    {customer_name: 'Primary administrator', value: 'admin'},
    {customer_name: 'Administrator', value: 'normal_admin'},
    {customer_name: 'Fleet Coordinator', value: 'coordinator'},
    {customer_name: 'Fleet Technician', value: 'maintenance'}
  ];

  $scope.taxDetails = [];
  $scope.editTax = ''
  $scope.editTax = ''

    $scope.fleetTypeSelected = function (type) {
      $scope.fleetType = type
      const currentFleet = JSON.parse(localStorage.getItem('currentFleet'))
      $scope.fieldChanged = currentFleet.type !== type.value
    }

    $scope.changeFleetTypeModal = function () {
      ngDialog.open({
        template: '../html/modals/change-fleet-type.html',
        controller: 'changeFleetTypeModalController'
      })
    }

    function activateReservationSettings () {
      $scope.activatingSettings = true
      ngDialog.openConfirm({
        showClose: false,
        template: '../html/modals/reservations.html',
        controller: 'enableReservationsModalController'
      }).then(function (confirmed) {
        if (!confirmed) {
          $scope.activatingSettings = false
          $scope.status.reservationsEnabled = false

          return
        }

        myFleetFactory.activateReservationSettings($scope.reservation_settings.fleet_id)
          .then(res => {
            $scope.reservation_settings = res.payload
            $scope.status.reservationsEnabled = true
            $scope.activatingSettings = false

            notify({
              message: 'Reservation settings activated.',
              duration: 2000,
              position: 'right'
            })
          }).catch(function (error) {
            $scope.activatingSettings = false
            $scope.status.reservationsEnabled = false

            notify({
              message: 'Failed to activate reservation settings.',
              duration: 2000,
              position: 'right'
            })
          })
      }).catch(function () {
        $scope.activatingSettings = false
        $scope.status.reservationsEnabled = false
      })
    }

    function deactivateReservationSettings () {
      $scope.activatingSettings = true
      myFleetFactory.deactivateReservationSettings($scope.reservation_settings.fleet_id)
        .then(res => {
          $scope.reservation_settings = res.payload
          $scope.status.reservationsEnabled = false
          $scope.activatingSettings = false

          notify({
            message: 'Reservation settings deactivated.',
            duration: 2000,
            position: 'right'
          })
        }).catch(function (error) {
          $scope.activatingSettings = false
          $scope.status.reservationsEnabled = false

          notify({
            message: 'Failed to deactivate reservation settings.',
            duration: 2000,
            position: 'right'
          })
        })
    }

    $scope.handleClick = function (option) {
      if (option && $scope.reservation_settings.deactivation_date) {
        activateReservationSettings()
      } else if (!option && $scope.reservation_settings.deactivation_date === null) {
        deactivateReservationSettings()
      } else {
        $scope.status.reservationsEnabled = option
      }
    }

    $scope.handleUpdates = function () {
      $scope.updateReservationSettings = true
      $scope.changeAppSettings = true
    }

    $scope.changeAppSettings = false
    $scope.changeSettings = function() {
      $scope.changeAppSettings = $scope.oldGps !== $scope.pricingdata.gps ||
        $scope.oldTripEmail !== $scope.pricingdata.start_trip_email ||
        $scope.oldPhoneNumber !== $scope.pricingdata.require_phone_number ||
        $scope.oldSmartDockingStations !== $scope.pricingdata.smart_docking_stations_enabled ||
        $scope.oldParkingAreaRestriction !== $scope.pricingdata.parking_area_restriction;
    }
    angular.element('#enable_reservations_off').trigger('click')

    $rootScope.$on('changeFleetType', function () {
      $scope.saveProfileSettings()
    })

    $scope.showDropDown = false
    $scope.addIntegrations = function () {
      $scope.showDropDown = !$scope.showDropDown
    }
    $scope.showNewTaxTextField = false
    $scope.showTaxField = function () {
      $scope.showNewTaxTextField = !$scope.showNewTaxTextField
    }
    $scope.show = false;
    $scope.visible = function () {
      $scope.show = !$scope.show
    }
    $scope.isEdittingTax = false
    $scope.editingTaxId;
    $scope.taxDetails = []
    const getTaxDetails = function () {
      const fleet_id = JSON.parse(localStorage.getItem('currentFleet')).fleet_id;
      profileSettingFactory.getTaxWithFleetId({ fleet_id:fleet_id }).then((response) => {
        const taxInfo = response && response.data && response.data.payload
          if (taxInfo && taxInfo.length){
            const result = taxInfo.map((item) => {
              return { tax_name: item.tax_name, tax_percent: +item.tax_percent, isEdittingTax: false, tax_id: item.tax_id }
            })
            $scope.taxDetails = result;
          }
      }).catch((error) => {
        notify({
          message: 'An error occured getting tax details',
          duration: 2000,
          position: 'right'
        })
      })
    }
    getTaxDetails();
    $scope.taxUpdates ={
      editingTaxId: null
    }

    $scope.updateTax = function(taxId, cancel, save, taxName, taxPercent) {
      if(cancel) {
        $scope.taxUpdates.editingTaxId = null
        return
      }
      const tax = $scope.taxDetails.find(detail=> detail.tax_id === taxId)
      if(!$scope.taxUpdates.editingTaxId || $scope.taxUpdates.editingTaxId !== taxId) {
        $scope.taxUpdates.editingTaxId = taxId
      } else if (taxId === $scope.taxUpdates.editingTaxId) {
        const data = {tax_id: taxId, tax_name: taxName, tax_percent: taxPercent}
        profileSettingFactory.updateTax(data).then(result=> {
          $scope.taxUpdates.editingTaxId = null
          notify({
            message: 'Tax value updates successfully',
            duration: 2000,
            position: 'right'
          })
          const data = result.data.payload
          const taxUpdates = { tax_name: data.tax_name, tax_percent: +data.tax_percent }
          $scope.taxDetails = $scope.taxDetails.map(d=> {
            if(d.tax_id === taxId) return { ...d, ...taxUpdates }
            return d
          })
          $scope.$apply()
        }).catch(err=> {
          console.log('An error occurred updating tax', JSON.stringify(err))
          notify({
            message: 'Failed to update tax.',
            duration: 2000,
            position: 'right'
          })
          $scope.taxUpdates.editingTaxId = null
          $scope.$apply()
        })
      }
    }

   $scope.update = function (data) {
     profileSettingFactory.updateTax(data).then((res) => {
       if(res.statusText === "OK"){
        getTaxDetails();
        notify({
          message: 'update Tax success.',
          duration: 2000,
          position: 'right'
        })
       }
     }).catch((error) => {
       if(error){
        notify({
          message: 'Failed to save tax.',
          duration: 2000,
          position: 'right'
        })
       }
     });
     getTaxDetails()
   }
  $scope.tax = {};
  $scope.savingTax = false
  $scope.saveTax =   function () {
    const fleet_id = JSON.parse(localStorage.getItem('currentFleet')).fleet_id;
    const  obj = {tax_percent: $scope.tax.tax_percent, tax_name: $scope.tax.tax_name, fleet_id: fleet_id};

      profileSettingFactory.saveTax(obj).then(res => {
        const newTax = res.data.payload
        newTax.tax_percent = +newTax.tax_percent
        $scope.taxDetails.push(newTax)
        $scope.showNewTaxTextField = false;
        notify({
          message: 'New tax added susccessfully',
          duration: 2000,
          position: 'right'
        })
    }).catch(function (error) {
      notify({
        message: 'Failed to save tax.',
        duration: 2000,
        position: 'right'
      })
    })
  }

  $scope.deactivateTax = function (taxId) {
    console.log(taxId)
    profileSettingFactory.deactivateTaxWithTaxId({ taxId }).then((res) => {
      $scope.taxDetails = $scope.taxDetails.filter(d => d.tax_id !== taxId)
      notify({
        message: 'delete Tax success.',
        duration: 2000,
        position: 'right'
      })
    }).catch((error) => {
      notify({
        message: 'Failed to deactivate tax.',
        duration: 2000,
        position: 'right'
      })
    })
  }

  $scope.moduleConstants = iotModuleConstants
  $scope.selectedModule = function (item) {
    $scope.selectedItem = item
    if ($scope.selectedItem.type === 'grow' && $scope.integrationTypes.includes('grow')) {
      const integration = $scope.integrations.find(int => int.integration_type === 'grow')
      let metadata = integration && integration.metadata
      if (metadata) {
        $scope.growDetails = metadata
      }
    }
  }


  $scope.moduleTaxConstants = [{name:"GST",id:1},{name:"SGST" , id:2},{name:"CGST",id:3}];
  $scope.selectedTaxModule = function (item) {
    $scope.selectedTaxItem = item
    if ($scope.selectedTaxItem){

    }
  }

    $scope.signIntoScout = async (data) => {
      $scope.scoutDetails = data
      data.fleet_id = rootScopeFactory.getData('fleetId')
      const scoutIntegrationData = await profileSettingFactory.loginScoutUser(data)
      if (scoutIntegrationData) {
        $scope.integrationTypes.push(scoutIntegrationData.payload.integration_type)
        notify({
          message: 'Successfully logged in scout user',
          duration: 2000,
          position: 'right'
        })
      } else {
        notify({
          message: 'Failed to login scout user',
          duration: 2000,
          position: 'right'
        })
      }
    }

    $scope.saveKisiAPIKey = async (data) => {
      console.log('Kisi API key saving data:::', data, $scope.kisiDetails)
      try {
        const data = { apiKey: $scope.kisiDetails.apiKey, integrationType: 'kisi', fleetId: rootScopeFactory.getData('fleetId') }
        const response = await profileSettingFactory.saveKisiSettings(data)
        $scope.integrationTypes.push(response.payload.integrationType)
        notify({
          message: 'Successfully added Kisi integration',
          duration: 2000,
          position: 'right'
        })
      } catch (error) {
        console.log('Error::::', error)
        notify({
          message: `An error occurred saving Kisi integration`,
          duration: 2000,
          position: 'right'
        })
      }
    }
    $scope.saveEdgeAPIKey = async (data) => {
      console.log('Edge API key saving data:::', data, $scope.EdgeDetails)
      try {
        const data = { apiKey: $scope.EdgeDetails.apiKey, integrationType: 'Edge', fleetId: rootScopeFactory.getData('fleetId') }
        const response = await profileSettingFactory.saveEdgeSettings(data)
        $scope.integrationTypes.push(response.payload.integrationType)
        notify({
          message: 'Successfully added Edge integration',
          duration: 2000,
          position: 'right'
        })
      } catch (error) {
        console.log('Error::::', error)
        notify({
          message: `An error occurred saving Edge integration`,
          duration: 2000,
          position: 'right'
        })
      }
    }
    $scope.authorizeGeotab = async (data) => {
      $scope.geotabDetails = data
      data.fleet_id = rootScopeFactory.getData('fleetId')
      const integrationData = await profileSettingFactory.authenticateGeotabUser(data)
      if (integrationData) {
        $scope.integrationTypes.push(integrationData.payload.integration_type)
        notify({
          message: 'Successfully logged in geotab user',
          duration: 2000,
          position: 'right'
        })
      } else {
        notify({
          message: 'Failed to login geotab user',
          duration: 2000,
          position: 'right'
        })
      }
    }

    $scope.registerLinka = async (data) => {
      $scope.linkaDetals = data
      data.fleetId = rootScopeFactory.getData('fleetId')
      const linkaIntegrationData = await profileSettingFactory.registerLinka(data)
      if (linkaIntegrationData) {
        $scope.integrationTypes.push(linkaIntegrationData.payload.integration_type)
        notify({
          message: 'Successfully registered merchant',
          duration: 2000,
          position: 'right'
        })
      } else {
        notify({
          message: 'Failed to register linka merchant',
          duration: 2000,
          position: 'right'
        })
      }
    }

    const truncateAtCode = window.location.search
    if (truncateAtCode) {
      const codeQuery = truncateAtCode.split('&')[0]
      const code = codeQuery.slice(6)
      if (code) {
        const fleetId = rootScopeFactory.getData('fleetId') || JSON.parse(localStorage.getItem('currentFleet')).fleet_id
        const integrationDetails = {
          code: code,
          fleetId: fleetId,
          integrationType: 'tapkey'
        }
        profileSettingFactory.registerIntegration(integrationDetails).then((response) => {
          notify({
            message: 'Successfully registered integration',
            duration: 2000,
            position: 'right'
          })
          window.open(`/#/profile`, '_self')
          localStorage.setItem('innertab', 8)
          $scope.integrationTypes.push(response.payload.integration_type)
        }).catch((error) => {
          localStorage.setItem('innertab', 8)
          notify({
            message: 'Failed to register integration' || error.message,
            duration: 2000,
            position: 'right'
          })
        })
      }
    }

    $scope.registerIntegration = async(data) => {
      let integration
      if ($scope.selectedItem.type === 'tapkey' ) {
        const scope = 'write:ip:users read:ip:users write:core:entities read:core:entities write:grants read:grants read:owneraccounts offline_access'
        const {payload: urls} = await profileSettingFactory.getTapKeyCredentials('URLS')
        const {payload: credentials } = await profileSettingFactory.getTapKeyCredentials('KEYS')
        const [environment] = urls.filter(url => url.Name === 'environment')
        const [redirectUri] = urls.filter(url => url.Name === `/env/${environment.Value}/TAP_KEY/URLS/REDIRECT_URI`)
        const [authCodeClientId] = credentials.filter(credential => credential.Name === `/env/${environment.Value}/TAP_KEY/KEYS/TAP_KEY_AUTH_CODE_CLIENT_ID`)

        const integrationData = {
          integrationType: $scope.selectedItem.type
        }
        const data = await window.open(
          `https://login.tapkey.com/connect/authorize?client_id=${authCodeClientId.Value}&redirect_uri=${redirectUri.Value}&response_mode=query&response_type=code&scope=${scope}&offline_access`,
          '_self'
        )
        return
      } else {
        $scope.integrationDetails = data
        data.integrationType = $scope.selectedItem.type
        data.fleetId = rootScopeFactory.getData('fleetId')
        integration = await profileSettingFactory.registerIntegration(data)
      }
      if (integration) {
        $scope.integrationTypes.push(integration.payload.integration_type)
        notify({
          message: 'Successfully registered integration',
          duration: 2000,
          position: 'right'
        })
      } else {
        notify({
          message: 'Failed to register integration',
          duration: 2000,
          position: 'right'
        })
      }
    }

    $scope.saveSettings = function () {
      if ($scope.status.reservationsEnabled && !$scope.reservation_settings.reservation_settings_id) {
        ngDialog.openConfirm({
          showClose: false,
          template: '../html/modals/reservations.html',
          controller: 'enableReservationsModalController'
        }).then(function (confirmed) {
          if (!confirmed) {
            return
          }

          $scope.updateReservationSettings = false
          $scope.changeAppSettings = false
          $scope.reservationSettings = cleanSettings($scope.reservation_settings)
          $scope.saveAppSettings()
        }).catch(function () { })
      } else {
        $scope.saveAppSettings()
      }
    }

    // helps us strip our input of the zeroes
    function compactObject (data) {
      return Object.keys(data).reduce((compacted, key) => {
        const value = data[key]
        if (value) {
          compacted[key] = value
        }
        return compacted
      }, {})
    }

    function cleanSettings (pricingData) {
      return {
        max_reservation_duration: compactObject(
          pricingData.max_reservation_duration
        ),
        min_reservation_duration: compactObject(
          pricingData.min_reservation_duration
        ),
        booking_window_duration: compactObject(
          pricingData.booking_window_duration
        )
      }
    }

    $scope.saveAppSettings = function () {
      let settings = { fleet_id: rootScopeFactory.getData('fleetId') }
      if ($scope.updateReservationSettings) {
        $scope.reservationSettings = cleanSettings($scope.reservation_settings)
      }
      if ($scope.pricingdata.startRideTime || $scope.pricingdata.shortRide) {
        if ($scope.pricingdata.startRideTime) {
          settings['bike_booking_interval'] = parseInt($scope.pricingdata.startRideTime) * 60
        }
        if ($scope.pricingdata.shortRide) {
          settings['skip_parking_image'] = parseInt($scope.pricingdata.shortRide)
        }
        if ($scope.pricingdata.gps) {
          settings['do_not_track_trip'] = parseInt($scope.pricingdata.gps)
        }
        if ($scope.pricingdata.start_trip_email) {
          settings['email_on_start_trip'] = parseInt($scope.pricingdata.start_trip_email)
        }
        if ($scope.pricingdata.require_phone_number) {
          settings['require_phone_number'] = parseInt($scope.pricingdata.require_phone_number)
        }
        if ($scope.pricingdata.parking_area_restriction) {
          settings['parking_area_restriction'] = parseInt($scope.pricingdata.parking_area_restriction)
        }

        if ($scope.pricingdata.smart_docking_stations_enabled) {
          settings['smart_docking_stations_enabled'] = parseInt($scope.pricingdata.smart_docking_stations_enabled)
        }

        if ($scope.reservationSettings) {
          settings['reservation_settings'] = $scope.reservationSettings
        }

        profileSettingFactory.updateAppSettings(settings, function (res) {
          if (res && res.status === 200) {
            notify({
              message: 'App settings has been updated successfully',
              duration: 2000,
              position: 'right'
            })
          } else {
            notify({
              message: 'Failed to update app settings',
              duration: 2000,
              position: 'right'
            })
          }
        })

        if ($scope.status.reservationsEnabled && $scope.updateReservationSettings && settings.reservation_settings) {
          myFleetFactory.updateReservationSettings(
            settings,
            $scope.reservation_settings.reservation_settings_id,
            function (res) {
              if (res && res.status === 200) {
                $scope.changeAppSettings = false
                notify({
                  message: 'Reservation settings have been updated successfully',
                  duration: 2000,
                  position: 'right'
                })
              } else {
                const message = error => {
                  const msg = 'Failed to update reservation settings'
                  if (Array.isArray(error)) {
                    return error[0].message || msg
                  }

                  return error.message || msg
                }

                notify({
                  message: message(res.data.error || res.data),
                  duration: 4000,
                  position: 'right'
                })
              }
            }
          )
        }

        if ($scope.status.reservationsEnabled && !$scope.reservation_settings.reservation_settings_id) {
          myFleetFactory.addReservationSettings(settings, function (res) {
            if (res && res.status === 201) {
              $scope.reservation_settings = res.payload
              notify({
                message: 'Reservation settings added successfully',
                duration: 2000,
                position: 'right'
              })
            } else if (res && res.status === 409) {
              notify({
                message: res.error.message,
                duration: 4000,
                position: 'right'
              })
            } else {
              notify({
                message: 'Failed to add reservation settings',
                duration: 2000,
                position: 'right'
              })
            }
          })
        }
      }
    }
    $scope.permissionData = {}
    $scope.pricingdata.skip_parking_image = '0'

    $scope.profileTimezone = timezoneConstants

    $scope.currencyList = _.keys(lattisConstants.currencyCodeSymbolMap)
      .map(function (code) { return { value: code } })

    $scope.selectedZone = {}
    $scope.selectedCountry = {}
    $scope.selectedCurrency = {}
    $scope.selectedZone.name = $scope.profileTimezone[28].text
    $scope.selectedZone.value = $scope.profileTimezone[28].value
    $scope.selectedCurrency.value = $scope.currencyList[0].value

    $scope.distancePreferences = ['miles', 'kilometers']
    $scope.distancePreference = $scope.distancePreferences[0]

    $scope.zoneSelected = function (i) {
      $scope.selectedZone.name = i.text
      $scope.selectedZone.value = i.value
    }

    $scope.currencySelected = function (i) {
      $scope.selectedCurrency.value = i.value
    }

    $scope.preferenceSelected = function (preference) {
      $scope.distancePreference = preference
    }

    $scope.openImageUpload = function () {
      ngDialog.open({
        template: '../../html/modals/upload-image.html',
        controller: 'selectImageController'
      })
    }

    $rootScope.$on('fleetProfileUploadCompleted', (event, uploadedFile) => {
      $scope.uploadedFile = uploadedFile

    })

    // handle the upload of the logo after modal disappears
    $rootScope.$on('saveFleetLogo', async () => {
      $rootScope.showLoader = true
      const uploadedImageDetails = $scope.uploadedFile[1]
      uploadedImageDetails.fleetId = rootScopeFactory.getData('fleetId')
      let formData = new FormData()
      formData.append('pic', $scope.uploadedFile[1])
      formData.append('fleetId', rootScopeFactory.getData('fleetId'))
      try {
        const { data } = await $http.post('api/fleet/update-logo', formData, {headers: {'Content-Type': undefined}})
        $scope.fleetDetails.logo = data.payload.link
        $rootScope.showLoader = false
        notify({
          message: 'Fleet logo updated',
          duration: 2000,
          position: 'right',
        });
      } catch (error) {
        $rootScope.showLoader = false
        notify({
          message: `Fleet logo update failed: ${error.message}`,
          duration: 2000,
          position: 'right',
        });
      }
    })

    $scope.savePermission = function () {
      let maxTripLength
      $scope.pricingdata.restriction === 'Yes' ? maxTripLength = 0 : maxTripLength = parseInt($scope.restrictionLimit) *
        parseInt($scope.pricingdata.restriction_trip_length.mulVal)
      if ($scope.permissionData.availability === 'Yes') {
        $scope.permissionData.bike_available_from = 0
        $scope.permissionData.bike_available_till = 1440
      } else {
        $scope.permissionData.bike_available_from = bike_available_start
        $scope.permissionData.bike_available_till = bike_available_end
      }
      profileSettingFactory.updateAppSettings({
        fleet_id: rootScopeFactory.getData('fleetId'),
        skip_parking_image: $scope.pricingdata.skip_parking_image,
        bike_available_from: $scope.permissionData.bike_available_from,
        bike_available_till: $scope.permissionData.bike_available_till,
        fleet_timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        max_trip_length: maxTripLength
      }, function (res) {
        if (res && res.status === 200) {
          notify({
            message: 'Permission has been updated successfully',
            duration: 2000,
            position: 'right'
          })
        } else {
          notify({
            message: 'Failed to update permission',
            duration: 2000,
            position: 'right'
          })
        }
      })
    }

    if (rootScopeFactory.getData('fleetId')) {
      fetchFleetProfile(rootScopeFactory.getData('fleetId'))
    }
    $('body').bind('mousewheel', function () {
      $('#timezone-scroll').niceScroll({ autohidemode: true, zindex: 999, smoothscroll: true, mousescrollstep: 40 }).resize()
    })
    $scope.currentOperator = sessionFactory.getCookieId()
    $scope.delUser = function (id) {
      profileSettingFactory.deleteOperator({ operator_id: id, fleet_id: rootScopeFactory.getData('fleetId') },
        function (response) {
          if (response && response.status === 200) {
            notify({
              message: 'Operator has been removed form the fleet successfully',
              duration: 2000,
              position: 'right'
            })
            profileSettingFactory.clearOnCallCache()
            $timeout(function () {
              fetchFleetProfile(rootScopeFactory.getData('fleetId'))
            }, 500)
          } else {
            notify({
              message: 'Failed to remove the operator from the fleet',
              duration: 2000,
              position: 'right'
            })
          }
        })
    }
    $scope.$on('fleetChange', function (event, id) {
      fetchFleetProfile(id)
    })

    function toggleValidation (condition) {
      if (condition) {
        $scope.form.firstname.$setDirty()
        $scope.form.lastname.$setDirty()
        $scope.form.tel.$setDirty()
        $scope.form.email.$setDirty()
      } else {
        $scope.form.firstname.$setPristine()
        $scope.form.lastname.$setPristine()
        $scope.form.tel.$setPristine()
        $scope.form.email.$setPristine()
      }
    }

    $('#slider-range').slider({
      range: true,
      min: 0,
      max: 1440,
      step: 60,
      values: [bike_available_start, bike_available_end],
      slide: function (e, ui) {
        var hours1 = Math.floor(ui.values[0] / 60)
        var minutes1 = ui.values[0] - (hours1 * 60)

        if (minutes1.length === 1) minutes1 = '0' + minutes1
        if (minutes1 === 0) minutes1 = '00'
        if (hours1 >= 12) {
          if (hours1 === 12) {
            hours1 = hours1
            // minutes1 = minutes1 + " pm";
            $scope.$apply(function () {
              $scope.dayFormat = 'pm'
            })
          } else {
            hours1 = hours1 - 12
            // minutes1 = minutes1 + " pm";
            $scope.$apply(function () {
              $scope.dayFormat = 'pm'
            })
          }
        } else {
          hours1 = hours1
          // minutes1 = minutes1 + " am";
          $scope.$apply(function () {
            $scope.dayFormat = 'am'
          })
        }
        if (hours1 === 0) {
          hours1 = 12
          minutes1 = minutes1
        }
        if (hours1.length === 1) hours1 = '0' + hours1
        bike_available_start = ui.values[0]
        $('.slider-time').html(hours1 + ':' + minutes1)

        var hours2 = Math.floor(ui.values[1] / 60)
        var minutes2 = ui.values[1] - (hours2 * 60)

        if (minutes2.length === 1) minutes2 = '0' + minutes2
        if (minutes2 == 0) minutes2 = '00'
        if (hours2 >= 12) {
          if (hours2 === 12) {
            hours2 = hours2
            $scope.$apply(function () {
              $scope.noonFormat = 'pm'
            })
            // minutes2 = minutes2 + " pm";
          } else if (hours2 === 24) {
            hours2 = 11
            $scope.$apply(function () {
              $scope.noonFormat = 'pm'
            })
            // minutes2 = "59 pm";
          } else {
            hours2 = hours2 - 12
            $scope.$apply(function () {
              $scope.noonFormat = 'pm'
            })
            // minutes2 = minutes2 + " pm";
          }
        } else {
          hours2 = hours2
          $scope.$apply(function () {
            $scope.noonFormat = 'am'
          })
          // minutes2 = minutes2 + " am";
        }
        if (hours2.length === 1) hours2 = '0' + hours2
        $('.slider-time2').html(hours2 + ':' + minutes2)
        bike_available_end = ui.values[1]
      }
    })

    function isPaymentFleet (fleetType) {
      return !fleetType.includes('no_payment')
    }
    $scope.saveProfileSettings = function () {
    $rootScope.showLoader = true
      if (!$scope.form.$pristine || $scope.fleetDetails.fleet_logo ||
        !_.isEqual($scope.oldSelectedZone, $scope.selectedZone) ||
        !_.isEqual($scope.oldSelectedCurrency, $scope.selectedCurrency) ||
        !_.isEqual($scope.oldDistancePreference, $scope.distancePreference) ||
        !_.isEqual($scope.oldFleetType.value, $scope.fleetType.value)) {
        toggleValidation(true)
        if (_.isEmpty($scope.form.$error.required) &&
          _.isEmpty($scope.form.$error.pattern)) {
          let formData = new FormData()
          formData.append('fleet_id', rootScopeFactory.getData('fleetId'))
          formData.append('customer_name', $scope.fleetDetails.customer_name)
          formData.append('email', $scope.fleetDetails.contact_email)
          formData.append('first_name', $scope.fleetDetails.contact_first_name)
          formData.append('last_name', $scope.fleetDetails.contact_last_name)
          formData.append('fleet_name', $scope.fleetDetails.fleet_name)
          formData.append('fleet_weblink', $scope.fleetDetails.contact_web_link)
          formData.append('phone_number', $scope.fleetDetails.contact_phone.replace(/\s/g, ''))
          formData.append('fleet_timezone', $scope.selectedZone.value)
          formData.append('distance_preference', $scope.distancePreference)
          formData.append('currency', $scope.selectedCurrency.value)
          formData.append('country_code', null)
          let type = $scope.fleetType.value
          if (isPaymentFleet($scope.oldFleetType.value)) {
            type = $scope.fleetType.value
          } else if ($scope.fieldChanged) {
            type = `${$scope.fleetType.value}_no_payment`
          }
          formData.append('type', type)
          profileSettingFactory.updateFleetProfile(formData, async function (response) {
            if (response && response.status === 200) {
              try {
                let fleetSwapResponse
                const fleetId  = rootScopeFactory.getData('fleetId')
                if ($scope.fleetType.value === 'private' && $scope.fieldChanged) {
                  fleetSwapResponse = await usersFactory.revokeAccessForAllFleetMembers({fleetId: fleetId})
                  $rootScope.$broadcast('fleetTypeUpdated', $scope.fleetType)
                  $rootScope.fleetType = $scope.fleetType
                  $scope.fieldChanged = false
                } else {
                  if ($scope.fieldChanged) {
                    fleetSwapResponse = await bikeFleetFactory.deleteFleetDomains({fleetId: fleetId})
                  }
                }
                if (fleetSwapResponse) {
                  notify({ message: 'Fleet type changed successfully', duration: 2000, position: 'right' })
                }
              } catch (error) {
                let message = 'Failed to change fleet type'
                $rootScope.showLoader = false
                notify({ message, duration: 2000, position: 'right' })
              }
              if (rootScopeFactory.getData('fleetId')) {
                fetchFleetProfile(rootScopeFactory.getData('fleetId'))
                profileSettingFactory.getFleetProfile({
                  operator_id: sessionFactory.getCookieId(),
                  fleet_id: rootScopeFactory.getData('fleetId')
                })
                  .then(res => {
                    const fleet = res.payload.find(fleet => fleet.fleet_id === rootScopeFactory.getData('fleetId'))
                    if (fleet) {
                      fleet.fleetFullName = fleet.customer_name + ' - ' + fleet.fleet_name
                      localStorage.setItem('currentFleet', JSON.stringify(fleet))
                      $scope.fleetType = {
                        name: utilsFactory.startCapitalize(fleet.type),
                        value: fleet.type
                      }
                    }
                  })
              }
              $rootScope.showLoader = false
              notify({ message: 'Fleet profile updated successfully', duration: 2000, position: 'right' })
            } else {
              let message = 'Failed to update fleet profile'
              $rootScope.showLoader = false
              if (response && response.data && response.data.error && response.data.error.message) {
                if (response.data.error.message === 'Duplicate fleet name') {
                  message += `. ${response.data.error.message}`
                }
              }
              notify({ message, duration: 2000, position: 'right' })
            }
          })
        }
      }
    }

    function toggleCardValidation () {
      $scope.billForm.card_number.$setDirty()
      $scope.billForm.expires.$setDirty()
      $scope.billForm.cvv.$setDirty()
    }

    function toggleAddressValidation () {
      $scope.billForm.billaddress.$setDirty()
      $scope.billForm.city.$setDirty()
      $scope.billForm.zip.$setDirty()
      $scope.billForm.country.$setDirty()
      $scope.billForm.state.$setDirty()
    }

    $scope.saveCard = function () {
      if ($scope.billForm.card_number.$dirty && $scope.billForm.card_number.$valid &&
        $scope.billForm.expires.$dirty && $scope.billForm.expires.$valid &&
        $scope.billForm.cvv.$dirty && $scope.billForm.cvv.$valid) {
        $rootScope.showLoader = true
        if (_.isString($scope.billingAddress.expires)) {
          var splitExpiry = $scope.billingAddress.expires.split('/')
        }
        paymentFactory.createFleetPaymentProfile({
          operator_id: sessionFactory.getCookieId(),
          fleet_id: rootScopeFactory.getData('fleetId'),
          cc_no: $scope.billingAddress.card_number,
          exp_month: splitExpiry[0],
          exp_year: splitExpiry[1],
          cvc: $scope.billingAddress.cvv
        }, function (response) {
          if (response && response.status === 200) {
            $scope.cardField = !$scope.cardField
            $rootScope.showLoader = false
            $timeout(function () {
              fetchCards(rootScopeFactory.getData('fleetId'))
            }, 500)
            notify({ message: 'Added the card to the database', duration: 2000, position: 'right' })
          } else {
            $rootScope.showLoader = false
            notify({ message: 'Failed to add card to the database', duration: 2000, position: 'right' })
          }
        })
      } else {
        toggleCardValidation()
      }
    }

    $scope.saveAddress = function () {
      if ($scope.billForm.$dirty && $scope.billForm.billaddress.$valid &&
        $scope.billForm.city.$valid && $scope.billForm.zip.$valid &&
        $scope.billForm.country.$valid && $scope.billForm.state.$valid) {
        $rootScope.showLoader = true
        if ($scope.addressId) {
          paymentFactory.editFleetBillingAddress({
            address_id: $scope.addressId,
            fleet_id: rootScopeFactory.getData('fleetId'),
            address1: $scope.billingAddress.address,
            address2: '',
            city: $scope.billingAddress.city,
            postal_code: $scope.billingAddress.zip,
            country: $scope.billingAddress.country,
            state: $scope.billingAddress.state
          }, function (response) {
            if (response && response.status === 200) {
              $scope.addressDisabled = true
              $rootScope.showLoader = false
              notify({ message: 'Updated the billing address', duration: 2000, position: 'right' })
            } else {
              $rootScope.showLoader = false
              notify({
                message: 'Failed to update the billing address',
                duration: 2000,
                position: 'right'
              })
            }
          })
        } else {
          paymentFactory.createFleetBillingAddress({
            fleet_id: rootScopeFactory.getData('fleetId'),
            address1: $scope.billingAddress.address,
            address2: '',
            city: $scope.billingAddress.city,
            postal_code: $scope.billingAddress.zip,
            country: $scope.billingAddress.country,
            state: $scope.billingAddress.state
          }, function (response) {
            if (response && response.status === 200) {
              $scope.addressDisabled = true
              $rootScope.showLoader = false
              notify({ message: 'Added the billing address', duration: 2000, position: 'right' })
            } else {
              $rootScope.showLoader = false
              notify({ message: 'Failed to add the billing address', duration: 2000, position: 'right' })
            }
          })
        }
      } else {
        toggleAddressValidation()
      }
    }

    $scope.cancelAddress = function () {
      $scope.addressDisabled = true
    }

    /* New Fleet profile */
    function fetchFleetProfile (fleetId) {
      profileSettingFactory.getFleetProfile({
        operator_id: sessionFactory.getCookieId(),
        fleet_id: fleetId
      }).then(function (response) {
        $scope.fleetDetails = _.findWhere(response.payload, { fleet_id: fleetId })
        if(['private','public'].includes($scope.fleetDetails.type)) {
          $scope.inner_menu_Items.push({
            id:9,
            title:'Tax'
          })
        }
        $scope.fleetDetails.disableCurrency = $scope.fleetDetails.type.includes('no_payment')
        $scope.fleetDetails.contract_file ? $scope.fleetDetails.contract_file_text =
          $scope.fleetDetails.contract_file.split('/').pop()
          : $scope.fleetDetails.contract_file_text = 'No contract file'
        let gotTimezone = _.findWhere(timezoneConstants, { value: $scope.fleetDetails.fleet_timezone })
        if (!_.isEmpty(gotTimezone)) $scope.selectedZone = { name: gotTimezone.text, value: gotTimezone.value }
        if ($scope.fleetDetails.currency) $scope.selectedCurrency.value = $scope.fleetDetails.currency
        $scope.oldSelectedZone = angular.copy($scope.selectedZone)
        $scope.oldSelectedCurrency = angular.copy($scope.selectedCurrency)
        $scope.oldDistancePreference = $scope.fleetDetails.distance_preference
        $scope.distancePreference = $scope.fleetDetails.distance_preference
        $scope.contact_web_link = $scope.fleetDetails.contact_web_link
        if ($scope.fleetDetails && $scope.fleetDetails.contact_phone) {
          $scope.$emit('contactPhoneAvailable', $scope.fleetDetails.contact_phone)
        }

        if ($scope.fleetDetails){
          const phoneInput = document.querySelector('#phone')
          let iti = window.intlTelInput(phoneInput, {
            hiddenInput: 'full_phone',
            utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.15/js/utils.js",
          });
          iti.setNumber($scope.fleetDetails.contact_phone)
          phoneInput.addEventListener('blur', function () {
            $scope.phoneNumberIsValid = !!iti.isValidNumber();
            $scope.fleetDetails.contact_phone = iti.getNumber()
            $scope.$apply();
          })
        }
      })

      let bike_groups = []
      bikeFleetFactory.getBikesData({ fleet_id: fleetId }).then(function (res) {
        const bikeDetails = res.payload.bike_data
        const segwayControllers = ['Segway', 'Segway IoT EU']
        if (bikeDetails) {
          const hasGrowVehicles = (bike) => bike.iot_module_type === 'Grow'
          const hasACTONVehicles = (bike) => bike.iot_module_type === 'ACTON'
          const hasOmniIoTVehicles = (bike) => bike.iot_module_type === 'Omni IoT'
          const hasTeltonikaVehicles = (bike) => bike.iot_module_type === 'Teltonika'
          const hasOkaiVehicles = (bike) => bike.iot_module_type === 'Okai'
          $scope.hasGrowVehicles = bikeDetails.some(hasGrowVehicles)
          $scope.hasACTONVehicles = bikeDetails.some(hasACTONVehicles)
          $scope.hasOmniIoTVehicles = bikeDetails.some(hasOmniIoTVehicles)
          $scope.hasTeltonikaVehicles = bikeDetails.some(hasTeltonikaVehicles)
          $scope.hasOkaiVehicles = bikeDetails.some(hasOkaiVehicles)

          $scope.segwayBikes = bikeDetails.filter(bike => segwayControllers.includes(bike.iot_module_type))
          if($scope.segwayBikes.length) {
            $scope.integrationTypes.push('segway')
            $scope.$broadcast('segwayBikesAvailable')
          }
        }
        if (res.payload.bike_data.length) {
          _.each(res.payload.bike_data, function (element) {
            bike_groups.push(element.bike_group_id)
          })
          bike_groups = _.uniq(bike_groups)
        }
        profileSettingFactory.getFleetSchedule({
          fleet_id: fleetId
        }, function (response) {
          $scope.fleetSchedule = angular.copy(response.payload)
          _.each($scope.fleetSchedule, function (element) {
            element.date_created = utilsFactory.dateStringToDate(element.date_created)
            if ($scope.distancePreference === 'miles') {
              element['show_schedule'] = utilsFactory.getMiles(element.maintenance_schedule)
            } else {
              element['show_schedule'] = utilsFactory.meterToKm(element.maintenance_schedule)
            }
          })
        })
      })

      profileSettingFactory.getReservationSettings(fleetId, function (response) {
        if (response && response.payload) {
          $scope.reservation_settings = response.payload
          $scope.status.reservationsEnabled = response.payload.deactivation_date === null
        } else {
          $scope.status.reservationsEnabled = false
        }
      })

      profileSettingFactory.getOnCall(fleetId).then(function (response) {
        $scope.operatorData = response.payload
        _.each($scope.operatorData, function (element) {
          if(!element.username || !element.email) return;
          element['full_name'] = utilsFactory.startCapitalize(element.first_name) + ' ' + utilsFactory.startCapitalize(element.last_name)
          if (element.acl === 'coordinator') {
            element['role'] = 'Fleet Coordinator'
          } else if (element.acl === 'maintenance') {
            element['role'] = 'Fleet Technician'
          } else if (element.acl === 'admin') {
            element['role'] = 'Primary administrator'
          } else if (element.acl === 'normal_admin') {
            element['role'] = 'Administrator'
          }
        })

        if (_.isEmpty(_.where($scope.operatorData, { on_call: 1 }))) {
          $scope.onCalloperator = [{ full_name: 'No on call user' }]
        } else {
          $scope.onCalloperator = _.where($scope.operatorData, { on_call: 1 })
        }
      })

      fetchCards(fleetId)

      paymentFactory.getFleetBillingAddress(fleetId, function (response) {
        if (response && response.payload.length) {
          $scope.addressId = response.payload[0].address_id
          $scope.addressDisabled = true
          $scope.billingAddress.state = response.payload[0].state
          $scope.billingAddress.country = response.payload[0].country
          $scope.billingAddress.zip = response.payload[0].postal_code
          $scope.billingAddress.city = response.payload[0].city
          $scope.billingAddress.address = response.payload[0].address1
        } else {
          $scope.addressId = null
          $scope.addressDisabled = false
        }
      })

      profileSettingFactory.getAppSettings({ fleet_id: rootScopeFactory.getData('fleetId') }, function (res) {
        if (res && res.payload) {
          addFleetFactory.getFleetWithId({ fleet_id: res.payload.fleet_id }, (response) => {
            let name = response.payload.type
            if (response.payload.type === 'private_no_payment') {
              name = 'Private'
            } else if (response.payload.type === 'public_no_payment') {
              name = 'Public'
            }
            $scope.fleetType = {
              name: utilsFactory.startCapitalize(name),
              value: response.payload.type
            }

            $rootScope.fleetType = $scope.fleetType
            $scope.oldFleetType = angular.copy($scope.fleetType)

            if (_.has(response.payload, 'parking_area_restriction') && response.payload.parking_area_restriction !== null) {
              $scope.pricingdata.parking_area_restriction = response.payload.parking_area_restriction.toString()
            }
          })

          if (res.payload.smart_docking_stations_enabled !== null) {
            $scope.pricingdata.smart_docking_stations_enabled = res.payload.smart_docking_stations_enabled.toString()
          }

          if (_.has(res.payload, 'max_trip_length')) {
            if (res.payload.max_trip_length === 0 || res.payload.max_trip_length == null) {
              $scope.restrictionLimit = ''
              $scope.pricingdata.restriction = 'Yes'
              $scope.pricingdata.restriction_trip_length = $scope.rideDeposit[0]
            } else if (res.payload.max_trip_length < 60) {
              $scope.pricingdata.restriction = 'No'
              $scope.restrictionLimit = res.payload.max_trip_length
              $scope.pricingdata.restriction_trip_length = $scope.rideDeposit[0]
            } else if (res.payload.max_trip_length <= 1440) {
              $scope.pricingdata.restriction = 'No'
              $scope.restrictionLimit = res.payload.max_trip_length / 60
              $scope.pricingdata.restriction_trip_length = $scope.rideDeposit[1]
            } else {
              $scope.pricingdata.restriction = 'No'
              $scope.restrictionLimit = res.payload.max_trip_length / 1440
              $scope.pricingdata.restriction_trip_length = $scope.rideDeposit[2]
            }

            bike_available_start = res.payload.bike_available_from
            bike_available_end = res.payload.bike_available_till
            if (res.payload.skip_parking_image !== null && res.payload.skip_parking_image >= 0) {
              $scope.pricingdata.skip_parking_image =
                res.payload.skip_parking_image.toString()
            }
            if (bike_available_start === 0 && bike_available_end === 1440) {
              $scope.permissionData.availability = 'Yes'
            } else {
              $scope.permissionData.availability = 'No'
            }
            $('#slider-range').slider('option', 'values', [bike_available_start, bike_available_end])
            timeSetting(bike_available_start, bike_available_end)
          }

          if (_.has(res.payload, 'skip_parking_image') && res.payload.skip_parking_image !== null) {
            $scope.pricingdata.shortRide = res.payload.skip_parking_image.toString()
          }
          if (_.has(res.payload, 'do_not_track_trip') && res.payload.do_not_track_trip !== null) {
            $scope.pricingdata.gps = res.payload.do_not_track_trip === 1 ? '0' : '1'
          }

          if (_.has(res.payload, 'email_on_start_trip') && res.payload.email_on_start_trip !== null) {
            $scope.pricingdata.start_trip_email = res.payload.email_on_start_trip.toString()
          }

          if (_.has(res.payload, 'require_phone_number') && res.payload.require_phone_number !== null) {
            $scope.pricingdata.require_phone_number = res.payload.require_phone_number.toString()
          }
          if (Number.isFinite(res.payload.bike_booking_min_bound)) {
            $scope.reservation_min = res.payload.bike_booking_min_bound / 60
          }
          if (Number.isFinite(res.payload.bike_booking_max_bound)) {
            $scope.reservation_max = res.payload.bike_booking_max_bound / 60
          }
          if (_.has(res.payload, 'bike_booking_interval') && Number.isFinite(res.payload.bike_booking_interval)) {
            $scope.pricingdata.startRideTime = Math.floor(res.payload.bike_booking_interval / 60)
          } else {
            $scope.pricingdata.startRideTime = Math.floor(res.payload.default_bike_booking_interval / 60)
          }
          $scope.oldGps = angular.copy($scope.pricingdata.gps)
          $scope.oldTripEmail = angular.copy($scope.pricingdata.start_trip_email)
          $scope.oldPhoneNumber = angular.copy($scope.pricingdata.require_phone_number)
          $scope.oldParkingAreaRestriction = angular.copy($scope.pricingdata.parking_area_restriction)
          $scope.oldSmartDockingStations = angular.copy($scope.pricingdata.smart_docking_stations_enabled )
        }
      })

      profileSettingFactory.getFleetIntegrations(rootScopeFactory.getData('fleetId')).then(function (data) {
        $scope.integrations = data.data.payload
        $scope.integrationTypes = $scope.integrations.map(int => int.integration_type) || []

        const actonDetails = $scope.integrations.find(int=> int.integration_type === 'ACTON')
        const omniIoTDetails = $scope.integrations.find(int=> int.integration_type === 'Omni IoT')
        const teltonikaDetails = $scope.integrations.find(int=> int.integration_type === 'Teltonika')
        const OkaiDetails = $scope.integrations.find(int=> int.integration_type === 'Okai')

        $scope.actonDetails = actonDetails && actonDetails.metadata
        $scope.omniIoTDetails = omniIoTDetails && omniIoTDetails.metadata
        $scope.teltonikaDetails = teltonikaDetails && teltonikaDetails.metadata
        $scope.OkaiDetails = OkaiDetails && OkaiDetails.metadata

        if ($scope.actonDetails) $scope.initialACTONMetaInfo = { ...actonDetails.metadata }
        else {
          $scope.initialACTONMetaInfo = { 'fleetId': rootScopeFactory.getData('fleetId'), 'maximumSpeedLimit': 20, 'default': true }
          $scope.actonDetails = {...$scope.initialACTONMetaInfo}
          $scope.integrationTypes.push('ACTON')
        }

        if ($scope.omniIoTDetails) $scope.initialOmniMetaInfo = { ...omniIoTDetails.metadata }
        else {
          $scope.initialOmniMetaInfo = { 'fleetId': rootScopeFactory.getData('fleetId'), 'maximumSpeedLimit': 20, 'default': true }
          $scope.omniIoTDetails = {...$scope.initialOmniMetaInfo}
          $scope.integrationTypes.push('Omni IoT')
        }

        if ($scope.teltonikaDetails) $scope.initialTeltonikaMetaInfo = { ...teltonikaDetails.metadata }
        else {
          $scope.initialTeltonikaMetaInfo = { 'fleetId': rootScopeFactory.getData('fleetId'), 'maximumSpeedLimit': 20, 'default': true }
          $scope.teltonikaDetails = {...$scope.initialTeltonikaMetaInfo}
          $scope.integrationTypes.push('Teltonika')
        }
        if ($scope.OkaiDetails) $scope.initialOkaiMetaInfo = { ...OkaiDetails.metadata }
        else {
          $scope.initialOkaiMetaInfo = { 'fleetId': rootScopeFactory.getData('fleetId'), 'maximumSpeedLimit': 20, 'default': true }
          $scope.OkaiDetails = {...$scope.initialOkaiMetaInfo}
          $scope.integrationTypes.push('Okai')
        }

        $scope.isEdittingACTON = false
        $scope.isEdittingOmniIoT = false
        $scope.isEdittingTeltonika = false
        $scope.isEdittingOkai = false


        const segwayDetails = $scope.integrations.find(int=> int.integration_type === 'segway')
        $scope.segwayDetails = segwayDetails && segwayDetails.metadata
        if (segwayDetails && segwayDetails.metadata) $scope.initialSegwayMetaInfo = { ...segwayDetails.metadata }

        let growDetails = $scope.integrations.find(int=> int.integration_type === 'grow')
        if(growDetails && growDetails.metadata && growDetails.metadata.growMaximumSpeed) growDetails.metadata.growMaximumSpeed = growDetails.metadata.growMaximumSpeed / 10
        $scope.growDetails = growDetails && growDetails.metadata
        $scope.growDrivingModes = { '0': 'ECO', '1': 'NORMAL', '2': 'SPORT' }
        $scope.growBrakeModes = { '0': 'feeble', '1': 'medium', '2': 'strong' }
        if (growDetails && growDetails.metadata) $scope.initialGrowMetaInfo = { ...growDetails.metadata }
        else {
          $scope.initialGrowMetaInfo = { 'fleetId': rootScopeFactory.getData('fleetId'), 'growLED': 1, 'scooterDriveMode': 0, 'growMaximumSpeed': 20, 'scooterBrakeMode': 1, 'default': true }
          $scope.growDetails = {...$scope.initialGrowMetaInfo}
          $scope.integrationTypes.push('grow')
        }
        $scope.isEdittingGrow = false
      }).catch(function (error) {
        console.log('Error:::', error)
      })
    }


    $scope.$on('segwayBikesAvailable', async function () {
      if (!$scope.segwayDetails) {
          //let's save the default values if they are missing
          $scope.segwayDetails = {
            segwayMaximumSpeedLimit: 24,
            speedMode: 2,
            controlType: 1,
            soundMode: 2,
            fleetId: rootScopeFactory.getData('fleetId')
          }
          await profileSettingFactory.saveSegwaySettings($scope.segwayDetails)
          $scope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
      }
    })

    function timeSetting (start, end) {
      var hours1 = Math.floor(start / 60)
      var minutes1 = start - (hours1 * 60)

      if (minutes1.length == 1) minutes1 = '0' + minutes1
      if (minutes1 == 0) minutes1 = '00'
      if (hours1 >= 12) {
        if (hours1 == 12) {
          hours1 = hours1
          $scope.dayFormat = 'pm'
        } else {
          hours1 = hours1 - 12
          $scope.dayFormat = 'pm'
        }
      } else {
        hours1 = hours1
        $scope.dayFormat = 'am'
      }
      if (hours1 == 0) {
        hours1 = 12
        minutes1 = minutes1
      }
      if (hours1.length == 1) hours1 = '0' + hours1
      $('.slider-time').html(hours1 + ':' + minutes1)

      var hours2 = Math.floor(end / 60)
      var minutes2 = end - (hours2 * 60)

      if (minutes2.length == 1) minutes2 = '0' + minutes2
      if (minutes2 == 0) minutes2 = '00'
      if (hours2 >= 12) {
        if (hours2 == 12) {
          hours2 = hours2
          $scope.noonFormat = 'pm'
        } else if (hours2 == 24) {
          hours2 = 11
          $scope.noonFormat = 'pm'
        } else {
          hours2 = hours2 - 12
          $scope.noonFormat = 'pm'
        }
      } else {
        hours2 = hours2
        $scope.noonFormat = 'am'
      }
      if (hours2.length == 1) hours2 = '0' + hours2
      $('.slider-time2').html(hours2 + ':' + minutes2)
    }

    function fetchCards (fleetId) {
      paymentFactory.getFleetCards(fleetId, function (response) {
        if (response && response.status === 200) {
          $scope.fleetCards = response.payload
          $scope.cardField = $scope.fleetCards.length === 0
        }
      })
    }

    $scope.addCard = function () {
      $scope.cardField = !$scope.cardField
    }

    $scope.enableCard = function () {
      $scope.addressDisabled = false
    }

    $scope.fleetSchedule = []
    $scope.initChange = function (index) {
      if ($scope.fleetSchedule[index].select === 'miles') {
        $scope.fleetSchedule[index].show_schedule =
          utilsFactory.roundOff(utilsFactory.kmToMiles($scope.fleetSchedule[index].show_schedule), 2)
      } else {
        $scope.fleetSchedule[index].show_schedule =
          utilsFactory.roundOff(utilsFactory.milesToKm($scope.fleetSchedule[index].show_schedule), 2)
      }
    }

    $scope.$on('uploadCompleted', function (event, data) {
      $scope.newLogo = data[1]
      $scope.newLogoSrc = data[0]
    })

    $scope.$on('logoSaved', function () {
      $scope.fleetDetails.logo = $scope.newLogoSrc
      $scope.fleetDetails.fleet_logo = $scope.newLogo
    })

    $scope.updateOnCall = function () {
      if (!$scope.onCalloperator[0] || $scope.onCalloperator[0].operator_id !== $scope.newOnCall.operator_id) {
        profileSettingFactory.updateOnCall({
          operator_id: $scope.newOnCall.operator_id,
          fleet_id: rootScopeFactory.getData('fleetId')
        }, function (response) {
          if (response && response.status === 200) {
            notify({
              message: 'Updated the on call operator successfully',
              duration: 2000,
              position: 'right'
            })
            profileSettingFactory.clearOnCallCache()
            fetchFleetProfile(rootScopeFactory.getData('fleetId'))
          } else if (response && response.status === 403) {
            notify({
              message: "The selected operator doesn't have phone number",
              duration: 2000,
              position: 'right'
            })
          } else {
            notify({ message: 'Failed to update the on call operator', duration: 2000, position: 'right' })
          }
        })
      }
    }

    $scope.updateACL = function (role, operator_id) {
      console.log({role})
      if (role.value === 'admin') {
        notify({
          message: "Invalid permissions",
          duration: 2000,
          position: 'right'
        })
        return;
      }

      profileSettingFactory.updateACL({
        role,
        operator_id: operator_id,
        fleet_id: rootScopeFactory.getData('fleetId')
      }, function (response) {
        if (response && response.status === 200) {
          notify({
            message: 'Operator permissions updated',
            duration: 2000,
            position: 'right'
          })
          profileSettingFactory.clearOnCallCache()
          fetchFleetProfile(rootScopeFactory.getData('fleetId'))
        } else if (response && response.status === 403) {
          notify({
            message: "Invalid permissions",
            duration: 2000,
            position: 'right'
          })
        } else {
          notify({ message: 'Failed to update the operator aclea', duration: 2000, position: 'right' })
        }
      })
    }

    $scope.clickCVV = false

    $scope.inquiryClickCVV = function () {
      $scope.clickCVV = !$scope.clickCVV
    }

    $scope.fleetDistanceSave = function (distance) {
      $scope.scheduledata = {}
      var bikegroupArr = []
      var updateMetersArr = [], changetometer
      _.each(distance, function (element, index) {
        if ($scope.distancePreference === 'miles') {
          changetometer = utilsFactory.getMeters($scope.fleetSchedule[index].show_schedule)
        } else {
          changetometer = utilsFactory.kmToMeter($scope.fleetSchedule[index].show_schedule)
        }
        bikegroupArr.push(element.bike_group_id)
        updateMetersArr.push(changetometer)
      })

      $scope.scheduledata = {
        bikes: bikegroupArr,
        maintenance_schedule: updateMetersArr,
        fleet_id: rootScopeFactory.getData('fleetId')
      }

      profileSettingFactory.updateFleetSchedule($scope.scheduledata, function (response) {
        if (response && response.status === 200) {
          notify({
            message: 'Maintenance Schedule successfully updated.',
            duration: 2000,
            position: 'right'
          })
        } else {
          notify({ message: 'Failed to update the Maintenance Schedule', duration: 2000, position: 'right' })
        }
      })
    }
    $scope.maxTriplength = function (session) {
      $scope.pricingdata.restriction_trip_length = session
    }

    $scope.pricingdata.restriction_trip_length = $scope.rideDeposit[0]

    $scope.$watch('growDetails', function () {
      const currentDetails = JSON.stringify($scope.growDetails)
      const growDetailsCopy = JSON.stringify($scope.initialGrowMetaInfo)
      if (currentDetails && growDetailsCopy && currentDetails !== growDetailsCopy) {
        $scope.isEdittingGrow = true
      } else $scope.isEdittingGrow = false
    }, true)

    $scope.$watch('segwayDetails', function () {
      const currentDetails = JSON.stringify($scope.segwayDetails)
      const segwayDetailsCopy = JSON.stringify($scope.initialSegwayMetaInfo)
      if (currentDetails && segwayDetailsCopy && currentDetails !== segwayDetailsCopy) {
        $scope.isEdittingSegway = true
      } else $scope.isEdittingSegway = false
    }, true)

    $scope.$watch('actonDetails', function () {
      const currentActonDetails = JSON.stringify($scope.actonDetails)
      const actonDetailsCopy = JSON.stringify($scope.initialACTONMetaInfo)
      if (currentActonDetails && actonDetailsCopy && currentActonDetails !== actonDetailsCopy) {
        $scope.isEdittingACTON = true
      } else $scope.isEdittingACTON = false
    }, true)

    $scope.$watch('omniIoTDetails', function () {
      const currentOmniIoTDetails = JSON.stringify($scope.omniIoTDetails)
      const omniIotDetailsCopy = JSON.stringify($scope.initialOmniMetaInfo)
      if (currentOmniIoTDetails && omniIotDetailsCopy && currentOmniIoTDetails !== omniIotDetailsCopy) {
        $scope.isEdittingOmniIoT = true
      } else $scope.isEdittingOmniIoT = false
    }, true)

    $scope.$watch('teltonikaDetails', function () {
      const currentTeltonikaDetails = JSON.stringify($scope.teltonikaDetails)
      const teltonikaDetailsCopy = JSON.stringify($scope.initialTeltonikaMetaInfo)
      if (currentTeltonikaDetails && teltonikaDetailsCopy && currentTeltonikaDetails !== teltonikaDetailsCopy) {
        $scope.isEdittingTeltonika = true
      } else $scope.isEdittingTeltonika = false
    }, true)

    $scope.$watch('OkaiDetails', function () {
      const currentOkaiDetails = JSON.stringify($scope.OkaiDetails)
      const OkaiDetailsCopy = JSON.stringify($scope.initialOkaiMetaInfo)
      if (currentOkaiDetails && OkaiDetailsCopy && currentOkaiDetails !== OkaiDetailsCopy) {
        $scope.isEdittingOkai = true
      } else $scope.isEdittingOkai = false
    }, true)

    $scope.saveGrowIntegration = (growDetails) => {
      let isApplying = $scope.integrationTypes.includes('grow')
      const fleetId = rootScopeFactory.getData('fleetId')
      profileSettingFactory.saveGrowSettings(Object.assign({ fleetId }, growDetails, { apply: isApplying }, { growMaximumSpeed: growDetails.growMaximumSpeed*10 })).then(res => {
        $scope.integrations = [...$scope.integrations, res.data.payload]
        $scope.integrationTypes = $scope.integrations.map(int => int.integration_type) || []
        const growDetails = $scope.integrations.find(int => int.integration_type === 'grow')
        $scope.growDetails = growDetails && growDetails.metadata
        $scope.initialGrowMetaInfo = { ...$scope.growDetails }
        $scope.isEdittingGrow = false
        notify({
          message: isApplying ? 'Grow Settings applied successfully.' : 'Grow Settings saved successfully.',
          duration: 2000,
          position: 'right'
        })
      }).catch(err => {
        $scope.isEdittingGrow = false
        notify({
          message: err.message || 'An error occurred saving Grow settings',
          duration: 2000,
          position: 'right'
        })
      })
    }

    $scope.saveSegwayIntegration = (segwayDetails) => {
      let isApplying = $scope.integrationTypes.includes('segway')
      const fleetId = rootScopeFactory.getData('fleetId')
      $rootScope.showLoader = true
      profileSettingFactory.saveSegwaySettings(Object.assign({ fleetId }, segwayDetails, { apply: isApplying })).then(res => {
        $scope.integrations = [...$scope.integrations, res.data.payload]
        $scope.integrationTypes = $scope.integrations.map(int => int.integration_type) || []
        const segwayDetails = $scope.integrations.find(int => int.integration_type === 'segway')
        $scope.segwayDetails = segwayDetails && segwayDetails.metadata
        $scope.initialSegwayMetaInfo = { ...$scope.segwayDetails }
        $rootScope.showLoader = false
        $scope.isEdittingSegway = false
        notify({
          message: isApplying ? 'Segway Settings applied successfully.' : 'Segway Settings saved successfully.',
          duration: 2000,
          position: 'right'
        })
      }).catch(err => {
        $scope.isEdittingSegway = false
        notify({
          message: err.message || 'An error occurred saving Segway settings',
          duration: 2000,
          position: 'right'
        })
      })
    }

    // For Acton, Omni IoT and Teltonika
    $scope.saveACTONIntegration = (actonDetails, vendor) => { // vendor is integration type
      let isApplying = $scope.integrationTypes.includes(vendor)
      const fleetId = rootScopeFactory.getData('fleetId')

      let detailsToSave;
      if(vendor === 'ACTON') {
        detailsToSave = Object.assign({ fleetId }, actonDetails, { apply: isApplying })
      } else if(vendor === 'Omni IoT') {
        detailsToSave = Object.assign({ fleetId }, omniIoTDetails, { apply: isApplying })
      } else if(vendor === 'Teltonika') {
        detailsToSave = Object.assign({ fleetId }, teltonikaDetails, { apply: isApplying })
      } else if(vendor === 'Okai') {
        detailsToSave = Object.assign({ fleetId }, OkaiDetails, { apply: isApplying })
      }
      $rootScope.showLoader = true
      profileSettingFactory.saveACTONSettings(detailsToSave, vendor).then(res => {
        $scope.integrations = [...$scope.integrations, res.data.payload]
        $scope.integrationTypes = $scope.integrations.map(int => int.integration_type) || []
        const vendorDetails = $scope.integrations.find(int => int.integration_type === vendor)
        if(vendor === 'ACTON') {
          $scope.actonDetails = vendorDetails && vendorDetails.metadata
          $scope.initialACTONMetaInfo = { ...$scope.actonDetails }
        } else if (vendor === 'Omni IoT') {
          $scope.omniIoTDetails = vendorDetails && vendorDetails.metadata
          $scope.initialOmniMetaInfo = { ...$scope.omniIoTDetails }
        } else if (vendor === 'Teltonika') {
          $scope.teltonikaDetails = vendorDetails && vendorDetails.metadata
          $scope.initialTeltonikaMetaInfo = { ...$scope.teltonikaDetails }
        } else if (vendor === 'Okai') {
          $scope.OkaiDetails = vendorDetails && vendorDetails.metadata
          $scope.initialOkaiMetaInfo = { ...$scope.OkaiDetails }
        }
        $rootScope.showLoader = false
        $scope.isEdittingACTON = false
        notify({
          message: isApplying ? `${vendor} Settings applied successfully.` : `${vendor} Settings saved successfully.`,
          duration: 2000,
          position: 'right'
        })
      }).catch(err => {
        $scope.isEdittingACTON = false
        notify({
          message: err.message || `An error occurred saving ${vendor} settings`,
          duration: 2000,
          position: 'right'
        })
      })
    }
  }

)
