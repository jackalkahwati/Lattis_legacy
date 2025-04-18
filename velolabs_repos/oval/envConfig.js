const awsParamStore = require('aws-param-store')

const currentEnv = process.env.CURRENT_ENVIRONMENT

const envConfig = (name) => {
  let param = awsParamStore.getParameterSync(
    `/env/${currentEnv}/oval/${name}`,
    { apiVersion: '2014-11-06', region: 'ca-central-1' }
  )
  return param.Value
}

module.exports = envConfig
