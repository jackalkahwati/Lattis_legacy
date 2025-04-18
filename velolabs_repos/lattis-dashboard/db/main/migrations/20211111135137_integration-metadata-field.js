module.exports = {
  async up (knex) {
    await knex.schema.alterTable('integrations', (table) => {
      table.text('metadata').alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('integrations', (table) => {
      table.string('metadata').alter()
    })
  }
}
