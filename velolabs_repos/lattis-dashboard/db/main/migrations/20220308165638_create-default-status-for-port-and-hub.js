module.exports = {
  async up (knex) {
    await knex.schema.alterTable('ports', (table) => {
      table.string('current_status', 25).notNullable().defaultTo('parked').alter()
    })
    await knex.schema.alterTable('hubs', (table) => {
      table.string('current_status', 25).notNullable().defaultTo('parked').alter()
    })
    await knex.schema.alterTable('hubs_and_fleets', (table) => {
      table.string('current_status', 25).notNullable().defaultTo('parked').alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('ports', table => {
      table.string('current_status', 25).nullable().alter()
    })
    await knex.schema.alterTable('hubs', table => {
      table.string('current_status', 25).nullable().alter()
    })
    await knex.schema.alterTable('hubs_and_fleets', table => {
      table.string('current_status', 25).nullable().alter()
    })
  }
}
