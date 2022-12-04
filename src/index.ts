import { EmitterSubscription, NativeEventEmitter, NativeModules } from 'react-native';
const { RNBackgroundRecord } = NativeModules;
const eventEmitter = new NativeEventEmitter(RNBackgroundRecord);

export const createNotificationChannel = async (channelConfig) => {
  return await RNBackgroundRecord.createNotificationChannel(channelConfig);
}

export const startRecording = async (notificationConfig): Promise<boolean> => {
  return await RNBackgroundRecord.startRecording(notificationConfig);
}

export const stopRecording = async (): Promise<string> => {
  return await RNBackgroundRecord.stopRecording();
}

export const on = (event, callback): EmitterSubscription => {
  return eventEmitter.addListener(event, callback);
}