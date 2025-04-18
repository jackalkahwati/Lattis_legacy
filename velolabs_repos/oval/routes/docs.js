'use strict'
const express = require('express')
const swaggerUi = require('swagger-ui-express')
const router = express.Router()

const { docsV1 } = require('../utils/docs/docsV1')
router.use('/', swaggerUi.serve)
router.get('/', swaggerUi.setup(docsV1))

module.exports = router
