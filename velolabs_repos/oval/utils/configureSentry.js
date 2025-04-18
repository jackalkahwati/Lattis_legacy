const platform = require('@velo-labs/platform')
const logger = platform.logger
const Sentry = require('@sentry/node')

const { getParameterStoreValue } = require('./parameterStore')

const configureSentry = () => {
  getParameterStoreValue('SENTRY/SENTRY_DNS')
    .then(({ Parameter: sentryDNS }) => {
      if (sentryDNS) {
        Sentry.init({
          dsn: sentryDNS.Value,
          tracesSampleRate: 1.0
        })
      }
    })
    .catch((error) => logger('Error: Sentry Config error:', error))
  return Sentry
}

module.exports = {
  configureSentry
}
