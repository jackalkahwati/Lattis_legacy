import { Feature, Point } from 'geojson'

interface DataPoint {
  value: number
  timestamp?: Date
  [key: string]: any
}

export const normalizeData = (data: DataPoint[]): DataPoint[] => {
  if (data.length === 0) return []

  const values = data.map(point => point.value)
  const min = Math.min(...values)
  const max = Math.max(...values)
  const range = max - min

  return data.map(point => ({
    ...point,
    value: range === 0 ? 0 : (point.value - min) / range,
  }))
}

export const calculateStatistics = (data: DataPoint[]) => {
  if (data.length === 0) {
    return {
      mean: 0,
      median: 0,
      standardDeviation: 0,
    }
  }

  const values = data.map(point => point.value)
  const sum = values.reduce((acc, val) => acc + val, 0)
  const mean = sum / values.length

  const sortedValues = [...values].sort((a, b) => a - b)
  const median = sortedValues[Math.floor(values.length / 2)]

  const squaredDiffs = values.map(value => Math.pow(value - mean, 2))
  const variance = squaredDiffs.reduce((acc, val) => acc + val, 0) / values.length
  const standardDeviation = Math.sqrt(variance)

  return {
    mean,
    median,
    standardDeviation,
  }
}

export const processTimeSeries = (data: DataPoint[]): DataPoint[] => {
  if (data.length === 0) return []

  return data
    .filter(point => point.timestamp instanceof Date)
    .sort((a, b) => a.timestamp!.getTime() - b.timestamp!.getTime())
    .map((point, index, array) => {
      if (index === 0) return point

      const previousPoint = array[index - 1]
      const timeDiff = point.timestamp!.getTime() - previousPoint.timestamp!.getTime()
      const valueDiff = point.value - previousPoint.value
      const rate = valueDiff / (timeDiff / 1000) // rate per second

      return {
        ...point,
        rate,
      }
    })
}

export const formatGeospatialData = (data: Feature<Point>[]): Feature<Point>[] => {
  return data.map(feature => ({
    ...feature,
    properties: {
      ...feature.properties,
      formattedCoordinates: {
        lat: feature.geometry.coordinates[1].toFixed(6),
        lng: feature.geometry.coordinates[0].toFixed(6),
      },
    },
  }))
}
