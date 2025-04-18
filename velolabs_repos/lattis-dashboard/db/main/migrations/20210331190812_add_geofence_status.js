module.exports = {
  async up (knex) {
    await knex.schema.table('geofences', table => {
      table.boolean('status').defaultTo(false)
    })
  },
  async down (knex) {
    await knex.schema.table('geofences', table => {
      table.dropColumn('status')
    })
  }
}
