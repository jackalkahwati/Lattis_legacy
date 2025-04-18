import { VStack } from '@chakra-ui/react'
import { Activity, LayoutDashboard, Map } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'

import { NavLink } from '@/components/navigation/NavLink'

interface NavItemProps {
  href: string
  icon: LucideIcon
  children: React.ReactNode
}

const NavItem = ({ href, icon: Icon, children }: NavItemProps): JSX.Element => (
  <NavLink href={href} icon={Icon}>
    {children}
  </NavLink>
)

export const Sidebar = (): JSX.Element => (
  <VStack spacing={4} align="stretch" w="100%" p={4}>
    <NavItem href="/dashboard" icon={LayoutDashboard}>
      Dashboard
    </NavItem>
    <NavItem href="/vehicles" icon={Map}>
      Vehicles
    </NavItem>
    <NavItem href="/maps" icon={Map}>
      Maps
    </NavItem>
    <NavItem href="/analytics" icon={Activity}>
      Analytics
    </NavItem>
  </VStack>
)
