"use strict"
const _ = require("underscore")
const otherConstants = require("./constants/other-constants")

// Insert default environment variables for development
if (process.env.NODE_ENV === 'development') {
  process.env.LATTIS_USERS_DB_USERNAME = process.env.LATTIS_USERS_DB_USERNAME || 'admin';
  process.env.LATTIS_USERS_DB_PASSWORD = process.env.LATTIS_USERS_DB_PASSWORD || 'secret';
  process.env.LATTIS_USERS_DB_NAME = process.env.LATTIS_USERS_DB_NAME || 'lattis_users';
  process.env.LATTIS_USERS_DB_HOST = process.env.LATTIS_USERS_DB_HOST || 'localhost';

  process.env.LATTIS_MAIN_DB_USERNAME = process.env.LATTIS_MAIN_DB_USERNAME || 'admin';
  process.env.LATTIS_MAIN_DB_PASSWORD = process.env.LATTIS_MAIN_DB_PASSWORD || 'secret';
  process.env.LATTIS_MAIN_DB_NAME = process.env.LATTIS_MAIN_DB_NAME || 'lattis_main';
  process.env.LATTIS_MAIN_DB_HOST = process.env.LATTIS_MAIN_DB_HOST || 'localhost';

  process.env.LATTIS_LOG_PATH = process.env.LATTIS_LOG_PATH || './logs';

  process.env.AWS_ACCESS_KEY_ID = process.env.AWS_ACCESS_KEY_ID || 'dummy-access-key';
  process.env.AWS_SECRET_KEY = process.env.AWS_SECRET_KEY || 'dummy-secret-key';
  process.env.AWS_REGION = process.env.AWS_REGION || 'us-west-2';

  process.env.LATTIS_NPM_TOKEN = process.env.LATTIS_NPM_TOKEN || 'dummy-npm-token';
  process.env.LATTIS_SENDER_EMAIL = process.env.LATTIS_SENDER_EMAIL || 'noreply@lattis.io';
  process.env.LATTIS_BASE_URL = process.env.LATTIS_BASE_URL || 'http://localhost:3000';
  process.env.MANDRILL_API_KEY = process.env.MANDRILL_API_KEY || 'dummy-mandrill-key';
  process.env.LATTIS_MAPBOX_TOKEN = process.env.LATTIS_MAPBOX_TOKEN || 'dummy-mapbox-token';
  process.env.LUCY_WRITE_QUEUE_URL = process.env.LUCY_WRITE_QUEUE_URL || 'dummy-lucy-write-queue';
  process.env.LUCY_READ_QUEUE_URL = process.env.LUCY_READ_QUEUE_URL || 'dummy-lucy-read-queue';
  process.env.STRIPE_API_KEY = process.env.STRIPE_API_KEY || 'dummy-stripe-key';
  process.env.STRIPE_CLIENT_ID = process.env.STRIPE_CLIENT_ID || 'dummy-stripe-client-id';
  process.env.LATTIS_GOOGLE_MAPS_API_KEY = process.env.LATTIS_GOOGLE_MAPS_API_KEY || 'dummy-google-maps-key';
}

const requiredParams = [
  "LATTIS_USERS_DB_USERNAME",
  "LATTIS_USERS_DB_PASSWORD",
  "LATTIS_USERS_DB_NAME",
  "LATTIS_USERS_DB_HOST",
  "LATTIS_MAIN_DB_USERNAME",
  "LATTIS_MAIN_DB_PASSWORD",
  "LATTIS_MAIN_DB_NAME",
  "LATTIS_MAIN_DB_HOST",
  "LATTIS_LOG_PATH",
  "AWS_ACCESS_KEY_ID",
  "AWS_SECRET_KEY",
  "AWS_REGION",
  "LATTIS_NPM_TOKEN",
  "LATTIS_SENDER_EMAIL",
  "LATTIS_BASE_URL",
  "MANDRILL_API_KEY",
  "LATTIS_MAPBOX_TOKEN",
  "LUCY_WRITE_QUEUE_URL",
  "LUCY_READ_QUEUE_URL",
  "STRIPE_API_KEY",
  "STRIPE_CLIENT_ID",
  "LATTIS_GOOGLE_MAPS_API_KEY",
]

const optionalParams = [
  "LATTIS_ELLIPTICAL_DB_PRIVATE_KEY",
  "LATTIS_ELLIPTICAL_DB_PUBLIC_KEY",
  "LATTIS_ELLIPTICAL_REST_PRIVATE_KEY",
  "LATTIS_ELLIPTICAL_REST_PUBLIC_KEY",
]

const missingParams = []
const missingOptionalParams = []

// Modified: collect missing environment variables first
for (let i = 0; i < requiredParams.length; i++) {
  if (process.env[requiredParams[i]] === undefined) {
    missingParams.push(requiredParams[i])
  }
}
if (missingParams.length !== 0) {
  if (process.env.NODE_ENV && process.env.NODE_ENV.toLowerCase() !== 'development') {
    console.log("Error: environment variables have not been properly setup for the Lattis Platform. The following variables were not found:", missingParams);
    throw new Error("Lattis Platform Environment Variables Not Properly Set");
  } else {
    console.warn("Warning: missing environment variables:", missingParams);
  }
}

for (let i = 0; i < optionalParams.length; i++) {
  if (!_.has(process.env, requiredParams[i])) {
    missingOptionalParams.push(requiredParams[i])
  }
  if (missingOptionalParams.length !== 0) {
    console.log(
      "Warning: optional env variables have not been properly setup for the Lattis Platform. The following variables was not found:",
      missingOptionalParams
    )
  }
}

