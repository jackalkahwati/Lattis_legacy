
import { BaseEntity, Entity, Column, PrimaryColumn } from 'typeorm';

@Entity({ name: "locks" })
export class Lock extends BaseEntity {    

    @PrimaryColumn()
    lock_id!: number;
    
    @Column()
    mac_id!: string;

    @Column()
    fleet_id!: number;

    @Column()
    name!: string;
}