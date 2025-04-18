module.exports = {
  async up (knex) {
    // Trip end mode either normal or ended by smart docking
    await knex.schema.table('trips', (table) => {
      table.boolean('rating_shown').nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('trips', (table) => {
      table.dropColumn('rating_shown')
    })
  }
}
