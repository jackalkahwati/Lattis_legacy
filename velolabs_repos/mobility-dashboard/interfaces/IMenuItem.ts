import { ComponentWithAs, IconProps } from '@chakra-ui/react'

interface IMenuItem {
  path: string
  title: string
  icon: ComponentWithAs<'svg', IconProps>
}

export default IMenuItem
