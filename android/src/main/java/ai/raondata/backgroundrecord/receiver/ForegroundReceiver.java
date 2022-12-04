package ai.raondata.backgroundrecord.receiver;

import static ai.raondata.backgroundrecord.common.Constants.FOREGROUND_SERVICE_BUTTON_PRESSED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ForegroundReceiver extends BroadcastReceiver {
    private final Callback foregroundReceiverCallback;

    public ForegroundReceiver(Callback callback) {
        foregroundReceiverCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(FOREGROUND_SERVICE_BUTTON_PRESSED)) {
            foregroundReceiverCallback.onPressedForegroundServiceButton();
        }
    }

    public interface Callback {
        void onPressedForegroundServiceButton();
    }
}