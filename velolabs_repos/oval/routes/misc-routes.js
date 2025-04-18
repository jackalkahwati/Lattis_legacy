'use strict'

const platform = require('@velo-labs/platform')
const express = require('express')
const _ = require('underscore')
const router = express.Router()
const logger = platform.logger
const errors = platform.errors
const config = require('./../config')
const ovalResponse = require('./../utils/oval-response')
const miscHandler = require('./../handlers/misc-handler')
const requestHelper = require('./../helpers/request-helper')

/**
 * Used to upload a file for file upload
 *
 * The request body should include files and type
 */
router.post('/upload', (req, res) => {
  if (!_.has(req, 'files') || !_.has(req.query, 'type')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error) => {
    if (error) return
    miscHandler.uploadImage(req.files, req.query.type, (error, uploadedUrl) => {
      if (error) {
        logger('Error: processing upload:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), uploadedUrl)
    })
  })
})

router.post('/operator-upload', (req, res) => {
  if (!_.has(req, 'files') || !_.has(req.query, 'type')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    miscHandler.uploadImage(req.files, req.query.type, (error, uploadedUrl) => {
      if (error) {
        logger('Error: processing upload:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), uploadedUrl)
    })
  })
})

router.get('/android-version', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error) => {
    if (error) return
    const ellipseVersion = config.android.ellipseVersion
    const lattisVersion = config.android.lattisVersion
    const payload = {
      ellipse: isFinite(ellipseVersion) ? parseInt(ellipseVersion) : 0,
      lattis: isFinite(lattisVersion) ? parseInt(lattisVersion) : 0
    }
    ovalResponse(res, errors.noError(), payload)
  })
})

module.exports = router
