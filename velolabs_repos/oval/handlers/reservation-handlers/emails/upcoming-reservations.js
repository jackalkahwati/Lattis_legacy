const { dateShort } = require('./utils')
const sendEmail = require('./send-email')
const createTemplate = require('./template')

async function upcomingReservation (reservationDetails, user) {
  const {
    bikes: bike,
    fleets: fleet,
    reservations: reservation
  } = reservationDetails

  const summary = [
    'You have an upcoming reservation for',
    bike.bike_name,
    'starting from',
    dateShort(reservation.reservation_start, reservation.reservation_timezone),
    'to',
    dateShort(reservation.reservation_end, reservation.reservation_timezone),
    '. Kindly note that once the reservation time arrives,',
    'you will be able to begin your trip from the Lattis application.',
    'This trip will be active for the entire duration of your reservation',
    'and ending this trip will end your reservation as well.',
    'You will be able to find your reserved vehicle by opening the Lattis application',
    'on your phone and following the directions as indicated on the map.'
  ].join(' ')

  const frenchSummary = [
    'Vous avez une réservation à venir pour',
    bike.bike_name,
    'débutant le',
    dateShort(reservation.reservation_start, reservation.reservation_timezone),
    'à',
    dateShort(reservation.reservation_end, reservation.reservation_timezone),
    '. Veuillez noter que lorsque la réservation commence,',
    'vous pouvez débuter votre location depuis l’application Lattis.',
    ' La location sera active pour toute la durée de la réservation et l’arrêt de la location mettra fin à la réservation',
    ' Vous pouvez trouver votre location en ouvrant l’application Lattis sur votre téléphone et en suivant les instructions sur la carte'
  ].join(' ')

  const subject = user && user.language_preference === 'fr' ? `Réservation à venir pour ${bike.bike_name}.` : `Upcoming Reservation for ${bike.bike_name}.`

  const template = createTemplate({
    fleet,
    title: subject,
    content: user && user.language_preference === 'fr' ? frenchSummary : summary
  })

  return sendEmail({
    subject,
    content: template,
    recipientEmail: user.email
  })
}

module.exports = {
  upcomingReservation
}
