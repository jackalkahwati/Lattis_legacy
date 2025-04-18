import { Flex } from '@chakra-ui/react'

import { UserNav } from '@/components/navigation/UserNav'
import { ThemeToggle } from '@/components/theme/ThemeToggle'

export const Header = (): JSX.Element => (
  <Flex
    as="header"
    align="center"
    justify="space-between"
    w="100%"
    px={4}
    py={2}
    bg="white"
    boxShadow="sm"
  >
    <div className="flex items-center space-x-4">
      <h2 className="text-lg font-semibold">Mobility Dashboard</h2>
    </div>
    <div className="flex items-center space-x-4">
      <ThemeToggle />
      <UserNav />
    </div>
  </Flex>
)
