'use strict';

let config = require('./config');
let bodyParser = require('body-parser');
let morgan = require('morgan');
let mysql = require('mysql');
let path = require('path');
let favicon = require('serve-favicon');
let express = require('express');
let app = express();
let logger = require('./utils/logger');

//BODY PARSER REQ.BODY INFORMATION USE
app.use(bodyParser.urlencoded({extended: true}));
app.use(bodyParser.json());

//MORGAN FOR DEV
app.use(morgan('dev'));

//SET ENGINE AND VIEWS PATH
app.set('views', path.join(__dirname + '/views'));
app.set('view engine', 'jade');

//SERVE STATIC PUBLIC FOLDER
app.use(express.static(path.join(__dirname, 'public')));

let api = require('./routes/api');
let pages = require('./routes/pages');
app.use('/', pages);
app.use('/api', api);

//SERVE FAVICON
app.use(favicon(__dirname + '/public/favicon.ico'));

//SERVER LISTEN
app.listen(config.appPort, function(){
	logger('Listening on port ' + config.appPort);
});
