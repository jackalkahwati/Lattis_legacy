module.exports = {
  async up (knex) {
    return knex.schema.table('fleet_metadata', table => {
      table.string('kuhmute_api_key', 50).defaultTo(null)
      table.timestamps(true, true)
    })
  },
  async down (knex) {
    return knex.schema.table('fleet_metadata', table => {
      table.dropColumn('kuhmute_api_key')
      table.dropColumn('created_at')
      table.dropColumn('updated_at')
    })
  }
}
