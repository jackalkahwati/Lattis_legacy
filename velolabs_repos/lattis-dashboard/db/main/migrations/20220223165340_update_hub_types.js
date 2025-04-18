module.exports = {
  async up (knex) {
    await knex.schema.alterTable('hubs_and_fleets', (table) => {
      table.enum('type', ['parking_station', 'docking_station']).notNullable().alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('status', table => {
      table.string('type', 15).notNullable().alter()
    })
  }
}
