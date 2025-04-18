module.exports = {
  async up (knex) {
    await knex.schema.table('bike_group', table => {
      table.string('iot_module_type', 64).defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('bike_group', table => {
      table.dropColumn('iot_module_type')
    })
  }
}
