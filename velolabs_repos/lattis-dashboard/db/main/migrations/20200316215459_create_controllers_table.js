module.exports = {
  up (knex) {
    return knex.schema.createTable('controllers', function (table) {
      table.bigIncrements('controller_id').primary()
      table.string('device_type').notNullable()
      table.string('vendor').notNullable()
      table.string('key').notNullable()
      table.string('fw_version').nullable()
      table.string('hw_version').nullable()
      table.string('make').nullable()
      table.string('model').nullable()
      table.decimal('latitude', 10, 8).nullable()
      table.decimal('longitude', 11, 8).nullable()
      table.timestamp('gps_log_time').nullable()
      table.string('status').nullable()
      table.integer('battery_level').nullable()
      table.string('qr_code').nullable()
      table.integer('fleet_id')
        .unsigned()
        .notNullable()
        .references('fleet_id')
        .inTable('fleets')
      table.bigInteger('added_by_operator_id').notNullable()
      table.integer('bike_id')
        .unsigned()
        .nullable()
        .references('bike_id')
        .inTable('bikes')
      table.timestamp('date_created').notNullable().defaultTo(knex.fn.now())
      table.unique(['vendor', 'key'])
    })
  },
  down (knex) {
    return knex.schema.dropTable('controllers')
  }
}
