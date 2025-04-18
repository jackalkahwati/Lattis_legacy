'use client'
import React from 'react'
import { ChakraProvider } from '@chakra-ui/react'
import { Provider } from 'react-redux'

import store from '../store'
import theme from '../utils/theme'
import Head from 'next/head'

type RootLayoutProps = {
  children: React.ReactNode
}

const RootLayout: React.FC<RootLayoutProps> = ({ children }) => {
  return (
    <html>
      <Head>
        <title>Mobility</title>
        <meta name="description" content="Mobility dashboard" />
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <body>
        <ChakraProvider theme={theme}>
          <Provider store={store}>{children}</Provider>
        </ChakraProvider>
      </body>
    </html>
  )
}

export default RootLayout
