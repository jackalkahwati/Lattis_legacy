'use strict'

const path = require('path')
const express = require('express')
const cookieParser = require('cookie-parser')
const bodyParser = require('body-parser')
const morgan = require('morgan')
const favicon = require('serve-favicon')
const helmet = require('helmet')
const compression = require('compression')
const rateLimit = require('express-rate-limit')
const winston = require('winston')

const passport = require('./utils/authentication/passport')
const auth = require('./utils/authentication')

// Route imports
const routes = require('./routes/index')
const login = require('./routes/login')
const fleet = require('./routes/fleet')
const bikeFleet = require('./routes/bike-fleet')
const alertsRoute = require('./routes/alerts')
const tripsRoute = require('./routes/trips-route')
const profileSettings = require('./routes/profile-settings')
const users = require('./routes/users')
const parking = require('./routes/parking')
const memberRoutes = require('./routes/member')
const operatorRoutes = require('./routes/operator')
const reports = require('./routes/reports')
const tickets = require('./routes/ticket')
const payments = require('./routes/payments')
const memberships = require('./routes/fleet-memberships')
const reservations = require('./routes/reservations')
const promotions = require('./routes/promotions')
const integrations = require('./routes/integrations')
const gpsService = require('./routes/gpsService')
const sentinel = require('./routes/sentinel')
const tax = require('./routes/tax')

// Initialize logger
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' })
  ]
})

if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.simple()
  }))
}

const app = express()

// Security middleware
app.use(helmet({
  contentSecurityPolicy: process.env.NODE_ENV === 'production' ? {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'", 'https:'],
      scriptSrc: ["'self'", "'unsafe-inline'", "'unsafe-eval'", 'https:'],
      imgSrc: ["'self'", 'data:', 'https:'],
      connectSrc: ["'self'", 'https:']
    }
  } : false // disable CSP in development so local assets and CDNs load without hassle
}))

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
})
app.use('/api/', limiter)

// Performance middleware
app.use(compression())

// Static file serving with cache headers
app.use(express.static(path.join(__dirname, 'public'), {
  maxAge: '1d',
  etag: true
}))

app.use(favicon(path.join(__dirname, './public/images', 'favicon.png')))

// Body parsing middleware with size limits and validation
app.use(bodyParser.json({ 
  limit: '10mb',
  verify: (req, res, buf) => {
    try {
      JSON.parse(buf)
    } catch(e) {
      res.status(400).json({ error: 'Invalid JSON' })
    }
  }
}))
app.use(bodyParser.urlencoded({ extended: true, limit: '10mb' }))
app.use(cookieParser())

// Logging middleware
app.use(morgan('combined', { stream: { write: message => logger.info(message.trim()) }}))

// Authentication middleware
app.use(passport.initialize())

// CORS headers
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', process.env.ALLOWED_ORIGINS || '*')
  res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS')
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With')
  res.header('Access-Control-Allow-Credentials', true)
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200)
  }
  next()
})

// Public routes
app.use('/', routes)
app.use('/api/user/', login)
app.use('/api/integration', integrations)
app.use('/api/gpsService', gpsService)
app.use('/api/sentinel', sentinel)

app.get('/api', (req, res) => {
  res.json({ msg: 'Welcome to the dashboard', version: '1.0.0' })
})

// Protected routes
app.use(auth.middleware.authenticateJWT)

app.use('/api/users/', users)
app.use('/api/profile/', profileSettings)
app.use('/api/fleet/', fleet)
app.use('/api/bike-fleet/', bikeFleet)
app.use('/api/alerts/', alertsRoute)
app.use('/api/trips/', tripsRoute)
app.use('/api/parking/', parking)
app.use('/api/members/', memberRoutes)
app.use('/api/operators/', operatorRoutes)
app.use('/api/tickets/', tickets)
app.use('/api/reports/', reports)
app.use('/api/payments/', payments)
app.use('/api/memberships/', memberships)
app.use('/api/reservations', reservations)
app.use('/api/promotions', promotions)
app.use('/api/tax', tax)

app.set('view engine', 'pug')

// Error handling middleware
app.use((err, req, res, next) => {
  if (err && err.error && err.error.isJoi) {
    return res.status(400).json({
      type: err.type,
      message: 'Validation error',
      details: err.error.details
    })
  }
  next(err)
})

// Generic error handler
app.use((err, req, res, next) => {
  const status = err.status || 500
  const message = status === 500 ? 'Internal Server Error' : err.message
  
  // Log error
  logger.error({
    message: err.message,
    stack: err.stack,
    method: req.method,
    path: req.path,
    ip: req.ip
  })

  // Send error response
  res.status(status).json({
    error: {
      message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    }
  })
})

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: {
      message: 'Not Found'
    }
  })
})

module.exports = app
