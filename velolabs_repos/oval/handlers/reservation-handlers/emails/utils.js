const moment = require('moment-timezone')

const dateShort = (date, tz, { abbrTimezone = true } = {}) =>
  moment
    .utc(date)
    .tz(tz)
    .format(`MM/DD/YYYY HH:mmA${abbrTimezone ? ' z' : ''}`)

const contactInfo = (fleet) => {
  let contactInfo = ''

  if (fleet.contact_email && fleet.contact_phone) {
    contactInfo = `<p style="margin-left: 5px;">Need help?</p>
    <p style="margin-left: 5px;">Email us at ${fleet.contact_email} or call us at ${fleet.contact_phone} for questions about your reservation.</p>`
  }

  return contactInfo
}

const contactInfoFrench = (fleet) => {
  let contactInfoFrench = ''

  if (fleet.contact_email && fleet.contact_phone) {
    contactInfoFrench = `<p style="margin-left: 5px;">Besoin d’aide?</p>
    <p style="margin-left: 5px;">Ecrivez nous à ${fleet.contact_email} ou appelez nous au ${fleet.contact_phone} si vous avez des questions sur votre réservation.</p>`
  }

  return contactInfoFrench
}

module.exports = {
  dateShort,
  contactInfo,
  contactInfoFrench
}
