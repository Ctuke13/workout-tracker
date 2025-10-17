/**
 * Unit conversion utilities for distance, weight, pace, and speed
 */

// ==================== DISTANCE CONVERSIONS ====================

/**
 * Convert kilometers to miles
 */
export const kmToMiles = (km: number): number => {
    return km * 0.621371;
};

/**
 * Convert miles to kilometers
 */
export const milesToKm = (miles: number): number => {
    return miles * 1.60934;
};

// ==================== WEIGHT CONVERSIONS ====================

/**
 * Convert kilograms to pounds
 */
export const kgToLbs = (kg: number): number => {
    return kg * 2.20462;
};

/**
 * Convert pounds to kilograms
 */
export const lbsToKg = (lbs: number): number => {
    return lbs * 0.453592;
};

// ==================== PACE CONVERSIONS ====================

/**
 * Convert pace from min/km to min/mile
 */
export const paceKmToMile = (minPerKm: number): number => {
    return minPerKm * 1.60934;
};

/**
 * Convert pace from min/mile to min/km
 */
export const paceMileToKm = (minPerMile: number): number => {
    return minPerMile / 1.60934;
};

// ==================== SPEED CONVERSIONS ====================

/**
 * Convert km/h to mph
 */
export const kmhToMph = (kmh: number): number => {
    return kmh * 0.621371;
};

/**
 * Convert mph to km/h
 */
export const mphToKmh = (mph: number): number => {
    return mph * 1.60934;
};

// ==================== SMART FORMATTING WITH CONVERSION ====================

/**
 * Format distance with automatic unit conversion
 */
export const formatDistanceWithUnit = (
    km: number,
    preferredUnit: 'km' | 'miles',
    decimals: number = 1
): string => {
    const value = preferredUnit === 'miles' ? kmToMiles(km) : km;
    return `${value.toFixed(decimals)} ${preferredUnit}`;
};

/**
 * Format weight with automatic unit conversion
 */
export const formatWeightWithUnit = (
    kg: number,
    preferredUnit: 'kg' | 'lbs',
    decimals: number = 1
): string => {
    const value = preferredUnit === 'lbs' ? kgToLbs(kg) : kg;
    return `${value.toFixed(decimals)} ${preferredUnit}`;
};

/**
 * Format pace with automatic unit conversion
 * Returns format like "5:30/km" or "8:51/mi"
 */
export const formatPaceWithUnit = (
    minPerKm: number,
    preferredUnit: 'km' | 'miles'
): string => {
    const pace = preferredUnit === 'miles' ? paceKmToMile(minPerKm) : minPerKm;
    const minutes = Math.floor(pace);
    const seconds = Math.round((pace - minutes) * 60);
    const unitLabel = preferredUnit === 'miles' ? 'mi' : 'km';
    return `${minutes}:${seconds.toString().padStart(2, '0')}/${unitLabel}`;
};

/**
 * Format speed with automatic unit conversion
 */
export const formatSpeedWithUnit = (
    kmh: number,
    preferredUnit: 'km' | 'miles',
    decimals: number = 1
): string => {
    const value = preferredUnit === 'miles' ? kmhToMph(kmh) : kmh;
    const unitLabel = preferredUnit === 'miles' ? 'mph' : 'km/h';
    return `${value.toFixed(decimals)} ${unitLabel}`;
};

// ==================== CONVERSION HELPERS ====================

/**
 * Convert distance based on preferred unit
 */
export const convertDistance = (km: number, toUnit: 'km' | 'miles'): number => {
    return toUnit === 'miles' ? kmToMiles(km) : km;
};

/**
 * Convert weight based on preferred unit
 */
export const convertWeight = (kg: number, toUnit: 'kg' | 'lbs'): number => {
    return toUnit === 'lbs' ? kgToLbs(kg) : kg;
};

/**
 * Convert pace based on preferred unit
 */
export const convertPace = (minPerKm: number, toUnit: 'km' | 'miles'): number => {
    return toUnit === 'miles' ? paceKmToMile(minPerKm) : minPerKm;
};

/**
 * Convert speed based on preferred unit
 */
export const convertSpeed = (kmh: number, toUnit: 'km' | 'miles'): number => {
    return toUnit === 'miles' ? kmhToMph(kmh) : kmh;
};

// ==================== TYPE DEFINITIONS ====================

export type DistanceUnit = 'km' | 'miles';
export type WeightUnit = 'kg' | 'lbs';

export interface UnitPreferences {
    distanceUnit: DistanceUnit;
    weightUnit: WeightUnit;
}