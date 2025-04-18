import {
  Avatar,
  Box,
  ChakraProps,
  Flex,
  Icon,
  IconButton,
  OmitCommonProps,
  useColorModeValue,
} from '@chakra-ui/react'
import { Bell } from 'react-feather'
import React from 'react'
import SidebarContent from './SidebarContent'

interface LayoutProps extends Omit<OmitCommonProps<ChakraProps>, 'children'> {
  children: React.ReactNode
  avatarUrl?: string
  avatarName?: string
  onNotificationClick?: () => void
  onAvatarClick?: () => void
}

const Layout: React.FC<LayoutProps> = ({
  children,
  avatarUrl,
  avatarName,
  onNotificationClick,
  onAvatarClick,
  ...rest
}) => {
  const bgColor = useColorModeValue('gray.50', 'gray.700')
  const headerBgColor = useColorModeValue('white', 'gray.800')
  const borderColor = useColorModeValue('blackAlpha.300', 'whiteAlpha.300')
  const iconColor = useColorModeValue('gray.500', 'gray.400')

  return (
    <Box as="section" bg={bgColor} minH="100vh" {...rest}>
      <SidebarContent display={{ base: 'none', md: 'unset' }} aria-label="Main Navigation" />
      <Box ml={{ base: 0, md: 60 }} transition=".3s ease">
        <Flex
          as="header"
          align="center"
          justify="flex-end"
          w="full"
          px="4"
          bg={headerBgColor}
          borderBottomWidth="1px"
          borderColor={borderColor}
          h="14"
          role="banner"
        >
          <Flex align="center">
            <IconButton
              aria-label="Notifications"
              variant="ghost"
              icon={<Icon as={Bell} boxSize="24px" color={iconColor} />}
              onClick={onNotificationClick}
            />
            <Avatar
              ml="4"
              size="sm"
              name={avatarName || 'User'}
              src={avatarUrl}
              cursor="pointer"
              onClick={onAvatarClick}
              aria-label={`${avatarName || 'User'} profile`}
            />
          </Flex>
        </Flex>
        <Box as="main" p="4" role="main" aria-label="Main Content">
          {children}
        </Box>
      </Box>
    </Box>
  )
}

export default Layout
