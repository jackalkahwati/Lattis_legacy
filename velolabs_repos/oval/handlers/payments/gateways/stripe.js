const {
  config,
  logger,
  errors,
  responseCodes
} = require('@velo-labs/platform')
const conversions = require('../../../utils/conversions')
const stripe = require('stripe')(config.stripe.apiKey)

const handleError = (error) => {
  logger('Error adding stripe card to user', errors.errorWithMessage(error))

  throw errors.invalidCreditCard(false)
}

const normalizeCard = (paymentMethod, customerId) => {
  return {
    ...paymentMethod.card,
    ...paymentMethod,
    customer_id: customerId
  }
}

async function addCard ({ user, userPaymentProfile, params }) {
  let customerId = (userPaymentProfile || {}).stripe_net_profile_id

  if (!customerId) {
    const {
      data: [customer]
    } = await stripe.customers.list({ email: user.email })

    customerId = (customer || {}).id
  }

  if (!params.intent && params.intent.payment_method) {
    throw errors.customError(
      'Add cards request is missing stripe setup intent or intent does not have a payment method',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  if (!customerId) {
    const options = {
      email: user.email,
      payment_method: params.intent.payment_method
    }

    return stripe.customers
      .create(options)
      .then(async (c) => {
        logger('Created Stripe customer', { customer: c.id })

        const paymentMethod = await stripe.paymentMethods.retrieve(
          options.payment_method
        )
        if (params.stripeAccountId) {
          await clonePaymentMethod(
            {
              user: user,
              paymentMethod: options.payment_method,
              customer: c.id,
              connectedAccountId: params.stripeAccountId
            })
        }
        return normalizeCard(paymentMethod, c.id)
      })
      .catch(handleError)

    // return stripe.customers
    //   .create(options)
    //   .then(async (c) => {
    //     logger('Created Stripe customer', { customer: c.id })

    //     const paymentMethod = await stripe.paymentMethods.retrieve(
    //       options.payment_method
    //     )

    //     return normalizeCard(paymentMethod, c.id)
    //   })
    //   .catch(handleError)
  }

  // Note (waiyaki: 2021-06-02) before now, the stripe integration wasn't saving
  // user emails in the customer object, which makes them really hard to track
  // on the Stripe dashboard. This code progressively updates existing customer
  // accounts with emails if they add cards.
  const customer = await stripe.customers.retrieve(customerId)
  if (!customer.email) {
    logger('Updating stripe customer email', {
      user_id: user.user_id,
      customerId
    })

    await stripe.customers.update(customerId, { email: user.email })
  }

  const attachedMethod = await stripe.paymentMethods.retrieve(
    params.intent.payment_method
  )

  if (attachedMethod.customer === customerId) {
    return normalizeCard(attachedMethod, customerId)
  }

  return stripe.paymentMethods
    .attach(params.intent.payment_method, { customer: customerId })
    .then(async (method) => {
      await clonePaymentMethod(
        {
          user: user,
          paymentMethod: params.intent.payment_method,
          customer: customerId,
          connectedAccountId: params.stripeAccountId
        })
      return normalizeCard(method, customerId)
    })
    .catch(handleError)
}

async function updateCard ({ user, userPaymentProfile, params }) {
  let customerId = (userPaymentProfile || {}).stripe_net_profile_id

  if (!customerId) {
    const {
      data: [customer]
    } = await stripe.customers.list({ email: user.email })

    customerId = (customer || {}).id
  }

  // if (!params.intent && params.intent.payment_method) {
  //   throw errors.customError(
  //     'Add cards request is missing stripe setup intent or intent does not have a payment method',
  //     responseCodes.BadRequest,
  //     'BadRequest'
  //   )
  // }

  const attachedMethod = await stripe.paymentMethods.retrieve(
    params.card_id
  )

  if (attachedMethod.customer === customerId) {
    return stripe.paymentMethods.update(
      params.card_id,
      {card: {exp_month: params.exp_month, exp_year: params.exp_year}}
    )
  } else {
    throw errors.customError(
      'The payment method does not belong to the customer',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }
}

const canClonePaymentMethod = (error) => {
  return (
    error.code === 'resource_missing' &&
    /Stripe-Account header was used but API request was provided with a platform-owned payment method ID/.test(
      error.raw.message
    )
  )
}

const clonePaymentMethod = async ({
  user,
  paymentMethod,
  customer,
  connectedAccountId
}) => {
  let platformCustomer = await stripe.customers.retrieve(customer)

  if (!platformCustomer.email || !platformCustomer.name) {
    logger('Clone:: updating platform customer email', customer)
    platformCustomer = await stripe.customers.update(customer, {
      email: user.email,
      name: `${user.first_name} ${user.last_name}`
    })
  }

  // Clone the customer into the connect account and attach the cloned payment
  // method to that customer. Need to do this attachment before payment intents
  // are created from the cloned payment method.
  // NOTE: (waiyaki: 2021-06-03) ideally, we should maintain a db mapping of
  // connect account customer ids to platform customer id, but we don't have
  // this kind of architecture at this time, so we silently create customers in
  // the connect account.
  let {
    data: [connectedAccountCustomer]
  } = await stripe.customers.list(
    { email: platformCustomer.email },
    { stripe_account: connectedAccountId }
  )

  let clonedPaymentMethod

  if (!connectedAccountCustomer) {
    logger('Cloning payment method', {
      paymentMethod,
      customer,
      connectedAccountId
    })

    clonedPaymentMethod = await stripe.paymentMethods.create(
      { customer, payment_method: paymentMethod },
      { stripe_account: connectedAccountId }
    )

    logger('Creating connected account customer', {
      connectedAccountId,
      paymentMethod: clonedPaymentMethod.id,
      platformCustomer: platformCustomer.id
    })

    await stripe.customers.create(
      {
        email: platformCustomer.email,
        name: platformCustomer.name,
        payment_method: clonedPaymentMethod.id
      },
      { stripe_account: connectedAccountId }
    )
  } else {
    const platformPaymentMethod = await stripe.paymentMethods.retrieve(
      paymentMethod
    )

    const { data: connectedPaymentMethods } = await stripe.paymentMethods.list(
      {
        customer: connectedAccountCustomer.id,
        type: 'card',
        limit: 20
      },
      { stripe_account: connectedAccountId }
    )

    const alreadyAttached = connectedPaymentMethods.find(
      (method) =>
        method.card.fingerprint === platformPaymentMethod.card.fingerprint
    )

    logger('Attaching payment method to customer', {
      connectedAccountId,
      paymentMethod: paymentMethod,
      platformCustomer: platformCustomer.id,
      connectedAccountCustomer: connectedAccountCustomer.id,
      alreadyAttached: !!alreadyAttached
    })

    let currentYear = new Date().getFullYear()
    let currentMonth = new Date().getMonth() + 1
    if (alreadyAttached && alreadyAttached.card && (alreadyAttached.card.exp_year >= currentYear) && (alreadyAttached.card.exp_month >= currentMonth)) {
      return alreadyAttached
    }

    logger('Cloning payment method', {
      paymentMethod,
      customer,
      connectedAccountId
    })

    clonedPaymentMethod = await stripe.paymentMethods.create(
      { customer, payment_method: paymentMethod },
      { stripe_account: connectedAccountId }
    )

    await stripe.paymentMethods.attach(
      clonedPaymentMethod.id,
      { customer: connectedAccountCustomer.id },
      { stripe_account: connectedAccountId }
    )
  }

  const methodWithCustomer = await retrievePaymentMethod({
    paymentMethod: clonedPaymentMethod.id,
    connectedAccountId
  })

  return methodWithCustomer
}

const retrievePaymentMethod = ({ paymentMethod, connectedAccountId }) => {
  return stripe.paymentMethods.retrieve(paymentMethod, {
    stripe_account: connectedAccountId
  })
}

const createPaymentIntent = (
  { paymentMethod, customer, amount, currency, connectedAccountId },
  params = {}
) => {
  logger('Creating payment intent', {
    paymentMethod,
    customer,
    amount,
    currency,
    connectedAccountId
  })

  return stripe.paymentIntents.create(
    {
      customer,
      payment_method: paymentMethod,
      amount: conversions.roundOffPricing(amount * 100),
      currency: currency.toLowerCase(),
      payment_method_types: ['card'],
      off_session: true,
      confirm: true,
      ...params
    },
    {
      stripe_account: connectedAccountId
    }
  )
}

/**
 * Charge amount from the card
 *
 * @param {object} options
 * @param {object} options.user
 * @param {string} options.user.email - Email of the user who owns the card
 * @param {Number} options.amount - amount to be charged
 * @param {String} options.currency - Currency to charge the amount with
 * @param {Object} options.card - Card to be charged from
 * @param {object} options.fleetPaymentSettings
 * @param {string} options.fleetPaymentSettings.stripe_account_id Stripe account
 *                     ID of the fleet to create the charge in.
 */
const charge = (
  { user, amount, currency, card, fleetPaymentSettings },
  params = {}
) => {
  const paymentMethod = card.payment_method
  const customer = card.stripe_net_profile_id
  const connectedAccountId = fleetPaymentSettings.stripe_account_id

  return retrievePaymentMethod({ paymentMethod, connectedAccountId })
    .then((method) => {
      return createPaymentIntent(
        {
          paymentMethod,
          customer: method.customer,
          amount,
          currency,
          connectedAccountId
        },
        params
      )
    })
    .catch((e) => {
      if (!canClonePaymentMethod(e)) {
        return Promise.reject(e)
      }

      return clonePaymentMethod({
        user,
        paymentMethod,
        customer,
        connectedAccountId
      }).then((clonedPaymentMethod) => {
        return createPaymentIntent(
          {
            paymentMethod: clonedPaymentMethod.id,
            amount,
            currency,
            connectedAccountId,
            customer: clonedPaymentMethod.customer
          },
          params
        )
      })
    })
    .catch((error) => {
      logger(
        `Error: Failed to charge from amount ${currency} ${amount} from card ${card.card_id} of user ${card.user_id} with error`,
        error || errors.errorWithMessage(error)
      )
      if (error.code === 'authentication_required') {
        const {
          id,
          client_secret: secret,
          last_payment_error: { message }
        } = error.raw.payment_intent

        const data = {
          error_code: error.code,
          payment_intent: {
            id,
            message,
            client_secret: secret
          }
        }

        const e = new Error(error.message)
        e.code = 402
        e.data = data

        throw e
      } else if (error.code === 'expired_card') {
        const e = new Error(error.message)
        e.status = 409

        throw e
      }

      throw error
    })
}

async function deleteSource (userPaymentProfile) {
  if (!userPaymentProfile.card_id) {
    logger(
      "Don't know how to delete stripe source without card_id",
      userPaymentProfile
    )

    return null
  }

  if (userPaymentProfile.card_id.startsWith('card')) {
    return stripe.customers.deleteSource(
      userPaymentProfile.stripe_net_profile_id,
      userPaymentProfile.card_id
    )
  } else if (userPaymentProfile.card_id.startsWith('pm')) {
    return stripe.paymentMethods.detach(userPaymentProfile.card_id)
  }

  logger("Don't know how to delete stripe source", userPaymentProfile)

  return null
}

async function retrieve (paymentIntentId, { connectedAccountId }) {
  return stripe.paymentIntents.retrieve(paymentIntentId, {
    stripe_account: connectedAccountId
  })
}

/**
 *  Like `charge`, except it captures payments instead of immediately charging them.
 *
 * @see charge
 */
async function authorize ({
  user,
  amount,
  currency,
  card,
  fleetPaymentSettings
}) {
  logger('Authorizing', {
    user: { user_id: user.user_id },
    amount,
    currency,
    card: { id: card.id, card_id: card.card_id },
    fleetPaymentSettings: {
      stripe_account_id: fleetPaymentSettings.stripe_account_id
    }
  })

  return charge(
    { user, amount, currency, card, fleetPaymentSettings },
    {
      capture_method: 'manual'
    }
  )
}

async function capture ({
  paymentIntentId,
  fleetPaymentSettings,
  amountToCapture
}) {
  const data = {}

  if (amountToCapture) {
    data.amount_to_capture = conversions.roundOffPricing(amountToCapture * 100)
  }

  logger('Capturing', {
    paymentIntentId,
    data,
    fleetPaymentSettings: {
      stripe_account: fleetPaymentSettings.stripe_account_id
    }
  })

  return stripe.paymentIntents
    .capture(paymentIntentId, data, {
      stripe_account: fleetPaymentSettings.stripe_account_id
    })
    .catch((error) => {
      if (
        error.raw.code === 'payment_intent_unexpected_state' &&
        error.raw.payment_intent.status === 'succeeded'
      ) {
        return error.raw.payment_intent
      }

      return Promise.reject(error)
    })
}

async function cancel ({ paymentIntentId, fleetPaymentSettings }) {
  logger('Cancelling', {
    paymentIntentId,
    fleetPaymentSettings: {
      stripe_account_id: fleetPaymentSettings.stripe_account_id
    }
  })

  return stripe.paymentIntents.cancel(paymentIntentId, {
    stripe_account: fleetPaymentSettings.stripe_account_id
  })
}

async function getCards (connectedAccountId) {
  let paymentMethods = await stripe.paymentMethods.list({
    customer: connectedAccountId,
    type: 'card'
  })
  if (paymentMethods && paymentMethods.data) {
    return paymentMethods.data
  } else {
    return []
  }
}

module.exports = {
  stripe,
  addCard,
  charge,
  deleteSource,
  retrieve,
  authorize,
  capture,
  cancel,
  updateCard,
  getCards
}
