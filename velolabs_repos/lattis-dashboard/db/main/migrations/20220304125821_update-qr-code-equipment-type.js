module.exports = {
  async up (knex) {
    await knex.schema.alterTable('qr_codes', (table) => {
      table.bigInteger('equipment_id', 20).alter()
      // .unsigned().references('controllers.controller_id').alter()
      table.string('code', 20).alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('qr_codes', table => {
      table.integer('equipment_id', 10).notNullable().alter()
      table.string('code').notNullable().alter()
    })
  }
}
