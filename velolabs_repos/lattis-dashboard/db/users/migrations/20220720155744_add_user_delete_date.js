module.exports = {
  async up (knex) {
    await knex.schema.table('users', (table) => {
      table.integer('delete_on', 15).nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('users', (table) => {
      table.dropColumn('delete_on')
    })
  }
}
