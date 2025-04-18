module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('ports_audit_log', (table) => {
        table.integer('ports_id', 11)
        table.string('column_name', 25)
        table.string('change_before', 25)
        table.string('change_after', 25)
        table.integer('timestamp', 30)
      }
      )
  },
  async down (knex) {
    return knex.schema.dropTable('ports_audit_log')
  }
}
