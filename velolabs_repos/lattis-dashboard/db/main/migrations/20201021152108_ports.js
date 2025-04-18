module.exports = {
  async up (knex) {
    return knex.schema.createTable('ports', (table) => {
      table.increments('port_id').primary()
      table.string('uuid').unique().notNullable()
      table.string('hub_uuid').notNullable()
      table.string('status').notNullable()
      table.string('vehicle_uuid').nullable()
      table.integer('number').notNullable()
      table.boolean('charging').defaultTo(false)
      table.foreign('hub_uuid').references('hubs.uuid').onDelete('CASCADE')
    })
  },
  async down (knex) {
    return knex.schema.dropTable('ports')
  }
}
