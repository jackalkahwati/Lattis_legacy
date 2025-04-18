const util = require('util')

const helpers = require('../helpers')
const userHandler = require('../../user-handler')
const conversions = require('../../../utils/conversions')
const membershipHelpers = require('../../fleet-membership-handlers/helpers')

const sendEmail = require('./send-email')
const createTemplate = require('./template')
const bikeConstants = require('../../../constants/bike-constants')

const getUserDetails = util.promisify(userHandler.getUserWithUserId)

async function lateReturnFeeCharged (record) {
  const [fleetDetails, userDetails, paymentDetails] = await Promise.all([
    helpers.fetchBikeFleetDetails(record.bike_id, {
      currentStatus: [bikeConstants.current_status.parked, bikeConstants.current_status.collect]
    }),
    getUserDetails(record.user_id),
    membershipHelpers.retrieveUserPaymentSettings({
      user: { user_id: record.user_id }
    })
  ])

  const currency = conversions.currencyCodeToSymbol(
    fleetDetails.fleetPaymentSettings.currency
  )

  const summary = [
    `Your credit card ending in`,
    paymentDetails.cc_no.slice(-4),
    'has been charged a late return fee of',
    `${currency}${fleetDetails.fleetPaymentSettings.price_for_reservation_late_return}`,
    'for the late return of',
    fleetDetails.bike.bike_name,
    'This vehicle had an upcoming reservation which has been subsequently cancelled.',
    record.reservation
      ? 'Kindly note that you will also be charged the pay-as-you-go rate for this vehicle for the extra time when ending the ride.'
      : ''
  ].join(' ')

  const template = createTemplate({
    content: summary,
    title: 'Late Return Fee',
    fleet: fleetDetails.fleet
  })

  return sendEmail({
    recipientEmail: userDetails.email,
    subject: 'Late Return Fee Charged',
    content: template
  })
}

module.exports = {
  lateReturnFeeCharged
}
