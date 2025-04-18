module.exports = {
  up (knex) {
    return knex.schema.createTable('geofences', function (table) {
      table.bigIncrements('geofence_id').primary()
      table.integer('fleet_id')
        .unsigned()
        .notNullable()
        .references('fleet_id')
        .inTable('fleets')
      table.string('name').notNullable()
      table.json('geometry').notNullable()
      table.timestamp('date_created').notNullable().defaultTo(knex.fn.now())
    })
  },
  down (knex) {
    return knex.schema.dropTable('geofences')
  }
}
