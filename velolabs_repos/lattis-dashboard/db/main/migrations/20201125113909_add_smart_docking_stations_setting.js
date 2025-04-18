module.exports = {
  async up (knex) {
    return knex.schema.table('fleet_metadata', (table) => {
      table.boolean('smart_docking_stations_enabled').nullable().defaultTo(false)
    })
  },
  async down (knex) {
    return knex.schema.table('fleet_metadata', (table) => {
      table.dropColumn('smart_docking_stations_enabled')
    })
  }
}
