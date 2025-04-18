module.exports = {
  async up (knex) {
    await knex.schema.table('controllers', table => {
      table.string('metadata').defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('controllers', table => {
      table.dropColumn('metadata')
    })
  }
}
