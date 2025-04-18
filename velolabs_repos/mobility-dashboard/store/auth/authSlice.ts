import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { METHOD_POST } from '../../utils/axios/constants'
import { authSet } from '../../utils/axios/request'
import { AppError, logger } from '../../utils/logger'

interface AuthState {
  token: string | null
  userId: string | null
  error: string | null
  loading: boolean
  isAuthenticated: boolean
}

interface LoginCredentials {
  email: string
  password: string
}

interface AuthResponse {
  token: string
  userId: string
}

const initialState: AuthState = {
  token: typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null,
  userId: typeof window !== 'undefined' ? localStorage.getItem('user_id') : null,
  error: null,
  loading: false,
  isAuthenticated: false,
}

export const login = createAsyncThunk<AuthResponse, LoginCredentials>(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authSet<AuthResponse>(METHOD_POST, '/api/user/login', credentials, {})
      if (!response.data.token || !response.data.userId) {
        throw new AppError('Invalid response from server', 500)
      }

      if (typeof window !== 'undefined') {
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('user_id', response.data.userId)
      }

      return response.data
    } catch (error: any) {
      logger.error('Login failed:', { error: error.message })
      return rejectWithValue(error.message || 'Login failed')
    }
  },
)

export const logout = createAsyncThunk('auth/logout', async (_, { rejectWithValue }) => {
  try {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('user_id')
    }
    return true
  } catch (error: any) {
    logger.error('Logout failed:', { error: error.message })
    return rejectWithValue('Logout failed')
  }
})

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: state => {
      state.error = null
    },
    setAuthenticated: (state, action: PayloadAction<boolean>) => {
      state.isAuthenticated = action.payload
    },
  },
  extraReducers: builder => {
    builder
      .addCase(login.pending, state => {
        state.loading = true
        state.error = null
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false
        state.token = action.payload.token
        state.userId = action.payload.userId
        state.isAuthenticated = true
        state.error = null
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
        state.isAuthenticated = false
      })
      .addCase(logout.pending, state => {
        state.loading = true
      })
      .addCase(logout.fulfilled, state => {
        state.loading = false
        state.token = null
        state.userId = null
        state.isAuthenticated = false
        state.error = null
      })
      .addCase(logout.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
  },
})

export const { clearError, setAuthenticated } = authSlice.actions

export default authSlice.reducer
