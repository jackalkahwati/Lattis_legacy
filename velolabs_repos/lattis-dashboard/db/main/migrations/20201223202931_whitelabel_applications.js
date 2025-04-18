module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('whitelabel_applications', (table) => {
        table.bigIncrements('app_id').primary()
        table.string('app_type').nullable()
        table.string('email').nullable()
        table.string('phone_number').nullable()
        table.text('app_logo', 'longtext').nullable()
        table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
      })
  },
  async down (knex) {
    return knex.schema.dropTable('whitelabel_applications')
  }
}
