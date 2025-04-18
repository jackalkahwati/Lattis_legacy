/** @format */

const axios = require("axios");
const qs = require("querystring");
const logger = require("../utils/logger").logger;

class SegwayApiClient {
  constructor({ baseUrl, clientId, clientSecret }) {
    this.baseUrl = baseUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.token = {
      value: null,
      expires: Date.now()
    };
  }

  async getToken() {
    try {
      if (this.token.value && this.token.expires > Date.now()) {
        return this.token.value;
      }
      const response = await axios.post(
        `${this.baseUrl}/oauth/token`,
        qs.stringify({
          client_id: this.clientId,
          client_secret: this.clientSecret,
          grant_type: "client_credentials"
        }),
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          }
        }
      );
      this.token.value = response.data.access_token;
      this.token.expires = Date.now() + response.data.expires_in * 1000;
      return this.token.value;
    } catch (error) {
      logger.error("Error Get token::", error.message || error);
    }
  }

  async lock (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/lock`,
        {
          iotCode: imei
        },
        {
          headers: {
            "Authorization": `bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error lock::", error.message || error);
    }
  }

  async unlock (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/unlock`,
        {
          iotCode: imei
        },
        {
          headers: {
            "Authorization": `bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error unlock::", error.message || error);
    }
  }

  async getCurrentStatus (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/status?iotCode=${imei}`,
        {
          headers: {
            "Authorization": `Bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error current status::", error.message || error);
    }
  }

  async getVehicleLocation (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/location?iotCode=${imei}`,
        {
          headers: {
            "Authorization": `Bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error Vehicle Location::", error.message || error);
    }
  }

  async getVehicleBatteryInfo (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/battery-info?iotCode=${imei}`,
        {
          headers: {
            "Authorization": `Bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error Vehicle Battery Info::", error.message || error);
    }
  }

  async getVehicleInformation (imei) {
    try {
      const currentStatus = this.getCurrentStatus(imei);
      const location = this.getVehicleLocation(imei);
      const batteryInfo = this.getVehicleBatteryInfo(imei);
      const response = await Promise.all([currentStatus, location, batteryInfo]);
      const statusData = response[0] && response[0].data;
      const locationData = response[1] && response[1].data;
      let responseData = {
        iotCode: imei,
        vehicleCode: imei,
      };
      if(statusData) {
        const statusInfo = {
          online: statusData.online,
          locked: statusData.locked,
          lockVoltage: statusData.lockVoltage,
          networkSignal: statusData.networkSignal,
          charging: statusData.charging,
          powerPercent: statusData.powerPercent,
          speedMode: statusData.speedMode,
          speed: statusData.speed,
          odometer: statusData.odometer,
          remainingRange: statusData.remainingRange,
          totalRidingSecs: statusData.totalRidingSecs,
          statusUtcTime: statusData.statusUtcTime
        };
        responseData = { ...responseData, ...statusInfo };
      }
      if(locationData) {
        let locationsInfo = {
          latitude: locationData.latitude,
          longitude: locationData.longitude,
          satelliteNumber: locationData.satelliteNumber,
          hdop: locationData.hdop,
          altitude: locationData.altitude,
          gpsUtcTime: new Date(locationData.gpsUTCTime).toISOString()
        };
        responseData = { ...responseData, ...locationsInfo };
      }
      return responseData;
    } catch (error) {
      logger.error("Error Vehicle Info::", error.message || error);
    }
  }

  async getFirmwareVersion(imei) {
    const response = await axios.get(
      `${this.baseUrl}/api/vehicle/query/version?iotCode=${imei}&vehicleCode=${imei}`,
      {
        headers: {
          Authorization: `Bearer ${await this.getToken()}`
        }
      }
    );
    return response.data;
  }

  async alertOutOfGeofence(imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/sound`,
        {
          iotCode: imei,
          controlType: 1,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error Alert out of Geo::", error.message || error);
    }
  }

  async setSound(imei, mode) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/setting/sound`,
        {
          iotCode: imei,
          workMode: mode,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${await this.getToken()}`
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error Sound Mode::", error.message || error);
    }
  }
}


module.exports = SegwayApiClient;
