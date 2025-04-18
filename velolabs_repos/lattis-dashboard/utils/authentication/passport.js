const passport = require('passport')
const { Strategy: LocalStrategy } = require('passport-local')
const { Strategy: JWTStrategy, ExtractJwt } = require('passport-jwt')
const bcrypt = require('bcrypt')
const { logger, createError } = require('../error-handler')
const db = require('../../db')

// Validate JWT_SECRET exists and has minimum length
const JWT_SECRET = process.env.JWT_SECRET
if (!JWT_SECRET || JWT_SECRET.length < 32) {
  throw new Error('JWT_SECRET must be at least 32 characters long')
}

/**
 * Retrieves the details of Operator User
 * @param {Object} where filters
 * @returns {Promise<Operator | null>}
 */
const getOperator = async (where) => {
  try {
    return await db.users('operators').where(where).first()
  } catch (error) {
    logger.error('Error retrieving operator:', error)
    throw error
  }
}

/**
 * Hash password using bcrypt
 * @param {string} password
 * @returns {Promise<string>}
 */
const hashPassword = async (password) => {
  const saltRounds = 12
  return bcrypt.hash(password, saltRounds)
}

/**
 * Compare password with hash
 * @param {string} password
 * @param {string} hash
 * @returns {Promise<boolean>}
 */
const comparePassword = async (password, hash) => {
  return bcrypt.compare(password, hash)
}

// Password validation
const validatePassword = (password) => {
  const minLength = 8
  const hasUpperCase = /[A-Z]/.test(password)
  const hasLowerCase = /[a-z]/.test(password)
  const hasNumbers = /\d/.test(password)
  const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password)

  const errors = []
  if (password.length < minLength) errors.push(`Password must be at least ${minLength} characters long`)
  if (!hasUpperCase) errors.push('Password must contain at least one uppercase letter')
  if (!hasLowerCase) errors.push('Password must contain at least one lowercase letter')
  if (!hasNumbers) errors.push('Password must contain at least one number')
  if (!hasSpecialChar) errors.push('Password must contain at least one special character')

  return {
    isValid: errors.length === 0,
    errors
  }
}

passport.use(
  'jwt',
  new JWTStrategy(
    {
      secretOrKey: JWT_SECRET,
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      algorithms: ['HS256'],
      ignoreExpiration: false
    },
    async (payload, done) => {
      try {
        // Validate token payload
        if (!payload || !payload.sub || !payload.iat) {
          logger.warn('Invalid token payload')
          return done(null, false)
        }

        // Check token age
        const tokenAge = Date.now() - (payload.iat * 1000)
        const maxAge = 24 * 60 * 60 * 1000 // 24 hours
        if (tokenAge > maxAge) {
          logger.warn('Token too old')
          return done(null, false)
        }

        const operator = await getOperator({ operator_id: payload.sub })

        if (!operator) {
          logger.warn('Missing operator with sub:', payload.sub)
          return done(null, false)
        }

        // Remove sensitive data
        delete operator.password
        return done(null, operator)
      } catch (error) {
        logger.error('Authentication error:', error)
        return done(error, false)
      }
    }
  )
)

passport.use(
  'register',
  new LocalStrategy(
    {
      usernameField: 'email',
      passwordField: 'password',
      passReqToCallback: true
    },
    async (req, email, password, done) => {
      try {
        // Validate email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(email)) {
          return done(createError.validation('Invalid email format'), null)
        }

        // Validate password
        const passwordValidation = validatePassword(password)
        if (!passwordValidation.isValid) {
          return done(createError.validation(passwordValidation.errors.join(', ')), null)
        }

        // Check if operator exists
        const operator = await getOperator({ username: email })
        if (operator) {
          logger.warn('Operator already exists with email:', email)
          return done(createError.conflict('Email already registered'), null)
        }

        // Create new operator
        const newOperator = {
          first_name: req.body.firstName,
          last_name: req.body.lastName,
          username: email,
          email: email,
          created_at: new Date(),
          updated_at: new Date()
        }

        // Hash password
        newOperator.password = await hashPassword(password)

        // Insert operator
        const [operatorId] = await db.users('operators').insert(newOperator)
        newOperator.operator_id = operatorId

        // Remove password from response
        delete newOperator.password
        return done(null, newOperator)
      } catch (error) {
        logger.error('Error creating operator:', error)
        return done(createError.internal('Failed to create operator'), null)
      }
    }
  )
)

passport.use(
  'login',
  new LocalStrategy(
    {
      usernameField: 'email',
      passwordField: 'password'
    },
    async (email, password, done) => {
      try {
        const operator = await getOperator({ username: email })
        
        if (!operator) {
          return done(null, false, { message: 'Invalid credentials' })
        }

        const isValidPassword = await comparePassword(password, operator.password)
        if (!isValidPassword) {
          return done(null, false, { message: 'Invalid credentials' })
        }

        // Remove sensitive data
        delete operator.password
        return done(null, operator)
      } catch (error) {
        logger.error('Login error:', error)
        return done(createError.internal('Authentication failed'), null)
      }
    }
  )
)

module.exports = passport
