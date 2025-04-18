const moment = require('moment')
const { dateShort } = require('./utils')
const sendEmail = require('./send-email')
const createTemplate = require('./template')
// const conversions = require('../../../utils/conversions')

async function onTripVehicleHasUpcomingReservation (reservationDetails, user) {
  const {
    bikes: bike,
    fleets: fleet,
    // fleet_payment_settings: fleetPaymentSettings,
    reservations: reservation
  } = reservationDetails

  // const currency = conversions.currencyCodeToSymbol(
  //   fleetPaymentSettings.currency
  // )

  const content = [
    'We would like to remind you that',
    bike.bike_name,
    ', the vehicle you are currently on trip with, has an upcoming reservation.',
    'Kindly return it to its assigned parking spot by',
    dateShort(
      moment
        .utc(reservation.reservation_start)
        .subtract(30, 'minutes')
        .startOf('minute')
        .format(),
      reservation.reservation_timezone
    ),
    'to avoid being charged a',
    // `${currency}${fleetPaymentSettings.price_for_reservation_late_return}`,
    'fine for the late return.'
  ].join(' ')

  const template = createTemplate({
    content,
    title: 'Upcoming Reservation For Your Current Vehicle.',
    fleet
  })

  return sendEmail({
    recipientEmail: user.email,
    subject: 'Upcoming Reservation For Your Current Vehicle',
    content: template
  })
}

module.exports = {
  onTripVehicleHasUpcomingReservation
}
