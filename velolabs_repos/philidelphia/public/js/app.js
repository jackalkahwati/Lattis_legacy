var app = angular.module('EllipseInfoUpdate', ['ui.router', 'ngAnimate']);

app.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
	//DEFAULT URL CONFIG
	$urlRouterProvider.otherwise('/home');


	//DEFAULT URL CONFIG WHEN /REGISTER
	$urlRouterProvider.when('/register', '/register/register_email');

	//DEFAULT URL CONFIG WHEN /CONFIRM
	$urlRouterProvider.when('/confirm', '/confirm/confirm_shipping');

	$stateProvider

		//HOME STATE 
		.state('home',{
			url: '/home',
            controller: 'homeController',
			templateUrl: 'partials/home.jade'
		})

		//REGISTRATION STATE & NEST(S)

		.state('registration',{
			url: '/register',
			controller: 'registrationController',
			templateUrl: 'partials/registration.jade'
		})

		.state('registration.email',{
			url: '/register_email',
			templateUrl: 'partials/registration.email.jade'
		})

		.state('registration.code',{
			url: '/register_code',
			templateUrl: 'partials/registration.code.jade'
		})

		.state('registration.reject',{
			url: '/register_deny',
			templateUrl: 'partials/registration.deny.jade'
		})

		//CONFIRMATION STATE & NEST(S)
		.state('confirmation',{
			url: '/confirm',
			controller: 'shippingController',
			templateUrl: 'partials/confirm.jade'
		})

		.state('confirmation.address1',{
			url: '/address1',
			templateUrl: 'partials/confirm.address1.jade'
		})

        .state('confirmation.address2',{
            url: '/address2',
            templateUrl: 'partials/confirm.address2.jade'
        })

        .state('confirmation.city',{
            url: '/city',
            templateUrl: 'partials/confirm.city.jade'
        })

        .state('confirmation.state',{
            url: '/state',
            templateUrl: 'partials/confirm.state.jade'
        })

		.state('confirmation.zip',{
			url:'/zip',
			templateUrl: 'partials/confirm.zip.jade'
		})

		.state('confirmation.country',{
			url: '/country',
			templateUrl: 'partials/confirm.country.jade'
		})

		//LAST PAGE INTRO STATE
		.state('final_intro',{
			url:'/final_intro',
			templateUrl: 'partials/final_intro.jade',
			controller: 'shippingController'
		})

		//ORDER COLOR STATE

		.state('order_color',{
			url: '/order_color',
			controller: 'colorController',
			templateUrl: 'partials/order_color.jade'
		})

		//FINAL CONFIRMATION STATE
		.state('confirmed',{
			url: '/confirmed',
			templateUrl: 'partials/confirmed.jade'
		})

		.state('verified',{
			url: '/register_verified',
			templateUrl: 'partials/registration.verified.jade'
		})
}]);
