import { Method, AxiosResponse, AxiosError } from 'axios'
import axiosInstance from './instance'
import { logger } from '../logger'

interface RequestConfig {
  headers?: Record<string, string>
  timeout?: number
  retry?: boolean
  maxRetries?: number
}

interface AuthCredentials {
  email: string
  password: string
}

export interface ApiResponse<T = any> {
  data: T
  message: string
}

interface ApiErrorResponse {
  message: string
}

const DEFAULT_TIMEOUT = 15000
const MAX_RETRIES = 3
const RETRY_DELAY = 1000

export class ApiError extends Error {
  public readonly status: number
  public readonly code?: string
  public readonly data?: any

  constructor(message: string, status: number, code?: string, data?: any) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.data = data
    Object.setPrototypeOf(this, ApiError.prototype)
  }
}

const handleError = (error: AxiosError<ApiErrorResponse>): never => {
  logger.error('API Request failed:', {
    url: error.config?.url,
    method: error.config?.method,
    status: error.response?.status,
    message: error.message,
  })

  throw new ApiError(
    error.response?.data?.message || error.message,
    error.response?.status || 500,
    undefined,
    error.response?.data,
  )
}

const delay = (ms: number): Promise<void> => new Promise(resolve => setTimeout(resolve, ms))

export const set = async <T>(
  method: Method,
  url: string,
  body?: unknown,
  config: RequestConfig = {},
): Promise<ApiResponse<T>> => {
  const { headers = {}, timeout = DEFAULT_TIMEOUT, retry = true, maxRetries = MAX_RETRIES } = config

  let attempts = 0

  while (attempts < (retry ? maxRetries : 1)) {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await axiosInstance({
        url,
        method,
        data: body,
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        timeout,
      })

      logger.info('API Request successful:', {
        url,
        method,
        status: response.status,
      })

      return response.data
    } catch (error) {
      attempts++
      if (error instanceof AxiosError) {
        if (
          error.response?.status &&
          error.response.status < 500 &&
          error.response.status !== 429
        ) {
          throw handleError(error)
        }

        if (attempts < (retry ? maxRetries : 1)) {
          await delay(RETRY_DELAY * attempts)
          continue
        }
      }
      throw handleError(error as AxiosError<ApiErrorResponse>)
    }
  }

  throw new ApiError('Maximum retry attempts exceeded', 500)
}

export const authSet = async <T>(
  method: Method,
  url: string,
  credentials: AuthCredentials,
  config: RequestConfig = {},
): Promise<ApiResponse<T>> => {
  if (!credentials.email || !credentials.password) {
    throw new ApiError('Invalid credentials', 400)
  }

  return set<T>(method, url, credentials, {
    ...config,
    headers: {
      ...config.headers,
      'X-Request-Type': 'auth',
    },
  })
}
