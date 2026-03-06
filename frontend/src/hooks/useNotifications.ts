// src/hooks/useNotifications.ts
// Handles notification permission request and FCM token registration.
//
// Usage:
//   Call requestPermission() when the user first logs in or from a settings toggle.
//   Call revokePermission() on logout.
//
// React Native migration note:
//   Replace getToken/deleteToken with @react-native-firebase/messaging equivalents.
//   The API calls to /api/notifications/token stay unchanged.

import { useState, useCallback } from 'react';
import { getToken, deleteToken } from 'firebase/messaging';
import { messaging } from '../services/firebase';
import apiClient from '../services/apiClient';

// Your VAPID key from Firebase Console:
// Project Settings → Cloud Messaging → Web Push certificates → Key pair
const VAPID_KEY = process.env.REACT_APP_FIREBASE_VAPID_KEY;

type PermissionStatus = 'idle' | 'granted' | 'denied' | 'unsupported' | 'error';

interface UseNotificationsReturn {
    permissionStatus: PermissionStatus;
    isRegistering: boolean;
    requestPermission: () => Promise<void>;
    revokePermission: () => Promise<void>;
}

export const useNotifications = (): UseNotificationsReturn => {
    const [permissionStatus, setPermissionStatus] = useState<PermissionStatus>('idle');
    const [isRegistering, setIsRegistering] = useState(false);

    /**
     * Request notification permission from the browser.
     * If granted, gets the FCM token and registers it with the backend.
     */
    const requestPermission = useCallback(async () => {
        // Check browser support
        if (!('Notification' in window) || !messaging) {
            console.warn('⚠️ Push notifications are not supported in this browser');
            setPermissionStatus('unsupported');
            return;
        }

        setIsRegistering(true);

        try {
            // Request browser permission
            const permission = await Notification.requestPermission();

            if (permission !== 'granted') {
                console.log('🔕 Notification permission denied by user');
                setPermissionStatus('denied');
                return;
            }

            // Get FCM token
            const token = await getToken(messaging, { vapidKey: VAPID_KEY });

            if (!token) {
                console.error('❌ Failed to get FCM token — no token returned');
                setPermissionStatus('error');
                return;
            }

            // Register token with backend
            await apiClient.post('/api/notifications/token', {
                token,
                platform: 'WEB',
            });

            setPermissionStatus('granted');
            console.log('🔔 Push notifications registered successfully');

        } catch (err) {
            console.error('❌ Failed to register push notifications:', err);
            setPermissionStatus('error');
        } finally {
            setIsRegistering(false);
        }
    }, []);

    /**
     * Revoke push notifications for this device.
     * Called on logout — deactivates the token on the backend.
     */
    const revokePermission = useCallback(async () => {
        try {
            // Deactivate on backend first
            await apiClient.delete('/api/notifications/token');

            // Then delete the token from FCM
            if (messaging) {
                await deleteToken(messaging);
            }

            setPermissionStatus('idle');
            console.log('🔕 Push notifications revoked');
        } catch (err) {
            console.error('❌ Failed to revoke push notifications:', err);
        }
    }, []);

    return {
        permissionStatus,
        isRegistering,
        requestPermission,
        revokePermission,
    };
};