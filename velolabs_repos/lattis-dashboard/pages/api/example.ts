import { config } from 'dotenv';

config(); // Load environment variables

export default function handler(req, res) {
  const apiKey = process.env.API_KEY;
  // Use apiKey securely
  // ...
}