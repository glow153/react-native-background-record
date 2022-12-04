package ai.raondata.backgroundrecord.service;

import static ai.raondata.backgroundrecord.common.Constants.NOTIFICATION_CONFIG;
import static ai.raondata.backgroundrecord.rn.RNBackgroundRecordModule.RECORD_FILEPATH;
import static ai.raondata.backgroundrecord.rn.RNBackgroundRecordModule.SAMPLE_RATE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.raondata.backgroundrecord.common.Actions;
import ai.raondata.backgroundrecord.common.NotificationHelper;
import ai.raondata.backgroundrecord.rn.RNBackgroundRecordModule;

@SuppressLint("MissingPermission")
public class BackgroundRecordService extends Service {
    private static final String TAG = "BackgroundRecordService";

    private MediaRecorder mediaRecorder;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread worker;
    private AudioRecord audioRecord;
    private int bufferSize;
    private byte[] buffer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand() start, action: " + action);
        if (action != null) {
            if (action.equals(Actions.ACTION_START_RECORD)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                    if (notificationConfig != null && notificationConfig.containsKey("id")) {
                        Log.d(TAG, "notifconfig.id: " + notificationConfig.getDouble("id"));
                        Notification notification = NotificationHelper.getInstance(getApplicationContext()).buildNotification(getApplicationContext(), notificationConfig);
                        startForeground((int)notificationConfig.getDouble("id"), notification);
                        init();
                        startRecord();
                    }
                } else {
                    Log.e(TAG, "onStartCommand(): intent extra has no key: " + NOTIFICATION_CONFIG);
                }
            } else if (action.equals(Actions.ACTION_STOP_RECORD)) {
                stopRecord();
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        stopRecord();
    }

    private void init() {
        Log.d(TAG, "init() start");
//        initMediaRecorder();
        initAudioRecord();
    }

    private void startRecord() {
//        startMediaRecorder();
        startAudioRecord();
    }

    private void stopRecord() {
//        stopMediaRecorder();
        stopAudioRecord();
    }


    private void initMediaRecorder() {
        // dhpark: 순서 반드시 지킬것!
        if (mediaRecorder != null) {
            stopRecord();
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.WEBM);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(16000);
        Log.d(TAG, "init() : filepath = " + RECORD_FILEPATH);
        mediaRecorder.setOutputFile(RECORD_FILEPATH);
    }

    private void startMediaRecorder() {
        Log.d("BackgroundRecordService", "startRecord() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaRecorder() {
        Log.d("BackgroundRecordService", "stopRecord() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void initAudioRecord() {
        if (isRunning.get() || audioRecord != null) {
            stopRecord();
        }

        audioRecord = createAudioRecord();
        worker = new Thread(() -> {
            if (audioRecord == null) {
                return;
            }
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(RECORD_FILEPATH);
                isRunning.set(true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (isRunning.get()) {
                final int status = audioRecord.read(buffer, 0, buffer.length);
                if (status == AudioRecord.ERROR_INVALID_OPERATION || status == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "status == AudioRecord.ERROR_INVALID_OPERATION || status == AudioRecord.ERROR_BAD_VALUE");
                    break;
                }
                try {
                    outputStream.write(buffer, 0, buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private AudioRecord createAudioRecord() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT); //CHANNEL_IN_MONO, ENCODING_PCM_16BIT, 샘플링 레이트로 최소 버퍼 사이즈 구함
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) { // 값이 비정상임
            Log.e(TAG, "sizeInBytes is bad value");
        }
        final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize); // AudioRecord init 시도
        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) { // 성공?
            buffer = new byte[bufferSize]; // byte[] 버퍼 생성
            return audioRecord;
        } else {
            audioRecord.release(); // 실패했으니 릴리즈.
            return null;
        }
    }

    private void startAudioRecord() {
        Log.d(TAG, "startRecord() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if (audioRecord != null) {
            try {
                isRunning.set(true);
                audioRecord.startRecording();
                worker.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "audioRecord is null");
        }
    }

    private void stopAudioRecord() {
        Log.d(TAG, "stopRecord() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if (audioRecord != null) {
            try {
                isRunning.set(false);
                worker = null;
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "audioRecord is null");
        }
    }
}