/** @format */

const MAX_V = 24.3;
const MIN_15_V = 21;
const MIN_V = 17;
// const DELTA_FILTER = 1.0; // # disregard values more or less than 1V around the estimate

function percentage (val) {
  let res;
  if (val > MIN_15_V) {
    const dv = MAX_V - MIN_15_V;
    const cleanValue = Math.max(Math.min(val, MAX_V), MIN_15_V);
    res = 0.15 + 0.85 * ((cleanValue - MIN_15_V) / dv);
  } else {
    const dv = MIN_15_V - MIN_V;
    const cleanValue = Math.max(Math.min(val, MIN_15_V), MIN_V);
    res = 0.15 * ((cleanValue - MIN_V) / dv);
  }
  return res;
}

function voltWeightedMean (values) {
  const arrayLength = values.length;
  let n = arrayLength;
  let fact = 0;
  let filtered = 0;

  for (let i = 0; i < arrayLength; i++) {
    //Do something
    filtered = filtered + (values[i] * 1.0) / n;
    fact = fact + 1.0 / n;
    n = n - 1;
  }
  return filtered / fact;
}

function cleanVoltFilter (values) {
  const wm = voltWeightedMean(values);
  const filtered = values.filter((v) => Math.abs(v - wm) < 1.0);
  if (filtered.length == 0) {
    return wm;
  }
  return voltWeightedMean(values);
}

function computePercentage (input) {
  const cleanVoltage = cleanVoltFilter(input.map((o) => o.data));
  return parseFloat(percentage(cleanVoltage) * 100.0).toFixed(2);
}

module.exports = { computePercentage };
