import { BaseEntity, Entity, Column, PrimaryColumn } from 'typeorm';

@Entity({ name: "fleets" })
export class Fleet extends BaseEntity {    

    @PrimaryColumn()
    fleet_id!: number;
    
    @Column()
    fleet_name!: string;
}
