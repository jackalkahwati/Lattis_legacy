module.exports = {
  async up (knex) {
    await knex.schema.table('integrations', table => {
      table.string('metadata').defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('integrations', table => {
      table.dropColumn('metadata')
    })
  }
}
