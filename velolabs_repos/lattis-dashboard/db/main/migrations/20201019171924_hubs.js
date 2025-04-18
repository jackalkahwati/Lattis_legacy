module.exports = {
  async up (knex) {
    return knex.schema.createTable('hubs', (table) => {
      table.bigIncrements('hub_id').primary()
      table.string('uuid').unique().notNullable()
      table.string('name').notNullable()
      table.string('status').notNullable()
      table.decimal('latitude', 10, 8).notNullable()
      table.decimal('longitude', 11, 8).notNullable()
      table.decimal('longitude', 11, 8).notNullable()
      table.integer('fleet_id')
      table.bigInteger('operator_id')
    })
  },
  async down (knex) {
    return knex.schema.dropTable('hubs')
  }
}
