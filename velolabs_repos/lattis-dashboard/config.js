'use strict'

const path = require('path')
const _ = require('underscore')
const fs = require('fs')
const mv = require('mv')
const parse = require('csv-parse')
const envConfig = require('./envConfig')
const errors = require('@velo-labs/platform').errors
const logger = require('@velo-labs/platform').logger

const requiredEnvParams = [
  'LATTIS_DASHBOARD_MODE',
  'LATTIS_DASHBOARD_PORT'
]

const requiredAWSParams = [
  'LATTIS_DASHBOARD_LOG_PATH',
  'OVAL_URL'
]

for (let i = 0; i < requiredEnvParams.length; i++) {
  if (!_.has(process.env, requiredEnvParams[i])) {
    console.log(
      'Error: environment variables have not been properly setup.',
      'The variable:',
      requiredEnvParams[i],
      'was not found.'
    )

    throw new Error('Environment Variables Not Properly Set')
  }
}

for (let i = 0; i < requiredAWSParams.length; i++) {
  if (!envConfig(requiredAWSParams[i])) {
    console.log(
      'Error: environment variables have not been properly setup.',
      'The variable:',
      requiredAWSParams[i],
      'was not found.'
    )

    throw new Error('Environment Variables Not Properly Set')
  }
}

module.exports = {
  appName: 'lattis-dashboard',

  logPath: envConfig('LATTIS_DASHBOARD_LOG_PATH'),

  port: process.env.LATTIS_DASHBOARD_PORT,

  mode: process.env.LATTIS_DASHBOARD_MODE || 'development',

  oval: {
    url: envConfig('OVAL_URL')
  },

  token_expiration_time: 172800,

  // set max, min bounds for dynamic bike booking time for fleets
  bike_booking_min_bound: 300,
  bike_booking_max_bound: 7200,

  default_bike_image_url: 'https://s3-us-west-1.amazonaws.com/lattis.bike.image.upload/Group.png',

  s3path: 'https://s3-us-west-1.amazonaws.com/',

  filePaths: {
    parentDir: __dirname,
    filesDir: path.join(__dirname, 'files'),
    CSVDir: path.join(__dirname, 'files/CSV'),
    points: path.join(__dirname, 'files/points.json'),
    maintenanceCategory: path.join(__dirname, 'files/maintenance-category.json'),
    preDefinedBikeTypes: path.join(__dirname, 'files/pre-defined-bike-types.json')
  },

  parseCsvUsingColumns: function (csvFile, columns, done) {
    const csvPath = this.filePaths.filesDir + '/CSV'
    if (!fs.existsSync(csvPath)) {
      fs.mkdirSync(csvPath, '0744')
    }
    const localFilePath = csvPath + '/' + csvFile.originalname
    mv(csvFile.path, csvPath + '/' + csvFile.originalname, (error) => {
      if (error) {
        logger('Error: while moving csv file', errors.errorWithMessage(error))
        return done(errors.internalServer(true), null)
      }
      const readStream = fs.createReadStream(localFilePath)
      const parser = parse({
        delimiter: ',',
        columns: columns
      })
      const values = []
      parser.on('readable', (error) => {
        if (error) {
          logger('Error: while reading csv file', errors.errorWithMessage(error))
          return done(errors.internalServer(true), null)
        }
        let record = parser.read()
        if (record && record[columns[3]] !== columns[3] && record[columns[4]] !== columns[4]) {
          values.push(record)
        }
      })

      parser.on('error', (error) => {
        logger('Error: while reading csv file', errors.errorWithMessage(error))
        done(errors.internalServer(true), null)
      })

      let finalValues = []
      parser.on('end', () => {
        fs.unlinkSync(csvPath + '/' + csvFile.originalname)
        _.each(values, function (value) {
          finalValues.push(value)
        })
        done(null, finalValues)
      })
      readStream.pipe(parser)
    })
  }
}
