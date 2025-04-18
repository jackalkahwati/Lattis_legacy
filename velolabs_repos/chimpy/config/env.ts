import dotenv from 'dotenv';

dotenv.config();

export const ENV = {
  DATABASE_URL: process.env.DATABASE_URL,
  API_KEY: process.env.API_KEY,
  // ... other environment variables
};