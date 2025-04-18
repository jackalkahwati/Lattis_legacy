const AWS = require('aws-sdk')
const { logger } = require('./error-handler')

class S3Handler {
  constructor() {
    this.s3 = new AWS.S3({
      apiVersion: '2006-03-01',
      region: process.env.AWS_REGION || 'ca-central-1'
    })
  }

  async upload(file, fileName, bucket, fileType, metadata = {}) {
    try {
      const params = {
        Bucket: bucket,
        Key: fileName,
        Body: file,
        ContentType: fileType,
        Metadata: metadata
      }

      const result = await this.s3.upload(params).promise()
      return {
        Location: result.Location,
        Key: result.Key,
        Bucket: result.Bucket
      }
    } catch (error) {
      logger.error('Error uploading to S3:', error)
      throw error
    }
  }

  async delete(bucket, key) {
    try {
      const params = {
        Bucket: bucket,
        Key: key
      }

      await this.s3.deleteObject(params).promise()
    } catch (error) {
      logger.error('Error deleting from S3:', error)
      throw error
    }
  }
}

module.exports = {
  S3Handler
}
