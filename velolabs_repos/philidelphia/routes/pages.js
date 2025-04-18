'use strict';

let _ = require('underscore');
let router = require('express').Router();
let logger = require('./../utils/logger');

router.route('/').get(function(req, res) {
    res.render('index');
});

router.route('/order_update').get(function(req, res) {
    if (!_.has(req.query, 'code')) {
        console.log('Error: request did not have the required partial parameter');
        // TODO This should throw an error with 405
        return;
    }

    logger('got code:', req.query.code);
    res.render('index');
});

router.route('/').get(function(req, res) {
    res.render('index')
});

router.route('/index').get(function(req, res) {
    res.render('index');
});

router.route('/partials/:filename').get(function(req, res) {
    if (!_.has(req.params, 'filename')) {
        logger('Error: request did not have the required partial parameter');
        // TODO This should throw an error with 405
        return;
    }

    res.render('partials/' + req.params.filename);
});

module.exports = router;
