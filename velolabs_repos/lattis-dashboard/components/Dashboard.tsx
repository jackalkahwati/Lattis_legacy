import { useApi } from '../hooks/useApi';

export function Dashboard() {
  const { data, loading, error } = useApi<DashboardData>('/api/dashboard');

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      {/* Render your dashboard using the data */}
    </div>
  );
}