import { Lock } from '../entities/Lock';
import { CreateLockDTO, UpdateLockDTO } from '../interfaces/lock.interface';
import { HttpException } from '../middleware/error.middleware';
import logger from '../utils/logger';
import { DeepPartial } from 'typeorm';

export class LockService {
  public async findAll(): Promise<Lock[]> {
    try {
      return await Lock.find();
    } catch (error) {
      logger.error('Error in findAll locks:', error);
      throw new HttpException(500, 'Error retrieving locks');
    }
  }

  public async findByFleet(fleetId: number): Promise<Lock[]> {
    try {
      return await Lock.find({ where: { fleet_id: fleetId } });
    } catch (error) {
      logger.error(`Error finding locks for fleet ${fleetId}:`, error);
      throw new HttpException(500, 'Error retrieving locks for fleet');
    }
  }

  public async findOne(lockId: number): Promise<Lock> {
    try {
      const lock = await Lock.findOne({ where: { lock_id: lockId } });
      if (!lock) {
        throw new HttpException(404, `Lock with id ${lockId} not found`);
      }
      return lock;
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error finding lock ${lockId}:`, error);
      throw new HttpException(500, 'Error retrieving lock');
    }
  }

  public async findByMacId(macId: string): Promise<Lock> {
    try {
      const lock = await Lock.findOne({ where: { mac_id: macId } });
      if (!lock) {
        throw new HttpException(404, `Lock with MAC ID ${macId} not found`);
      }
      return lock;
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error finding lock with MAC ID ${macId}:`, error);
      throw new HttpException(500, 'Error retrieving lock');
    }
  }

  public async create(lockData: CreateLockDTO): Promise<Lock> {
    try {
      const newLock = new Lock();
      Object.assign(newLock, lockData as DeepPartial<Lock>);
      return await newLock.save();
    } catch (error) {
      logger.error('Error creating lock:', error);
      throw new HttpException(500, 'Error creating lock');
    }
  }

  public async update(lockId: number, lockData: UpdateLockDTO): Promise<Lock> {
    try {
      const lock = await this.findOne(lockId);
      Object.assign(lock, lockData as DeepPartial<Lock>);
      return await lock.save();
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error updating lock ${lockId}:`, error);
      throw new HttpException(500, 'Error updating lock');
    }
  }

  public async delete(lockId: number): Promise<void> {
    try {
      const result = await Lock.delete({ lock_id: lockId });
      if (result.affected === 0) {
        throw new HttpException(404, `Lock with id ${lockId} not found`);
      }
    } catch (error) {
      if (error instanceof HttpException) throw error;
      logger.error(`Error deleting lock ${lockId}:`, error);
      throw new HttpException(500, 'Error deleting lock');
    }
  }
}
