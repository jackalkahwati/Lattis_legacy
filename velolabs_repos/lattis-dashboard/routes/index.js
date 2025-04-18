'use strict'

const express = require('express')
const path = require('path')

const router = express.Router()

// GET home page
router.get('/', (req, res) => {
  const indexPath = path.join(__dirname, '../public/index.html')
  res.sendFile(indexPath)
})

module.exports = router
