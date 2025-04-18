const awsParamStore = require('aws-param-store')
const { logger } = require('./utils/error-handler')

/**
 * Get configuration value from environment variables or AWS Parameter Store
 * @param {string} name - The name of the configuration parameter
 * @param {string} [defaultValue] - Optional default value if parameter is not found
 * @returns {string|undefined} The configuration value
 */
function envConfig(name, defaultValue) {
  // First try to get from environment variables
  const envValue = process.env[name]
  if (envValue !== undefined) {
    return envValue
  }

  // If not in env vars and we're not in production, use default or return undefined
  if (process.env.NODE_ENV !== 'production') {
    if (defaultValue !== undefined) {
      logger.debug(`Using default value for ${name}: ${defaultValue}`)
      return defaultValue
    }
    return undefined
  }

  // In production, try AWS Parameter Store
  try {
    const currentEnv = process.env.CURRENT_ENVIRONMENT || 'production'
    const params = awsParamStore.getParameterSync(
      [`/env/${currentEnv}/oval/${name}`],
      { 
        apiVersion: '2014-11-06',
        region: process.env.AWS_REGION || 'ca-central-1'
      }
    )

    if (params.InvalidParameters && params.InvalidParameters.length) {
      logger.warn(`Parameter ${name} not found in AWS Parameter Store`)
      return defaultValue
    }

    if (params.Parameters && params.Parameters[0]) {
      return params.Parameters[0].Value
    }

    logger.warn(`No value found for ${name} in AWS Parameter Store`)
    return defaultValue

  } catch (error) {
    logger.error('Error fetching from AWS Parameter Store:', {
      parameter: name,
      error: error.message,
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
    })

    // In case of AWS Parameter Store error, fall back to default
    return defaultValue
  }
}

// Provide sensible defaults for critical variables in development
if (process.env.NODE_ENV !== 'production') {
  process.env.NODE_ENV = process.env.NODE_ENV || 'development'
  process.env.JWT_SECRET = process.env.JWT_SECRET || 'myverysecuresecretkey1234567890X'
}

// Validate required environment variables (only enforce in production)
if (process.env.NODE_ENV === 'production') {
  const requiredEnvVars = ['NODE_ENV', 'JWT_SECRET']
  const missingEnvVars = requiredEnvVars.filter((envVar) => !process.env[envVar])
  if (missingEnvVars.length > 0) {
    throw new Error(`Missing required environment variables: ${missingEnvVars.join(', ')}`)
  }
}

module.exports = envConfig

// Wrap the existing config export in a function if it's not already a function
if (typeof module.exports !== 'function') {
  const originalConfig = module.exports;
  module.exports = function() {
    // Set default environment variables in development if they are missing
    process.env.NODE_ENV = process.env.NODE_ENV || 'development';
    process.env.JWT_SECRET = process.env.JWT_SECRET || 'myverysecuresecretkey1234567890!';
    process.env.LATTIS_USERS_DB_PASSWORD = process.env.LATTIS_USERS_DB_PASSWORD || 'secret';
    return originalConfig;
  };
}
