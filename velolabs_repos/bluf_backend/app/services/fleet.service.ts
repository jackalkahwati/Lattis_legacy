import { Fleet } from '../entities/Fleet';
import { CreateFleetDTO, UpdateFleetDTO } from '../interfaces/fleet.interface';
import { HttpException } from '../middleware/error.middleware';
import logger from '../utils/logger';
import { DeepPartial } from 'typeorm';

export class FleetService {
  public async findAll(): Promise<Fleet[]> {
    try {
      return await Fleet.find();
    } catch (error) {
      logger.error('Error in findAll fleets:', error);
      throw new HttpException(500, 'Error retrieving fleets');
    }
  }

  public async findOne(fleetId: number): Promise<Fleet> {
    try {
      const fleet = await Fleet.findOne({ where: { fleet_id: fleetId } });
      if (!fleet) {
        throw new HttpException(404, `Fleet with id ${fleetId} not found`);
      }
      return fleet;
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error finding fleet ${fleetId}:`, error);
      throw new HttpException(500, 'Error retrieving fleet');
    }
  }

  public async create(fleetData: CreateFleetDTO): Promise<Fleet> {
    try {
      const newFleet = new Fleet();
      Object.assign(newFleet, fleetData as DeepPartial<Fleet>);
      return await newFleet.save();
    } catch (error) {
      logger.error('Error creating fleet:', error);
      throw new HttpException(500, 'Error creating fleet');
    }
  }

  public async update(fleetId: number, fleetData: UpdateFleetDTO): Promise<Fleet> {
    try {
      const fleet = await this.findOne(fleetId);
      Object.assign(fleet, fleetData as DeepPartial<Fleet>);
      return await fleet.save();
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error updating fleet ${fleetId}:`, error);
      throw new HttpException(500, 'Error updating fleet');
    }
  }

  public async delete(fleetId: number): Promise<void> {
    try {
      const result = await Fleet.delete({ fleet_id: fleetId });
      if (result.affected === 0) {
        throw new HttpException(404, `Fleet with id ${fleetId} not found`);
      }
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error deleting fleet ${fleetId}:`, error);
      throw new HttpException(500, 'Error deleting fleet');
    }
  }
}
