module.exports = {
  async up (knex) {
    await knex.schema.table('integrations', table => {
      table.string('session_id').defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('integrations', table => {
      table.dropColumn('session_id')
    })
  }
}
