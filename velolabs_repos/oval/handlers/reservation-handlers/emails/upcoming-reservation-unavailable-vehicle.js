const { dateShort } = require('./utils')
const sendEmail = require('./send-email')
const createTemplate = require('./template')

async function upcomingReservationWithUnavailableVehicle (
  reservationDetails,
  user
) {
  const {
    bikes: bike,
    fleets: fleet,
    reservations: reservation
  } = reservationDetails

  const content = [
    'Unfortunately, your reservation of',
    bike.bike_name,
    `from ${dateShort(
      reservation.reservation_start,
      reservation.reservation_timezone
    )}`,
    `to ${dateShort(
      reservation.reservation_end,
      reservation.reservation_timezone
    )}`,
    'has been cancelled due to unavailability of the reserved vehicle.',
    'The fee charged for this reservation has been fully refunded.'
  ].join(' ')

  const frenchContent = [
    'Malheureusement votre réservation pour',
    bike.bike_name,
    `débutant le ${dateShort(
      reservation.reservation_start,
      reservation.reservation_timezone
    )}`,
    `à ${dateShort(
      reservation.reservation_end,
      reservation.reservation_timezone
    )}`,
    'a été annulée pour indisponibilité.',
    'Les frais de réservation ont été remboursés.'
  ].join(' ')

  const template = createTemplate({
    content,
    title: 'Upcoming Reservation Cancelled Due to Vehicle Unavailability',
    fleet
  })

  const frenchTemplate = createTemplate({
    frenchContent,
    title: 'La réservation à venir est annulée pour indisponibilité',
    fleet
  })

  return sendEmail({
    recipientEmail: user.email,
    subject: user && user.language_preference === 'fr' ? 'Annulation de la réservation à venir' : 'Upcoming Reservation Cancelled',
    content: user && user.language_preference === 'fr' ? frenchTemplate : template
  })
}

module.exports = {
  upcomingReservationWithUnavailableVehicle
}
