module.exports = {
  async up (knex) {
    return knex.schema.table('tickets', function (table) {
      table.enum('priority', ['low', 'medium', 'high']).defaultTo('medium')
    })
  },
  async down (knex) {
    return knex.schema.table('tickets', (table) => {
      table.dropColumn('priority')
    })
  }
}
