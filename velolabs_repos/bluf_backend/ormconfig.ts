import { DataSourceOptions } from 'typeorm';
import dotenv from 'dotenv';

dotenv.config();

const config: DataSourceOptions = {
  type: 'mysql',
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  username: process.env.DB_USERNAME,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_DATABASE,
  synchronize: process.env.NODE_ENV === 'development',
  logging: process.env.NODE_ENV === 'development',
  entities: ['app/entities/**/*.ts'],
  migrations: ['app/migrations/**/*.ts'],
  subscribers: ['app/subscribers/**/*.ts'],
  connectTimeout: 20000,
  extra: {
    connectionLimit: 10,
    maxPoolConnections: 10,
    minPoolConnections: 5
  }
};

export default config;
