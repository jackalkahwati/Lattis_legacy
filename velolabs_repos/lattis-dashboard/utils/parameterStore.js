const AWS = require('aws-sdk')
const environment = process.env.CURRENT_ENVIRONMENT
const AWS_REGION = 'us-west-1'

const getParameterStoreValuesByPath = async (path, region = AWS_REGION) => {
  const ssmClient = new AWS.SSM({
    region: region
  })
  const fullPath = `/env/${environment}/${path}`
  try {
    return await ssmClient
      .getParametersByPath({
        Path: fullPath,
        Recursive: true
      })
      .promise()
  } catch (error) {
    console.log('🚀 ~ Error: parameterStore.js:GPSVBP:', path, region, { name: error.name, message: error.message }, error)
  }
}

const getParameterStoreValue = async (parameter, region = AWS_REGION) => {
  const ssmClient = new AWS.SSM({
    region: region
  })
  const fullPath = `/env/${environment}/${parameter}`
  const paramPath = {
    Name: fullPath
  }
  try {
    return await ssmClient.getParameter(paramPath).promise()
  } catch (error) {
    console.log('🚀 ~ Error: parameterStore.js::GPSBV', parameter, region, { name: error.name, message: error.message }, error)
  }
}

module.exports = {
  getParameterStoreValuesByPath,
  getParameterStoreValue
}
