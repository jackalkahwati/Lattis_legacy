'use client'
import React from 'react'
import { Box } from '@chakra-ui/react'

type AuthLayoutProps = {
  children: React.ReactNode
}

const Layout: React.FC<AuthLayoutProps> = ({ children }) => <Box>{children}</Box>

export default Layout
