'use client'
import React from 'react'
import { Box } from '@chakra-ui/react'

type LoginLayoutProps = {
  children: React.ReactNode
}

const Layout: React.FC<LoginLayoutProps> = ({ children }) => <Box>{children}</Box>

export default Layout
