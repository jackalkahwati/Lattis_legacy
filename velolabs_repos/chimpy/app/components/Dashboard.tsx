import React, { lazy, Suspense } from 'react';

const Chart = lazy(() => import('./Chart'));
const Table = lazy(() => import('./Table'));

function Dashboard() {
  return (
    <div>
      <Suspense fallback={<div>Loading...</div>}>
        <Chart />
        <Table />
      </Suspense>
    </div>
  );
}

export default Dashboard;