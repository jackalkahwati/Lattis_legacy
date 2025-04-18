const { logger } = require('@velo-labs/platform')
const axios = require('axios').default
const config = require('../../config')

if (!config.oval.url) {
  logger(new Error('Missing `OVAL_URL` configuration.'))
}

const client = axios.create({
  baseURL: config.oval.url
})

function useDefaultInterceptors (client) {
  client.interceptors.response.use(
    (response) => {
      return Promise.resolve(response.data)
    },
    (error) => {
      if (error.response && error.response.data) {
        return Promise.reject(error.response.data)
      }

      return Promise.reject(error.message)
    }
  )
}

const configure = ({ url, token }, { withDefaultInterceptors = true } = {}) => {
  const c = axios.create({
    baseURL: url || config.oval.url,
    headers: {
      Authorization: ` ${token}`
    }
  })

  if (withDefaultInterceptors) {
    useDefaultInterceptors(c)
  }

  return c
}

useDefaultInterceptors(client)

module.exports = { client, configure }
