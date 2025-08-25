import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  View,
  Text,
  Button,
  AppState,
  AppStateStatus,
  NativeModules,
  StyleSheet,
} from 'react-native';
import { allGranted, requestAll } from './permissions';

const { SosModule } = NativeModules;

export default function App() {
  const [granted, setGranted] = useState(false);

  console.log(NativeModules.SosModule);

  const ensurePerms = async () => {
    const ok = (await allGranted()) || (await requestAll());
    setGranted(ok);
    if (ok) SosModule.promptIfGpsOff();
  };

  const handleAppState = (state: AppStateStatus) => {
    console.log('App state changed:', state);
    if (state !== 'active' && granted) {
      console.log('Starting SOS Foreground Service');
      SosModule.startSOSService();
    } else if (state === 'active') {
      console.log('Stopping SOS service');
      SosModule.stopSOSService();
    }
  };

  useEffect(() => {
    ensurePerms();
    const sub = AppState.addEventListener('change', handleAppState);
    return () => sub.remove();
  }, [granted]);

  return (
    <SafeAreaView style={styles.container}>
      {granted ? (
        <View>
          <Text style={styles.big}>Sahayak is armed.</Text>
          <Text>
            Minimize the app to send SOS SMS every 5 seconds.{'\n'}
            Open the app to stop SOS.
          </Text>
        </View>
      ) : (
        <View>
          <Text style={styles.big}>Permissions required</Text>
          <Button title="Grant now" onPress={ensurePerms} />
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  big: { fontSize: 20, fontWeight: '600', marginBottom: 10 },
});
