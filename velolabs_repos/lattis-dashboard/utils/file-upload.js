const { S3Handler } = require('./aws-utils')
const { logger } = require('./error-handler')
const { errors } = require('./response-utils')
const path = require('path')
const fs = require('fs')
const { promisify } = require('util')
const unlinkAsync = promisify(fs.unlink)
const multer = require('multer')

/**
 * File upload utilities
 */
class FileUpload {
  /**
   * Upload an image file to S3
   * @param {Object} fileDetails - File details
   * @param {string} fileName - Name to save the file as
   * @param {string} bucket - S3 bucket name
   * @param {string} mimeType - File mime type
   * @returns {Promise<Object>} - Upload result with link
   */
  static async uploadImageFile(fileDetails, fileName, bucket, mimeType) {
    try {
      // Validate file
      if (!fileDetails || !fileDetails.path) {
        throw errors.validationError('No file provided')
      }

      // Validate mime type
      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
      if (!allowedTypes.includes(mimeType)) {
        throw errors.validationError('Invalid file type. Only JPEG, PNG and GIF are allowed.')
      }

      // Read file
      const fileContent = await fs.promises.readFile(fileDetails.path)

      // Upload to S3
      const s3Handler = new S3Handler()
      const extension = path.extname(fileDetails.path)
      const s3FileName = `${fileName}${extension}`

      const result = await s3Handler.upload(
        fileContent,
        s3FileName,
        bucket,
        mimeType,
        {
          originalName: path.basename(fileDetails.path),
          uploadedAt: new Date().toISOString()
        }
      )

      // Clean up local file
      await unlinkAsync(fileDetails.path)

      return {
        link: result.Location,
        key: result.Key,
        bucket: result.Bucket
      }
    } catch (error) {
      logger.error('Error uploading image file:', error)
      // Clean up local file if it exists
      if (fileDetails && fileDetails.path) {
        try {
          await unlinkAsync(fileDetails.path)
        } catch (cleanupError) {
          logger.error('Error cleaning up local file:', cleanupError)
        }
      }
      throw error
    }
  }

  /**
   * Upload a document file to S3
   * @param {Object} fileDetails - File details
   * @param {string} fileName - Name to save the file as
   * @param {string} bucket - S3 bucket name
   * @param {string} mimeType - File mime type
   * @returns {Promise<Object>} - Upload result with link
   */
  static async uploadDocumentFile(fileDetails, fileName, bucket, mimeType) {
    try {
      // Validate file
      if (!fileDetails || !fileDetails.path) {
        throw errors.validationError('No file provided')
      }

      // Validate mime type
      const allowedTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ]
      if (!allowedTypes.includes(mimeType)) {
        throw errors.validationError('Invalid file type. Only PDF and Word documents are allowed.')
      }

      // Read file
      const fileContent = await fs.promises.readFile(fileDetails.path)

      // Upload to S3
      const s3Handler = new S3Handler()
      const extension = path.extname(fileDetails.path)
      const s3FileName = `${fileName}${extension}`

      const result = await s3Handler.upload(
        fileContent,
        s3FileName,
        bucket,
        mimeType,
        {
          originalName: path.basename(fileDetails.path),
          uploadedAt: new Date().toISOString()
        }
      )

      // Clean up local file
      await unlinkAsync(fileDetails.path)

      return {
        link: result.Location,
        key: result.Key,
        bucket: result.Bucket
      }
    } catch (error) {
      logger.error('Error uploading document file:', error)
      // Clean up local file if it exists
      if (fileDetails && fileDetails.path) {
        try {
          await unlinkAsync(fileDetails.path)
        } catch (cleanupError) {
          logger.error('Error cleaning up local file:', cleanupError)
        }
      }
      throw error
    }
  }

  /**
   * Delete a file from S3
   * @param {string} bucket - S3 bucket name
   * @param {string} key - S3 object key
   * @returns {Promise<void>}
   */
  static async deleteFile(bucket, key) {
    try {
      const s3Handler = new S3Handler()
      await s3Handler.delete(bucket, key)
    } catch (error) {
      logger.error('Error deleting file from S3:', error)
      throw error
    }
  }

  /**
   * Get file extension from mime type
   * @param {string} mimeType - File mime type
   * @returns {string} File extension
   */
  static getExtensionFromMimeType(mimeType) {
    const mimeToExt = {
      'image/jpeg': '.jpg',
      'image/png': '.png',
      'image/gif': '.gif',
      'application/pdf': '.pdf',
      'application/msword': '.doc',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx'
    }
    return mimeToExt[mimeType] || ''
  }

  /**
   * Validate file size
   * @param {number} size - File size in bytes
   * @param {number} maxSize - Maximum allowed size in bytes
   * @returns {boolean}
   */
  static validateFileSize(size, maxSize) {
    return size <= maxSize
  }
}

// Attach a pre‑configured multer instance so legacy routes can use
//   uploadFile.upload.single('field')
FileUpload.upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 } // 10 MB
})

module.exports = FileUpload
