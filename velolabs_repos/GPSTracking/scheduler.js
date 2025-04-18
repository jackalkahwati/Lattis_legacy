/** @format */

const { TripModel, TicketModel, BikeAndControllerModel } = require("./database/models");
const { GeotabApi } = require("./integrations");
const { logger } = require("./utils");
const axios = require("axios");
const { DASHBOARD_URL, DASHBOARD_API_KEY } = process.env;

const config = {
  headers: {
    "x-api-key": DASHBOARD_API_KEY,
    "x-api-client": "GPSTrackingService"
  }
};

const checkAndCreateTickets = async (bike) => {
  const { fleetId, bikeId } = bike;
  console.log(`<Tickets> have been summoned for bike ${bikeId}</Tickets>`);
  if (bike.bikeBattery <= 15) {
    console.log(
      `<Creating> battery ticket for bike ${bikeId} with ${bike.bikeBattery}</Creating>`
    );
    const checkPendingBatteryTicket = await TicketModel.findOne({
      bike_id: bike.bikeId,
      resolved: false,
      category: "low_battery"
    }).exec();
    if (!checkPendingBatteryTicket) {
      const data = {
        category: "low_battery",
        fleet_id: fleetId,
        customer_id: 11111111,
        operator_id: 11111111,
        bike_id: bikeId,
        powerPercent: bike.bikeBattery,
        time: bike.statusUtcTime,
        notes: `Low battery on bike ${bikeId}`
      };

      await axios
        .post(`${DASHBOARD_URL}/api/gpsService/create-ticket`, data, config)
        .then((response) => {
          const payload = response.data.payload;
          const ticketInstance = new TicketModel({
            status: payload && payload.status,
            fleet_id: fleetId,
            ticket_id: payload && payload.ticket_id,
            bike_id: bikeId,
            category: "low_battery"
          });
          ticketInstance
            .save()
            .then(() => {
              console.log(
                `<Ticket> created and Saved for ${bikeId} with ${bike.bikeBattery}</Ticket>`
              );
            })
            .catch((err) => {
              throw new Error(err);
            });
        })
        .catch((error) => {
          logger.error("Error creating ticket in dashboard::", error.message || error);
        });
    }
  } else if (bike.bikeBattery <= 0) {
    console.log(
      `<Creating> depleted battery ticket for bike ${bikeId} with ${bike.bikeBattery}</Creating>`
    );
    const checkPendingBatteryTicket = await TicketModel.findOne({
      bike_id: bike.bikeId,
      resolved: false,
      category: "low_battery"
    }).exec();
    if (!checkPendingBatteryTicket) {
      const data = {
        category: "depleted_battery",
        fleet_id: fleetId,
        customer_id: 11111111,
        operator_id: 11111111,
        bike_id: bikeId,
        powerPercent: bike.bikeBattery,
        time: bike.statusUtcTime,
        notes: `Depleted battery on bike ${bikeId}`
      };

      await axios
        .post(`${DASHBOARD_URL}/api/gpsService/create-ticket`, data, config)
        .then((response) => {
          const payload = response.data.payload;
          const ticketInstance = new TicketModel({
            status: payload && payload.status,
            fleet_id: fleetId,
            ticket_id: payload && payload.ticket_id,
            bike_id: bikeId,
            category: "depleted_battery"
          });
          ticketInstance
            .save()
            .then(() => {
              console.log(
                `<Ticket> created and Saved for ${bikeId} with ${bike.bikeBattery}</Ticket>`
              );
            })
            .catch((err) => {
              throw new Error(err);
            });
        })
        .catch((error) => {
          logger.error("Error creating ticket in dashboard::", error.message || error);
        });
    } else {
      const data = {
        category: "low_battery",
        fleet_id: fleetId,
        customer_id: 11111111,
        operator_id: 11111111,
        bike_id: bikeId,
        powerPercent: bike.bikeBattery,
        time: bike.statusUtcTime,
        notes: `Low battery on bike ${bikeId}`
      };

      await axios
        .post(`${DASHBOARD_URL}/api/gpsService/resolve-ticket`, data, config)
        .then((response) => {
          const payload = response.data.payload;
          const ticketInstance = new TicketModel({
            status: payload && payload.status,
            fleet_id: fleetId,
            ticket_id: payload && payload.ticket_id,
            bike_id: bikeId,
            category: "low_battery"
          });
          ticketInstance
            .save()
            .then(async () => {
              const data = {
                category: "depleted_battery",
                fleet_id: fleetId,
                customer_id: 11111111,
                operator_id: 11111111,
                bike_id: bikeId,
                powerPercent: bike.bikeBattery,
                time: bike.statusUtcTime,
                notes: `Depleted battery on bike ${bikeId}`
              };

              await axios
                .post(`${DASHBOARD_URL}/api/gpsService/create-ticket`, data, config)
                .then((response) => {
                  const payload = response.data.payload;
                  const ticketInstance = new TicketModel({
                    status: payload && payload.status,
                    fleet_id: fleetId,
                    ticket_id: payload && payload.ticket_id,
                    bike_id: bikeId,
                    category: "depleted_battery"
                  });
                  ticketInstance
                    .save()
                    .then(() => {
                      console.log(
                        `<Ticket> created and Saved for ${bikeId} with ${bike.bikeBattery}</Ticket>`
                      );
                    })
                    .catch((err) => {
                      throw new Error(err);
                    });
                })
                .catch((error) => {
                  logger.error(
                    "Error creating ticket in dashboard::",
                    error.message || error
                  );
                });
            })
            .catch((err) => {
              throw new Error(err);
            });
        })
        .catch((error) => {
          logger.error("Error creating ticket in dashboard::", error.message || error);
        });
    }
  }
};

