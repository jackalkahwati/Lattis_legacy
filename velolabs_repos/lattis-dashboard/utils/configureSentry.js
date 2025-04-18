const Sentry = require('@sentry/node')
const { ProfilingIntegration } = require('@sentry/profiling-node')

function configureSentry() {
  const environment = process.env.NODE_ENV || 'development'
  const dsn = process.env.SENTRY_DSN

  if (!dsn) {
    console.warn('Sentry DSN not configured. Error tracking will be disabled. Using dummy Sentry object.')
    return {
      captureException: function(err) { console.error('Sentry disabled:', err); },
      captureMessage: function(msg) { console.log('Sentry disabled:', msg); }
    };
  }

  const integrationsArray = [
    new Sentry.Integrations.Http({ tracing: true }),
    new Sentry.Integrations.Express()
  ];

  if (typeof ProfilingIntegration === 'function') {
    integrationsArray.push(new ProfilingIntegration());
  } else {
    console.warn('ProfilingIntegration is not a constructor, skipping profiling integration');
  }

  Sentry.init({
    dsn,
    environment,
    integrations: integrationsArray,
    tracesSampleRate: environment === 'production' ? 0.1 : 1.0,
    profilesSampleRate: environment === 'production' ? 0.1 : 1.0,
    
    // Configure error filtering
    beforeSend(event) {
      // Don't send errors in development
      if (environment === 'development') {
        return null
      }

      // Filter out specific errors you don't want to track
      if (event.exception) {
        const exceptionValue = event.exception.values[0].value.toLowerCase()
        
        // List of errors to ignore
        const ignoredErrors = [
          'connection refused',
          'network timeout',
          'invalid token'
        ]

        if (ignoredErrors.some(error => exceptionValue.includes(error))) {
          return null
        }
      }

      return event
    },

    // Configure error context
    beforeBreadcrumb(breadcrumb) {
      // Don't track specific URLs or patterns
      if (breadcrumb.category === 'http') {
        const url = breadcrumb.data?.url
        if (url && (
          url.includes('/health') ||
          url.includes('/ping') ||
          url.includes('.js.map')
        )) {
          return null
        }
      }

      return breadcrumb
    }
  })

  // Handle unhandled rejections
  process.on('unhandledRejection', (err) => {
    Sentry.captureException(err)
  })

  // Handle uncaught exceptions
  process.on('uncaughtException', (err) => {
    Sentry.captureException(err)
  })

  const SentryInstance = Sentry;
  return SentryInstance;
}

// Create the instance once and export it directly so
//   const Sentry = require('../utils/configureSentry')
// returns the initialized Sentry client (an object, **not** a function).
// We still attach the factory for rare legacy uses:

configureSentry(); // initialise immediately (stores state inside the imported SDK)

// Export the ready‑to‑use client
module.exports = Object.assign(Sentry, { configureSentry });
