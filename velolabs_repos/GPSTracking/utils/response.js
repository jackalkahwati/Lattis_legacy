/** @format */

const { logger } = require("./logger");
/**
 * Formats a response to a request
 *
 * @param {Object} response Express response object
 * @param {(null|true)} error Whether an error is set or not
 * @param {string} [message] Error message
 * @param {Object} [payload]
 * @param {Object} [stack] Error stack trace
 */
const formatResponse = (req, error, message, payload, stack) => {
  let msg;
  if (!error) {
    msg = { error: null, payload };
  } else {
    msg = {
      path: req.originalUrl,
      error: true,
      message,
      stack,
      body: JSON.stringify(req.body, null, 2),
      params: JSON.stringify(req.params, null, 2),
      queryString: JSON.stringify(req.query, null, 2)
    };
    logger.error(msg);
  }
  return msg;
};

module.exports = formatResponse;