// Update data in the dashboard after every 5 minutes for vehicles updated within last 5 minutes
const updateDashboardData = async () => {
  try {
    const updatedWithin5Minutes = await BikeAndControllerModel.find({
      updatedAt: { $gt: new Date().getTime() - 5 * 60 * 1000 }
    }).lean();
    const bikes = updatedWithin5Minutes.map((bike) => {
      const position = bike.position;
      const metaInfo = bike.meta;
      return {
        bikeId: bike.bike_id,
        bikeBattery: bike.battery,
        batteryIoT: bike.batteryIoT || bike.battery,
        name: bike.bike_name,
        vendor: bike.vendor,
        controllerKey: bike.controller_key,
        statusUtcTime:
          (metaInfo && metaInfo.statusUtcTime) || Date.parse(bike.updatedAt),
        position: position || null,
        fleetId: bike.fleet_id
      };
    });
    if (bikes.length)
      await axios.patch(
        `${DASHBOARD_URL}/api/gpsService/bikes`,
        { data: bikes },
        config
      );
    const ticketPromises = [];
    for (let bike of bikes) {
      // Check and create ticket without blocking the request
      ticketPromises.push(checkAndCreateTickets(bike));
    }
    Promise.all(ticketPromises);
  } catch (error) {
    logger.error("Error:: 🚀 ", error.message || error);
  }
};

const trackGeotabTrips = async () => {
  try {
    const geotabVehiclesOnTrip = await BikeAndControllerModel.find({
      status: "on_trip",
      vendor: "Geotab IoT",
      current_trip: { $ne: null }
    })
      .populate({
        path: "trip",
        model: "Trip"
      })
      .exec();
    for (let geotabVehicle of geotabVehiclesOnTrip) {
      if (!geotabVehicle.geotabInfo) continue;
      const geotabInfo =
        geotabVehicle.geotabInfo &&
        Object.assign(
          {},
          ...[...geotabVehicle.geotabInfo.entries()].map(([k, v]) => ({ [k]: v }))
        );
      const onGoingTrip = geotabVehicle.trip;
      if (!onGoingTrip) {
        logger.error(
          `Current trip for ${geotabVehicle.bike_id}: ${geotabVehicle.bike_name} missing`
        );
        return;
      }
      if (geotabInfo && geotabInfo.id) {
        const client = new GeotabApi(geotabVehicle.fleet_id);
        const dataFeed = await client.getDeviceDataFeed(
          geotabInfo.id,
          onGoingTrip.toVersion
        ); // toVersion is the last feed that was fetched.
        const tripId = onGoingTrip.trip_id;
        if (dataFeed && dataFeed.steps && dataFeed.steps.length) {
          const steps = dataFeed.steps;
          const lastPosition = steps.slice(-1)[0];
          if (lastPosition) {
            const position = {
              latitude: lastPosition[0],
              longitude: lastPosition[1],
              gpsUtcTime: lastPosition[2]
            };
            BikeAndControllerModel.updateOne(
              { bike_id: geotabVehicle.bike_id },
              { position }
            )
              .then(() => {
                /* bike details updated */
              })
              .catch((error) => {
                logger.error("Error updating IoT::", error);
              });
          }
          await TripModel.findOneAndUpdate(
            {
              trip_id: tripId
            },
            {
              toVersion: dataFeed.toVersion,
              $push: {
                steps: steps
              }
            },
            { new: true }
          );
        }
      } else {
        logger.error(
          `Geotab vehicle ${geotabVehicle.bike_id}: ${geotabVehicle.bike_name} missing ID, GPS info won't te tracked`
        );
      }
    }
  } catch (error) {
    logger.error(`An error occurred updating onGoing geotab vehicles: ${error.message}`);
    logger.error(JSON.stringify(error, null, 2));
  }
};

const updateGeotabBattery = async () => {
  try {
    const geotabBikes = await BikeAndControllerModel.find({
      vendor: "Geotab IoT",
      geotabInfo: { $ne: null }
    }).lean();
    const promises = [];
    const clients = {};
    for (let bike of geotabBikes) {
      let client;
      if (!clients[bike.fleet_id]) {
        client = new GeotabApi(bike.fleet_id);
        clients[bike.fleet_id] = client;
      } else client = clients[bike.fleet_id];
      const p = client.getDeviceBatteryStatus(bike);
      promises.push(p);
    }
    Promise.all(promises); // Execute the promises without waiting
  } catch (error) {
    logger.error(`An error occurred updating geotab vehicles battery: ${error.message}`);
  }
};

module.exports = {
  updateDashboardData,
  trackGeotabTrips,
  updateGeotabBattery
};
