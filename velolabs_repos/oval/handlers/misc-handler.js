'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const config = platform.config
const S3Handler = platform.S3Handler
const _ = require('underscore')
const miscConstants = require('./../constants/misc-constants')
const hash = require('./../utils/hash')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/** This method finds which bucket the image corresponds to
 *
 * @param {String} fileType - Identifier to check which file it is
 * @private
 */
const _findBucket = (fileType) => {
  let bucket
  if (fileType === miscConstants.imageUpload.bike) {
    bucket = config.aws.s3.bikeImages.bucket
  } else if (fileType === miscConstants.imageUpload.parkingImage) {
    bucket = config.aws.s3.bikeParkedImages.bucket
  } else if (fileType === miscConstants.imageUpload.maintenance) {
    bucket = config.aws.s3.damageReports.bucket
  }
  return bucket
}

/**
 * To get the file name
 *
 * @param {String} fileType - file type identification
 * @private
 */
const _getFileName = (fileType) => {
  let fileName
  if (fileType === miscConstants.imageUpload.bike) {
    fileName = miscConstants.uploadConstants.types.bike
  } else if (fileType === miscConstants.imageUpload.maintenance) {
    fileName = miscConstants.uploadConstants.types.damage_report
  } else if (fileType === miscConstants.imageUpload.parkingImage) {
    fileName = miscConstants.uploadConstants.types.bike_parking
  }
  return fileName + '-' + hash.randomString(16)
}

/** * This method uploads the image
 *
 * @param {Object} file - the file to upload
 * @param {Object} fileType - which type of file it is.
 * @param {Function} callback
 */
const uploadImage = (file, fileType, callback) => {
  const s3Handler = new S3Handler()
  const bucketName = _findBucket(fileType)
  s3Handler.upload(
    file[_.keys(file)[0]],
    _getFileName(fileType),
    bucketName,
    fileType,
    (error, imageData) => {
      if (error) {
        Sentry.captureException(error, { bucketName })
        logger(
          'Error: failed to Upload  ',
          +fileType + ' ' + 'Image' + 'Failed with error:',
          error
        )
        callback(error, null)
        return
      }
      callback(null, { uploaded_url: imageData.Location })
    }
  )
}

module.exports = {
  uploadImage: uploadImage
}
