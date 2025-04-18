import axios from 'axios';
import { setupCache } from 'axios-cache-adapter';

const cache = setupCache({
  maxAge: 15 * 60 * 1000 // Cache for 15 minutes
});

const apiClient = axios.create({
  adapter: cache.adapter
});

export default apiClient;