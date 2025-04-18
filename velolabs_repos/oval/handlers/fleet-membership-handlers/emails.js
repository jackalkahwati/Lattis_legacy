const util = require('util')

const moment = require('moment')
const { logger, mailHandler } = require('@velo-labs/platform')

const userHandler = require('../user-handler')
const conversions = require('../../utils/conversions')
const db = require('../../db')
const { configureSentry } = require('../../utils/configureSentry')

const Sentry = configureSentry()

const getUserDetails = util.promisify(userHandler.getUserWithUserId)

const dateShort = date => moment(date).format('MM/DD/YYYY')

const contactInfo = (fleet) => {
  let info = ''
  if (fleet.contact_email && fleet.contact_phone) {
    info = `<p>Need help?</p>
    <p>Email us at ${fleet.contact_email} or call us at ${fleet.contact_phone} for questions about your subscription</p>`
  }

  return info
}

function retrieveWhiteLabelInfo (fleet) {
  return db
    .main('fleet_metadata')
    .where({ fleet_id: fleet.fleet_id })
    .join(
      'whitelabel_applications',
      'whitelabel_applications.app_id',
      'fleet_metadata.whitelabel_id'
    )
    .select('whitelabel_applications.*')
    .first()
}

function subscriptionCharged ({ subscription, paymentDetails, fleet, fleetMembership, renewal }) {
  const contact = contactInfo(fleet)

  const summary = [
    `This email confirms that your credit card ending in ${paymentDetails.cc_no.slice(-4)}`,
    `has been charged`,
    renewal ? 'to renew your' : 'for a',
    `${fleetMembership.payment_frequency} membership subscription`,
    `to ${fleet.fleet_name} and covers usage from ${dateShort(subscription.period_start)}`,
    `to ${dateShort(subscription.period_end)}.`
  ].join(' ')

  const currency = conversions.currencyCodeToSymbol(fleetMembership.membership_price_currency)

  return `
  <div style="display:block; margin: auto; max-width:768px; text-align:center">
    <img
      style="display:block;margin: 0 auto;"
      src=${fleet.logo || 'https://cdn.shopify.com/s/files/1/0727/8385/t/50/assets/lattis-logo-large.png'}
      alt="Fleet Logo"
      height="50"
      style="max-width:50px; height:auto; max-height:50px;"
    />
    <div style=" margin: 15px 15px;">
      <p>
        ${summary}
      </p>
      <span>-------------------------------------------------</span> <br />
      <span>INVOICE</span> <br />
      <span>
        -------------------------------------------------
      </span> <br />
      <p><strong>Date:</strong> ${dateShort(subscription.created_at)}</p>
      <p><strong>Payment Plan:</strong> ${fleetMembership.payment_frequency}</p>
      <p>
        <strong>Description</strong>
        ${fleet.fleet_name} ${fleetMembership.payment_frequency} subscription, ${dateShort(subscription.period_start)} to ${dateShort(subscription.period_end)}
      </p>
      <p>
        <strong>Price:</strong> ${currency}${fleetMembership.membership_price}
      </p>
      <p>
        <strong>Tax:</strong> ${currency}${fleetMembership.tax_sub_total}
      </p>
      <p>
        <strong>Total:</strong> ${currency}${Number(fleetMembership.membership_price) + Number(fleetMembership.tax_sub_total)}
      </p>
    </div>
    <div>
        <img style="display: inline-block" src="${fleet.logo}" alt="Fleet Logo" width="50" height="50"/>
        <div style="display: inline-block; padding-left: 5px">
            <p>Thank you for choosing ${fleet.fleet_name}.</p>
        </div>
    </div>
    <div style="background: #66b1e3; color: #fff; padding: 15px;">
        <img
            src="https://cdn.shopify.com/s/files/1/0727/8385/t/50/assets/lattis-logo-large.png"
            alt="Lattis Logo"
            width="159"
            height="27"
        />
        ${contact}
    </div>
</div>`
}

function subscriptionCancelled (subscription) {
  const {
    fleet_membership: fleetMembership,
    fleet_membership: { fleet }
  } = subscription

  const contact = contactInfo(fleet)
  const currency = conversions.currencyCodeToSymbol(fleetMembership.membership_price_currency)

  const summary = [
    `This email confirms that your`,
    `${currency}${fleetMembership.membership_price} ${fleetMembership.payment_frequency}`,
    'membership subscription to',
    `${fleet.fleet_name} has been canceled successfully.`,
    `You will continue to enjoy ${fleetMembership.membership_incentive}% off`,
    `for trips taken in this fleet until ${dateShort(subscription.period_end)}.`
  ].join(' ')

  return `
    <div style="display:block; margin: auto; max-width:768px; text-align:center">
      <img
        style="display:block;margin: 0 auto;"
        src=${fleet.logo || 'https://cdn.shopify.com/s/files/1/0727/8385/t/50/assets/lattis-logo-large.png'}
        alt="Fleet Logo"
        height="50"
        style="max-width:50px; height:auto; max-height:50px;"
      />
      <div style=" margin: 15px 15px;">
        <p>
          ${summary}
        </p>
      </div>
      <div>
          <img style="display: inline-block" src="${fleet.logo}" alt="Fleet Logo" width="50" height="50"/>
          <div style="display: inline-block; padding-left: 5px">
              <p>Thank you for choosing ${fleet.fleet_name}.</p>
          </div>
      </div>
      <div style="background: #66b1e3; color: #fff; padding: 15px;">
          <img
              src="https://cdn.shopify.com/s/files/1/0727/8385/t/50/assets/lattis-logo-large.png"
              alt="Lattis Logo"
              width="159"
              height="27"
          />
          ${contact}
      </div>
    </div>
  `
}

function sendEmail ({ recipientEmail, subject, content, source = 'Lattis' }) {
  mailHandler.sendMail(recipientEmail, subject, content, null, source, (error) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error sending email: ', error)
    }
  })
}

async function sendSubscriptionEmail ({
  user,
  fleet,
  fleetMembership,
  paymentDetails,
  subscription,
  renewal = false
}) {
  const content = subscriptionCharged({
    subscription,
    paymentDetails,
    fleet,
    fleetMembership,
    renewal
  })

  const userDetails = await getUserDetails(user.user_id)

  const whiteLabel = await retrieveWhiteLabelInfo(fleet)
  const source = whiteLabel ? whiteLabel.app_name : 'Lattis'

  sendEmail({
    content,
    recipientEmail: userDetails.email,
    subject: `${source || 'Lattis'} - ${fleet.fleet_name} subscription receipt`,
    source
  })
}

async function sendSubscriptionCancelledEmail ({ subscription, user }) {
  const content = subscriptionCancelled(subscription)

  const userDetails = await getUserDetails(user.user_id)

  const whiteLabel = await retrieveWhiteLabelInfo(subscription.fleet_membership.fleet)
  const source = whiteLabel ? whiteLabel.app_name : 'Lattis'

  return sendEmail({
    content,
    recipientEmail: userDetails.email,
    subject: `${source || 'Lattis'} - ${
      subscription.fleet_membership.fleet.fleet_name
    } subscription canceled`,
    source
  })
}

module.exports = {
  subscriptionCharged,
  sendSubscriptionEmail,
  subscriptionCancelled,
  sendSubscriptionCancelledEmail
}
