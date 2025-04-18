import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { CounterState } from '../../interfaces'

const initialState: CounterState = {
  value: 0,
  incrementAmount: 1,
}

export const counterSlice = createSlice({
  name: 'counter',
  initialState,
  reducers: {
    increment: (state: CounterState) => {
      state.value += state.incrementAmount
    },
    decrement: (state: CounterState) => {
      state.value -= state.incrementAmount
    },
    changeIncrementAmount: (state: CounterState, action: PayloadAction<number>) => {
      state.incrementAmount = action.payload
    },
  },
})

export const { increment, decrement, changeIncrementAmount } = counterSlice.actions

export default counterSlice.reducer
