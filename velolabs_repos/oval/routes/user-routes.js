'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const errors = platform.errors
const encryptionHandler = platform.encryptionHandler
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const stripeController = require('../handlers/payments/gateways/stripe')
const ovalResponse = require('./../utils/oval-response')
const { getAppDetails } = require('../utils/routes')
const userHandler = require('./../handlers/user-handler')
const requestHelper = require('./../helpers/request-helper')
const lockHandler = require('./../handlers/lock-handler')
const bikeHandler = require('./../handlers/bike-handler')
const userTypes = require('./../constants/user-types')
const confirmationCodeTypes = require('./../constants/confirmation-code-types')
const throttleHandler = platform.throttleHandler
const throttleTypes = platform.throttleTypes
const accountTypes = require('../constants/account-types')
const privateAccountHandler = require('./../handlers/private-account-handler')
const paymentHandler = require('../handlers/payment-handler')
const {configureSentry} = require('../utils/configureSentry')
const { responseCodes } = require('@velo-labs/platform')
const db = require('../db')

const Sentry = configureSentry()

// for user registration
router.post('/registration', (req, res) => {
  if (!userHandler.isUserValid(req.body)) {
    logger('Could not register user. The user is invalid')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  userHandler.processUser(req.body, (error, user) => {
    if (error) {
      logger('Error: processing user:', error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }

    ovalResponse(res, errors.noError(), {
      user_id: user.user_id,
      verified: !!user.verified
    })
  })
})

// adding new private account
router.post('/add-private-account', (req, res) => {
  if (!_.has(req.body, 'email')) {
    logger('Could not add private account. There is no email in req body')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(
    req,
    res,
    true,
    async (error, user) => {
      if (error) return

      const appDetails = await getAppDetails(req)

      privateAccountHandler.processPrivateAccount(
        user,
        req.body.email,
        appDetails,
        (error, lattisAccount) => {
          if (error) {
            logger(
              'Error: could not add private account for email:',
              req.body.email,
              'Error:',
              error
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          ovalResponse(res, errors.noError(), {
            lattis_account: lattisAccount
          })
        }
      )
    }
  )
})

// for getting new tokens
router.post('/new-tokens', (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'password')) {
    logger('Error: getting tokens. Missing parameters in request body')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (throttleHandler.handleRequest(res, req, throttleTypes.newTokens)) {
    userHandler.getRestTokens(
      req.body.user_id,
      req.body.password,
      (error, newTokens) => {
        if (error) {
          logger('Error: getting tokens. Failed to update tokens.')
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(res, errors.noError(), newTokens)
      }
    )
  }
})

// for getting new refresh tokens
router.post('/refresh-tokens', (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'refresh_token')) {
    logger('Error: refreshing tokens. Missing parameters in request body')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (throttleHandler.handleRequest(res, req, throttleTypes.refreshTokes)) {
    userHandler.refreshRestTokens(
      req.body.user_id,
      req.body.refresh_token,
      (error, newTokens) => {
        if (error) {
          logger('Error: refreshing tokens. Failed to update tokens.')
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(res, errors.noError(), newTokens)
      }
    )
  }
})

// for getting user information
router.post('/get-user', (req, res) => {
  requestHelper.userAuthorizationValidator(
    req,
    res,
    true,
    async (error, user) => {
      if (error) return
      const appType = req.headers['user-agent']
      const whiteLabelFleetIds = appType
        ? await bikeHandler.getAppFleets(appType)
        : []
      if (_.has(req.body, 'user_id')) {
        userHandler.getUserWithUserId(req.body.user_id, (error, user) => {
          if (error) {
            logger(
              'Error: could not retrieve user with user id:',
              req.body.user_id,
              'Error:',
              error
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          if (!user) {
            ovalResponse(res, errors.resourceNotFound(true), null)
            return
          }

          if (user.user_type === userTypes.lattis) {
            privateAccountHandler.getPrivateAccount(
              { user_id: req.body.user_id, verified: 1, access: 1 },
              (error, privateAccount) => {
                if (error) {
                  ovalResponse(res, errors.formatErrorForWire(error), null)
                  return
                }
                privateAccountHandler.formatPrivateAccountForWire(
                  privateAccount,
                  (error, formattedPrivateAccount) => {
                    if (error) {
                      ovalResponse(res, errors.formatErrorForWire(error), null)
                      return
                    }
                    user.phone_number = user.phone_number
                      ? user.phone_number
                      : ''
                    if (
                      whiteLabelFleetIds.length &&
                      formattedPrivateAccount.length
                    ) {
                      const whiteLabelAccounts = formattedPrivateAccount.filter(
                        (account) =>
                          whiteLabelFleetIds.includes(account.fleet_id)
                      )
                      ovalResponse(res, errors.noError(), {
                        user: userHandler.formatUserForWire(user, true),
                        private_account: whiteLabelAccounts
                      })
                      return
                    }
                    ovalResponse(res, errors.noError(), {
                      user: userHandler.formatUserForWire(user, true),
                      private_account: formattedPrivateAccount
                    })
                  }
                )
              }
            )
          } else {
            ovalResponse(res, errors.noError(), {
              user: userHandler.formatUserForWire(user, true)
            })
          }
        })
      } else {
        if (user.user_type === userTypes.lattis) {
          privateAccountHandler.getPrivateAccount(
            { user_id: user.user_id, verified: 1, access: 1 },
            (error, privateAccount) => {
              if (error) {
                ovalResponse(res, errors.formatErrorForWire(error), null)
                return
              }
              privateAccountHandler.formatPrivateAccountForWire(
                privateAccount,
                (error, formattedPrivateAccount) => {
                  if (error) {
                    ovalResponse(res, errors.formatErrorForWire(error), null)
                    return
                  }
                  user.phone_number = user.phone_number
                    ? user.phone_number
                    : ''
                  if (
                    whiteLabelFleetIds.length &&
                    formattedPrivateAccount.length
                  ) {
                    const whiteLabelAccounts = formattedPrivateAccount.filter(
                      (account) => whiteLabelFleetIds.includes(account.fleet_id)
                    )
                    ovalResponse(res, errors.noError(), {
                      user: userHandler.formatUserForWire(user, true),
                      private_account: whiteLabelAccounts
                    })
                    return
                  }
                  ovalResponse(res, errors.noError(), {
                    user: userHandler.formatUserForWire(user, true),
                    private_account: formattedPrivateAccount
                  })
                }
              )
            }
          )
        } else {
          ovalResponse(res, errors.noError(), {
            user: userHandler.formatUserForWire(user, true)
          })
        }
      }
    }
  )
})

// for getting current status of user
router.all('/get-current-status', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      Sentry.captureException(error, req)
      return
    }
    userHandler.getCurrentStatus(_.pick(user, 'user_id'), (error, payload) => {
      if (error) {
        Sentry.captureException(error)
        logger(
          'Error: getting current status for user with user_id:',
          user.user_id,
          'with error:',
          errors.errorWithMessage(error)
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }
      // check and update if device details are sent
      if (!!req.body.device_model && !!req.body.device_os) {
        userHandler.processUserMetadata(
          user,
          _.pick(req.body, 'device_model', 'device_os', 'device_language'),
          (error) => {
            if (error) {
              Sentry.captureException(error)
              logger(
                'Error: getting current status for user with user_id:',
                user.user_id,
                'Failed to save user metadata with error:',
                errors.errorWithMessage(error)
              )
              ovalResponse(res, errors.internalServer(true), null)
              return
            }
            ovalResponse(res, errors.noError(), payload)
          }
        )
      } else {
        ovalResponse(res, errors.noError(), payload)
      }
    })
  })
})

// for updating user
router.post('/update-user', (req, res) => {
  if (!_.has(req.body, 'properties')) {
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    userHandler.updateUser(user, req.body.properties, (error, updatedUser) => {
      if (error) {
        logger(
          'Error: could not update user with id',
          user.users_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      if (user.user_type === userTypes.lattis) {
        privateAccountHandler.getPrivateAccount(
          { user_id: req.body.user_id, verified: 1 },
          (error, privateAccount) => {
            if (error) {
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }
            privateAccountHandler.formatPrivateAccountForWire(
              privateAccount,
              (error, formattedPrivateAccount) => {
                if (error) {
                  ovalResponse(res, errors.formatErrorForWire(error), null)
                  return
                }
                ovalResponse(res, errors.noError(), {
                  user: userHandler.formatUserForWire(user, false),
                  private_account: formattedPrivateAccount
                })
              }
            )
          }
        )
      } else {
        ovalResponse(res, errors.noError(), {
          user: userHandler.formatUserForWire(updatedUser)
        })
      }
    })
  })
})

router.post('/sign-in-code', async (req, res) => {
  if (!_.has(req.body, 'user_id')) {
    logger(
      'Error: could not text user verification code. Missing params in request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (throttleHandler.handleRequest(res, req, throttleTypes.signInCode)) {
    const appDetails = await getAppDetails(req)

    userHandler.textUserVerificationCode(
      req.body.user_id,
      confirmationCodeTypes.signIn,
      appDetails,
      (error) => {
        if (error) {
          logger(
            'Error: failed to text verification code to #:',
            req.body.phone_number,
            'Failed with error:',
            error
          )
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(res, errors.noError(), null)
      }
    )
  }
})

router.post('/confirm-user-code', (req, res) => {
  if (!_.has(req.body, 'confirmation_code') || !_.has(req.body, 'user_id')) {
    logger('Failed to confirm user code. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(res, req, throttleTypes.signInConformation)
  ) {
    userHandler.checkConfirmationCode(
      req.body.user_id,
      req.body.confirmation_code,
      confirmationCodeTypes.signIn,
      (error, updatedUser) => {
        if (error) {
          logger(
            'Error: failed to confirm confirmation code for user with id:',
            req.body.user_id,
            'Failed with error:',
            error
          )
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(
          res,
          errors.noError(),
          userHandler.formatUserForWire(updatedUser, true, true)
        )
      }
    )
  }
})

router.post('/forgot-password-code', (req, res) => {
  if (!_.has(req.body, 'phone_number')) {
    logger(
      'Failed to send forgot password code. Missing parameters in request body.'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(res, req, throttleTypes.forgotPasswordCode)
  ) {
    userHandler.getUserWithPhoneNumber(
      req.body.phone_number,
      req.body.country_code,
      async (error, user) => {
        if (error) {
          logger(
            'Error: failed to send forgot password code. Error retrieving user:',
            req.body.phone_number
          )
          ovalResponse(res, errors.internalServer(true), null)
          return
        }

        if (!user) {
          logger(
            'Error: failed to send forgot password code. No user in db with #:',
            req.body.phoneNumber
          )
          ovalResponse(res, errors.resourceNotFound(true), null)
          return
        }

        if (user.user_type !== userTypes.ellipse) {
          logger(
            'Failed to send forgot password code. User is not of type ellipse.'
          )
          ovalResponse(res, errors.invalidParameter(true), null)
          return
        }

        const appDetails = await getAppDetails(req)

        userHandler.textUserVerificationCode(
          user.user_id,
          confirmationCodeTypes.forgotPassword,
          appDetails,
          (error) => {
            if (error) {
              logger(
                'Error: failed to text password reset code to user:',
                user.user_id,
                'Failed with error:',
                error
              )
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }

            ovalResponse(res, errors.noError(), null)
          }
        )
      }
    )
  }
})

router.post('/forgot-password', (req, res) => {
  if (!_.has(req.body, 'email')) {
    logger(
      'Failed to send forgot password code. Missing parameters in request body.'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(res, req, throttleTypes.forgotPasswordCode)
  ) {
    userHandler.getUserWithUsersId(req.body.email, async (error, user) => {
      // App name and language
      const appDetails = await getAppDetails(req)
      if (error) {
        logger(
          'Error: failed to send forgot password code. Error retrieving user:',
          req.body.email
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      if (!user) {
        logger(
          'Error: failed to send forgot password code. No user in db with email:',
          req.body.email
        )
        ovalResponse(res, errors.resourceNotFound(true), null)
        return
      }

      if (user.user_type !== userTypes.lattis) {
        logger(
          'Failed to send forgot password code. User is not of type lattis.'
        )
        ovalResponse(res, errors.invalidParameter(true), null)
        return
      }

      userHandler.emailUserConfirmationCode(
        user.user_id,
        confirmationCodeTypes.forgotPassword,
        false,
        appDetails,
        (error) => {
          if (error) {
            logger(
              'Error: failed to email verification code to',
              req.body.email,
              'Failed with error:',
              error
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          ovalResponse(res, errors.noError(), null)
        }
      )
    })
  }
})

router.post('/confirm-forgot-password-code', (req, res) => {
  if (
    !_.has(req.body, 'confirmation_code') ||
    !_.has(req.body, 'phone_number') ||
    !_.has(req.body, 'password')
  ) {
    logger(
      'Failed to confirm forgot password code. Missing parameters in request body.'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.forgotPasswordConfirmation
    )
  ) {
    userHandler.getUserWithPhoneNumber(
      req.body.phone_number,
      req.body.country_code,
      (error, user) => {
        if (error) {
          logger(
            'Failed to confirm forgot password code for phone number:',
            req.body.phone_number
          )
          ovalResponse(res, errors.internalServer(true), null)
          return
        }

        if (!user) {
          logger(
            'Failed to confirm forgot password code for phone_number:',
            req.body.phone_number,
            'no user in db'
          )
          ovalResponse(res, errors.resourceNotFound(true), null)
          return
        }

        userHandler.forgotPassword(
          user,
          req.body.confirmation_code,
          req.body.password,
          (error) => {
            if (error) {
              logger(
                'Error: could not change forgotten user password in confirm-forgot-password-code'
              )
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }

            ovalResponse(res, errors.noError(), null)
          }
        )
      }
    )
  }
})

router.post('/confirm-forgot-password', (req, res) => {
  if (
    !_.has(req.body, 'confirmation_code') ||
    !_.has(req.body, 'email') ||
    !_.has(req.body, 'password')
  ) {
    logger(
      'Failed to confirm forgot password code. Missing parameters in request body.'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.forgotPasswordConfirmation
    )
  ) {
    userHandler.getUserWithUsersId(req.body.email, (error, user) => {
      if (error) {
        logger(
          'Failed to confirm forgot password code for phone number:',
          req.body.phone_number
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      if (!user) {
        logger(
          'Failed to confirm forgot password code for email:',
          req.body.email,
          'no user in db'
        )
        ovalResponse(res, errors.resourceNotFound(true), null)
        return
      }

      userHandler.forgotPassword(
        user,
        req.body.confirmation_code,
        req.body.password,
        (error) => {
          if (error) {
            logger(
              'Error: could not change forgotten user password in confirm-forgot-password-code'
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          ovalResponse(res, errors.noError(), null)
        }
      )
    })
  }
})

router.get('/update-password-code', (req, res) => {
  if (
    throttleHandler.handleRequest(res, req, throttleTypes.updatePasswordCode)
  ) {
    requestHelper.userAuthorizationValidator(
      req,
      res,
      true,
      async (error, user) => {
        if (error) return

        if (user.user_type !== userTypes.ellipse) {
          logger(
            'Failed to send forgot password code. User is not of type ellipse or lattis.'
          )
          ovalResponse(res, errors.unauthorizedAccess(true), null)
          return
        }

        const appDetails = await getAppDetails(req)

        userHandler.textUserVerificationCode(
          user.user_id,
          confirmationCodeTypes.updatePassword,
          appDetails,
          (error) => {
            if (error) {
              logger(
                'Error: failed to text password update code to user:',
                user.user_id,
                'Failed with error:',
                error
              )
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }
            ovalResponse(res, errors.noError(), null)
          }
        )
      }
    )
  }
})

router.post('/update-password', (req, res) => {
  if (!_.has(req.body, 'password') || !_.has(req.body, 'confirmation_code')) {
    logger(
      'Error: could not update password. Missing parameters in the request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.updatePasswordConfirmation
    )
  ) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return

      userHandler.updatePassword(
        user,
        req.body.confirmation_code,
        req.body.password,
        (error) => {
          if (error) {
            logger(
              'Error: could not update user password in update-password endpoint'
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          ovalResponse(res, errors.noError(), null)
        }
      )
    })
  }
})

router.post('/change-password', (req, res) => {
  if (!_.has(req.body, 'password') || !_.has(req.body, 'new_password')) {
    logger(
      'Error: could not update password. Missing parameters in the request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.updatePasswordConfirmation
    )
  ) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return

      if (encryptionHandler.verifyPassword(req.body.password, user.password)) {
        userHandler.updatePassword(
          user,
          null,
          req.body.new_password,
          (error) => {
            if (error) {
              logger(
                'Error: could not update user password in change-password endpoint'
              )
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }

            ovalResponse(res, errors.noError(), null)
          }
        )
      } else {
        logger(
          'Error: Unable to update password for:',
          user.user_id,
          "Password doesn't match"
        )
        return ovalResponse(res, errors.invalidPassword(true), null)
      }
    })
  }
})

router.post('/update-email-code', async (req, res) => {
  // App name and language
  const appDetails = await getAppDetails(req)
  if (!_.has(req.body, 'email')) {
    logger(
      'Error: could not send update Email code. Missing parameters in the request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (throttleHandler.handleRequest(res, req, throttleTypes.updateEmailCode)) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return

      userHandler.getUserWithUsersId(req.body.email, (error, oldUser) => {
        if (error) {
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        if (oldUser) {
          logger(
            'Error:',
            req.body.email,
            'is already registered for user',
            oldUser.user_id
          )
          ovalResponse(res, errors.duplicateUser(true), null)
          return
        }
        if (req.body.email !== user.users_id && req.body.email !== user.email) {
          userHandler.emailUserConfirmationCode(
            user.user_id,
            confirmationCodeTypes.updateEmail,
            req.body.email,
            appDetails,
            (error) => {
              if (error) {
                logger(
                  'Error: failed to send email verification code to',
                  req.body.email,
                  'Failed with error:',
                  error
                )
                ovalResponse(res, errors.formatErrorForWire(error), null)
                return
              }
              ovalResponse(res, errors.noError(), null)
            }
          )
        } else {
          ovalResponse(res, errors.duplicateUser(true), null)
        }
      })
    })
  }
})

router.post('/update-email', (req, res) => {
  if (!_.has(req.body, 'email') || !_.has(req.body, 'confirmation_code')) {
    logger(
      'Error: could not send update email code. Missing parameters in the request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.confirmEmailVerificationCode
    )
  ) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return
      userHandler.updateEmail(
        user,
        req.body.confirmation_code,
        req.body.email,
        (error) => {
          if (error) {
            logger(
              'Error: failed to confirm verification code for:',
              req.body.email,
              'Failed with error:',
              error
            )
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }
          ovalResponse(res, errors.noError(), null)
        }
      )
    })
  }
})

router.post('/update-phone-number-code', (req, res) => {
  if (!_.has(req.body, 'phone_number')) {
    logger(
      'Error: could not send update phone number code. Missing parameters in the request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(res, req, throttleTypes.updatePhoneNumberCode)
  ) {
    requestHelper.userAuthorizationValidator(
      req,
      res,
      true,
      async (error, user) => {
        if (error) return

        const appDetails = await getAppDetails(req)

        userHandler.textUserVerificationCodeForPhoneUpdate(
          user.user_id,
          req.body.phone_number,
          req.body.country_code,
          confirmationCodeTypes.updatePhoneNumber,
          appDetails,
          (error) => {
            if (error) {
              logger(
                'Error: failed to text phone number update code to user:',
                user.user_id,
                'Failed with error:',
                error
              )
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }

            ovalResponse(res, errors.noError(), null)
          }
        )
      }
    )
  }
})

router.post('/update-phone-number', (req, res) => {
  if (
    !_.has(req.body, 'phone_number') ||
    !_.has(req.body, 'confirmation_code')
  ) {
    logger(
      'Error: failed to update phone number. Missing parameter in request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.updatePhoneNumberConfirmation
    )
  ) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return

      userHandler.updatePhoneNumber(
        user,
        req.body.confirmation_code,
        req.body.phone_number,
        req.body.country_code,
        (error) => {
          if (error) {
            logger('Error: could not update user phone number.')
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }

          ovalResponse(res, errors.noError(), null)
        }
      )
    })
  }
})

router.get('/delete-account', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, false, (error, user) => {
    if (error) return
    userHandler.deleteAccount(user, (error) => {
      if (error) {
        logger(
          'Error: failed to delete user:',
          user.user_id,
          'Failed with error:',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      if (user.user_type !== userTypes.lattis) {
        lockHandler.deleteLocksForUser(user, (error) => {
          if (error) {
            logger(
              'Error: could not delete locks for user account:',
              user.user_id
            )
            return ovalResponse(res, errors.formatErrorForWire(error), null)
          }

          ovalResponse(res, errors.noError(), null)
        })
      } else {
        ovalResponse(res, errors.noError(), null)
      }
    })
  })
})

router.get('/terms-and-conditions', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    userHandler.getTermsAndConditions(user, (error, termsAndConditions) => {
      if (error) {
        logger(
          'Error: failed to retrieve terms and conditions',
          'Failed with error:',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), {
        terms_and_conditions: JSON.parse(termsAndConditions)
      })
    })
  })
})

router.post('/accept-terms-and-conditions', (req, res) => {
  if (!_.has(req.body, 'did_accept')) {
    logger(
      'Error: failed to save user action on terms and conditions. Missing parameter in request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    userHandler.acceptTermsAndConditions(user, req.body.did_accept, (error) => {
      if (error) {
        logger(
          'Error: failed to save user action on terms and conditions.',
          'Error:',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), null)
    })
  })
})

router.get('/check-accepted-terms-and-conditions', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    userHandler.hasUserAcceptedTermsAndConditions(
      user,
      (error, hasAccepted) => {
        if (error) {
          logger(
            'Error: failed to check user accepted terms and conditions',
            'Error:',
            error
          )
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(res, errors.noError(), { has_accepted: !!hasAccepted })
      }
    )
  })
})

router.post('/email-verification-code', async (req, res) => {
  // App name and language
  const appDetails = await getAppDetails(req)
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'account_type')) {
    logger(
      'Error: could not email user verification code. Missing params in request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(res, req, throttleTypes.emailVerificationCode)
  ) {
    userHandler.emailUserConfirmationCode(
      req.body.user_id,
      req.body.account_type === accountTypes.privateAccount
        ? confirmationCodeTypes.lattisAccount
        : confirmationCodeTypes.lattisSignIn,
      false,
      appDetails,
      (error) => {
        if (error) {
          logger(
            'Error: failed to send email verification code to',
            req.body.email,
            'Failed with error:',
            error
          )
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        ovalResponse(res, errors.noError(), null)
      }
    )
  }
})

router.post('/confirm-email-verification-code', (req, res) => {
  if (
    !_.has(req.body, 'user_id') ||
    !_.has(req.body, 'confirmation_code') ||
    !_.has(req.body, 'account_type')
  ) {
    logger(
      'Error: could not confirm email verification code. Missing params in request body'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    throttleHandler.handleRequest(
      res,
      req,
      throttleTypes.confirmEmailVerificationCode
    )
  ) {
    userHandler.checkEmailConfirmationCode(
      req.body.user_id,
      req.body.confirmation_code,
      req.body.account_type === accountTypes.privateAccount
        ? confirmationCodeTypes.lattisAccount
        : confirmationCodeTypes.lattisSignIn,
      (error, user) => {
        if (error) {
          logger(
            'Error: failed to confirm verification code for:',
            req.body.email,
            'Failed with error:',
            error
          )
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }

        if (req.body.account_type === accountTypes.privateAccount) {
          ovalResponse(res, errors.noError(), user)
        } else {
          ovalResponse(
            res,
            errors.noError(),
            userHandler.formatUserForWire(user, true, true)
          )
        }
      }
    )
  }
})

// get user's stored cc cards
router.post('/get-cards', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    userHandler.getCards(null, { user_id: user.user_id }, async (error, user) => {
      if (error) {
        logger('Error: processing user:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      let cardArr = []
      let cards = []
      if (user && user.length && user[0].payment_gateway === 'stripe') {
        cards = await stripeController.getCards(user[0].stripe_net_profile_id)
      }
      for (let i = 0; i < user.length; i++) {
        let n1 = user[i]
        n1.is_primary = n1.is_primary === 1
        if (cards && cards.length) {
          let flag = false
          for (let n = 0; n < cards.length; n++) {
            if (cards[n].id === n1.payment_method) {
              flag = true
              cardArr.push(n1)
            }
          }
          if (!flag) {
            await db
              .main('user_payment_profiles')
              .where({ payment_method: n1.payment_method })
              .delete()
          }
        } else if (cards.length === 0) {
          cardArr = []
          await db
            .main('user_payment_profiles')
            .where({ user_id: user[0].user_id })
            .delete()
        } else {
          cardArr.push(n1)
        }
      }
      ovalResponse(res, errors.noError(), cardArr)
    })
  })
})

// set cards to primary
router.post('/set-card-primary', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    userHandler.setCardPrimary(req.body, (error, user) => {
      if (error) {
        logger('Error: processing set card primary:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), user)
    })
  })
})

// delete card
router.post('/delete-card', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    userHandler.deleteCard(req.body, (error, user) => {
      if (error) {
        logger('Error: processing delete card:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), user)
    })
  })
})

router.post('/add-cards', (req, res) => {
  requestHelper.userAuthorizationValidator(
    req,
    res,
    true,
    async (error, user) => {
      if (error) return

      req.body.user_id = user.user_id

      const appDetails = await getAppDetails(req)
      const setupOpts = { ...req.query, user }
      const appType = req.headers['user-agent']
      const whiteLabelFleetIds = appType
        ? await bikeHandler.getAppFleets(appType)
        : []

      const qb = await db
        .main('fleets')
        .whereIn('fleets.fleet_id', whiteLabelFleetIds)
        .leftOuterJoin(
          'fleet_payment_settings',
          'fleet_payment_settings.fleet_id',
          'fleets.fleet_id'
        ).select('fleet_payment_settings.stripe_account_id')
      switch (req.query.action) {
        case 'setup_intent':
          paymentHandler
            .setupIntent(setupOpts, appDetails)
            .then((intent) => ovalResponse(res, null, intent))
            .catch((error) =>
              ovalResponse(res, errors.formatErrorForWire(error), null)
            )
          break
        default: {
          const params = {
            ...req.body,
            appDetails
          }
          if (qb && qb.length) {
            params.stripeAccountId = qb[0].stripe_account_id
          }

          userHandler.addCards(params, (error, user) => {
            if (error) {
              error.code = error.name === 'cardExist' ? 410 : error.code
              logger('Error: processing user:', error)
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }
            ovalResponse(res, errors.noError(), user)
          })
        }
      }
    }
  )
})

router.post('/update-card', (req, res) => {
  requestHelper.userAuthorizationValidator(
    req,
    res,
    true,
    async (error, user) => {
      if (error) return

      req.body.user_id = user.user_id

      const appDetails = await getAppDetails(req)
      const setupOpts = { ...req.query, user }

      switch (req.query.action) {
        case 'setup_intent':
          paymentHandler
            .setupIntent(setupOpts, appDetails)
            .then((intent) => ovalResponse(res, null, intent))
            .catch((error) =>
              ovalResponse(res, errors.formatErrorForWire(error), null)
            )
          break
        default: {
          const params = {
            ...req.body,
            appDetails
          }

          userHandler.updateCard(params, (error, user) => {
            if (error) {
              logger('Error: processing user:', error)
              error.code = 400
              ovalResponse(res, errors.formatErrorForWire(error), null)
              return
            }
            ovalResponse(res, errors.noError(), user)
          })
        }
      }
    }
  )
})

// To get users details and all locks [For support purpose]
router.post('/get-user-details', (req, res) => {
  if (
    (!_.has(req.body, 'phone') && !_.has(req.body, 'email')) ||
    !_.has(req.body, 'super_admin_password')
  ) {
    logger(
      'Error: failed to get user details. Missing parameters in request body.'
    )
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  userHandler.getUserDetails(req.body, (error, userDetails) => {
    if (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    ovalResponse(res, errors.noError(), userDetails)
  })
})

router.put('/delete', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    try {
      const inTwentydays = await userHandler.deleteUserAccount(user.user_id)
      if (inTwentydays.status && inTwentydays.status === 'deleted') {
        return ovalResponse(res, errors.customError(`The account is already deleted or scheduled for deletion on ${inTwentydays.on}`, responseCodes.InternalServer, 'DeleteAccountError', true), { deleted: true })
      }
      return ovalResponse(res, errors.noError(), { msg: `User account marked for deletion on ${inTwentydays.format('MMM DD, YYYY')}` })
    } catch (error) {
      Sentry.captureException(error, { ...req.body, endpoint: '/users/delete' })
      ovalResponse(res, errors.customError('An error occurred deleting the user account', responseCodes.InternalServer, 'DeleteAccountError', true), null)
    }
  })
})

module.exports = router
