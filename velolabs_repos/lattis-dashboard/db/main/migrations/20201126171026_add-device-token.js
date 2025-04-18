module.exports = {
  async up (knex) {
    // Device token for making push notifications
    await knex.schema.table('trips', (table) => {
      table.string('device_token').nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('trips', (table) => {
      table.dropColumn('device_token')
    })
  }
}
