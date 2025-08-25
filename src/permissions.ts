import { PermissionsAndroid, Permission, Platform } from 'react-native';

export const REQUIRED_PERMISSIONS: Permission[] = [
    PermissionsAndroid.PERMISSIONS.SEND_SMS,
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION
];

// Add notification permission for Android 13+
if (Platform.OS === 'android' && Platform.Version >= 33) {
    REQUIRED_PERMISSIONS.push(
        (PermissionsAndroid as any).PERMISSIONS.POST_NOTIFICATIONS,
    );
}

/** true ⇢ every required permission is already granted */
export async function allGranted(): Promise<boolean> {
    const checks = await Promise.all(
        REQUIRED_PERMISSIONS.map(p => PermissionsAndroid.check(p)),
    );
    return checks.every(Boolean);
}

/** prompts the user for any missing permissions */
export async function requestAll(): Promise<boolean> {
    const res = await PermissionsAndroid.requestMultiple(REQUIRED_PERMISSIONS);
    return REQUIRED_PERMISSIONS.every(
        p => res[p] === PermissionsAndroid.RESULTS.GRANTED,
    );
}
