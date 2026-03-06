// src/services/firebase.ts
// Initializes the Firebase app and exports the messaging instance.
//
// React Native migration note:
//   Replace this file with @react-native-firebase/messaging.
//   The useNotifications hook and backend endpoints stay unchanged.

import { initializeApp, getApps, getApp } from 'firebase/app';
import { getMessaging, Messaging } from 'firebase/messaging';

const firebaseConfig = {
    apiKey:            process.env.REACT_APP_FIREBASE_API_KEY,
    authDomain:        process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
    projectId:         process.env.REACT_APP_FIREBASE_PROJECT_ID,
    storageBucket:     process.env.REACT_APP_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID,
    appId:             process.env.REACT_APP_FIREBASE_APP_ID,
};

// Avoid re-initializing on hot reload
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();

// Messaging is only supported in browsers that support the Push API.
// This guard prevents crashes in Safari or environments without service worker support.
let messaging: Messaging | null = null;
try {
    messaging = getMessaging(app);
} catch (err) {
    console.warn('⚠️ Firebase Messaging is not supported in this browser:', err);
}

export { messaging };
export default app;