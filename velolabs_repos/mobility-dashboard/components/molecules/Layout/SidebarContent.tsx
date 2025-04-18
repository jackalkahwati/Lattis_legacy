import React from 'react'
import { Flex, Text, Box, Collapse, useDisclosure } from '@chakra-ui/react'
import {
  ChevronUp,
  ChevronDown,
  Home,
  Map,
  Radio,
  Lock,
  User,
  Users,
  CreditCard,
  PieChart,
  TrendingUp,
  HelpCircle,
} from 'react-feather'

import NavItem from './NavItem'

type SidebarContentProps = {
  [x: string]: any // for any other props that come into the component
}
const SidebarContent: React.FC<SidebarContentProps> = ({ ...rest }) => {
  const mapDisclosure = useDisclosure()
  const fleetStatusDisclosure = useDisclosure()
  const hubsDisclosure = useDisclosure()

  return (
    <Box
      as="nav"
      pos="fixed"
      top="0"
      left="0"
      zIndex="sticky"
      h="full"
      pb="10"
      overflowX="hidden"
      overflowY="auto"
      bg="brand.500"
      _dark={{
        bg: 'gray.800',
      }}
      color="inherit"
      borderRightWidth="1px"
      w="60"
      {...rest}
    >
      <Flex px="4" py="5" align="center">
        <Text
          fontSize="2xl"
          ml="4"
          _dark={{
            color: 'white',
          }}
          fontWeight="semibold"
        >
          Logo
          {/* TODO: Add custom logo here */}
        </Text>
      </Flex>
      <Flex direction="column" as="nav" fontSize="sm" color="gray.600" aria-label="Main Navigation">
        <NavItem url="/" icon={Home}>
          Home
        </NavItem>

        {/* Start of Map */}
        <NavItem icon={Map} onClick={mapDisclosure.onToggle}>
          <Flex w="80%">
            <Text>Map</Text>
            <Box ml="auto">
              {mapDisclosure.isOpen ? <ChevronUp size="18" /> : <ChevronDown size="18" />}
            </Box>
          </Flex>
        </NavItem>
        <Collapse in={mapDisclosure.isOpen}>
          <NavItem url="/trip-explorer" pl="12" py="2">
            Trip Explorer
          </NavItem>
          <NavItem url="/parking" pl="12" py="2">
            Parking
          </NavItem>
          <NavItem url="/ride-locator" pl="12" py="2">
            Ride Locator
          </NavItem>
          <NavItem url="/geofencing" pl="12" py="2">
            Geofencing
          </NavItem>
        </Collapse>

        {/* Start of Fleet Status */}
        <NavItem icon={Radio} onClick={fleetStatusDisclosure.onToggle}>
          <Flex w="80%">
            <Text>Fleet Status</Text>
            <Box ml="auto">
              {fleetStatusDisclosure.isOpen ? <ChevronUp size="18" /> : <ChevronDown size="18" />}
            </Box>
          </Flex>
        </NavItem>
        <Collapse in={fleetStatusDisclosure.isOpen}>
          <NavItem url="/fleet/live" pl="12" py="2">
            Live
          </NavItem>
          <NavItem url="/fleet/staging" pl="12" py="2">
            Staging
          </NavItem>
          <NavItem url="/fleet/out-of-service" pl="12" py="2">
            Out of Service
          </NavItem>
          <NavItem url="/fleet/archived" pl="12" py="2">
            Archived
          </NavItem>
        </Collapse>

        {/* Start of Hubs */}
        <NavItem icon={Lock} onClick={hubsDisclosure.onToggle}>
          <Flex w="80%">
            <Text>Hubs</Text>
            <Box ml="auto">
              {hubsDisclosure.isOpen ? <ChevronUp size="18" /> : <ChevronDown size="18" />}
            </Box>
          </Flex>
        </NavItem>
        <Collapse in={hubsDisclosure.isOpen}>
          <NavItem url="/hubs/live" pl="12" py="2">
            Live
          </NavItem>
          <NavItem url="/hubs/staging" pl="12" py="2">
            Staging
          </NavItem>
        </Collapse>

        {/* Other sidebar items */}
        <NavItem url="/users" icon={Users}>
          Users
        </NavItem>
        <NavItem url="/payments" icon={CreditCard}>
          Payments
        </NavItem>
        <NavItem url="/reports" icon={PieChart}>
          Reports
        </NavItem>
        <NavItem url="/analytics" icon={TrendingUp}>
          Analytics
        </NavItem>
        <NavItem url="/profile" icon={User}>
          Profile
        </NavItem>
        <NavItem url="/help" icon={HelpCircle}>
          Help
        </NavItem>
      </Flex>
    </Box>
  )
}

export default SidebarContent
