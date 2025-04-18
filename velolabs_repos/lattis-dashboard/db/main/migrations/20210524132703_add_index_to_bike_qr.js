module.exports = {
  async up (knex) {
    return knex.schema.table('bikes', table => {
      table.index('qr_code_id')
      table.timestamps(true, true)
    })
  },
  async down (knex) {
    return knex.schema.table('bikes', table => {
      table.dropIndex('qr_code_id')
      table.dropColumn('created_at')
      table.dropColumn('updated_at')
    })
  }
}
