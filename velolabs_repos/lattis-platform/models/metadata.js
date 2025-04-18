
'use strict';

let model = require('./model');

let metadata = {
    tableName: 'metadata',

    columns : {
        id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        metadata_ver: 'VARCHAR(64)',
        time_stamp: 'INT(11)',
        twilio_message_number: 'VARCHAR(64)',
        hint: 'BLOB',
        crash_mav: 'VARCHAR(64)',
        crash_sd: 'VARCHAR(64)',
        theft_low_mav: 'VARCHAR(64)',
        theft_low_sd: 'VARCHAR(64)',
        theft_med_mav: 'VARCHAR(64)',
        theft_med_sd: 'VARCHAR(64)',
        theft_high_mav: 'VARCHAR(64)',
        theft_high_sd: 'VARCHAR(64)',
        shackle_x_mav: 'VARCHAR(64)',
        shackle_x_sd:  'VARCHAR(64)',
        shackle_y_mav: 'VARCHAR(64)',
        shackle_y_sd: 'VARCHAR(64)',
        shackle_z_mav: 'VARCHAR(64)',
        shackle_z_sd: 'VARCHAR(64)',
        rssi_1: 'VARCHAR(64)',
        rssi_2: 'VARCHAR(64)',
        rssi_3: 'VARCHAR(64)',
        rssi_4: 'VARCHAR(64)',
        rssi_5: 'VARCHAR(64)',
        battery_25: 'VARCHAR(64)',
        battery_50: 'VARCHAR(64)',
        battery_75: 'VARCHAR(64)',
        battery_100: 'VARCHAR(64)',
        update_firmware: 'VARCHAR(64)',
        update_android: 'VARCHAR(64)',
        update_ios: 'VARCHAR(64)',
        firmware_fixes: 'VARCHAR(1000)'
    },
    columnOrder : [
        'id',
        'metadata_ver',
        'time_stamp',
        'twilio_message_number',
        'hint',
        'crash_mav',
        'crash_sd',
        'theft_low_mav',
        'theft_low_sd',
        'theft_med_mav',
        'theft_med_sd',
        'theft_high_mav',
        'theft_high_sd',
        'shackle_x_mav',
        'shackle_x_sd',
        'shackle_y_mav',
        'shackle_y_sd',
        'shackle_z_mav',
        'shackle_z_sd',
        'rssi_1',
        'rssi_2',
        'rssi_3',
        'rssi_4',
        'rssi_5',
        'battery_25',
        'battery_50',
        'battery_75',
        'battery_100',
        'update_firmware',
        'update_android',
        'update_ios',
        'firmware_fixes'
    ]

};

metadata.__proto__ = model;

module.exports = metadata;



