'use strict';
const config = require('../config');
const options = config.googleMaps.options;
const nodeGeocoder = require('node-geocoder');
const geocoder = nodeGeocoder(options);
const logger = require('./../utils/logger');


/**
 * This method gets address data for a given coordinate.
 *
 * @param {Object} coordinate - An object with keys latitude and longitude
 * @param {Function} callback - Callback function
 */
const getAddressInformationForCoordinate = (coordinate, callback) => {
    let addressInfo = {};
    geocoder.reverse({lat: coordinate.latitude, lon: coordinate.longitude}, (error, response) => {
        if(error){
            logger('Error: Could not get address for coordinate', coordinate);
            callback(error, null);
        }
        if(response.length > 0){
            addressInfo.street = response[0].streetNumber ? response[0].streetNumber.concat(" ").concat(response[0].streetName) : response[0].streetName;
            addressInfo.address = response[0].formattedAddress;
            addressInfo.city = response[0].city;
            addressInfo.country = response[0].country;
            addressInfo.countryCode = response[0].countryCode;
            addressInfo.zipCode = response[0].zipCode;
        }
        callback(null, addressInfo);
    })
};


module.exports = {
    getAddressInformationForCoordinate : getAddressInformationForCoordinate
};

