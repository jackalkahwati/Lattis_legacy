'use strict'

angular.module('skyfleet.controllers').controller(
  'accountSettingController',
  function ($scope, sessionFactory, $rootScope, $state, profileSettingFactory, _, utilsFactory, $timeout, myFleetFactory, rootScopeFactory, notify, assetfetchFactory, authFactory, regexPatterns) {

    const [patterns] = regexPatterns
    $scope.emailRegexPatterns = patterns.email
    $scope.inner_menu_Items = [
      {id: 1, title: 'Summary'},
      {id: 2, title: 'Notification'},
      {id: 3, title: 'Change password'}
    ]
    $scope.notification = {}
    $state.params.not === 'notification' ? $scope.innertab = 2 : $scope.innertab = 1
    profileSettingFactory.getOperatorProfile(function (resposne) {
      $scope.profile = resposne.payload.operator[0]
      $scope.fName = $scope.profile.first_name
      $scope.lName = $scope.profile.last_name
      $scope.phoneNumber = $scope.profile.phone_number
      $scope.email = $scope.profile.email
      $scope.currentPassword = $scope.profile.password
      if ($scope.phoneNumber) {
        const phoneInput = document.querySelector('#phoneNumber')
        let iti = window.intlTelInput(phoneInput, {
          hiddenInput: 'full_phone',
          utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.15/js/utils.js",
        });
        iti.setNumber($scope.phoneNumber)
        phoneInput.addEventListener('blur', function () {
          $scope.phoneNumberIsValid = !!iti.isValidNumber();
          $scope.phoneNumber = iti.getNumber()
          $scope.$apply();
        })
      }
    })

    $scope.passwordVerified = true
    $scope.verifyPassword = () => {
      const userDetails = {
        password: $scope.form.oldPassword.$viewValue,
        hashedPassword: $scope.currentPassword
      }
      authFactory.verifyPassword(userDetails, function (response) {
        $scope.passwordVerified = response.payload
      })
    }

    $scope.passwordsMatch = true
    $scope.checkPasswords = async () => {
      $scope.passwordsMatch = $scope.password.newPassword === $scope.password.passwordConfirm;
    }

    $scope.validateFields = function (data) {
      data.$setDirty()
    }

    function toggleValidation (condition) {
      if (condition) {
        $scope.form.firstname.$setDirty()
        $scope.form.lastname.$setDirty()
        $scope.form.phoneNo.$setDirty()
        $scope.form.email.$setDirty()
      } else {
        $scope.form.firstname.$setPristine()
        $scope.form.lastname.$setPristine()
        $scope.form.phoneNo.$setPristine()
        $scope.form.email.$setPristine()
      }
    }
    $scope.notification = {
      theft_is_reported: false,
      damage_is_reported: false,
      maintenance_due: false,
      bike_is_parked_outside_zone: false,
      ellipse_battery_low: false }

    $scope.checkAll = function () {
      if (!$scope.all_tickets) {
        _.each($scope.notification, function (value, key) {
          $scope.notification[key] = false
        })
      } else {
        _.each($scope.notification, function (value, key) {
          $scope.notification[key] = true
        })
      }
    }

    $scope.$watch('notification', function () {
      $scope.all_tickets = _.every(_.values($scope.notification), function (v) { return v })
    }, true)

    window.onload = function () {
      const phoneInput = document.querySelector('#phoneNumber')
      let iti = window.intlTelInput(phoneInput, {
        hiddenInput: 'full_phone',
        utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.15/js/utils.js",
      });
      phoneInput.addEventListener('blur', function () {
        $scope.phoneNumberIsValid = !!iti.isValidNumber();
        $scope.phoneNumber = iti.getNumber()
        $scope.$apply();
      })
    }

    assetfetchFactory.getPreferences(function (res) {
      $scope.getdata = res.payload
      if ($scope.getdata == null) {
        _.each($scope.notification, function (v, key) {
          $scope.notification[key] = false
        })
      } else {
        $scope.getdata = utilsFactory.binaryToTrue($scope.getdata)
        _.each($scope.notification, function (v, key) {
          $scope.notification[key] = $scope.getdata[key]
        })
      }
    })
    $scope.save = function () {
      if (!$scope.form.$pristine) {
        toggleValidation(true)
        if (_.isEmpty($scope.form.$error.required) && _.isEmpty($scope.form.$error.pattern)) {
          profileSettingFactory.editOperatorProfile({
            'first_name': $scope.fName,
            'last_name': $scope.lName,
            'phone_number': $scope.phoneNumber,
            'email': $scope.email,
            'operator_id': sessionFactory.getCookieId()
          }, function (res) {
            if (res && res.status === 200) {
              notify({message: 'Profile updated successfully', duration: 2000, position: 'right'})
              sessionFactory.setCookieData({
                name: $scope.fName,
                operator_id: sessionFactory.getCookieId()
              })
              $rootScope.$broadcast('nameChange')
              $rootScope.$emit('nameChange')
              if ($state.params.isFirst) {
                $state.go('lattis-home')
              }
            }
          })
        } else {
          toggleValidation(false)
        }
      }
    }
    $scope.preferenceSave = function () {
      var setdata = utilsFactory.trueTOBinary($scope.notification)
      assetfetchFactory.setPreferences({
        'operator_id': parseInt(sessionFactory.getCookieId()),
        'theft_is_reported': setdata.theft_is_reported,
        'damage_is_reported': setdata.damage_is_reported,
        'maintenance_due': setdata.maintenance_due,
        'ellipse_battery_low': setdata.ellipse_battery_low,
        'bike_is_parked_outside_zone': setdata.bike_is_parked_outside_zone,
        'bike_is_parked_outside_spot': setdata.bike_is_parked_outside_zone,
        'all_tickets': $scope.all_tickets
      }, function (res) {
        if (res && res.status === 200) {
          notify({message: 'Preference updated successfully', duration: 2000, position: 'right'})
        }
      })
      utilsFactory.binaryToTrue($scope.notification)
      utilsFactory.binaryToTrue($scope.all_tickets)
    }

    $scope.updatePassword = async (newPassword) => {
      try {
        await authFactory.passwordReset({
          email: $scope.email,
          password: newPassword,
          oldPassword: $scope.password.oldPassword,
          hashedPassword: $scope.currentPassword
        })
        notify({
          message: `Password updated successfully for ${$scope.email}`,
          duration: 2000, position: 'right'
        })
      } catch (error) {
        notify({
          message: `Password change failed ${error}`,
          duration: 2000, position: 'right'
        })
        console.error(`There was a problem updating password: ${error}`)
      }
    }
  })
