'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const S3Handler = platform.S3Handler
const s3Constants = platform.s3Constants
const _ = require('underscore')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

class FirmwareHandler {
  constructor () {
    this._chunkSize = 128
    this._buffer = null
  };

  /**
     * This method will return the names of the firmware files in order.
     * The order is oldest to newest.
     *
     * @param {function} callback
     */
  fetchFirmwareFiles (callback) {
    const s3Handler = new S3Handler()
    s3Handler.getBucketContents({bucket: s3Constants.firmware.bucket}, function (error, files) {
      if (error) {
        Sentry.captureException(error)
        logger('Error: failed to fetch firmware from s3. Failed with error:', error)
        callback(error, null)
        return
      }

      let sortedFileObjects = {}
      _.each(files, function (file) {
        const parts = file.split('_')
        sortedFileObjects[parts[1] + parts[2]] = file
      })

      logger(sortedFileObjects)
      let keys = _.keys(sortedFileObjects)
      keys.sort()

      let sortedFiles = []
      _.each(keys, function (key) {
        sortedFiles.push(sortedFileObjects[key])
      })

      callback(null, sortedFiles)
    })
  };

  /**
     * This method fetches a firmware file and formats it to send along the wire.
     *
     * @param {string} fileName
     * @param {function} callback
     */
  fetchFirmware (fileName, callback) {
    const params = {
      bucket: s3Constants.firmware.bucket,
      key: fileName
    }

    const s3Handler = new S3Handler()
    s3Handler.download(params, function (error, firmwareFileObject) {
      if (error) {
        Sentry.captureException(error, {firmwareFileObject})
        logger('Error: failed to fetch firmware from s3. Failed with error:', error)
        callback(error, null)
        return
      }

      const data = firmwareFileObject.body
      this._buffer = Buffer.from(data, 'utf8', data.length)
      let length = this._buffer.length
      let offset = 0
      let lines = []
      let counter = 0
      while (length > 0) {
        let lineBuffer = Buffer.alloc(this._chunkSize)
        if (length > this._chunkSize) {
          lineBuffer = this._getChunk(offset, this._chunkSize)
        } else {
          const remainder = this._buffer.length - offset
          const padding = Array.from({length: this._chunkSize - remainder}, () => 255)
          lineBuffer = this._getChunk(offset, remainder)
          lineBuffer = Buffer.concat([lineBuffer, Buffer.from(padding)])
        }

        // This is here because the lock needs to check that the correct line is
        // being written to it. This 4 byte "index" is how that counting is accomplished.
        const prependBuffer = Buffer.allocUnsafe(4)
        prependBuffer.writeInt32LE(128 * counter)
        lineBuffer = Buffer.concat([prependBuffer, lineBuffer])

        counter += 1
        length -= this._chunkSize
        offset += this._chunkSize

        lines.push(lineBuffer.toString('hex'))
      }

      logger('Fetched firmware version:', fileName)
      callback(null, lines)
    }.bind(this))
  };

  getUpdateLog (callback) {
    const params = {
      bucket: s3Constants.firmwareUpdateLog.bucket,
      key: s3Constants.firmwareUpdateLog.file
    }

    const s3Handler = new S3Handler()
    s3Handler.download(params, function (error, updateLog) {
      if (error) {
        Sentry.captureException(error)
        logger('Error: failed to fetch firmware update log from s3. Failed with error:', error)
        callback(error, null)
        return
      }

      callback(null, JSON.parse(updateLog.body.toString('utf8')))
    })
  }

  versionNumberFromFileName (fileName) {
    const parts = fileName.split('_')
    return parts[1] + '.' + parts[2]
  };

  _getChunk (start, length) {
    let chunk = []
    for (let i = start; i < start + length; i++) {
      chunk.push(this._buffer[i])
    }

    return Buffer.from(chunk)
  };
}

module.exports = FirmwareHandler
