'use client'
import React from 'react'
import { Layout } from '../../components'

type ProtectedLayoutProps = {
  children: React.ReactNode
}

const RootLayout: React.FC<ProtectedLayoutProps> = ({ children }) => <Layout>{children}</Layout>

export default RootLayout
