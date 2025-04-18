const { StatusCodes } = require("http-status-codes");
const i18n = require("i18n");

const checkScooterIdValidOrNot = (req, res, next) => {
  const ScooterID = req.body.ScooterID;
  if (ScooterID.match(/^\d{10}$/)) {
    next();
  } else {
    res.status(StatusCodes.BAD_REQUEST).json({
      data: "",
      status: false,
      message: i18n.__("SCOOTERID_IS_INVALID"),
    });
  }
};

module.exports = { checkScooterIdValidOrNot };
