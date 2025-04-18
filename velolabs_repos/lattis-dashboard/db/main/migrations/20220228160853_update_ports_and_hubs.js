module.exports = {
  async up (knex) {
    await knex.schema.table('ports', (table) => {
      table.string('current_status', 25).nullable()
    })
    await knex.schema.table('hubs', (table) => {
      table.string('current_status', 25).nullable()
    })
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.string('current_status', 25).nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('ports', (table) => {
      table.dropForeign('current_status')
    })
    await knex.schema.table('hubs', (table) => {
      table.dropForeign('current_status')
    })
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.dropForeign('current_status')
    })
  }
}
