module.exports = {
  async up (knex) {
    return knex.schema.createTable('tax', (table) => {
      table.increments('tax_id').primary()
      table.string('tax_name', 50)
      table.integer('tax_percent', 3)
      table.integer('fleet_id', 5)
      table.enum('status', ['active', 'inactive']).notNullable().defaultTo('active')
    })
  },
  async down (knex) {
    return knex.schema.dropTable('tax')
  }
}
