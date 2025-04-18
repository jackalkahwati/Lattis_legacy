module.exports = {
  async up (knex) {
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.string('make').nullable()
      table.string('model').nullable()
      table.text('image', 'longtext').nullable()
      table.string('type').nullable()
      table.string('description').nullable()
      table.string('integration').nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.dropColumn('make')
      table.dropColumn('model')
      table.dropColumn('image')
      table.dropColumn('type')
      table.dropColumn('description')
      table.dropColumn('integration')
    })
  }
}
