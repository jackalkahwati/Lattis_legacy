module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('integrations', (table) => {
        table.bigIncrements('integrations_id').primary()
        table
          .integer('fleet_id')
          .unsigned()
          .unique()
          .notNullable()
          .references('fleet_id')
          .inTable('fleets')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')

        table.string('email').nullable()

        table.string('integration_type').nullable()

        table.string('api_key').nullable()

        table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
      })
  },
  async down (knex) {
    return knex.schema.dropTable('integrations')
  }
}
