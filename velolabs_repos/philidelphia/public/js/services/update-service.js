app.service(
    'updateService',
    [
        '$http',
        '$q',
        function($http, $q) {
            var user = null;
            var enteredCode = null;

            var getUserForCode = function(userCode, callback) {
                var postData = JSON.stringify({
                    userCode: userCode
                });

                $http.post('/api/get-user', postData)
                    .success(function(returnedData) {
                        if (returnedData.error) {
                            callback(false);
                            return;
                        }

                        if (returnedData.statusCode === 200 && returnedData.payload) {
                            user = returnedData.payload;
                            user.colors = JSON.parse(user.colors);
                            callback(true);
                            return;
                        }

                        callback(false);
                    })
                    .error(function(error) {
                        callback(false);
                    });
            };

            var getUser = function() {
                return user;
            };

            var setNewEmail = function(newEmail) {
                if (user) {
                    user.new_email = newEmail;
                }
            };

            var setEnteredCode = function(code) {
                enteredCode = code;
            };

            var verifyUser = function(callback) {
                if (!user) {
                    callback(false);
                    return
                }

                var postData = JSON.stringify({
                    user: user,
                    enteredCode: enteredCode
                });

                $http.post('/api/verify-user', postData)
                    .success(function(returnedData) {
                        if (returnedData.error) {
                            callback(false);
                            return;
                        }

                        if (returnedData.statusCode === 200 &&
                            returnedData.payload &&
                            _.has(returnedData.payload, 'verified'))
                        {   
                            callback(returnedData.payload.verified);
                            return;
                        }

                        callback(null);
                    })
                    .error(function(error) {
                        callback(null);
                    });
            };

            var updateUser = function(callback) {
                if (!user) {
                    callback(false);
                    return;
                }

                var postData = JSON.stringify({user: user});
                $http.post('/api/update-user', postData)
                    .success(function(returnedData) {
                        if (returnedData.error) {
                            callback(false);
                            return;
                        }

                        if (returnedData.statusCode === 200 &&
                            returnedData.payload &&
                            _.has(returnedData.payload, 'updated'))
                        {
                            callback(returnedData.payload.updated);
                            return;
                        }

                        callback(false);
                    })
                    .error(function(error) {
                        callback(false);
                    });
            };

            return {
                getUserForCode: getUserForCode,
                getUser: getUser,
                setNewEmail: setNewEmail,
                setEnteredCode: setEnteredCode,
                verifyUser: verifyUser,
                updateUser: updateUser
            };
        }
    ]
);
