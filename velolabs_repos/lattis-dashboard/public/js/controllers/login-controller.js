'use strict'

angular.module('skyfleet.controllers').controller(
  'loginController',
  function ($scope, authFactory, sessionFactory, $rootScope, $state, $window, $timeout, utilsFactory, regexPatterns) {
    $rootScope.preLoader = true
    const [patterns] = regexPatterns
    $scope.emailRegexPatterns = patterns.email

    angular.element(document).ready(function () {
      $rootScope.preLoader = false
    })
    var login_body = utilsFactory.getValueof('fullheight_slider')
    var windowsize = angular.element($window).height()
    login_body.style.height = windowsize + 'px'
    $scope.resizeWindow = function () {
      if (login_body) {
        login_body.style.height = angular.element($window).height() + 'px'
      }
    }

    angular.element($window).on('resize', function () {
      $scope.resizeWindow()
    })

    $timeout(function () {
      $scope.resizeWindow()
    }, 1)

    // Session
    $scope.session = function () {
      if (authFactory.isAuthenticated()) {
        $rootScope.name = sessionFactory.getCookieUser()
        $state.go('lattis-home')
      } else {
        if ($state.current.name != 'login') {
          $state.go('login')
        }
      }
    }
    // Login
    $scope.login = function () {
      var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/igm

      $scope.valid = true
      $scope.divHtmlVar = ''

      // Validations
      if ($scope.loginForm.email == '' || !$scope.loginForm.hasOwnProperty('email')) {
        $scope.divHtmlVar = $scope.divHtmlVar + '<br/><i>Email is Blank!</i>'
        $scope.valid = false
      } else if (re.test($scope.loginForm.email)) {
        $scope.valid = true
      } else {
        $scope.divHtmlVar = $scope.divHtmlVar + '<br/><i>Email is not Valid!</i>'
        $scope.valid = false
      }
      if ($scope.loginForm.password == '' || !$scope.loginForm.hasOwnProperty('password')) {
        $scope.divHtmlVar = $scope.divHtmlVar + '<br/><i>Password is Blank!</i>'
        $scope.valid = false
      }
      // initial values
      if ($scope.valid) {
        // calling login function to factories
        $scope.payload = {email: $scope.loginForm.email, password: $scope.loginForm.password}
        $scope.disabled = true
        authFactory.login($scope.payload, function (error, response) {
          $scope.disabled = false

          if (error) {
            $scope.error = true
            $scope.errorMessage = "Email and password don't match!"
            $timeout(function () {
              $scope.error = false
            }, 2000)
          } else {
            $scope.success = true

            authFactory.setAuthToken(response.token)

            sessionFactory.setCookieData({
              name: response.operator_info.operator_name,
              operator_id: response.operator_info.operator_id
            })
            response.operator_info.acl ? sessionStorage.setItem('super_user', true) : sessionStorage.setItem('super_user', false)
            localStorage.setItem('acl', JSON.stringify(response.fleet_info))
            $scope.successMessage = 'Login Successful!'
            if (response.operator_info.phone_number) {
              $state.go('lattis-home')
            } else {
              $state.go('accountSettings', {isFirst: true})
            }

            $scope.loginForm = {}
            $timeout(function () {
              $scope.success = false
            }, 2000)
          }
        })
      }
    }

    $scope.forgetactive = true
    var firstPanel = angular.element(document.querySelector('#panel1'))
    var lastPanel = angular.element(document.querySelector('#panel2'))
    firstPanel.addClass('flipInX')
    lastPanel.addClass('flipInY')

    $scope.forgetActive = function () {
      $scope.divHtmlVar = ''
      $scope.forgetactive = !$scope.forgetactive
      $scope.resetHeader = 'Reset your password'
      $scope.resetInfo = "Enter the email address associated with your Lattis account and we'll send you a reset link."
      $scope.resetInfo2 = ''
      angular.element(document.querySelector('#reset-info2')).removeClass('p-b-50')
      $scope.disableReset = false
    }

    $scope.email_confirm = false
    $scope.reset = function () {
      $scope.disableReset = true
      authFactory.getResetLink({
        email: $scope.loginForm.email
      }, function (response) {
        if (response && response.status === 200) {
          $scope.divHtmlVar = ''
          $scope.email_confirm = !$scope.email_confirm
          $scope.resetHeader = 'Reset link sent'
          angular.element(document.querySelector('#reset-info2')).addClass('p-b-50')
          $scope.resetInfo = 'A password link has been sent to the email address you provided.'
          $scope.resetInfo2 = "If you don't see it in a couple minutes, check your spam folder. It was sent from support@lattis.io"
        } else {
          $scope.divHtmlVar = ''
          $scope.resetHeader = 'Reset link not sent'
          // angular.element(document.querySelector('#reset-info2')).addClass('p-b-50');
          $scope.resetInfo = 'Email ID which you have provided is not associated to any lattis users please try agains'
        }
      })
      // TO DO: SERVER CALLS TO RESET LINK
    }

    // Register
    $scope.registerUser = function () {
      var regularExpOfEmail = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/igm

      $scope.valid = true
      $scope.ErrorMsg = ''

      // Validations
      if ($scope.registerForm.email == '' || !$scope.registerForm.hasOwnProperty('email')) {
        $scope.ErrorMsg = $scope.ErrorMsg + '<br/><i>Email Id is Blank!</i>'
        $scope.valid = false
      } else if (regularExpOfEmail.test($scope.registerForm.email)) {
        $scope.valid = true
      } else {
        $scope.ErrorMsg = $scope.ErrorMsg + '<br/><i>EmailId is not Valid!</i>'
        $scope.valid = false
      }
      if ($scope.registerForm.password == '' || !$scope.registerForm.hasOwnProperty('password')) {
        $scope.ErrorMsg = $scope.ErrorMsg + '<br/><i>Password is Blank!</i>'
        $scope.valid = false
      }
      if ($scope.registerForm.firstName == '' || !$scope.registerForm.hasOwnProperty('firstName')) {
        $scope.ErrorMsg = $scope.ErrorMsg + '<br/><i>Firstname is Blank!</i>'
        $scope.valid = false
      }
      if ($scope.registerForm.lastName == '' || !$scope.registerForm.hasOwnProperty('lastName')) {
        $scope.ErrorMsg = $scope.ErrorMsg + '<br/><i>Lastname is Blank!</i>'
        $scope.valid = false
      }

      if ($scope.valid) {
        // initial values
        $scope.error = false
        $scope.success = false

        // calling register function to factories
        $scope.payload = {
          email: $scope.registerForm.email,
          password: $scope.registerForm.password,
          firstName: $scope.registerForm.firstName,
          lastName: $scope.registerForm.lastName
        }

        authFactory.register($scope.payload, function (error, response) {
          if (error) {
            $scope.error = true
            $scope.errorMessage = 'Something went wrong!'
            $scope.registerForm = {}
            $timeout(function () {
              $scope.error = false
            }, 2000)
          } else {
            $scope.success = true
            $scope.successMessage = 'Registration Successful!'
            $scope.loginheader = 'Login'
            $scope.registerClick = false
            $scope.registerForm = {}
            $timeout(function () {
              $scope.success = false
            }, 2000)
          }
        })
      }
    }

    $scope.registerBack = function () {
      $scope.registerClick = false
      $scope.loginheader = 'Login'
    }

    $rootScope.showLoader = false
  })
