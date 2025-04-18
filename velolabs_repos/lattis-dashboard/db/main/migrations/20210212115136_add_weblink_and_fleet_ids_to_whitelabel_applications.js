module.exports = {
  async up (knex) {
    await knex.schema.table('whitelabel_applications', table => {
      table.string('weblink').defaultTo(null)
      table.string('fleet_id').defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('whitelabel_applications', table => {
      table.dropColumn('weblink')
      table.dropColumn('fleet_id')
    })
  }
}
