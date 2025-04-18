const AWS = require('aws-sdk')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const AWS_REGION = 'us-west-1'

const { CURRENT_ENVIRONMENT: environment } = process.env

const getParameterStoreValuesByPath = async (path, region = AWS_REGION) => {
  const ssmClient = new AWS.SSM({
    region: region
  })
  const fullPath = `/env/${environment}/${path}`
  try {
    return await ssmClient.getParametersByPath({
      Path: fullPath,
      Recursive: true
    }).promise()
  } catch (error) {
    throw error
  }
}

const getParameterStoreValue = async (parameter, region = AWS_REGION) => {
  const ssmClient = new AWS.SSM({
    region: region
  })
  const fullPath = `/env/${environment}/${parameter}`
  const paramPath = {
    'Name': fullPath
  }
  try {
    return await ssmClient.getParameter(paramPath).promise()
  } catch (error) {
    throw error
  }
}

const readTapKeyPrivateKeyFromS3 = async (fleetId, region = AWS_REGION) => {
  try {
    let accessKeyId = await getParameterStoreValue(region, 'DASHBOARD_S3_ACCESSKEY_ID')
    accessKeyId = accessKeyId && accessKeyId.Parameter.Value
    let secretAccessKey = await getParameterStoreValue(region, 'DASHBOARD_S3_SECRET_ACCESS_KEY')
    secretAccessKey = secretAccessKey && secretAccessKey.Parameter.Value
    let tapKeyPrivarekeysBucketName = await getParameterStoreValue(region, 'TAPKEY_PRIVATE_KEYS_BUCKET_NAME')
    tapKeyPrivarekeysBucketName = tapKeyPrivarekeysBucketName && tapKeyPrivarekeysBucketName.Parameter.Value
    const s3 = new AWS.S3({
      accessKeyId,
      secretAccessKey,
      Bucket: tapKeyPrivarekeysBucketName
    })

    const key = `${environment}/tapkey_${fleetId}.json`
    const params = {
      Bucket: tapKeyPrivarekeysBucketName,
      Key: key
    }
    let data = await s3.getObject(params).promise()
    data = JSON.parse(data.Body.toString('utf-8'))
    return data
  } catch (error) {
    logger('An error occurred reading s3 data')
  }
}

module.exports = {
  getParameterStoreValuesByPath,
  getParameterStoreValue,
  readTapKeyPrivateKeyFromS3
}
