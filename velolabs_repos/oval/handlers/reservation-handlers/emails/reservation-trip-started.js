const { dateShort } = require('./utils')
const sendEmail = require('./send-email')
const createTemplate = require('./template')

async function reservationTripStarted (reservationDetails, user) {
  const {
    bikes: bike,
    fleets: fleet,
    reservations: reservation
  } = reservationDetails

  const summary = [
    'Your reservation of',
    bike.bike_name,
    'starting from',
    dateShort(reservation.reservation_start, reservation.reservation_timezone),
    'to',
    dateShort(reservation.reservation_end, reservation.reservation_timezone),
    'is now ready to start!',
    'You can find your reserved vehicle by opening the Lattis application',
    'on your phone and following the directions as indicated on the map,',
    'after which you can begin your trip.',
    'Kindly note that this trip will be active for the entire duration of your reservation',
    'and ending this trip will end your reservation as well.'
  ].join(' ')

  const template = createTemplate({
    content: summary,
    title: 'Reservation Trip Started.',
    fleet
  })

  return sendEmail({
    recipientEmail: user.email,
    subject: `${bike.bike_name} Reservation Update: Trip Started.`,
    content: template
  })
}

module.exports = {
  reservationTripStarted
}
