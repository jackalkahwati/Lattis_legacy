const axios = require("axios");
const { logger } = require("../utils");
class ActonAPIClient {
  constructor ({ baseUrl, MAPTEX_API_KEY, cached = 1, wait = 60 }) {
    this.baseUrl = baseUrl;
    this.MAPTEX_API_KEY = MAPTEX_API_KEY;
    this.cached = cached;
    this.wait = wait;
  }

  async getCurrentStatus (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/current-status?cached=${this.cached}&wait=${this.wait}`;
      const response = await axios.get(
        path,
        {
          headers: {
            "MAPTEX-API-KEY": this.MAPTEX_API_KEY
          }
        }
      );
      return response.data;
    } catch (error) {
      logger.error("Error ACTON status::", error.message || error);
    }
  }

  // Starts and unlocks
  async startVehicle (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/start-vehicle?cached=${this.cached}&wait=${this.wait}`;
      const config = {
        method: "post",
        url: path,
        headers: {
          "MAPTEX-API-KEY": this.MAPTEX_API_KEY
        }
      };
      const response = await axios(config);
      return response.data;
    } catch (error) {
      logger.error("Error ACTON status::", error.message || error);
    }
  }

  // Stops and locks
  async stopVehicle (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/stop-vehicle?cached=${this.cached}&wait=${this.wait}`;
      const config = {
        method: "post",
        url: path,
        headers: {
          "MAPTEX-API-KEY": this.MAPTEX_API_KEY
        }
      };
      const response = await axios(config);
      return response.data;
    } catch (error) {
      logger.error("Error ACTON status::", error.message || error);
    }
  }

  // Locate vehicle
  async locateVehicle (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/locate?cached=${this.cached}&wait=${this.wait}`;
      const config = {
        method: "post",
        url: path,
        headers: {
          "MAPTEX-API-KEY": this.MAPTEX_API_KEY
        }
      };
      const response = await axios(config);
      return response.data;
    } catch (error) {
      logger.error("Error sending locate command to ACTON::", error.message || error);
    }
  }
}

const ActonAPI = new ActonAPIClient({
  baseUrl: process.env.ACTON_API_URL,
  MAPTEX_API_KEY: process.env.MAPTEX_API_KEY
});

const OmniAPI = new ActonAPIClient({
  baseUrl: process.env.OMNI_API_URL,
  MAPTEX_API_KEY: process.env.OMNI_MAPTEX_API_KEY
});

const OkaiAPIClient = new ActonAPIClient({
  baseUrl: process.env['OKAI_API_URL'],
  MAPTEX_API_KEY: process.env['OKAI_MAPTEX_API_KEY']
})

module.exports = { ActonAPIClient, ActonAPI, OmniAPI, OkaiAPIClient };
