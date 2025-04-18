module.exports = {
  async up (knex) {
    await knex.schema.table('fleets', (table) => {
      table.string('contact_web_link').nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('fleets', (table) => {
      table.dropColumn('contact_web_link')
    })
  }
}
