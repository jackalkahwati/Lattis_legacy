'use strict'
const platform = require('@velo-labs/platform')
const S3Handler = platform.S3Handler
const errors = platform.errors
const logger = platform.logger

const uploadPic = (file, fileName, bucket, done) => {
  const s3Handler = new S3Handler()
  if (file) {
    s3Handler.upload(file, fileName, bucket, file.mimetype, (error, imageData) => {
      if (error) {
        logger('Error: uploading bike image to s3', errors.errorWithMessage(error))
        done(errors.internalServer(true), null)
        return
      }
      done(errors.noError(), imageData.Location)
    })
  } else {
    done(errors.noError(), null)
  }
}

module.exports = {
  uploadPic: uploadPic
}
