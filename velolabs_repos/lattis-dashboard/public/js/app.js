'use strict'

// if(window.location.hostname !== 'localhost' || window.location.hostname !== '127.0.0.1') {
//     Raven.config('https://1ee2935720b84c73a5c6a33f9b959206@sentry.io/193198').install();
// }

angular.module('underscore', []).factory('_', [
  '$window',
  function ($window) {
    return $window._
  }
])

angular.module('skyfleet',
  [
    'ui.router',
    'skyfleet.controllers',
    'ngStorage',
    'ngAnimate',
    'angAccordion',
    'ui-rangeSlider',
    'underscore',
    'ngRoute',
    'ngCookies',
    'chart.js',
    'ngMessages',
    'ui.bootstrap',
    'cgNotify',
    'angular-nicescroll',
    'ngIntlTelInput',
    'ngCapsLock',
    'Scope.safeApply',
    'ngDialog',
    'highcharts-ng'
  ])
  .constant('lattisConstants', {
    'liveStatus': 'active',
    'suspendedStatus': 'suspended',
    'inactiveStatus': 'inactive',
    'deletedStatus': 'deleted',
    'parkedSubStatus': 'parked',
    'reservedSubStatus': 'reserved',
    'onTripSubStatus': 'on_trip',
    'damagedSubStatus': 'damaged',
    'stolenSubStatus': 'stolen',
    'reportedStolenSubStatus': 'reported_stolen',
    'totalLossSubStatus': 'total_loss',
    'defleetedSubStatus': 'defleeted',
    'maintenanceSubStatus': 'under_maintenance',
    'lockAssignedSubStatus': 'lock_assigned',
    'lockNotAssignedSubStatus': 'lock_not_assigned',
    'balancingSubStatus': 'balancing',
    'transportSubStatus': 'transport',
    'collectSubStatus': 'collect',
    'mapboxAccessToken': 'pk.eyJ1IjoiYXJhdmluZDc0MSIsImEiOiJjaWtkbnBhMnkwMDI0dXZsemNkcmZ6aTFhIn0.aFH5C34Q-iRjYUrx_nHofw',
    'stripeProductionClientID': 'ca_BVPkExfiZBoRSiWJZODbk5tvT0MdCzpr',
    'stripeDevClientID': 'ca_BVPk1ZbSKS3KRDPDVjewtUGUA6hITArn',
    currencyCodeSymbolMap: {
      USD: '$',
      EUR: '€',
      GBP: '£',
      CAD: 'C$',
      DKK: 'DKK',
      SEK: 'SEK',
      HUF: 'Ft',
      CLP: 'CLP$',
      PEN: 'S/',
      COP: 'COL$',
      BRL: 'R$'
    }
  })

  .constant('iotModuleConstants',
    [
      {type: 'scout', name: 'Scout IoT'},
      {type: 'geotab', name: 'Geo Tab'},
      {type: 'grow', name: 'Grow'},
      {type: 'segway', name: 'Segway'},
      {type: 'linka', name: 'Linka'},
      {type: 'ACTON', name: 'ACTON'},
      {type: 'Omni Lock', name: 'Omni Lock'},
      {type: 'Omni IoT', name: 'Omni IoT'},
      {type: 'Teltonika', name: 'Teltonika'},
      {type: 'duckt', name: 'DuckT'},
      {type: 'tapkey', name: 'Tapkey'},
      {type: 'kisi', name: 'Kisi'},
      {type: 'sas', name: 'Sas'},
      {type: 'Sentinel', name: 'Sentinel'},
      {type: 'ParcelHive', name: 'ParcelHive'},
      {type: 'Edge', name: 'Edge'},
    ]
  )
  .constant('regexPatterns',
    [
      {email: /^[-a-zA-Z0-9][-._!#$%&*+/=?`{|~}a-zA-Z0-9]*@[-.a-zA-Z0-9]+(\.[-.a-zA-Z0-9]+)*\.(com|edu|info|gov|int|mil|net|org|biz|name|bike|museum|coop|aero|pro|io|mobi|limited|[a-zA-Z]{2})$/}
    ]
  )
  .constant('timezoneConstants',
    [{value: 'Etc/GMT+12', text: '(GMT-12:00) International Date Line West'},
      {value: 'Pacific/Midway', text: '(GMT-11:00) Midway Island, Samoa'},
      {value: 'Pacific/Honolulu', text: '(GMT-10:00) Hawaii'},
      {value: 'US/Alaska', text: '(GMT-09:00) Alaska'},
      {value: 'America/Los_Angeles', text: '(GMT-08:00) Pacific Time (US & Canada)'},
      {value: 'America/Tijuana', text: '(GMT-08:00) Tijuana, Baja California'},
      {value: 'US/Arizona', text: '(GMT-07:00) Arizona'},
      {value: 'America/Chihuahua', text: '(GMT-07:00) Chihuahua, La Paz, Mazatlan'},
      {value: 'US/Mountain', text: '(GMT-07:00) Mountain Time (US & Canada)'},
      {value: 'America/Managua', text: '(GMT-06:00) Central America'},
      {value: 'US/Central', text: '(GMT-06:00) Central Time (US & Canada)'},
      {value: 'America/Mexico_City', text: '(GMT-06:00) Guadalajara, Mexico City, Monterrey'},
      {value: 'Canada/Saskatchewan', text: '(GMT-06:00) Saskatchewan'},
      {value: 'America/Bogota', text: '(GMT-05:00) Bogota, Lima, Quito, Rio Branco'},
      {value: 'US/Eastern', text: '(GMT-05:00) Eastern Time (US & Canada)'},
      {value: 'US/East-Indiana', text: '(GMT-05:00) Indiana (East)'},
      {value: 'Canada/Atlantic', text: '(GMT-04:00) Atlantic Time (Canada)'},
      {value: 'America/Caracas', text: '(GMT-04:00) Caracas, La Paz'},
      {value: 'America/Manaus', text: '(GMT-04:00) Manaus'},
      {value: 'America/Santiago', text: '(GMT-04:00) Santiago'},
      {value: 'Canada/Newfoundland', text: '(GMT-03:30) Newfoundland'},
      {value: 'America/Sao_Paulo', text: '(GMT-03:00) Brasilia'},
      {value: 'America/Argentina/Buenos_Aires', text: '(GMT-03:00) Buenos Aires, Georgetown'},
      {value: 'America/Godthab', text: '(GMT-03:00) Greenland'},
      {value: 'America/Montevideo', text: '(GMT-03:00) Montevideo'},
      {value: 'America/Noronha', text: '(GMT-02:00) Mid-Atlantic'},
      {value: 'Atlantic/Cape_Verde', text: '(GMT-01:00) Cape Verde Is.'},
      {value: 'Atlantic/Azores', text: '(GMT-01:00) Azores'},
      {value: 'Africa/Casablanca', text: '(GMT+00:00) Casablanca, Monrovia, Reykjavik'},
      {value: 'Etc/Greenwich', text: '(GMT+00:00) Greenwich Mean Time : Dublin, Edinburgh, Lisbon, London'},
      {value: 'Europe/Amsterdam', text: '(GMT+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna'},
      {value: 'Europe/Belgrade', text: '(GMT+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague'},
      {value: 'Europe/Brussels', text: '(GMT+01:00) Brussels, Copenhagen, Madrid, Paris'},
      {value: 'Europe/Sarajevo', text: '(GMT+01:00) Sarajevo, Skopje, Warsaw, Zagreb'},
      {value: 'Africa/Lagos', text: '(GMT+01:00) West Central Africa'},
      {value: 'Asia/Amman', text: '(GMT+02:00) Amman'},
      {value: 'Europe/Athens', text: '(GMT+02:00) Athens, Bucharest, Istanbul'},
      {value: 'Asia/Beirut', text: '(GMT+02:00) Beirut'},
      {value: 'Africa/Cairo', text: '(GMT+02:00) Cairo'},
      {value: 'Africa/Harare', text: '(GMT+02:00) Harare, Pretoria'},
      {value: 'Europe/Helsinki', text: '(GMT+02:00) Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius'},
      {value: 'Asia/Jerusalem', text: '(GMT+02:00) Jerusalem'},
      {value: 'Europe/Minsk', text: '(GMT+02:00) Minsk'},
      {value: 'Africa/Windhoek', text: '(GMT+02:00) Windhoek'},
      {value: 'Asia/Kuwait', text: '(GMT+03:00) Kuwait, Riyadh, Baghdad'},
      {value: 'Europe/Moscow', text: '(GMT+03:00) Moscow, St. Petersburg, Volgograd'},
      {value: 'Africa/Nairobi', text: '(GMT+03:00) Nairobi'},
      {value: 'Asia/Tbilisi', text: '(GMT+03:00) Tbilisi'},
      {value: 'Asia/Tehran', text: '(GMT+03:30) Tehran'},
      {value: 'Asia/Muscat', text: '(GMT+04:00) Abu Dhabi, Muscat'},
      {value: 'Asia/Baku', text: '(GMT+04:00) Baku'},
      {value: 'Asia/Yerevan', text: '(GMT+04:00) Yerevan'},
      {value: 'Asia/Kabul', text: '(GMT+04:30) Kabul'},
      {value: 'Asia/Yekaterinburg', text: '(GMT+05:00) Yekaterinburg'},
      {value: 'Asia/Karachi', text: '(GMT+05:00) Islamabad, Karachi, Tashkent'},
      {value: 'Asia/Calcutta', text: '(GMT+05:30) Chennai, Kolkata, Mumbai, New Delhi'},
      {value: 'Asia/Calcutta', text: '(GMT+05:30) Sri Jayawardenapura'},
      {value: 'Asia/Katmandu', text: '(GMT+05:45) Kathmandu'},
      {value: 'Asia/Almaty', text: '(GMT+06:00) Almaty, Novosibirsk'},
      {value: 'Asia/Dhaka', text: '(GMT+06:00) Astana, Dhaka'},
      {value: 'Asia/Rangoon', text: '(GMT+06:30) Yangon (Rangoon)'},
      {value: 'Asia/Bangkok', text: '(GMT+07:00) Bangkok, Hanoi, Jakarta'},
      {value: 'Asia/Krasnoyarsk', text: '(GMT+07:00) Krasnoyarsk'},
      {value: 'Asia/Hong_Kong', text: '(GMT+08:00) Beijing, Chongqing, Hong Kong, Urumqi'},
      {value: 'Asia/Kuala_Lumpur', text: '(GMT+08:00) Kuala Lumpur, Singapore'},
      {value: 'Asia/Irkutsk', text: '(GMT+08:00) Irkutsk, Ulaan Bataar'},
      {value: 'Australia/Perth', text: '(GMT+08:00) Perth'},
      {value: 'Asia/Taipei', text: '(GMT+08:00) Taipei'},
      {value: 'Asia/Tokyo', text: '(GMT+09:00) Osaka, Sapporo, Tokyo'},
      {value: 'Asia/Seoul', text: '(GMT+09:00) Seoul'},
      {value: 'Asia/Yakutsk', text: '(GMT+09:00) Yakutsk'},
      {value: 'Australia/Adelaide', text: '(GMT+09:30) Adelaide'},
      {value: 'Australia/Darwin', text: '(GMT+09:30) Darwin'},
      {value: 'Australia/Brisbane', text: '(GMT+10:00) Brisbane'},
      {value: 'Australia/Canberra', text: '(GMT+10:00) Canberra, Melbourne, Sydney'},
      {value: 'Australia/Hobart', text: '(GMT+10:00) Hobart'},
      {value: 'Pacific/Guam', text: '(GMT+10:00) Guam, Port Moresby'},
      {value: 'Asia/Vladivostok', text: '(GMT+10:00) Vladivostok'},
      {value: 'Asia/Magadan', text: '(GMT+11:00) Magadan, Solomon Is., New Caledonia'},
      {value: 'Pacific/Auckland', text: '(GMT+12:00) Auckland, Wellington'},
      {value: 'Pacific/Fiji', text: '(GMT+12:00) Fiji, Kamchatka, Marshall Is.'},
      {value: 'Pacific/Tongatapu', text: '(GMT+13:00) Nuku alofa'}
    ])

  .constant('lattisErrors', {
    OK: 200,
    EntryCreated: 201,
    BadRequest: 400,
    Unauthorized: 401,
    Forbidden: 403,
    ResourceNotFound: 404,
    MethodNotAllowed: 405,
    Conflict: 409,
    TokenInvalid: 412,
    InternalServer: 500
  })

  .config(['$locationProvider', '$httpProvider', function ($locationProvider, $httpProvider) {
    $locationProvider.hashPrefix('')

    $httpProvider.defaults.withCredentials = true
    $httpProvider.interceptors.push('authInterceptor')
  }])

  .run(
    ['$rootScope', '$state', '$stateParams',
      function ($rootScope, $state, $stateParams) {
        $rootScope.$state = $state
        $rootScope.$stateParams = $stateParams
      }
    ]
  )

  .config(function ($provide) {
    $provide.decorator('$uiViewScroll', function ($delegate) {
      return function () {
        window.scrollTo(0, (top - 30))
      }
    })
  })

  .config(['ChartJsProvider', function (ChartJsProvider) {
    ChartJsProvider.setOptions({ maintainAspectRatio: false })
  }])

  .config(['ngDialogProvider', function (ngDialogProvider) {
    ngDialogProvider.setDefaults({
      className: 'ngdialog-theme-default',
      showClose: true,
      closeByDocument: false
    })
  }])

  .config(function (ngIntlTelInputProvider) {
    ngIntlTelInputProvider.set({
      defaultCountry: 'us',
      utilsScript: 'libs/intl-tel-input/build/js/utils.js'
    })
  })

  .config(
    [
      '$stateProvider',
      '$urlRouterProvider',
      '$logProvider',
      function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('/login')
        $urlRouterProvider.when('/payments', '/payments/payment-gateway')

        $stateProvider
          .state('login', {
            url: '/login',
            templateUrl: '../html/login.html',
            access: {
              restricted: true
            }
          })
          .state('password-reset', {
            url: '/password-reset/:hash/:usrType',
            templateUrl: '../html/password-reset.html',
            params: {
              hash: null,
              usrType: null
            }
          })
          .state('lattis-home', {
            url: '/home',
            templateUrl: '../html/home-page.html',
            activetab: 'home',
            title: 'Home'
          })
          .state('activity-feed', {
            /* todo has cleanup in controller */
            url: '/activity-feed',
            templateUrl: '../html/activity-feed.html',
            activetab: 'activity',
            title: 'Activity Feed'
          })
          .state('add-fleet', {
            url: '/add-fleet',
            templateUrl: '../html/add-fleet.html',
            controller: 'addFleetController',
            activetab: 'home',
            title: 'Home'
          })
          .state('live-activity', {
            url: '/live-activity',
            templateUrl: '../html/real-time-map.html',
            controller: 'realTimeMapController',
            params: {
              fleetId: null
            },
            activetab: 'live-activity',
            title: 'Trip Explorer'
          })
          .state('fleet-setup', {
            url: '/fleet-setup',
            templateUrl: '../html/fleet-setup.html',
            activetab: 'myfleet',
            title: 'Fleet Status'
          })
          .state('add-bikes', {
            url: '/add-bikes',
            templateUrl: '../html/add-bikes.html',
            activetab: '',
            title: 'Add Rides'
          })
          .state('order-new-ellipse', {
            url: '/bikefleet/order',
            templateUrl: '../html/ordernew-ellipse.html',
            activetab: 'myfleet',
            title: 'Order New Ellipse'
          })
          .state('bike-details', {
            url: '/bike-details/:bikeId',
            params: {
              bikeId: null,
              currentPage: null
            },
            templateUrl: '../html/bike-details.html',
            activetab: 'myfleet',
            title: 'Fleet Status'
          })
          .state('payments', {
            url: '/payments',
            templateUrl: '../html/payments/payments.html',
            controller: 'paymentController',
            activetab: 'payments',
            title: 'Payments'
          })
          .state({
            name: 'payments.payment_gateway',
            url: '/payment-gateway',
            templateUrl: '../html/payments/payment-gateway.html',
            title: 'Connect to Your Payment Gateway',
            controller: 'paymentGatewayController'
          })
          .state({
            name: 'payments.pay_per_use',
            url: '/pay-per-use',
            templateUrl: '../html/payments/pay-per-use.html',
            title: 'Configure Pay Per Use Pricing',
            controller: 'paymentController'
          })
          .state({
            name: 'payments.rental_fares',
            url: '/pricing-options',
            templateUrl: '../html/payments/pricing-options.html',
            title: 'Configure Rental Fares',
            controller: 'pricingOptionsController'
          })
          .state({
            name: 'payments.memberships',
            url: '/memberships',
            templateUrl: '../html/payments/memberships.html',
            title: 'Configure Memberships',
            controller: 'paymentController'
          })
          .state({
            name: 'payments.promotions',
            url: '/promotions',
            templateUrl: '../html/payments/promotions.html',
            title: 'Configure Promotions',
            controller: 'paymentController'
          })
        /* TODO Member controller pending---- */
          .state('member-list', {
            url: '/members-list',
            templateUrl: '../html/members-list.html',
            params: {
              bikeData: null
            },
            activetab: 'member-list',
            title: 'Users'
          })
          .state('member-profile', {
            url: '/member-profile/:userId',
            templateUrl: '../html/member-profile-and-trip-history.html',
            params: {
              userId: null,
              currentPage: null
            },
            activetab: 'member-list',
            title: 'Users'
          })
          .state('trip-details', {
            url: '/trip-details/:trip_id',
            templateUrl: '../html/trip-details.html',
            params: {
              trip_id: null
            },
            activetab: 'member-list',
            title: 'Trip details'
          })
          .state('analytics', {
            url: '/analytics',
            templateUrl: '../html/analytics.html',
            activetab: 'analytics',
            title: 'Analytics'
          })
          .state('profile', {
            url: '/profile',
            templateUrl: '../html/profile-settings.html',
            controller: 'profileSettingController',
            activetab: 'profile',
            title: 'Profile'
          })
          .state('reports', {
            url: '/reports',
            templateUrl: '../html/reports.html',
            activetab: 'reports',
            title: 'Reports'
          })
        // .state('reports', {
        //     url: '/reports',
        //     templateUrl: '../html/reports-detail.html',
        //     activetab: 'reports',
        //     title: 'Report'
        // })
          .state('reports.trips', {
            url: '/trips',
            templateUrl: '../html/report-trips.html',
            activetab: 'reports',
            title: 'Report Trips'
          })
          .state('bike-locator', {
            url: '/bike-locator',
            templateUrl: '../html/bike-locator.html',
            activetab: 'bike-locator',
            title: 'Ride Locator'
          })
          .state('parking', {
            url: '/parking',
            templateUrl: '../html/parking.html',
            activetab: 'parking',
            title: 'Parking'
          })
          .state('geofencing', {
            url: '/geofencing',
            templateUrl: '../html/geofencing.html',
            activetab: 'geofencing',
            title: 'Geofencing'
          })
          .state('alert-addbikes', {
            url: '/alert-add',
            templateUrl: '../html/alert-add.html',
            activetab: 'addbike',
            title: 'Add Rides'
          })
          .state('add-member', {
            url: '/add-member',
            templateUrl: '../html/add-member.html',
            activetab: 'member-list',
            title: 'Users'
          })
          .state('live-bikes', {
            url: '/live-bikes',
            templateUrl: '../html/live-bikes-status.html',
            activetab: 'live-bikes',
            params: {
              bikeData: null
            },
            title: 'Live'
          })
          .state('staged-bikes', {
            url: '/staged-bikes',
            templateUrl: '../html/staged-bike-status.html',
            activetab: 'staged-bikes',
            params: {
              bikeData: null
            },
            title: 'Staging'
          })
          .state('out-of-service-bikes', {
            url: '/out-of-service-bikes',
            templateUrl: '../html/out-of-service-bikes-status.html',
            activetab: 'out-of-service-bikes',
            params: {
              bikeData: null
            },
            title: 'Out of Service'
          })
          .state('archived-bikes', {
            url: '/archived-bikes',
            templateUrl: '../html/archived-bikes-status.html',
            activetab: 'archived-bikes',
            title: 'Archived'
          })
          .state('accountSettings', {
            url: '/account-settings/:not',
            templateUrl: '../html/account-settings.html',
            controller: 'accountSettingController',
            title: 'Account Settings',
            params: {
              isFirst: null,
              not: null
            }
          })
          .state('add-customer', {
            url: '/add-customer',
            templateUrl: '../html/add-customer.html',
            controller: 'addCustomerController',
            title: 'Customer onboarding',
            activetab: 'add-customer'
          })
          .state('under-development', {
            url: '/under-development',
            templateUrl: '../html/templates/under-development.html',
            title: 'Reports'
          })
          .state('hubs-live', {
            url: '/hubs-live',
            templateUrl: '../html/hubs/live.html',
            title: 'Live Hubs',
            activetab: 'live-hubs'
          })
          .state('hubs-staging', {
            url: '/hubs-staging',
            templateUrl: '../html/hubs/staging.html',
            title: 'Hubs in Staging',
            activetab: 'staging-hubs'
          })
          .state('hubs-add', {
            url: '/hubs',
            templateUrl: '../html/hubs/add.html',
            title: 'Add Hubs'
          })
          .state('hub-edit', {
            url: '/hubs/:uuid/edit',
            templateUrl: '../html/hubs/edit.html',
            params: {
              uuid: null
            },
            title: 'Edit a Hub'
          })
          .state('hub', {
            url: '/hubs/:uuid',
            templateUrl: '../html/hubs/overview.html',
            params: { uuid: null },
            title: 'Hub Details'
          })
          .state({
            name: 'hub.details',
            url: '/details',
            templateUrl: '../html/hubs/details.html',
            title: 'Hub Details'
          })
          .state({
            name: 'hub.ports',
            url: '/ports',
            templateUrl: '../html/hubs/ports.html',
            title: 'Hub Ports'
          })
          .state('port-details',{
            url: '/port/:uuid',
            templateUrl: '../html/hubs/port-details.html',
            params: { uuid: null },
            title: 'Port Details'
          })
      }
    ])

  .run([
    '$rootScope',
    '$state',
    'authFactory',
    function ($rootScope, $state, authFactory) {
      /* Redirects all routes to login if the user is not authorized */
      $rootScope.$on('$locationChangeStart', function (event, next) {
        if (!authFactory.isAuthenticated()) {
          if (_.indexOf(next.split('/'), 'password-reset') !== 4 && _.indexOf(['login'], next.split('/').pop()) === -1) {
            $state.go('login')
            event.preventDefault()
          }
        }
      })
    }
  ]
  )

angular.module('skyfleet.controllers', [])
