package ai.raondata.backgroundrecord.receiver;

import static ai.raondata.backgroundrecord.common.Constants.RECEIVED_RECORD_DATA;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecordReceiver extends BroadcastReceiver {
    private final Callback recordReceiverCallback;

    public RecordReceiver(Callback callback) {
        recordReceiverCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(RECEIVED_RECORD_DATA)) {
            String base64Data = intent.getExtras().getString("data");
            recordReceiverCallback.onReceivedRecordData(base64Data);
        }
    }

    public interface Callback {
        void onReceivedRecordData(String data);
    }
}