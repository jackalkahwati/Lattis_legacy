export interface IFleet {
  fleet_id: number;
  fleet_name: string;
}

export interface CreateFleetDTO {
  fleet_name: string;
}

export interface UpdateFleetDTO {
  fleet_name?: string;
}
