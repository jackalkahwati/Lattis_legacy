module.exports = {
  async up (knex) {
    await knex.schema.table('integrations', (table) => {
      table.dropForeign('fleet_id')
      table.dropUnique('fleet_id')
    })
  },
  async down (knex) {
    return Promise.resolve()
  }
}
