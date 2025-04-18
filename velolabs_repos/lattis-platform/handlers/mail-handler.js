'use strict';

const config = require('./../config');
const mandrillApi = require("mandrill-api");
const mandrill_client = new mandrillApi.Mandrill(config.mailer.mandrillApiKey);
const _ = require('underscore');
const logger = require('./../utils/logger');
const errors = require('./../errors/lattis-errors');
const moment = require('moment');

/*
 * This method is to send Email to the recipients
 *
 * @param {Array || String} recipients - List of recipients
 * @param {String} subject - Subject to append to Email
 * @param {String} content - Message to the Email
 * @param {Function} callback
 */
const sendMail = (recipients, subject, content, bcc = false, source, callback) => {
    if (!Array.isArray(recipients)) {
        recipients = [recipients];
    }
    const lattisReceiverEmails = bcc ? config.mailer.bccEmail ? [{
        "email": config.mailer.bccEmail,
        "name": "Lattis BCC Receiver",
        "type": "bcc"
    }] : [] : [];

    _.each(recipients, (recipient) => {
        lattisReceiverEmails.push({
            "email": recipient,
            "type": "to"
        });
    });
    const message = {
        "html": content,
        "subject": subject,
        "from_email": config.mailer.senderEmail,
        "from_name": source || config.mailer.senderName,
        "to": lattisReceiverEmails,
        "headers": {
            "Reply-To": config.mailer.senderEmail
        },
        "important": false,
        "track_opens": null,
        "track_clicks": null,
        "auto_text": null,
        "auto_html": false,
        "inline_css": true,
        "url_strip_qs": null,
        "preserve_recipients": null,
        "view_content_link": null,
        "tracking_domain": null,
        "signing_domain": null,
        "return_path_domain": null,
        "tags": [
            "password-resets"
        ],
        "metadata": {
            "website": config.mailer.website
        },
    };
    const async = false;
    const ip_pool = "Main Pool";
    const send_at = moment().subtract(1, 'hours').utc().format('YYYY-MM-DD HH:mm:ss');

    mandrill_client.messages.send({
        "message": message,
        "async": async,
        "ip_pool": ip_pool,
        "send_at": send_at
    }, (result) => {
        logger('Successfully Email sent to', recipients, 'id:', _.pluck(result, '_id'));
        return callback(null);
    }, (e) => {
        logger('A mandrill error occurred: ' + e.name + ' - ' + e.message);
        return callback(errors.cannotSendEmail(false));
    });
};


module.exports = {
    sendMail: sendMail
};
