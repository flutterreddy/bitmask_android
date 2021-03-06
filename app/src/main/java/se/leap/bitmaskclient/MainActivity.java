/**
 * Copyright (c) 2018 LEAP Encryption Access Project and contributers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package se.leap.bitmaskclient;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import se.leap.bitmaskclient.drawer.NavigationDrawerFragment;
import se.leap.bitmaskclient.eip.EipCommand;
import se.leap.bitmaskclient.fragments.LogFragment;
import se.leap.bitmaskclient.utils.ConfigHelper;

import static android.content.Intent.CATEGORY_DEFAULT;
import static se.leap.bitmaskclient.Constants.BROADCAST_EIP_EVENT;
import static se.leap.bitmaskclient.Constants.BROADCAST_PROVIDER_API_EVENT;
import static se.leap.bitmaskclient.Constants.BROADCAST_RESULT_CODE;
import static se.leap.bitmaskclient.Constants.BROADCAST_RESULT_KEY;
import static se.leap.bitmaskclient.Constants.EIP_ACTION_START;
import static se.leap.bitmaskclient.Constants.EIP_ACTION_STOP;
import static se.leap.bitmaskclient.Constants.EIP_REQUEST;
import static se.leap.bitmaskclient.Constants.PROVIDER_KEY;
import static se.leap.bitmaskclient.Constants.REQUEST_CODE_CONFIGURE_LEAP;
import static se.leap.bitmaskclient.Constants.REQUEST_CODE_LOG_IN;
import static se.leap.bitmaskclient.Constants.REQUEST_CODE_SWITCH_PROVIDER;
import static se.leap.bitmaskclient.Constants.SHARED_PREFERENCES;
import static se.leap.bitmaskclient.EipFragment.ASK_TO_CANCEL_VPN;
import static se.leap.bitmaskclient.ProviderAPI.CORRECTLY_DOWNLOADED_EIP_SERVICE;
import static se.leap.bitmaskclient.ProviderAPI.CORRECTLY_UPDATED_INVALID_VPN_CERTIFICATE;
import static se.leap.bitmaskclient.ProviderAPI.ERRORS;
import static se.leap.bitmaskclient.ProviderAPI.INCORRECTLY_DOWNLOADED_EIP_SERVICE;
import static se.leap.bitmaskclient.ProviderAPI.INCORRECTLY_UPDATED_INVALID_VPN_CERTIFICATE;
import static se.leap.bitmaskclient.ProviderAPI.USER_MESSAGE;
import static se.leap.bitmaskclient.R.string.downloading_vpn_certificate_failed;
import static se.leap.bitmaskclient.R.string.vpn_certificate_user_message;
import static se.leap.bitmaskclient.utils.PreferenceHelper.getSavedProviderFromSharedPreferences;
import static se.leap.bitmaskclient.utils.PreferenceHelper.storeProviderInPreferences;


public class MainActivity extends AppCompatActivity {

    public final static String TAG = MainActivity.class.getSimpleName();

    private Provider provider = new Provider();
    private SharedPreferences preferences;
    private NavigationDrawerFragment navigationDrawerFragment;
    private MainActivityBroadcastReceiver mainActivityBroadcastReceiver;

    public final static String ACTION_SHOW_VPN_FRAGMENT = "action_show_vpn_fragment";
    public final static String ACTION_SHOW_LOG_FRAGMENT = "action_show_log_fragment";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mainActivityBroadcastReceiver = new MainActivityBroadcastReceiver();
        setUpBroadcastReceiver();

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        provider = getSavedProviderFromSharedPreferences(preferences);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        handleIntentAction(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        FragmentManagerEnhanced fragmentManagerEnhanced = new FragmentManagerEnhanced(getSupportFragmentManager());
        if (fragmentManagerEnhanced.findFragmentByTag(EipFragment.TAG) == null) {
            Fragment fragment = new EipFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(PROVIDER_KEY, provider);
            fragment.setArguments(bundle);
            fragmentManagerEnhanced.beginTransaction()
                    .replace(R.id.container, fragment, EipFragment.TAG)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentAction(intent);
    }

    private void handleIntentAction(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String fragmentTag = null;
        Fragment fragment = null;
        switch (intent.getAction()) {
            case ACTION_SHOW_VPN_FRAGMENT:
                fragment = new EipFragment();
                fragmentTag = EipFragment.TAG;
                Bundle bundle = new Bundle();
                if (intent.hasExtra(ASK_TO_CANCEL_VPN)) {
                    bundle.putBoolean(ASK_TO_CANCEL_VPN, true);
                }
                bundle.putParcelable(PROVIDER_KEY, provider);
                fragment.setArguments(bundle);
                break;
            case ACTION_SHOW_LOG_FRAGMENT:
                fragment = new LogFragment();
                break;
            default:
                break;
        }
        // on layout change / recreation of the activity, we don't want create new Fragments
        // instead the fragments themselves care about recreation and state restoration
        intent.setAction(null);

        if (fragment != null) {
            new FragmentManagerEnhanced(getSupportFragmentManager()).beginTransaction()
                    .replace(R.id.container, fragment, fragmentTag)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        if (resultCode == RESULT_OK && data.hasExtra(Provider.KEY)) {
            provider = data.getParcelableExtra(Provider.KEY);

            if (provider == null) {
                return;
            }

            storeProviderInPreferences(preferences, provider);
            navigationDrawerFragment.refresh();

            switch (requestCode) {
                case REQUEST_CODE_SWITCH_PROVIDER:
                    EipCommand.stopVPN(this);
                    break;
                case REQUEST_CODE_CONFIGURE_LEAP:
                    Log.d(TAG, "REQUEST_CODE_CONFIGURE_LEAP - onActivityResult - MainActivity");
                    break;
                case REQUEST_CODE_LOG_IN:
                    EipCommand.startVPN(this, true);
                    break;
            }
        }
        //TODO: Why do we want this --v? legacy and redundant?
        Fragment fragment = new EipFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(PROVIDER_KEY, provider);
        fragment.setArguments(arguments);
        new FragmentManagerEnhanced(getSupportFragmentManager()).beginTransaction()
                .replace(R.id.container, fragment, EipFragment.TAG)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainActivityBroadcastReceiver);
        mainActivityBroadcastReceiver = null;
        super.onDestroy();
    }

    private void setUpBroadcastReceiver() {
        IntentFilter updateIntentFilter = new IntentFilter(BROADCAST_EIP_EVENT);
        updateIntentFilter.addAction(BROADCAST_PROVIDER_API_EVENT);
        updateIntentFilter.addCategory(CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mainActivityBroadcastReceiver, updateIntentFilter);
        Log.d(TAG, "broadcast registered");
    }

    private class MainActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received Broadcast");

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            int resultCode = intent.getIntExtra(BROADCAST_RESULT_CODE, RESULT_CANCELED);
            Bundle resultData = intent.getParcelableExtra(BROADCAST_RESULT_KEY);
            if (resultData == null) {
                resultData = Bundle.EMPTY;
            }

            switch (action) {
                case BROADCAST_EIP_EVENT:
                    handleEIPEvent(resultCode, resultData);
                    break;
                case BROADCAST_PROVIDER_API_EVENT:
                    handleProviderApiEvent(resultCode, resultData);
                    break;
            }
        }
    }

    private void handleEIPEvent(int resultCode, Bundle resultData) {
        String request = resultData.getString(EIP_REQUEST);

        if (request == null) {
            return;
        }

        switch (request) {
            case EIP_ACTION_START:
                switch (resultCode) {
                    case RESULT_OK:
                        break;
                    case RESULT_CANCELED:
                        String error = resultData.getString(ERRORS);
                        if (LeapSRPSession.loggedIn() || provider.allowsAnonymous()) {
                            showMainActivityErrorDialog(error);
                        } else {
                            askUserToLogIn(getString(vpn_certificate_user_message));
                        }
                        break;
                }
                break;
            case EIP_ACTION_STOP:
                switch (resultCode) {
                    case RESULT_OK:
                        break;
                    case RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    public void handleProviderApiEvent(int resultCode, Bundle resultData) {
        // TODO call DOWNLOAD_EIP_SERVICES ore remove respective cases
        switch (resultCode) {
            case CORRECTLY_DOWNLOADED_EIP_SERVICE:
                provider = resultData.getParcelable(PROVIDER_KEY);
                EipCommand.startVPN(this, true);
                break;
            case INCORRECTLY_DOWNLOADED_EIP_SERVICE:
                // TODO CATCH ME IF YOU CAN - WHAT DO WE WANT TO DO?
                break;

            case CORRECTLY_UPDATED_INVALID_VPN_CERTIFICATE:
                provider = resultData.getParcelable(PROVIDER_KEY);
                storeProviderInPreferences(preferences, provider);
                EipCommand.startVPN(this, true);
                break;
            case INCORRECTLY_UPDATED_INVALID_VPN_CERTIFICATE:
                if (LeapSRPSession.loggedIn() || provider.allowsAnonymous()) {
                    showMainActivityErrorDialog(getString(downloading_vpn_certificate_failed));
                } else {
                    askUserToLogIn(getString(vpn_certificate_user_message));
                }
                break;
        }
    }

    /**
     * Shows an error dialog
     */
    public void showMainActivityErrorDialog(String reasonToFail) {
        try {

            FragmentTransaction fragmentTransaction = new FragmentManagerEnhanced(
                    this.getSupportFragmentManager()).removePreviousFragment(
                            MainActivityErrorDialog.TAG);
            DialogFragment newFragment;
            try {
                JSONObject errorJson = new JSONObject(reasonToFail);
                newFragment = MainActivityErrorDialog.newInstance(provider, errorJson);
            } catch (JSONException e) {
                e.printStackTrace();
                newFragment = MainActivityErrorDialog.newInstance(provider, reasonToFail);
            }
            newFragment.show(fragmentTransaction, MainActivityErrorDialog.TAG);
        } catch (IllegalStateException | NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "error dialog leaked!");
        }

    }


    private void askUserToLogIn(String userMessage) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(PROVIDER_KEY, provider);
        if (userMessage != null) {
            intent.putExtra(USER_MESSAGE, userMessage);
        }
        startActivityForResult(intent, REQUEST_CODE_LOG_IN);
    }
}
