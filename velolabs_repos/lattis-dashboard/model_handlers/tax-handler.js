'use strict'

const platform = require('@velo-labs/platform')
const errors = platform.errors
const db = require('../db')
/** * This function is to add the Tax details by default tax is active
 *
 * @param {Object} TaxDetails - Tax details to be added
 * @param {Function} callback
 */

const createTax = async (taxDetails) => {
  try {
    const [id] = await db.main('tax').insert({...taxDetails.data, status: 'active'})
    const added = await db.main('tax').where({ tax_id: id }).first()
    return added
  } catch (error) {
    throw errors.customError(
      `An error occurred while add tax details`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

const getTaxWithFleetId = async (fleetId) => {
  try {
    const result = await db.main('tax').where({
      fleet_id: fleetId,
      status: 'active'
    })
    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while get tax details with fleetId`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

/**
 * This methods deletes tax  that are associated with a request identifier.
 *
 * @param {Object} TaxDetails - Request parameter for identifier that are associated with deleting
 * a tax
 * @param {Function} done
 */
const deactivateTax = async (taxId) => {
  try {
    await db.main('tax').where({tax_id: taxId}).update({status: 'inactive'})
    const updated = await db.main('tax').where({ tax_id: taxId }).first()
    return updated
  } catch (error) {
    throw errors.customError(
      `An error occurred while deactivating tax`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

/**
 * This methods update tax  that are associated with a request identifier.
 *
 * @param {Object} TaxDetails - Request parameter for identifier that are associated with deleting
 * a tax
 * @param {Function} done
 */
const updateTax = async (data) => {
  try {
    await db.main('tax').where({tax_id: data.tax_id}).update({tax_name: data.tax_name, tax_percent: data.tax_percent})
    const updated = await db.main('tax').where({ tax_id: data.tax_id }).first()
    return updated
  } catch (error) {
    throw errors.customError(
      `An error occurred while update tax details`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

const getTaxWithTaxId = async (taxId) => {
  try {
    const result = await db.main('tax').whereIn('tax_id', taxId)

    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while get tax details`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

const updateTaxSubTotalAndTax = async (tax) => {
  try {
    const result = await db.main('fleet_memberships').where({fleet_membership_id: tax.fleet_membership_id}).update({taxes: JSON.stringify(tax.taxes), tax_sub_total: tax.tax_sub_total})

    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while get tax details`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      true
    )
  }
}

module.exports = {
  createTax,
  getTaxWithFleetId,
  deactivateTax,
  updateTax,
  getTaxWithTaxId,
  updateTaxSubTotalAndTax
}
