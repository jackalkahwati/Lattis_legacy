const jwt = require("jsonwebtoken");
const { StatusCodes } = require("http-status-codes");
const i18n = require("i18n");

// User Authentication middleware
const checkValidToken = (req, res, next) => {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.split(" ")[1];
  if (token) {
    jwt.verify(token, process.env.ACCESS_TOKEN_KEY, (error, user) => {
      if (error) {
        res.status(StatusCodes.UNAUTHORIZED).send({
          data: "",
          status: false,
          message: i18n.__("INVALID_TOKEN_ERROR"),
        });
        return;
      } else {
        req.user = user;
        next();
      }
    });
  } else {
    res.status(StatusCodes.BAD_REQUEST).send({
      data: "",
      status: false,
      message: i18n.__("AUTHENTICATION_ERROR"),
    });
    return;
  }
};

module.exports = { checkValidToken };
