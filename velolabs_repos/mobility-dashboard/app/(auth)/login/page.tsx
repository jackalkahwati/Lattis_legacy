'use client'
import React from 'react'
import {
  Box,
  Button,
  Center,
  Flex,
  FormControl,
  FormErrorMessage,
  FormLabel,
  Input,
  Text,
} from '@chakra-ui/react'
import { InfoOutlineIcon } from '@chakra-ui/icons'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'

//local imports
import { ILoginInput } from '../../../interfaces'
import { loginSchema } from '../../../utils'

const Login: React.FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ILoginInput>({
    resolver: yupResolver(loginSchema),
  })

  const onSubmit = (data: ILoginInput) => {
    console.log(data)
    return data
  }

  return (
    <Center bg="brand.50" h="100vh">
      <Flex direction="row" bg="white" w="45%" h={400} minW={400} rounded="lg" boxShadow="2xl">
        <Flex direction="column" w="70%" p="8">
          <Box textAlign="center" fontSize="xl" mb={5}>
            Sign in to your account
          </Box>
          <Box textAlign="center" fontSize="sm" color="gray.500">
            <form onSubmit={handleSubmit(onSubmit)}>
              <FormControl isInvalid={!!errors?.email} mb={10}>
                <FormLabel fontSize="sm" htmlFor="email">
                  Email Address
                </FormLabel>
                <Input id="email" {...register('email')} type="email" />
                <FormErrorMessage>{errors.email && errors.email.message}</FormErrorMessage>
              </FormControl>

              <FormControl isInvalid={!!errors?.password}>
                <FormLabel fontSize="sm" htmlFor="password">
                  Password
                </FormLabel>
                <Input id="name" {...register('password')} type="password" />
                <FormErrorMessage>{errors.password && errors.password.message}</FormErrorMessage>
              </FormControl>
              <Button w="100%" mt={10} colorScheme="brand" isLoading={isSubmitting} type="submit">
                Sign In
              </Button>
            </form>
          </Box>
        </Flex>

        <Flex
          bg="brand.400"
          direction="column"
          align="center"
          justify="center"
          w="30%"
          p={3}
          roundedRight="md"
        >
          <InfoOutlineIcon fontSize={30} bgSize="auto" />
          <Text fontSize={14} mt={4}>
            No need to ring for service. Our help center is always here for you!
          </Text>
        </Flex>
      </Flex>
    </Center>
  )
}

export default Login
