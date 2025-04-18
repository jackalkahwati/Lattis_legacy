module.exports = {
  async up (knex) {
    await knex.schema.table('booking', (table) => {
      table.dropForeign('lock_id', 'FK_booking_locks')
    })
    await knex.schema.table('booking', (table) => {
      table.integer('lock_id').unsigned().alter().nullable()
      table.foreign('lock_id', 'FK_booking_locks')
        .references('lock_id')
        .inTable('locks')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')
    })
  },
  async down (knex) {
    await knex.schema.table('booking', (table) => {
      table.dropForeign('lock_id', 'FK_booking_locks')
    })
    await knex.schema.table('booking', (table) => {
      table.integer('lock_id').unsigned().alter().notNullable()
      table.foreign('lock_id', 'FK_booking_locks')
        .references('lock_id')
        .inTable('locks')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')
    })
  }
}
