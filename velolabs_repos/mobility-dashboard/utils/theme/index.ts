import { extendTheme } from '@chakra-ui/react'

const colors = {
  brand: {
    50: '#e3f3ff',
    100: '#bdd9f5',
    200: '#96bee9',
    300: '#6ea4dd',
    400: '#478ad2',
    500: '#2d71b8',
    600: '#205890',
    700: '#143f68',
    800: '#062641',
    900: '#000e1b',
  },
}

const theme = extendTheme({ colors })

export default theme
