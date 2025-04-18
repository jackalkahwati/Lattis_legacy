const { logger, mailHandler } = require('@velo-labs/platform')
const {configureSentry} = require('../../../utils/configureSentry')

const Sentry = configureSentry()

function sendEmail (
  { recipientEmail, subject, content },
  { bubble = false } = {}
) {
  return new Promise((resolve, reject) => {
    mailHandler.sendMail(recipientEmail, subject, content, null, null, (error) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error sending email: ', error, {
          recipientEmail,
          subject,
          content
        })

        if (bubble) {
          return reject(error)
        }
      } else {
        return resolve()
      }

      if (error && !bubble) {
        Sentry.captureException(error)
        return resolve()
      }
    })
  })
}

module.exports = sendEmail
