import { ReactNode } from 'react'

import { Grid, GridItem } from '@chakra-ui/react'

import { Header } from '@/components/navigation/Header'
import { Sidebar } from '@/components/navigation/Sidebar'

export interface DashboardLayoutProps {
  children: ReactNode
}

export const DashboardLayout = ({ children }: DashboardLayoutProps): JSX.Element => {
  return (
    <Grid
      templateAreas={{
        base: `"header" "main"`,
        md: `"sidebar header" "sidebar main"`,
      }}
      gridTemplateRows={{ base: 'auto 1fr', md: 'auto 1fr' }}
      gridTemplateColumns={{
        base: '1fr',
        md: '250px 1fr',
      }}
      h="100vh"
    >
      <GridItem area="sidebar" display={{ base: 'none', md: 'block' }}>
        <Sidebar />
      </GridItem>
      <GridItem area="header">
        <Header />
      </GridItem>
      <GridItem area="main" bg="gray.50" p={4} overflowY="auto">
        {children}
      </GridItem>
    </Grid>
  )
}
