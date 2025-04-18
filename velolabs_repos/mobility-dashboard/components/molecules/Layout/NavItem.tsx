import React from 'react'
import { Flex, Icon } from '@chakra-ui/react'

import StyledLink from '../../atoms/StyledLink'

type NavItemProps = {
  icon?: React.ElementType
  children: React.ReactNode
  url?: string
  [x: string]: any // for any other props that come into the component
}

const NavItem: React.FC<NavItemProps> = ({ icon, url, children, ...rest }) => {
  return (
    <Flex
      align="center"
      px="4"
      mx="2"
      rounded="md"
      py="3"
      cursor="pointer"
      color="whiteAlpha.700"
      _hover={{
        bg: 'blackAlpha.300',
        color: 'whiteAlpha.900',
      }}
      role="group"
      fontWeight="semibold"
      transition=".15s ease"
      {...rest}
    >
      {icon && (
        <Icon
          mr={4}
          boxSize="4"
          _groupHover={{
            color: 'gray.300',
          }}
          as={icon}
        />
      )}
      {url ? (
        <StyledLink w="100%" href={url}>
          {children}
        </StyledLink>
      ) : (
        children
      )}
    </Flex>
  )
}

export default NavItem
