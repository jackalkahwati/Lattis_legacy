module.exports = {
  async up (knex) {
    return knex.schema.createTable('qr_codes', (table) => {
      table.increments('id').primary()
      table.string('code').unique().notNullable()
      table.enum('type', ['port', 'hub', 'bike']).notNullable()
      table.string('equipment_id').notNullable() // This can be either vehicle, port or hub_id. Use in combination with type to get correct table
    })
  },
  async down (knex) {
    return knex.schema.dropTable('qr_codes')
  }
}
