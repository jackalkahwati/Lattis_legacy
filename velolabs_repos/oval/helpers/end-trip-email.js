module.exports = (data) => {
  let contactInfo = ''
  if (data.contact.email && data.contact.phone) {
    contactInfo = `<p>Need help?</p>
    <p>Email us at ${data.contact.email} or call us at ${data.contact.phone} for questions regarding your rental.</p>`
  }
  let taxes = ''
  if (data.charges.taxes && data.charges.taxes.length) {
    taxes = data.charges.taxes.map((item) => (`<p>${item.name}, ${item.percentage}%, ${data.charges.currency}${Number(item.amount).toFixed(2)}</p>`))
    taxes.join('')
  }
  return `
  <div style="display:block; margin: auto; max-width: 500px; text-align:center">
  <img style="display:block;margin: 0 auto;"
          src="${data.fleetLogo}"
          alt="Fleet Logo"
          height="50"
          style="max-width:50px; height:auto; max-height:50px;"
      />
  <h1 style="text-align: center;">Rental receipt</h1>
  <div>
      <h1 style="display:inline-block; font-size:3rem">${data.charges.currency}${data.charges.amount_captured}</h1>
      ${data.charges.cc_no && data.charges.cc_type
    ? `<span style="display: inline-block; padding-left: 15px;">${data.charges.cc_type} ${data.charges.cc_no}</span>`
    : ''}
  </div>
  <div style=" margin: 0px 15px;">
    ${data.charges.bike_unlock_fee
    ? `<p><strong>Unlock fee:</strong> ${data.charges.currency}${data.charges.bike_unlock_fee}</p>`
    : ''}
      <p><strong>Duration charges:</strong> ${data.charges.currency}${data.charges.duration}</p>
      <p><strong>Penalty charges :</strong> ${data.charges.currency}${data.charges.penalty_fees}</p>
      <p><strong>Over usage fees :</strong> ${data.charges.currency}${data.charges.over_usage_fees}</p>
    ${data.charges.membership_discount
    ? `<p><strong>Membership discount:</strong> -${data.charges.currency}${data.charges.membership_discount}</p>`
    : ''}
    ${data.charges.promo_code_discount
    ? `<p><strong>Promo code discount:</strong> -${data.charges.currency}${data.charges.promo_code_discount}</p>`
    : ''}
    <hr/>
    ${data.charges.taxes && data.charges.taxes.length ? `<p><strong>Taxes</strong></p> ${taxes.join('')}` : ''}
    ${data.charges.taxes && data.charges.taxes.length
    ? `<p><strong>Tax Sub Total:</strong> ${data.charges.tax_sub_total}</p>`
    : ''}
    <hr/>
      ${data.charges.preauth_amount
    ? `<p><strong>Amount charged:</strong> ${data.charges.currency}${data.charges.amount_captured}</p>`
    : ''}
    ${data.charges.preauth_amount
    ? `<p><strong>Usage total:</strong> ${data.charges.currency}${data.charges.total}</p>`
    : ''}
    ${data.charges.preauth_amount
    ? `<p><strong>Pre-authorized amount:</strong> ${data.charges.currency}${data.charges.preauth_amount}</p>`
    : ''}
    ${data.charges.preauth_amount
    ? `<p><strong>Amount charged:</strong> ${data.charges.currency}${data.charges.amount_captured}</p>`
    : ''}
  </div>
  <div>
      <div style="display: inline-block; padding-left: 5px">
          <p>Thank you, ${data.name}.</p>
          <p>${data.start.date}</p>
      </div>
  </div>
  <div style="border-top: 1px solid #394d65;margin: 25px 0px;">
      <p>${data.start.time} | ${data.start.place}</p>
      <p>${data.end.time} | ${data.end.place}</p>
  </div>
  <div style="background: #66b1e3; color: #fff; padding: 15px;">
      <img style="display:block;margin: 0 auto;"
          src="${data.fleetLogo}"
          alt="Fleet Logo"
          height="50"
          style="max-width:50px; height:auto; max-height:50px;"
      />
      ${contactInfo}
  </div>
</div>`
}
