'use strict';
const config = require('./../config');

/**
 * This method is used to get the subject,content for all activity ticket mails
 *
 * @operatorName {string}
 * @fleetName {string}
 */
const allActivityTicket = (operatorName, fleetName) => {
    const url = config.mailer.baseUrl ? config.mailer.baseUrl : '';
    const lattisLogo = config.lattisLogoUrl;
    const subject = "Lattis dashboard notification - ALL activity";
    const content = `<p style="color:#4A5060 ;margin-top:20px;">Hi <span style="color:#3F4E69 ;font-weight: 600;">` + operatorName +`</span></p><p style="color:#4A5060;"> There has been new Activity for 
                     <span style="color:#3F4E69;font-weight: 600;">` + fleetName + `</span>, <a href=` + url + `/#/activity-feed>click here</a> to view </p>
                     <p style="color:#4A5060;"> To change email settings, <a href=` + url + `/#/account-settings/>click here.</a></p>
                     <br/> <p style="color:#4A5060 ;margin-bottom: 0px;">Powered By Lattis</p><img style="width:28%;margin-left: -18px;" src= ` + lattisLogo +` alt="LATTIS LOGO" />`;

    return {
        subject : subject,
        content : content
    };
};

/**
 * This method is used to get the subject,content for theft reported ticket mails
 *
 * @operatorName {string}
 * @fleetName {string}
 *
 */
const theftReportedTicket = (operatorName, fleetName) => {
    const url = config.mailer.baseUrl ? config.mailer.baseUrl : '';
    const lattisLogo = config.lattisLogoUrl;
    const subject = "Lattis dashboard notification - Theft reported";
    const content = `<p style="color:#4A5060 ;margin-top:20px;">Hi <span style="color:#3F4E69 ;font-weight: 600;">` 
        + operatorName +`</span></p><p style="color:#4A5060;"> There has been new 'Theft reported' activity for
        <span style="color:#3F4E69;font-weight: 600;">` + fleetName + `</span>, <a href=` + url + `/#/activity-feed>click here</a> to view </p>
        <p style="color:#4A5060;"> To change email settings, <a href=` + url + `/#/account-settings/>click here.</a></p>
        <br/> <p style="color:#4A5060 ;margin-bottom: 0px;">Powered By Lattis</p>
        <img style="width:28%;margin-left: -18px;" src= ` +lattisLogo+` alt="LATTIS LOGO" />`;

    return {
        subject : subject,
        content : content
    };
};


/**
 * This method is used to get the subject,content for theft reported ticket mails
 *
 * @operatorName {string}
 * @fleetName {string}
 *
 */
 const potentialTheftTicket = (operatorName, fleetName) => {
  const url = config.mailer.baseUrl ? config.mailer.baseUrl : '';
  const lattisLogo = config.lattisLogoUrl;
  const subject = "Lattis dashboard notification - Potential theft";
  const content = `<p style="color:#4A5060 ;margin-top:20px;">Hi <span style="color:#3F4E69 ;font-weight: 600;">`
      + operatorName +`</span></p><p style="color:#4A5060;"> There has been new 'Potential theft' activity for
      <span style="color:#3F4E69;font-weight: 600;">` + fleetName + `</span>, <a href=` + url + `/#/activity-feed>click here</a> to view </p>
      <p style="color:#4A5060;"> To change email settings, <a href=` + url + `/#/account-settings/>click here.</a></p>
      <br/> <p style="color:#4A5060 ;margin-bottom: 0px;">Powered By Lattis</p>
      <img style="width:28%;margin-left: -18px;" src= ` +lattisLogo+` alt="LATTIS LOGO" />`;

  return {
      subject : subject,
      content : content
  };
};


module.exports ={
    allActivityTicket : allActivityTicket,
    theftReportedTicket : theftReportedTicket,
    potentialTheftTicket
};