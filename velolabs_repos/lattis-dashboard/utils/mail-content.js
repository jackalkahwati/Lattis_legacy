'use strict'

let platFormConfig = {};
// Lazy getter to avoid accessing platform config before it is initialized
const getLogoUrl = () => {
  try {
    const platform = require('@velo-labs/platform')
    const cfg = typeof platform === 'function' ? platform()?.config : platform?.config
    return cfg?.lattisLogoUrl || 'https://via.placeholder.com/150'
  } catch {
    return 'https://via.placeholder.com/150'
  }
}

// We resolve the logo URL lazily to avoid reading platform config before it has finished initializing

function logo () {
  return getLogoUrl()
}

/**
 * This method is used to get the subject,content for create operator mails
 *
 * @param {string} token of the operator
 * @param {string} tokenExpiration time of the operator
 * @param {string} url of the dashboard
 */
const createOperator = (token, tokenExpirationTime, url) => {
  const subject = 'Welcome to Lattis!'
  const content = "<img style='width:40%;display:block;margin:10px auto;'" +
        ' src=' + logo() + " alt='LATTIS LOGO'><br/><h2 style='text-align:center'>Your Lattis account has been " +
        'created! Start with setting up your password. </h2><a href=' +
        url + '/' + token + '/new-user' +
        " style='text-decoration:none;color:white;'><button style='background:#394D65;" +
        'padding:15px;width:250px;color:white;font-size: 16px;border:none;display:block;' +
        "margin:30px auto;'>Get Started</button></a><p style='text-align:center'>" +
        "<i style='font: bold;'>Link:</i> " + url + '/' + token + "/new-user </p><p style='text-align:center'>" +
        "<i style='font: bold;'>Note:</i> Please know for security purposes, the link will " +
        'expire after ' +
        tokenExpirationTime + ' from the time you received the Email. If you need a link ' +
        're-sent, contact us directly.</p>'

  return {
    subject: subject,
    content: content
  }
}

/**
 * This method is used to get the subject,content for forgot password mails
 *
 * @param {string} token of the operator
 * @param {string} tokenExpiration time of the operator
 * @param {string} url of the dashboard
 */
const forgotPassword = (token, tokenExpirationTime, url) => {
  const subject = 'Reset Your Password'
  const content = "<img style='width:40%;display:block;margin:10px auto;'" +
        ' src=' + logo() + " alt='LATTIS LOGO'><br/><h2 style='text-align:center'>Need to reset your password? " +
        'Simply click on the button below or copy the link below and paste into your browser.</h2>' +
        '<a href=' + url + '/' + token + '/user' +
        " style='text-decoration:none;color:white;'><button style='background:#394D65;" +
        'padding:15px;width:250px;color:white;font-size: 16px;border:none;display:block;' +
        "margin:30px auto;'>Reset Password</button></a><p style='text-align:center'>" +
        "<i style='font: bold;'>Link:</i> " + url + '/' + token + "/user </p><p style='text-align:center'>" +
        "(<i style='font: bold;'>Note:</i> This link expires in " + tokenExpirationTime +
        " If exceeded, you'll have to request a new one.)</p><p style='text-align:center'>" +
        "If you didn't make this request, please contact support@lattis.io immediately.</p>"

  return {
    subject: subject,
    content: content
  }
}

module.exports = {
  createOperator: createOperator,
  forgotPassword: forgotPassword
}
