import { LucideIcon } from 'lucide-react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

import { cn } from '@/lib/utils'

export interface NavLinkProps {
  href: string
  children: React.ReactNode
  className?: string
  icon?: LucideIcon
}

export const NavLink = ({ href, children, className, icon: Icon }: NavLinkProps): JSX.Element => {
  const pathname = usePathname()
  const isActive = pathname === href

  return (
    <Link
      href={href}
      className={cn(
        'flex items-center text-sm font-medium transition-colors hover:text-primary',
        isActive ? 'text-primary' : 'text-muted-foreground',
        className,
      )}
    >
      {Icon && <Icon className="mr-2 h-4 w-4" />}
      {children}
    </Link>
  )
}
