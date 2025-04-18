'use strict'

angular.module('skyfleet').factory('authFactory', [
  '$http',
  '$window',
  function ($http, $window) {
    return {
      setAuthToken: function(token) {
        $window.localStorage.setItem('token', token)
      },
      removeAuthToken: function() {
        $window.localStorage.removeItem('token')
      },
      retrieveAuthToken: function() {
        return $window.localStorage.getItem('token') || null
      },
      isAuthenticated: function() {
        return !!this.retrieveAuthToken()
      },
      getAuth: function() {
        const token = this.retrieveAuthToken()

        return {
          token,
          isAuthenticated: !!token
        }
      },
      decodeTokenPayload: function () {
        const token = this.retrieveAuthToken()

        if (!token) {
          return ''
        }

        const [, payload] = token.split('.')
        try {
          return JSON.parse(atob(payload))
        } catch (error) {
          return ''
        }
      },
      login: function (payload, done) {
        $http.post('/api/user/login', JSON.stringify(payload))
          .then(function (response) {
            done(null, response.data.payload)
          }, function (error) {
            done(error, null)
          })
      },
      logout: function (done) {
        this.removeAuthToken()

        $http.get('/api/user/logout')
          .then(function (response) {
            done(null, response.data.payload)
          }, function (error) {
            console.log(error)
            done(error, null)
          })
      },
      register: function (payload, done) {
        $http.post('/api/user/register', JSON.stringify(payload))
          .then(function (response) {
            done(null, response.data.payload)
          }, function (error) {
            done(error, null)
          })
      },
      validateToken: function (data, callback) {
        $http.post('/api/user/validate-token', JSON.stringify(data))
          .then(function (response) {
            callback(response.data)
          }, function (error) {
            callback(error)
          })
      },
      resetPassword: function (data, callback) {
        $http.post('/api/user/change-password', JSON.stringify(data))
          .then(function (response) {
            callback(response.data)
          }, function (error) {
            callback(error)
          })
      },

      verifyPassword: function (data, callback) {
        $http.post('api/user/verify-password', JSON.stringify(data))
          .then(function (response) {
            callback(response.data)
          }, function (error) {
            callback(null, error)
          })
      },
      getResetLink: function (data, callback) {
        $http.post('/api/user/forgot-password', JSON.stringify(data))
          .then(function (response) {
            callback(response.data)
          }, function (error) {
            callback(error)
          })
      },
      passwordReset: async (data) => {
        try {
          return await $http.post('/api/user/change-password-internally', JSON.stringify(data))
        } catch (error) {
          throw new Error((error.data && error.data.error) || "An error occurred generating new tokens")
        }
      }
    }
  }
])
.factory('authInterceptor', function ($q, $rootScope, $window, $state) {
  return {
    request: function (config) {
      const token = $window.localStorage.getItem('token') || null
      const isAuthenticated = !!token

      if (isAuthenticated) {
        return {
          ...config,
          headers: {
            ...config.headers,
            Authorization: `Bearer ${token}`
          }
        }
      }

      return config
    },
    responseError: function (response) {
      if (response.status === 401) {
        $window.localStorage.removeItem('token')
        $rootScope.showAfterLoad = false
        $state.go('login')
      }

      return $q.reject(response)
    }
  }
})
