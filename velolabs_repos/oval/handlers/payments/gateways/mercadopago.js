const { errors, logger, responseCodes } = require('@velo-labs/platform')
const mercadoPago = require('mercadopago')
const axios = require('axios').default

mercadoPago.configurations.configure({
  sandbox: true,
  // Use a dummy string here to make the library load without barfing.
  // Each fleet connected to Mercado Pago will have an access token stored
  // in the db. The access token will be configured for each request in the
  // request options. As such, we will never have to use a "platform" access
  // token, since that would make money go into the "platform", which may be the
  // wrong place.
  access_token: '<placeholder>'
})

const client = axios.create({
  baseURL: 'https://api.mercadopago.com/v1/'
})

client.interceptors.response.use(
  (response) => {
    return Promise.resolve(response.data)
  },
  (error) => {
    if (error.response && error.response.data) {
      return Promise.reject(error.response.data)
    }

    return Promise.reject(error.message)
  }
)

const customers = {
  search ({ email }, { accessToken }) {
    return client.get('/customers/search', {
      data: { email },
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    })
  }
}

const cards = {
  tokens: {
    create ({ cardId, publicKey }) {
      return client.post(
        '/card_tokens',
        { card_id: cardId },
        { params: { public_key: publicKey } }
      )
    }
  }
}

const MP = {
  client,
  customers,
  cards
}

async function retrieveCustomer ({ email }, { accessToken }) {
  const {
    results: [customer]
  } = await MP.customers.search({ email }, { accessToken })

  return customer
}

async function createCustomer ({ email, ...rest }, { accessToken } = {}) {
  logger('Creating customer', { email, ...rest })

  const customer = await retrieveCustomer({ email }, { accessToken })

  if (customer) {
    logger('>>>>> Customer already exists', customer)
  }

  return (
    customer ||
    mercadoPago.customers
      .create({ email, ...rest }, { access_token: accessToken })
      .then((response) => response.body)
  )
}

async function createCard ({ customer, cardToken }, { accessToken } = {}) {
  console.log('>>>>> Creating Card: ', {
    customer,
    cardToken: (cardToken || '').slice(0, 4) + '...'
  })

  const { body: card } = await mercadoPago.card.create(
    { token: cardToken, customer_id: customer.id },
    { access_token: accessToken }
  )

  return card
}

/**
 * Add a card to a Mercado Pago customer.
 *
 * @param {object} options
 * @param {object} options.user
 * @param {string} options.user.email
 * @param {object} options.params
 * @param {string} options.params.token
 * @param {object} context Execution context
 * @param {string} context.accessToken Fleet specific Mercado Pago access token.
 *
 * @returns {object} Added Mercado Pago card.
 */
async function addCard ({ user, params }, context = {}) {
  if (!params.token) {
    throw errors.customError(
      'A card token is required to add a Mercado Pago card.',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  try {
    const customer = await createCustomer(
      {
        email: user.email,
        first_name: user.first_name,
        last_name: user.last_name
      },
      context
    )

    const card = await createCard(
      { customer, cardToken: params.token },
      context
    )

    return card
  } catch (error) {
    console.error(error)
    logger(
      'Error adding a Mercado Pago card to user',
      errors.errorWithMessage(error)
    )

    throw errors.invalidCreditCard(false)
  }
}

async function retrieveCard ({ customerId, id }, { accessToken }) {
  const { body: card } = await mercadoPago.card
    .get(customerId, id, {
      access_token: accessToken
    })
    .catch((error) => {
      logger('Error retrieving card', errors.errorWithMessage(error))

      return { body: null }
    })

  return card
}

async function charge (
  { amount, card, user },
  { cardToken, accessToken, publicKey } = {}
) {
  let token = cardToken

  if (!token) {
    if (!publicKey) {
      throw errors.customError(
        'A publicKey is required to create a Mercado Pago card token.',
        responseCodes.BadRequest,
        'BadRequest'
      )
    }

    const tokenResponse = await MP.cards.tokens.create({
      cardId: card.card_id,
      publicKey
    })

    token = tokenResponse.id
  }

  const mpCard = await retrieveCard(
    { customerId: card.stripe_net_profile_id, id: card.card_id },
    { accessToken }
  )

  if (!mpCard) {
    logger('Invalid MercadoPago card', {
      customerId: card.stripe_net_profile_id,
      id: card.card_id
    })

    throw errors.customError(
      'Invalid Card',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  const data = {
    token,
    transaction_amount: amount,
    installments: 1,
    payment_method_id: mpCard.payment_method.id,
    payer: {
      type: 'customer',
      id: mpCard.customer_id,
      email: user.email,
      first_name: user.first_name,
      last_name: user.last_name
    }
  }

  logger('*********** charging MP', {
    ...data,
    token: `${data.token.slice(0, 4)}...`
  })

  const { body: charge } = await mercadoPago.payment.create(data, {
    access_token: accessToken
  })

  return charge
}

async function deleteCard (userPaymentProfile, context = {}) {
  const { body } = await mercadoPago.card.delete(
    userPaymentProfile.stripe_net_profile_id,
    userPaymentProfile.card_id,
    { access_token: context.accessToken }
  )

  return body
}

module.exports = {
  MP,
  mercadoPago,
  retrieveCustomer,
  createCustomer,
  createCard,
  addCard,
  charge,
  deleteCard
}
