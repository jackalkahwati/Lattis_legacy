'use strict';

let mysql = require('mysql');
let config = require('./../config');
let logger = require('./../utils/logger');

module.exports = {
    _pool: null,

    initPool: function(shouldUseDatabase) {
        if (this._pool) {
            return;
        }

        let poolConfig;
        if (shouldUseDatabase) {
            poolConfig = {
                host: config.dataBaseInfo.host,
                user: config.dataBaseInfo.user,
                password: config.dataBaseInfo.password,
                database: config.dataBaseInfo.database,
                port: config.dataBaseInfo.port
            };
        } else {
            poolConfig = {
                host: config.dataBaseInfo.host,
                user: config.dataBaseInfo.user,
                password: config.dataBaseInfo.password,
                port: config.dataBaseInfo.port
            }
        }

        this._pool = mysql.createPool(poolConfig);
    },

    _getPool: function() {
        if (!this._pool) {
            this.initPool(true);
        }

        return this._pool;
    },

    makeQuery: function(query, done) {
        logger('opening pool for query:', query);
        this._getPool().getConnection(function(error, connection) {
            logger('got connection from pool with error:', error);
            if (error) {
                done(error, null);
                return;
            }

            logger('attemping to establish connection for query:', query);
            connection.query(query, function(error, rows) {
                logger('made query:', query, 'with error:', error, 'and rows:', rows);
                done(error, rows);
                connection.release();
            });
        });
    },
    
    destroyPool: function(done) {
        this._pool.end(function (error) {
            if (error) {
                done(error);
                return;
            }
            
            this._pool = null;
            done(null);
        }.bind(this));
    },

    escape: function(input) {
        return mysql.escape(input);
    }
};
