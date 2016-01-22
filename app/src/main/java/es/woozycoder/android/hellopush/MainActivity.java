package es.woozycoder.android.hellopush;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import java.net.MalformedURLException;


public class MainActivity
        extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MFPPush push; // Push client
    private MFPPushNotificationListener notificationListener; // Notification listener to handle a push sent to the phone

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView buttonText = (TextView) findViewById(R.id.button_text);

        try {
            // initialize SDK with IBM Bluemix application ID and route
            // You can find your backendRoute and backendGUID in the Mobile Options section on top of your Bluemix application dashboard
            // TODO: Please replace <APPLICATION_ROUTE> with a valid ApplicationRoute and <APPLICATION_ID> with a valid ApplicationId
            BMSClient.getInstance().initialize(this, "http://ladespensa.eu-gb.mybluemix.net", "4afd19c1-6c13-41ec-8342-74294d0ea6f8");
        } catch (MalformedURLException mue) {
            this.setStatus(
                    "Unable to parse Application Route URL\n Please verify you have entered your Application Route and Id correctly",
                    false);
            buttonText.setClickable(false);
        }

        // Initialize Push client
        MFPPush.getInstance().initialize(this);

        // Create notification listener and enable pop up notification when a message is received
        notificationListener = new MFPPushNotificationListener() {

            @Override
            public void onReceive (final MFPSimplePushNotification message) {

                Log.i(TAG, "Received a Push Notification: " + message.toString());
                runOnUiThread(new Runnable() {

                    public void run () {

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Received a Push Notification")
                                .setMessage(message.getAlert())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                    public void onClick (DialogInterface dialog, int whichButton) {

                                    }
                                })
                                .show();
                    }
                });
            }
        };
    }

    /**
     * Called when the register device button is pressed.
     * Attempts to register the device with your push service on Bluemix.
     * If successful, the push client sdk begins listening to the notification listener.
     *
     * @param view the button pressed
     */
    public void registerDevice (View view) {

        TextView buttonText = (TextView) findViewById(R.id.button_text);
        buttonText.setClickable(false);

        // Grabs push client sdk instance
        push = MFPPush.getInstance();

        TextView errorText = (TextView) findViewById(R.id.error_text);
        errorText.setText("Registering for notifications");

        Log.i(TAG, "Registering for notifications");

        // Creates response listener to handle the response when a device is registered.
        MFPPushResponseListener<String> registrationResponselistener = new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess (String s) {

                setStatus("Device Registered Successfully", true);
                Log.i(TAG, "Successfully registered for push notifications");
                push.listen(notificationListener);
            }

            @Override
            public void onFailure (MFPPushException e) {

                setStatus("Error registering for push notifications: " + e.getErrorMessage(), false);
                Log.e(TAG, e.getErrorMessage());
                push = null;
            }
        };

        // Attempt to register device using response listener created above
        push.register(registrationResponselistener);

    }

    // If the device has been registered previously, hold push notifications when the app is paused
    @Override
    protected void onPause () {

        super.onPause();

        if (push != null) {
            push.hold();
        }
    }

    // If the device has been registered previously, ensure the client sdk is still using the notification listener from onCreate when app is resumed
    @Override
    protected void onResume () {

        super.onResume();
        if (push != null) {
            push.listen(notificationListener);
        }
    }

    /**
     * Manipulates text fields in the UI based on initialization and registration events
     *
     * @param messageText   String main text view
     * @param wasSuccessful Boolean dictates top 2 text view texts
     */
    private void setStatus (final String messageText, boolean wasSuccessful) {

        final TextView errorText = (TextView) findViewById(R.id.error_text);
        final TextView topText = (TextView) findViewById(R.id.top_text);
        final TextView bottomText = (TextView) findViewById(R.id.bottom_text);
        final TextView buttonText = (TextView) findViewById(R.id.button_text);
        final String topStatus = wasSuccessful ? "Yay!" : "Bummer";
        final String bottomStatus = wasSuccessful ? "You Are Connected" : "Something Went Wrong";

        runOnUiThread(new Runnable() {

            @Override
            public void run () {

                buttonText.setClickable(true);
                errorText.setText(messageText);
                topText.setText(topStatus);
                bottomText.setText(bottomStatus);
            }
        });
    }
}
