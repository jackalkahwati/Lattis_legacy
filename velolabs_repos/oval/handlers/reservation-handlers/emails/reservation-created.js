const util = require('util')

const helpers = require('../helpers')
const userHandler = require('../../user-handler')
const conversions = require('../../../utils/conversions')
const membershipHelpers = require('../../fleet-membership-handlers/helpers')

const { dateShort } = require('./utils')
const sendEmail = require('./send-email')
const createTemplate = require('./template')
const createFrenchTemplate = require('./frenchTemplate')
const bikeConstants = require('../../../constants/bike-constants')

const getUserDetails = util.promisify(userHandler.getUserWithUserId)

const operatorReservationCreatedNotification = ({
  reservation,
  fleetDetails,
  userDetails
}) => {
  const from = dateShort(
    reservation.reservation_start,
    reservation.reservation_timezone,
    { abbrTimezone: true }
  )

  const to = dateShort(
    reservation.reservation_end,
    reservation.reservation_timezone,
    { abbrTimezone: true }
  )

  const name =
    userDetails.first_name && userDetails.last_name
      ? `${userDetails.first_name} ${userDetails.last_name}`
      : userDetails.email

  const content = [
    'A reservation for',
    fleetDetails.bike.bike_name,
    `from ${from}`,
    `to ${to}`,
    `has been made by ${name}.`
  ].join(' ')

  return createTemplate({
    content,
    title: userDetails && userDetails.language_preference === 'fr' ? `Réservation créée: ${fleetDetails.bike.bike_name}, ${from} - ${to}` : `Reservation Created: ${fleetDetails.bike.bike_name}, ${from} - ${to}`,
    fleet: fleetDetails.fleet
  })
}

const frenchOperatorReservationCreatedNotification = ({
  reservation,
  fleetDetails,
  userDetails
}) => {
  const from = dateShort(
    reservation.reservation_start,
    reservation.reservation_timezone,
    { abbrTimezone: true }
  )

  const to = dateShort(
    reservation.reservation_end,
    reservation.reservation_timezone,
    { abbrTimezone: true }
  )

  const name =
    userDetails.first_name && userDetails.last_name
      ? `${userDetails.first_name} ${userDetails.last_name}`
      : userDetails.email

  const content = [
    'Une réservation pour',
    fleetDetails.bike.bike_name,
    `débutant le ${from}`,
    `jusqu’à ${to}`,
    `a été effectuée par ${name}.`
  ].join(' ')

  return createFrenchTemplate({
    content,
    title: userDetails && userDetails.language_preference === 'fr' ? `Réservation créée: ${fleetDetails.bike.bike_name}, ${from} - ${to}` : `Reservation Created: ${fleetDetails.bike.bike_name}, ${from} - ${to}`,
    fleet: fleetDetails.fleet
  })
}

async function reservationCreated ({ reservation, charge, trip }) {
  const [fleetDetails, userDetails, paymentDetails] = await Promise.all([
    helpers.fetchBikeFleetDetails(reservation.bike_id, {
      currentStatus: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect,
        bikeConstants.current_status.reserved
      ]
    }),
    getUserDetails(reservation.user_id),
    membershipHelpers
      .retrieveUserPaymentSettings({
        user: { user_id: reservation.user_id }
      })
      .catch(() => null)
  ])

  const currency = conversions.currencyCodeToSymbol(
    fleetDetails.fleetPaymentSettings.currency
  )

  const summaryHeader = charge.id
    ? [
      `This email confirms that your credit card ending in`,
      paymentDetails.cc_no.slice(-4),
      `has been charged`,
      `${currency}${charge.amount ? `${charge.amount / 100}` : 0}`,
      `(with a ${currency}${trip.membership_discount} membership discount)`
    ]
    : ['This is a confirmation']

  const summary = [
    ...summaryHeader,
    `for the reservation of ${fleetDetails.bike.bike_name}`,
    `from ${dateShort(
      reservation.reservation_start,
      reservation.reservation_timezone
    )}`,
    `to ${dateShort(
      reservation.reservation_end,
      reservation.reservation_timezone
    )}.`
  ].join(' ')

  const frenchSummaryHeader = charge.id
    ? [
      `Cet e-mail confirme que votre carte de crédit se termine par`,
      paymentDetails.cc_no.slice(-4),
      `a été accusé`,
      `${currency}${charge.amount ? `${charge.amount / 100}` : 0}`,
      `(avec un ${currency}${trip.membership_discount} réduction d’adhésion)`
    ]
    : ['Ceci est une confirmation']

  const frenchSummary = [
    ...frenchSummaryHeader,
    `pour la réservation de ${fleetDetails.bike.bike_name}`,
    `débutant le ${dateShort(
      reservation.reservation_start,
      reservation.reservation_timezone
    )}`,
    `jusqu’à ${dateShort(
      reservation.reservation_end,
      reservation.reservation_timezone
    )}.`
  ].join(' ')

  const template = createTemplate({
    content: summary,
    title: 'Reservation Confirmation',
    fleet: fleetDetails.fleet
  })

  const frenchTemplate = createFrenchTemplate({
    content: frenchSummary,
    title: 'Confirmation de réservation',
    fleet: fleetDetails.fleet
  })

  sendEmail({
    recipientEmail: userDetails.email,
    subject: userDetails && userDetails.language_preference === 'fr' ? 'Confirmation de réservation' : 'Reservation Confirmation',
    content: userDetails && userDetails.language_preference === 'fr' ? frenchTemplate : template
  })

  if (fleetDetails.fleet.contact_email) {
    // TODO: (waiyaki) - Instead of sending another email here, see how to modify
    // `sendEmail` to bcc the operator the same email sent to the user.
    sendEmail({
      recipientEmail: fleetDetails.fleet.contact_email,
      subject: userDetails && userDetails.language_preference === 'fr' ? `Réservation créée: ${fleetDetails.bike.bike_name}` : `Reservation Created: ${fleetDetails.bike.bike_name}`,
      content: userDetails && userDetails.language_preference === 'fr' ? frenchOperatorReservationCreatedNotification({
        reservation,
        fleetDetails,
        userDetails
      }) : operatorReservationCreatedNotification({
        reservation,
        fleetDetails,
        userDetails
      })
    })
  }
}

module.exports = {
  reservationCreated
}
