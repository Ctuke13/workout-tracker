// src/utils/dateUtils.ts - Fix for date shifting bug
export class DateUtils {
    /**
     * Get today's date as YYYY-MM-DD string in local timezone
     * This prevents the date shifting bug by using local timezone consistently
     */
    static getTodayString(): string {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    /**
     * Get date string for any date in local timezone
     */
    static getDateString(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    /**
     * Parse date string to local Date object
     */
    static parseLocalDate(dateString: string): Date {
        const [year, month, day] = dateString.split('-').map(Number);
        return new Date(year, month - 1, day);
    }

    /**
     * Check if a date string is today
     */
    static isToday(dateString: string): boolean {
        return dateString === DateUtils.getTodayString();
    }

    /**
     * Check if a date string is in the past
     */
    static isPast(dateString: string): boolean {
        return dateString < DateUtils.getTodayString();
    }

    /**
     * Check if a date string is in the future
     */
    static isFuture(dateString: string): boolean {
        return dateString > DateUtils.getTodayString();
    }

    /**
     * Get start of week for a given date (Sunday)
     */
    static getStartOfWeek(date: Date): Date {
        const startOfWeek = new Date(date);
        const dayOfWeek = startOfWeek.getDay();
        startOfWeek.setDate(startOfWeek.getDate() - dayOfWeek);
        return startOfWeek;
    }

    /**
     * Format date for display
     */
    static formatDisplayDate(dateString: string): string {
        const date = DateUtils.parseLocalDate(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    }
}