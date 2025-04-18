'use strict'

const platform = require('@velo-labs/platform')
const jsonResponse = platform.jsonResponse
const responseCodes = platform.responseCodes

module.exports = function (res, error, payload) {
  let status
  if (error) {
    status = error.code || responseCodes.BadRequest
  } else {
    status = responseCodes.OK
  }

  jsonResponse(res, status, error, payload)
}