module.exports = {
  appName: "lattis-platform",

  paths: {
    logPath: process.env.LATTIS_LOG_PATH,
  },

  bikeBookingExpiration: process.env.BIKE_BOOKING_EXPIRATION
    ? process.env.BIKE_BOOKING_EXPIRATION
    : 900,

  databaseInfo: {
    main: {
      userName: process.env.LATTIS_MAIN_DB_USERNAME,
      password: process.env.LATTIS_MAIN_DB_PASSWORD,
      databaseName: process.env.LATTIS_MAIN_DB_NAME,
      host: process.env.LATTIS_MAIN_DB_HOST,
      port: process.env.LATTIS_MAIN_DB_PORT,
    },

    users: {
      userName: process.env.LATTIS_USERS_DB_USERNAME,
      password: process.env.LATTIS_USERS_DB_PASSWORD,
      databaseName: process.env.LATTIS_USERS_DB_NAME,
      host: process.env.LATTIS_USERS_DB_HOST,
      port: process.env.LATTIS_USERS_DB_PORT,
    },
  },

  serverDatabaseInfo: {
    main: {
      userName: process.env.LATTIS_MAIN_SERVER_DB_USERNAME,
      password: process.env.LATTIS_MAIN_SERVER_DB_PASSWORD,
      databaseName: process.env.LATTIS_MAIN_SERVER_DB_NAME,
      host: process.env.LATTIS_MAIN_SERVER_DB_HOST,
      port: process.env.LATTIS_MAIN_SERVER_DB_PORT,
    },
    users: {
      userName: process.env.LATTIS_USERS_SERVER_DB_USERNAME,
      password: process.env.LATTIS_USERS_SERVER_DB_PASSWORD,
      databaseName: process.env.LATTIS_USERS_SERVER_DB_NAME,
      host: process.env.LATTIS_USERS_SERVER_DB_HOST,
      port: process.env.LATTIS_USERS_SERVER_DB_PORT,
    },
  },

  aws: {
    keyId: process.env.AWS_ACCESS_KEY_ID,
    key: process.env.AWS_SECRET_ACCESS_KEY,
    region: process.env.AWS_REGION,
    s3: {
      firmware: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.ellipse.firmware"
            : "lattis.ellipse.firmware.dev",
      },
      firmwareUpdateLog: {
        bucket: "lattis.ellipse.firmware.log",
        file: "ellipse_fw_log.json",
      },
      termsAndConditions: {
        bucket: "terms.and.conditions",
        file: "terms_and_conditions.json",
      },
      fleetTAndC: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/fleet_t_and_c"
            : "lattis.development/fleet_t_and_c",
      },
      damageReports: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/damage_reports"
            : "lattis.development/damage_reports",
      },
      parkingSpots: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/parking_spots"
            : "lattis.development/parking_spots",
      },
      bikeImages: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/bike_images"
            : "lattis.development/bike_images",
      },
      bikeParkedImages: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/bike_parked_images"
            : "lattis.development/bike_parked_images",
      },
      fleetLogos: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/fleet_logos"
            : "lattis.development/fleet_logos",
      },
      fleetDetails: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/fleet_details"
            : "lattis.development/fleet_details",
      },
      memberLists: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/member_lists"
            : "lattis.development/member_lists",
      },
      fleetReports: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/reports"
            : "lattis.development/reports",
      },
      contracts: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/contracts"
            : "lattis.development/contracts",
      },
      startTrip: {
        bucket:
          this.mode === otherConstants.server.prod
            ? "lattis.production/fleet_start_trip_email"
            : "lattis.development/fleet_start_trip_email",
      },
    },
    sqs: {
      lucy: {
        queues: {
          write: process.env.LUCY_WRITE_QUEUE_URL,
          read: process.env.LUCY_READ_QUEUE_URL,
        },
      },
    },
  },

  mailer: {
    baseUrl: process.env.LATTIS_BASE_URL,
    senderEmail: process.env.LATTIS_SENDER_EMAIL,
    senderName: "Lattis",
    website: "www.lattis.io",
    bccEmail: _.has(process.env, "LATTIS_BCC_EMAIL")
      ? process.env.LATTIS_BCC_EMAIL
      : null,
    mandrillApiKey: process.env.MANDRILL_API_KEY,
  },
  cronURL: {
    baseUrl: process.env.LATTIS_BASE_URL,
    port: process.env.LATTIS_DASHBOARD_PORT,
  },
  mapBox: {
    geoCodingBaseUrl: "https://api.mapbox.com/geocoding/v5/mapbox.places/",
    directionsBaseUrl: "https://api.mapbox.com/directions/v5/mapbox/cycling/",
    apiToken: process.env.LATTIS_MAPBOX_TOKEN,
  },
  stripe: {
    apiKey: process.env.STRIPE_API_KEY,
    stripeClientId: process.env.STRIPE_CLIENT_ID,
    stripeMerchantId: process.env.STRIPE_MERCHANT_ID,
  },
  ellipticalKeys: {
    database: {
      privateKey: process.env.LATTIS_ELLIPTICAL_DB_PRIVATE_KEY,
      publicKey: process.env.LATTIS_ELLIPTICAL_DB_PUBLIC_KEY,
    },

    rest: {
      privateKey: process.env.LATTIS_ELLIPTICAL_REST_PRIVATE_KEY,
      publicKey: process.env.LATTIS_ELLIPTICAL_REST_PUBLIC_KEY,
    },
  },
  googleMaps: {
    options: {
      provider: "google",
      httpAdapter: "https", // Default
      apiKey: process.env.LATTIS_GOOGLE_MAPS_API_KEY, // for Mapquest, OpenCage, Google Premier
      formatter: null, // 'gpx', 'string', ...
    },
  },
  lattisLogoUrl: process.env.LATTIS_LOGO_URL || 'https://via.placeholder.com/150',
}
