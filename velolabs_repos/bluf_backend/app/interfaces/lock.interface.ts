export interface ILock {
  lock_id: number;
  mac_id: string;
  fleet_id: number;
  name: string;
}

export interface CreateLockDTO {
  mac_id: string;
  fleet_id: number;
  name: string;
}

export interface UpdateLockDTO {
  mac_id?: string;
  fleet_id?: number;
  name?: string;
}
