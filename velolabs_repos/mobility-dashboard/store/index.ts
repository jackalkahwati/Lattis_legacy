import { configureStore } from '@reduxjs/toolkit'
import { counterSlice } from './slices'
import authSlice from './auth/authSlice'

const store = configureStore({
  reducer: {
    counter: counterSlice,
    auth: authSlice,
  },
  middleware: getDefaultMiddleware => getDefaultMiddleware().concat(),
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export default store
