import { useMemo } from 'react';

export function useExpensiveCalculation(data: any[]) {
  return useMemo(() => {
    // Perform expensive calculation here
    return result;
  }, [data]);
}