const Api = require("mg-api-js");
const { logger } = require("../utils");
const { FleetMetadataModel, BikeAndControllerModel } = require("../database/models");
const { encryptionHandler } = require("@velo-labs/platform");
const { computePercentage } = require("../utils");
class GeotabApi {
  constructor(fleetId) {
    this.fleetId = fleetId;
  }

  async init() {
    try {
      const fleet = await FleetMetadataModel.findOne({ fleet_id: this.fleetId }).lean();
      let geotabInfo = fleet && fleet.meta && fleet.meta.geotabInfo;
      if (geotabInfo) {
        const { database, server_name: serverName, userName, sessionId, options } = geotabInfo;
        this.options = options;
        this.authentication = {
          credentials: { userName, sessionId, database },
          path: serverName
        };
        this.api = new Api(this.authentication, options);
      } else throw new Error("Error instantiating Geotab API::");
    } catch (error) {
      logger.error("Error instantiating Geotab API::", error);
    }
  }

  async getDeviceData(serialNumber) {
    try {
      await this.init();
      const [data] = await this.api.call("Get", {
        typeName: "Device",
        search: { serialNumber }
      });
      if (data) {
        const { id, name, serialNumber, vehicleIdentificationNumber, deviceType } = data;
        return { id, name, serialNumber, vehicleIdentificationNumber, deviceType };
      }
    } catch (error) {
      logger.error("Error getting Geotab device info::", error);
    }
  }

  /**
   * Get geotab vehicle data feed
   *
   * @param {*} deviceId
   * @param {String} startFromVersion last version data that was fetched for trip. We start here
   * @returns
   * @memberof GeotabApi
   */
  async getDeviceDataFeed(deviceId, startFromVersion) {
    try {
      await this.init();
      const params = {
        typeName: "LogRecord",
        search: {
          "deviceSearch": {
            id: deviceId
          }
        }
      };
      if (startFromVersion) {
        params.fromVersion = startFromVersion;
      } else {
        params.search.fromDate = (new Date()).toISOString();
      }
      const result = await this.api.call("GetFeed", params);
      const { data, toVersion } = result;
      let mappedInfo = [];
      if (data && data.length) {
        mappedInfo = data.map(point => {
          return [point.latitude, point.longitude, Math.floor(Date.parse(point.dateTime) / 1000)];
        });
      }
      return { steps: mappedInfo, toVersion };
    } catch (error) {
      logger.error("Error getting Geotab device data feed::", error);
      if (error && error.message && error.message.includes("Incorrect login credentials")) {
        await this.renewGeotabSession();
        await this.getDeviceDataFeed(deviceId, startFromVersion);
      }
    }
  }

  /**
   * Fetch battery status from 5hrs ago, pick the latest
   *
   * @param {String} bikeId
   */
  async getDeviceBatteryStatus(bike) {
    try {
      const vehicle = bike;
      await this.init();
      if (vehicle.geotabInfo && vehicle.geotabInfo.id) {
        const id = vehicle.geotabInfo.id;
        let lastVersion;
        if (vehicle.geotabInfo.batteryValues && vehicle.geotabInfo.batteryValues.length) {
          lastVersion = vehicle.geotabInfo.batteryValues.slice(-1)[0].version;
        }

        // const serialNumber = vehicle.geotabInfo.serialNumber;
        const params = {
          "typeName": "StatusData",
          "search": {
            // "serialNumber": serialNumber,
            "deviceSearch": {
              id
            },
            "diagnosticSearch": {
              id: "DiagnosticGoDeviceVoltageId"
            },
            "fromDate": new Date((new Date()).valueOf() - 1000 * 60 * 60 * 5) // From 5 days ago, pick the latest
          }
        };
        if (lastVersion) {
          params.fromVersion = lastVersion;
        } else {
          params.search["fromDate"] = new Date((new Date()).valueOf() - 1000 * 60 * 60 * 24 * 5); // From 5 days ago, pick the latest
        }

        const { data } = await this.api.call("GetFeed", params);
        let lastHundredValues = (vehicle.geotabInfo && vehicle.geotabInfo.batteryValues) || [];
        const slicedNewValues = data && data.slice(-100);
        if (slicedNewValues.length) {
          if (slicedNewValues.length > 50 || slicedNewValues.length > lastHundredValues.length) {
            lastHundredValues = slicedNewValues;
          } else if(data && Array.isArray(data) && data.length && lastHundredValues && Array.isArray(lastHundredValues)) {
            const firstValueVersion = data[0].version; // first version of the new values
            const valueIndex = lastHundredValues.findIndex(value => value.version === firstValueVersion);
            const deleteCount = valueIndex < 0 ? 0 : lastHundredValues.length - valueIndex;
            lastHundredValues = lastHundredValues.splice(valueIndex, deleteCount, slicedNewValues);
          } else {
            lastHundredValues = slicedNewValues;
          }
        }
        const percentage = computePercentage(lastHundredValues);
        await BikeAndControllerModel.findOneAndUpdate({ bike_id: vehicle.bike_id }, { battery: percentage, batteryIoT: percentage, $set: { "geotabInfo.batteryValues": lastHundredValues } });
      }
    } catch (error) {
      logger.error("Error getting Geotab battery status::", error);
      if (error && error.message && error.message.includes("Incorrect login credentials")) {
        await this.renewGeotabSession();
        await this.getDeviceBatteryStatus(bike);
      }
    }
  }

  async renewGeotabSession() {
    try {
      // Renew sessionId
      const fleetInfo = await FleetMetadataModel.findOne({ fleet_id: this.fleetId }).lean();
      if (fleetInfo.meta && fleetInfo.meta.geotabInfo) {
        const metaInfoCopy = { ...fleetInfo.meta.geotabInfo };
        const password = encryptionHandler.decryptDbValue(metaInfoCopy.apiKey);
        this.authentication.credentials.password = password;
        delete this.authentication.credentials.sessionId;
        this.api = new Api(this.authentication, this.options);
        const { credentials } = await this.api.getSession();
        await FleetMetadataModel.updateOne({ fleet_id: this.fleetId }, { $set: { "meta.geotabInfo.sessionId": credentials.sessionId } });
      }
    } catch (error) {
      console.log(`🚀 ~ Geotab renewing session for fleet ${ this.fleetId } failed`);
      logger.error("Error updating geotab sessionId::", error);
    }
  }
}

module.exports = GeotabApi;