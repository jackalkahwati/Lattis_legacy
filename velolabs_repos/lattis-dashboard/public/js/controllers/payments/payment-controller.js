'use strict'

angular.module('skyfleet.controllers').controller(
  'paymentController',
  function ($scope, authFactory, sessionFactory, $rootScope, $state, $window, $timeout, rootScopeFactory, utilsFactory, paymentFactory, bikeFleetFactory, notify, $location, $cookies, lattisConstants, membershipFactory, promotionsFactory,ngDialog) {
    $scope.editPayment = false
    $scope.editBankAccount = false
    $scope.currencyMap = lattisConstants.currencyCodeSymbolMap

    $scope.clickEditPayment = function () {
      $scope.editPayment = !$scope.editPayment
      $scope.editBankAccount = false
    }

    $scope.clickEditBank = function () {
      $scope.editBankAccount = !$scope.editBankAccount
    }

    if (window.location.hostname === 'bikeshare.lattis.io') {
      $scope.connect_stripe_id = lattisConstants.stripeProductionClientID
    } else {
      $scope.connect_stripe_id = lattisConstants.stripeDevClientID
    }

    $scope.inner_menu = [{
      id: 1,
      title: 'Payment Gateway',
      url: 'payments.payment_gateway'
    },
    {
      id: 2,
      title: 'Pay per Use Setup',
      url: 'payments.pay_per_use'
    },
    {
      id: 3,
      title: 'Rental Fares',
      url: 'payments.rental_fares'
    },
    {
      id: 4,
      title: 'Membership Setup',
      url: 'payments.memberships'
    },
    {
      id: 5,
      title: 'Promotions Setup',
      url: 'payments.promotions'
    }]

    $scope.loadingPaymentInfo = true;
    $scope.fleetCurrency = rootScopeFactory.getData('fleetCurrency')
    $scope.model = {}
    $scope.refmarkers = [{ref: 'abc'}, {ref: 'def'}, {ref: 'ghi'}]
    $scope.pricingdata = {}
    $scope.dropdownlist = [{
      property: 'current_status',
      name: 'Mins',
      id: 'Mins'
    }, {
      property: 'current_status',
      name: 'Hours',
      id: 'Hours'
    }, {
      property: 'current_status',
      name: 'Days',
      id: 'Days'
    }, {
      property: 'current_status',
      name: 'Weeks',
      id: 'Weeks'
    }, {
      property: 'current_status',
      name: 'Months',
      id: 'Months'
    }]

    $scope.rideDeposit = [{
      property: 'current_status',
      name: 'Per Ride',
      id: 'Per Ride'
    }, {
      property: 'current_status',
      name: 'OneTime',
      id: 'OneTime'
    }]

    $scope.pricingdata.usage_surcharge = 'No'
    $scope.pricingdata.ride_deposit = 'No'
    $scope.pricingdata.price_type = $scope.dropdownlist[0].id
    $scope.pricingdata.excess_usage_type = $scope.dropdownlist[0].id
    $scope.pricingdata.excess_usage_type_after_type = $scope.dropdownlist[0].id
    $scope.pricingdata.price_for_ride_deposit_type = $scope.rideDeposit[0].id
    $scope.pricingdata.refund_criteria_value = $scope.dropdownlist[0].id

    if (rootScopeFactory.getData('fleetId')) {
      updatePricing(rootScopeFactory.getData('fleetId'))
    }

    $scope.$on('fleetChange', function (event, id) {
      updatePricing(id)
    })

    $scope.validateFields = function (data) {
      data.$setDirty()
    }

    function toggleValidation (condition) {
      if (condition) {
        $scope.myForm.price_for_membership.$setDirty()
        $scope.myForm.price_type_value.$setDirty()
        if ($scope.myForm.excess_usage_fees) $scope.myForm.excess_usage_fees.$setDirty()
        if ($scope.myForm.excess_usage_type_after_value) $scope.myForm.excess_usage_type_after_value.$setDirty()
        if ($scope.myForm.excess_usage_type_value) $scope.myForm.excess_usage_type_value.$setDirty()
        if ($scope.myForm.enable_preauth === 'true') {
          $scope.myForm.preauth_amount.$setDirty()
        }
      } else {
        $scope.myForm.price_for_membership.$setPristine()
        $scope.myForm.price_type_value.$setPristine()
        if ($scope.myForm.excess_usage_fees) $scope.myForm.excess_usage_fees.$setPristine()
        if ($scope.myForm.excess_usage_type_after_value) $scope.myForm.excess_usage_type_after_value.$setPristine()
        if ($scope.myForm.excess_usage_type_value) $scope.myForm.excess_usage_type_value.$setPristine()
      }
    }

    $scope.savePricing = function () {
      toggleValidation(true)
      setTimeout(() => {
        if (
          _.isEmpty($scope.myForm.$error.required) &&
          rootScopeFactory.getData("fleetId")
        ) {
          paymentFactory.createFleetBillingCycle(
            {
              fleet_id: rootScopeFactory.getData("fleetId"),
              price_for_membership: $scope.pricingdata.price_for_membership,
              price_type_value: $scope.pricingdata.price_type_value,
              price_type: $scope.pricingdata.price_type,
              usage_surcharge: $scope.pricingdata.usage_surcharge,
              excess_usage_fees: $scope.pricingdata.excess_usage_fees,
              excess_usage_type_value:
                $scope.pricingdata.excess_usage_type_value,
              excess_usage_type: $scope.pricingdata.excess_usage_type,
              excess_usage_type_after_value:
                $scope.pricingdata.excess_usage_type_after_value,
              excess_usage_type_after_type:
                $scope.pricingdata.excess_usage_type_after_type,
              ride_deposit: $scope.pricingdata.ride_deposit,
              price_for_ride_deposit_type:
                $scope.pricingdata.price_for_ride_deposit_type,
              refund_criteria: $scope.pricingdata.refund_criteria,
              refund_criteria_value: $scope.pricingdata.refund_criteria_value,
              price_for_penalty_outside_parking:
                $scope.pricingdata.price_for_penalty_outside_parking,
              price_for_penalty_outside_parking_below_battery_charge:
                $scope.pricingdata
                  .price_for_penalty_outside_parking_below_battery_charge,
              price_for_ride_deposit: $scope.pricingdata.price_for_ride_deposit,
              price_for_forget_plugin:
                $scope.pricingdata.price_for_forget_plugin,
              price_for_bike_unlock: $scope.pricingdata.price_for_bike_unlock,
              price_for_reservation_late_return:
                $scope.pricingdata.price_for_reservation_late_return,
              enable_preauth: $scope.pricingdata.enable_preauth,
              preauth_amount: $scope.pricingdata.preauth_amount,
            },
            function (response) {
              if (response && response.status === 200) {
                notify({
                  message: "Fleet billing updated",
                  duration: 2000,
                  position: "right",
                });
                updatePricing(rootScopeFactory.getData("fleetId"));
              } else {
                notify({
                  message: "Failed to update fleet billing",
                  duration: 2000,
                  position: "right",
                });
              }
            }
          );
        } else {
          notify({
            message: "Fill all required fields",
            duration: 2000,
            position: "right",
          });
        }
      }, 0);

    }
    // TODO: Clean up this mess
    // $scope.open = function (data,tax_total) {
    //   ngDialog.open({
    //     template: '../../html/modals/show-tax.html',
    //     data:{taxes:data,taxTotal:tax_total}
    //   })
    // }

    function updatePricing (id) {
      let fleetId = rootScopeFactory.getData('fleetId') || JSON.parse(localStorage.getItem('currentFleet')).fleet_id
        bikeFleetFactory.getBikesData({fleet_id: fleetId}).then(function (response) {
          $scope.bikeData = response.payload.bike_data[0] || {};
          if ($scope.bikeData.type === "regular") {
            $scope.bikeData.type = "Bike";
          }
          if ($scope.bikeData.type === "electric") {
            $scope.bikeData.type = "Electric Bike";
          }
        }).catch(function (error) {
          console.error(error)
        })
      if (id) {
        $scope.loadingPaymentInfo = true;
        paymentFactory.getAccountStatus(id, function (response) {
          if (!$scope.fleetCurrency) {
            $scope.fleetCurrency = response.payload[0].currency
          }

          listMemberships(response.payload[0].fleet_id)
          listPromotions(response.payload[0].fleet_id)

          var getpricingstatus = angular.copy(response.payload[0])

          if (getpricingstatus.price_type_value != null || getpricingstatus.price_for_ride_deposit_type != null || getpricingstatus.refund_criteria_value != null) {
            $scope.pricingdata = {
              price_for_membership: getpricingstatus.price_for_membership,
              price_type_value: getpricingstatus.price_type_value,
              price_type: getpricingstatus.price_type,
              usage_surcharge: getpricingstatus.usage_surcharge,
              excess_usage_fees: getpricingstatus.excess_usage_fees,
              excess_usage_type_value: getpricingstatus.excess_usage_type_value,
              excess_usage_type: getpricingstatus.excess_usage_type,
              excess_usage_type_after_value: getpricingstatus.excess_usage_type_after_value,
              excess_usage_type_after_type: getpricingstatus.excess_usage_type_after_type,
              ride_deposit: getpricingstatus.ride_deposit,
              price_for_ride_deposit_type: getpricingstatus.price_for_ride_deposit_type,
              refund_criteria: getpricingstatus.refund_criteria,
              refund_criteria_value: getpricingstatus.refund_criteria_value,
              price_for_penalty_outside_parking: getpricingstatus.price_for_penalty_outside_parking,
              price_for_penalty_outside_parking_below_battery_charge: getpricingstatus.price_for_penalty_outside_parking_below_battery_charge,
              price_for_ride_deposit: getpricingstatus.price_for_ride_deposit,
              price_for_forget_plugin: getpricingstatus.price_for_forget_plugin,
              price_for_bike_unlock: getpricingstatus.price_for_bike_unlock,
              price_for_reservation_late_return: getpricingstatus.price_for_reservation_late_return,
              payment_gateway: getpricingstatus.payment_gateway,
              enable_preauth: getpricingstatus.enable_preauth ? 'true' : 'false',
              preauth_amount: Number(getpricingstatus.preauth_amount)
            }
          }

          $scope.loadingPaymentInfo = false;
        })
      }
    }

    function dismissMembershipError() {
      $scope.membershipError = null
    }

    function formatMembership(membership) {
      return Object.assign({}, membership, {
        membership_incentive: Number(membership.membership_incentive),
        membership_price: Number(membership.membership_price)
      })
    }

    function formatMemberships(memberships) {
      return memberships.map(formatMembership)
    }

    function listMemberships(fleet_id) {
      $scope.membershipLoading = true;
      membershipFactory
        .list(fleet_id)
        .then(response => {
          $scope.membershipLoading = false;
          $scope.memberships = formatMemberships(response.payload)
        })
        .catch(error => {
          $scope.membershipLoading = false;
          console.error(error)
        })
    }

    function updateScopeMemberships(membership, newMembership) {
      return $scope.memberships.map(
        function(m) {
          if(m.fleet_membership_id === membership.fleet_membership_id) {
            return formatMembership(newMembership)
          }

          return m
        }
      )
    }

    function createMembership(membership, index) {
      $scope.membershipLoading = true
      $scope.loadingIndex = index

      membershipFactory.create({
        fleet_id: rootScopeFactory.getData('fleetId'),
        membership_price: membership.membership_price,
        membership_price_currency: $scope.fleetCurrency,
        membership_incentive: membership.membership_incentive,
        payment_frequency: membership.payment_frequency
      }).then(function(response) {
        $scope.membershipLoading = false
        $scope.loadingIndex = null

        $scope.memberships = updateScopeMemberships(membership, response.payload)

        notify({
          message: 'Membership created.',
          duration: 2000,
          position: 'right'
        })
      }).catch(function (error) {
        console.error(error)
        $scope.membershipLoading = false
        $scope.membershipError = error && error.data
          ? error.data.message || error.data.error.message
          : null
      })
    }

    function updateMembership(membership, index) {
      $scope.membershipLoading = true
      $scope.loadingIndex = index

      membershipFactory.update(membership.fleet_membership_id, {
        membership_price: membership.membership_price,
        membership_price_currency: $scope.fleetCurrency,
        membership_incentive: membership.membership_incentive,
        payment_frequency: membership.payment_frequency
      }).then(function(response) {
        $scope.memberships = updateScopeMemberships(membership, response.payload)

        $scope.membershipLoading = false
        $scope.loadingIndex = null

        notify({
          message: 'Membership updated.',
          duration: 2000,
          position: 'right'
        })
      }).catch(function (error) {
        console.error(error)
        $scope.membershipLoading = false
        $scope.loadingIndex = null
        $scope.membershipError = error && error.data
          ? error.data.message || error.data.error.message
          : null
      })
    }

    function activateMembership(membership, index) {
      $scope.activating = true
      $scope.activatingIndex = index

      membershipFactory.activate(membership.fleet_membership_id)
      .then(function(response) {
        $scope.memberships = updateScopeMemberships(membership, response.payload)

        $scope.activating = false
        $scope.activatingIndex = null

        notify({
          message: 'Membership activated.',
          duration: 2000,
          position: 'right'
        })
      }).catch(function (error) {
        console.error(error)
        $scope.activating = false
        $scope.activatingIndex = null
        $scope.membershipError = error && error.data
          ? error.data.message || error.data.error.message
          : null
      })
    }

    function deactivateMembership(membership, index) {
      $scope.deactivating = true
      $scope.deactivatingIndex = index

      membershipFactory.deactivate(membership.fleet_membership_id)
      .then(function(response) {
        $scope.memberships = updateScopeMemberships(membership, response.payload)

        $scope.deactivating = false
        $scope.deactivatingIndex = null

        notify({
          message: 'Membership deactivated.',
          duration: 2000,
          position: 'right'
        })
      }).catch(function (error) {
        console.error(error)
        $scope.deactivating = false
        $scope.deactivatingIndex = null
        $scope.membershipError = error && error.data
          ? error.data.message || error.data.error.message
          : null
      })
    }

    function executeMembership(membership, index) {
      if (membership.fleet_membership_id) {
        updateMembership(membership, index)
      } else {
        createMembership(membership, index)
      }
    }

    function addMembership() {
      if ($scope.memberships) {
        if (!$scope.memberships.find(m => m.fleet_membership_id === undefined)) {
          $scope.memberships.push({})
        }
      } else {
        $scope.memberships = [{}]
      }
    }

    function canAddMembership() {
      return $scope.memberships && $scope.memberships.filter(
        function(membership) {
          return membership.deactivation_date === null
        }
      ).length < 3
    }

    $scope.dismissMembershipError = dismissMembershipError
    $scope.executeMembership = executeMembership
    $scope.activateMembership = activateMembership
    $scope.deactivateMembership = deactivateMembership
    $scope.addMembership = addMembership
    $scope.canAddMembership = canAddMembership

    $scope.promotions = []
    $scope.newPromotion = null

    function getErrorMessage(error) {
      const errorData = error && error.data
      if (!errorData) {
        return null
      }

      const message = errorData.message || errorData.error.message

      return Array.isArray(errorData.error) ? errorData.error[0].message : message
    }

    function listPromotions(fleet_id) {
      $scope.promotionsLoading = true;
      return promotionsFactory
        .list(fleet_id)
        .then(response => {
          $scope.promotionsLoading = false;
          $scope.promotions = response.payload
        })
        .catch(error => {
          $scope.promotionsLoading = false;
          console.error(error)
        })
    }

    function formatPromotionDiscount(promotion) {
      return Number(promotion.amount)
    }

    function formatPromotionUsage(promotion) {
      const usage = {
        single: 'Single',
        multiple: 'Multiple',
        multiple_unlimited: 'Unlimited'
      }

      return usage[promotion.usage]
    }

    function deactivatePromotion(promotion) {
      $scope.deactivatingPromotionId = promotion.promotion_id

      return promotionsFactory.deactivate(promotion.promotion_id)
      .then(response => {
        $scope.deactivatePromotionId = null

        $scope.promotions = $scope.promotions.map(p => {
          if (p.promotion_id === promotion.promotion_id) {
            return response.payload
          }

          return p
        })
      })
      .catch(error => {
        $scope.promoError = getErrorMessage(error)
      })
    }

    function createPromotion() {
      $scope.creatingPromotion = true

      return promotionsFactory.create(Object.assign({
        ...$scope.newPromotion,
        fleet_id: rootScopeFactory.getData('fleetId') || JSON.parse(localStorage.getItem('currentFleet')).fleet_id
      }))
      .then(response => {
        $scope.creatingPromotion = false
        $scope.newPromotion = {}
        $scope.promotions.push(response.payload)
      })
      .catch(error => {
        $scope.creatingPromotion = false;
        $scope.promoError = getErrorMessage(error)
      })
    }

    $scope.dismissPromoError = () => {
      $scope.promoError = null
    }

    $scope.formatPromotionDiscount = formatPromotionDiscount
    $scope.formatPromotionUsage = formatPromotionUsage
    $scope.deactivatePromotion = deactivatePromotion
    $scope.createPromotion = createPromotion
  })
