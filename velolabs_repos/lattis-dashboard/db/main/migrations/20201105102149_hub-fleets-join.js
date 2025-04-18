module.exports = {
  async up (knex) {
    return knex.schema.createTable('hubs_and_fleets', (table) => {
      table.increments('id').primary()
      table.integer('fleet_id', 10).unsigned().notNullable().references('fleets.fleet_id')
      table.bigInteger('hub_id').unsigned().references('hubs.hub_id')
      table.string('hub_uuid').notNullable().references('hubs.uuid')
      table.string('status').notNullable()
    })
  },
  async down (knex) {
    return knex.schema.dropTable('hubs_and_fleets')
  }
}
