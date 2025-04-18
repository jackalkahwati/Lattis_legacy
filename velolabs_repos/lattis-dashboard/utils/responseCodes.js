const platform = require('@velo-labs/platform')

const codes = platform.responseCodes

const codesByStatus = Object.keys(codes).reduce(
  (acc, name) => ({
    ...acc,
    [codes[name]]: name
  }),
  {}
)

/**
 * Given an error, return the HTTP status code from that error.
 * Defaults to an InternalServerError code 500.
 *
 * @param {Error} error
 * @return {Number} Error code.
 */
const statusCodeFromError = error => {
  const status =
    codes[codesByStatus[error.status || error.statusCode || error.code]]
  return status || codes.InternalServer
}

module.exports = {
  codesByStatus,
  statusCodeFromError
}
