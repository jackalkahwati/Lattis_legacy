module.exports = {
  async up (knex) {
    return knex.schema.table('fleet_metadata', table => {
      table.boolean('geofence_enabled').defaultTo(true)
    })
  },
  async down (knex) {
    return knex.schema.table('fleet_metadata', table => {
      table.dropColumn('geofence_enabled')
    })
  }
}
