import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { logger } from '../../utils/logger'

const axiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_BASE_URL,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
  responseType: 'json',
})

// Request interceptor
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    logger.info('API Request:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      headers: config.headers,
    })
    return config
  },
  (error: AxiosError) => {
    logger.error('Request Error:', {
      message: error.message,
      code: error.code,
    })
    return Promise.reject(error)
  },
)

// Response interceptor
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    logger.info('API Response:', {
      status: response.status,
      url: response.config.url,
      method: response.config.method?.toUpperCase(),
    })
    return response
  },
  (error: AxiosError) => {
    logger.error('Response Error:', {
      status: error.response?.status,
      message: error.message,
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
    })
    return Promise.reject(error)
  },
)

export default axiosInstance
