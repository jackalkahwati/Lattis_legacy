'use strict'

angular.module('skyfleet.controllers').controller(
  'addFleetController',
  function ($scope, $rootScope, sessionFactory, addFleetFactory, utilsFactory, myFleetFactory, _, rootScopeFactory, notify, $timeout, bikeFleetFactory, $state, $window, timezoneConstants, lattisConstants, regexPatterns) {
    // FIXME: All of the variables that are defined randomly in this file should be
    // put into an initialization method. The method should be called from the view
    // from ng-init.

    const [patterns] = regexPatterns
    $scope.emailRegexPatterns = patterns.email
    $scope.fleetLogo = null
    $scope.enableCurrency = false
    $scope.imageLarge = false
    $scope.selectCurrency = function (s) {
      $rootScope.fleetCurrency = s
    }
    $scope.maxFleetNameLength = 25

    $scope.currencies = _.keys(lattisConstants.currencyCodeSymbolMap)
      .map(function(value) {
        return { code: value }
      })

    window.onload = function () {
      const phoneInput = document.querySelector("#pno");
      function getIp(callback) {
        fetch("https://ipinfo.io", {
          headers: { Accept: "application/json" },
        })
          .then((resp) => resp.json())
          .catch(() => {
            return {
              country: "",
            };
          })
          .then((resp) => callback(resp.country));
      }

      let iti = window.intlTelInput(phoneInput, {
        allowDropdown: true,
        dropdownContainer: document.body,
        hiddenInput: "full_number",
        nationalMode: false,
        formatOnDisplay: true,
        separateDialCode: true,
        autoHideDialCode: true,
        autoPlaceholder: "aggressive",
        initialCountry: "auto",
        placeholderNumberType: "MOBILE",
        preferredCountries: ["us"],
        geoIpLookup: getIp,
        utilsScript:
          "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.15/js/utils.js",
      });
      phoneInput.addEventListener("blur", function () {
        $rootScope.phoneNumberIsValid = !!iti.isValidNumber();
        $scope.pno = iti.getNumber();
        $scope.$apply();
      });
    };

    $scope.distancePreferences = ["miles", "kilometers"];
    $scope.distancePreference = $scope.distancePreferences[0];
    $scope.rectangle = $scope.polygon = $scope.circle = false;
    myFleetFactory.getFleetData().then(function (response) {
      if (response.fleet_data.length) {
        $scope.currentFleet = _.where(response.fleet_data, {
          fleet_id: rootScopeFactory.getData("fleetId"),
        })[0];
      } else {
        $scope.currentFleet = response.customer_data[0];
      }
      $scope.fleetCustomename = $scope.currentFleet.customer_name;
    });
    $scope.fleetChange = function () {
      $scope.form.customername.$setPristine();
      $scope.fleetCustomename = $scope.currentFleet.customer_name;
    };

    $scope.fleetSame = function () {}

    $scope.customerSelected = function (i) {
      $scope.paymtentType.type = i.type;
      $scope.paymtentType.name = i.name;
      $scope.enableCurrency = ["public", "private"].includes(i.name);
    };

    $scope.selectPreference = function (preference) {
      $scope.distancePreference = preference;
    };

    $("body").bind("mousewheel", function () {
      $(".timezone-scroll")
        .niceScroll({
          autohidemode: true,
          zindex: 999,
          smoothscroll: true,
          mousescrollstep: 40,
        })
        .resize();
    });
    $scope.selectedZone = {};
    $scope.timezone = timezoneConstants;
    $scope.selectedZone.name = $scope.timezone[9].text;
    $scope.selectedZone.value = $scope.timezone[9].value;
    $scope.zoneSelected = function (i) {
      $scope.selectedZone.name = i.text;
      $scope.selectedZone.value = i.value;
    };

    $scope.formatDate = function (date) {
      function pad(n) {
        return n < 10 ? "0" + n : n;
      }

      return (
        date &&
        date.getFullYear() +
          "-" +
          pad(date.getMonth() + 1) +
          "-" +
          pad(date.getDate())
      );
    };

    $scope.parseDate = function (s) {
      var tokens = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s);

      return tokens && new Date(tokens[1], tokens[2] - 1, tokens[3]);
    };

    // Public variables
    // $scope.formValid = false;
    $scope.newCustomerChoice = true;
    $scope.customerHide = false;
    $scope.fleetHide = true;
    $scope.finished = true;
    $scope.geofence = null;

    // public methods
    $scope.validateFields = function (data) {
      data.$setDirty();
    };

    function toggleValidation(condition) {
      if (condition) {
        $scope.form.firstname.$setDirty();
        $scope.form.lastname.$setDirty();
        $scope.form.email.$setDirty();
        $scope.form.city.$setDirty();
        $scope.form.state.$setDirty();
        $scope.form.zip.$setDirty();
        $scope.form.country.$setDirty();
        $scope.form.customername.$setDirty();
      } else {
        $scope.form.firstname.$setPristine();
        $scope.form.lastname.$setPristine();
        $scope.form.email.$setPristine();
        $scope.form.city.$setPristine();
        $scope.form.state.$setPristine();
        $scope.form.zip.$setPristine();
        $scope.form.country.$setPristine();
        $scope.form.customername.$setPristine();
      }
    }

    $scope.backprofile = function () {
      $scope.customerHide = false
      $scope.fleetHide = true
    }

    $scope.newCustomer = true
    $scope.newCustomerRadio = function () {
      $scope.newCustomer = true
      $scope.customerSelected()
      $scope.newCustomerChoice = !$scope.newCustomerChoice
    }

    $scope.exsistingCustomerRadio = function () {
      addFleetFactory.getCustomerName(function (res) {
        $scope.customerList = res.payload
      })
      $scope.newCustomer = false
      $scope.newCustomerChoice = false
    }

    $scope.kindOfFleet = [{
      type: 'Private no payment',
      name: 'private_no_payment'
    }, {
      type: 'Private with payment',
      name: 'private'
    }, {
      type: 'Public no payment',
      name: 'public_no_payment'
    }, {
      type: 'Public with payment',
      name: 'public'
    }]

    $scope.paymtentType = $scope.kindOfFleet[0]
    $scope.fleetLogoerr = false
    $scope.submitForm = function () {
      if ($scope.fleetLogo == null) {
        $scope.fleetLogoerr = true
      }

      if ($scope.fleetLogo.size > 500000) {
        $scope.imageLarge = true
      } else {
        $scope.imageLarge = false
      }
      $scope.form.customername.$setDirty()
      $scope.form.fleetname.$setDirty()
      toggleValidation(true)
      $rootScope.showLoader = true
      if (_.isEmpty($scope.form.$error.required) && _.isEmpty($scope.form.$error.required) && $scope.fleetLogo && $scope.form.customername.$error) {
        let formData = new FormData()
        formData.append('operator_id', sessionFactory.getCookieId())
        formData.append('customer_name', $scope.fleetCustomename)
        formData.append('fleet_name', $scope.fleetName)
        formData.append('first_name', $scope.fName)
        formData.append('last_name', $scope.lName)
        formData.append('country_code', null)
        formData.append('city', $scope.city)
        formData.append('state', $scope.state)
        formData.append('postal_code', $scope.zip)
        formData.append('type', $scope.paymtentType.name)
        formData.append('country', $scope.country)
        formData.append('email', $scope.email)
        formData.append('fleet_timezone', $scope.selectedZone.value)
        formData.append('distance_preference', $scope.distancePreference);
        $scope.enableCurrency && formData.append('currency', $scope.fleetCurrency.code)
        formData.append('fleet_logo', $scope.fleetLogo)
        formData.append('fleet_id', rootScopeFactory.getData('fleetId'))
        if ($scope.pno) formData.append('phone_number', $scope.pno)
        if ($scope.fleetType === 'true') formData.append('customer_id', rootScopeFactory.getData('customerId'))
        
        addFleetFactory.createFleet(formData, function (response) {
          $rootScope.showLoader = false
          if (response && response.status === 200) {
            notify({message: 'Successfully added fleet', duration: 2000, position: 'right'})
            bikeFleetFactory.clearBikeDataCache()
            myFleetFactory.clearFleetCache()
            $rootScope.$broadcast('fleetAdded')
            $rootScope.$emit('fleetAdded')
            $state.go('lattis-home')
          } else {
            notify({message: 'Failed to create fleet', duration: 2000, position: 'right'})
            $state.go('lattis-home')
          }
        })
      } else {
        $rootScope.showLoader = false
        $window.scrollTo(0, 0)
      }
    }
  })
