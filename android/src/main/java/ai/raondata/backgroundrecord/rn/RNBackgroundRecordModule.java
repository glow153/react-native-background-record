package ai.raondata.backgroundrecord.rn;

import static ai.raondata.backgroundrecord.common.Actions.ACTION_START_RECORD;
import static ai.raondata.backgroundrecord.common.Actions.ACTION_STOP_RECORD;
import static ai.raondata.backgroundrecord.common.Constants.ERROR_INVALID_CONFIG;
import static ai.raondata.backgroundrecord.common.Constants.ERROR_SERVICE_ERROR;
import static ai.raondata.backgroundrecord.common.Constants.NOTIFICATION_CONFIG;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ai.raondata.backgroundrecord.common.NotificationHelper;
import ai.raondata.backgroundrecord.service.BackgroundRecordService;


@SuppressLint("MissingPermission")
public class RNBackgroundRecordModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "RNBackgroundRecord";
    public static final int SAMPLE_RATE = 16000;

    public static String RECORD_FILENAME = "record.pcm";
    public static String RECORD_FILEPATH = null;
    public static List<String> dataBuffer = new ArrayList<>();

    @Override
    public String getName() {
        return TAG;
    }

    public RNBackgroundRecordModule(ReactApplicationContext context) {
        super(context);
        RECORD_FILEPATH = getReactApplicationContext().getDataDir() + "/" + RECORD_FILENAME;
    }

    private void sendRNEvent(String event, @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }

    // ======================== ReactMethod ======================== //
    @ReactMethod
    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        Log.d(TAG, "createNotificationChannel() start");
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: Channel config is invalid");
            return;
        }
        NotificationHelper.getInstance(getReactApplicationContext()).createNotificationChannel(channelConfig, promise);
    }

    @ReactMethod
    public void startRecording(ReadableMap notificationConfig, Promise promise) {
        Log.d(TAG, "startRecording() start");
        try {
            if (notificationConfig == null) {
                promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: Notification config is invalid");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!notificationConfig.hasKey("channelId")) {
                    promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: channelId is required");
                    return;
                }
            }

            if (!notificationConfig.hasKey("id")) {
                promise.reject(ERROR_INVALID_CONFIG , "BackgroundRecord: id is required");
                return;
            }

            if (!notificationConfig.hasKey("icon")) {
                promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: icon is required");
                return;
            }

            if (!notificationConfig.hasKey("title")) {
                promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: title is reqired");
                return;
            }

            if (!notificationConfig.hasKey("text")) {
                promise.reject(ERROR_INVALID_CONFIG, "BackgroundRecord: text is required");
                return;
            }

            Log.d(TAG, "startRecording(): notificationConfig is valid");
            Intent intent = new Intent(getReactApplicationContext(), BackgroundRecordService.class);
            intent.setAction(ACTION_START_RECORD);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));


            ComponentName componentName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                componentName = getReactApplicationContext().startForegroundService(intent);
            } else {
                componentName = getReactApplicationContext().startService(intent);
            }

            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "BackgroundRecord: Foreground service is not started");
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void stopRecording(Promise promise) {
        Log.d(TAG, "stopRecording() start");
        Intent intent = new Intent(getReactApplicationContext(), BackgroundRecordService.class);
        intent.setAction(ACTION_STOP_RECORD);
        boolean stopped = getReactApplicationContext().stopService(intent);
        if (stopped) {
            resolveFromFile(promise);
//            resolveFromBuffer(promise);
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "BackgroundRecord: Foreground service failed to stop");
        }
    }
    // ======================== ReactMethod ======================== //

    private void resolveFromFile(Promise promise) {
        try {
            byte[] recordedFile = Files.readAllBytes(Paths.get(RECORD_FILEPATH));
            String encoded = Base64.encodeToString(recordedFile, Base64.DEFAULT);
            promise.resolve(encoded);
        } catch (IOException e) {
            e.printStackTrace();
            promise.reject(ERROR_SERVICE_ERROR, "BackgroundRecord: failed to convert record file to bytes");
        }
    }

    private void resolveFromBuffer(Promise promise) {
        byte[] binaryData = dataBuffer.stream()
                .map(s -> Base64.decode(s, Base64.NO_WRAP))
                .collect(
                        ByteArrayOutputStream::new,
                        (b, e) -> b.write(e, 0, e.length),
                        (a, b) -> {}
                ).toByteArray();

        promise.resolve(Base64.encodeToString(binaryData, Base64.NO_WRAP));
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostResume()");
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostPause()");
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostDestroy()");
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        Log.d(TAG, "************LifecycleEventListener************ : onCatalystInstanceDestroy()");
    }
}
