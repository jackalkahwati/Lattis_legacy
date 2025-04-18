import { useQuery } from 'react-query';
import apiClient from '../api/apiClient';

export function useData(endpoint: string) {
  return useQuery(endpoint, () => apiClient.get(endpoint).then(res => res.data));
}