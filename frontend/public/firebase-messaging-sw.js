// public/firebase-messaging-sw.js
// Service worker for receiving push notifications when the app is in the background.
//
// IMPORTANT: This file must live in the /public folder so it's served from the root.
// Firebase requires the service worker to be at /firebase-messaging-sw.js
//
// React Native migration note:
//   Service workers are web-only. React Native handles background notifications
//   natively via @react-native-firebase/messaging — this file is not needed.

importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

// These values must match your .env — hardcoded here since service workers
// cannot access process.env. Keep this file out of git if you prefer,
// but these are public-facing Firebase config values (safe to expose).
firebase.initializeApp({
    apiKey:            "AIzaSyAi3CCyEThLCxg6dkAUNyRPBXQVwWEpSeY",
    authDomain:        "evopet-fd663.firebaseapp.com",
    projectId:         "evopet-fd663",
    storageBucket:     "evopet-fd663.firebasestorage.app",
    messagingSenderId: "388303919239",
    appId:             "1:388303919239:web:2d48f894bee452a7848022"
});

const messaging = firebase.messaging();

// Handle background messages
messaging.onBackgroundMessage((payload) => {
    console.log('📬 Background message received:', payload);

    const { title, body } = payload.notification || {};

    if (title) {
        self.registration.showNotification(title, {
            body: body || '',
            icon: '/logo192.png',
            badge: '/logo192.png',
            tag: 'evopet-notification', // Replaces previous notification instead of stacking
        });
    }
});