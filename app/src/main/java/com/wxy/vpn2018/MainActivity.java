package com.wxy.vpn2018;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNManagement;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

import static de.blinkt.openvpn.core.OpenVPNService.humanReadableByteCount;

public class MainActivity extends AppCompatActivity implements VpnStatus.ByteCountListener, VpnStatus.StateListener {
    private ActionProcessButton btnConnect;
    private LinearLayout layoutSpeedMeter;
    private TextView textUpload, textDownload;
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    protected void onStop() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        layoutSpeedMeter = findViewById(R.id.speedMeterLayout);
        textUpload = findViewById(R.id.textUpload);
        textDownload = findViewById(R.id.textDownload);
        btnConnect = findViewById(R.id.buttonConnect);
        if (!App.isStart) {
            DataCleanManager.cleanCache(this);
            btnConnect.setEnabled(false);
            final ProgressBar progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);
            profileAsync = new ProfileAsync(this, new ProfileAsync.OnProfileLoadListener() {
                @Override
                public void onProfileLoadSuccess() {
                    progressBar.setVisibility(View.GONE);
                    btnConnect.setEnabled(true);
                }

                @Override
                public void onProfileLoadFailed(String msg) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.init_fail) + msg, Toast.LENGTH_SHORT).show();
                }
            });
            profileAsync.execute();
//            rate();
        }
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (!App.isStart) {
                            startVPN();
                            App.isStart = true;
                        } else {
                            stopVPN();
                            App.isStart = false;
                        }
                    }
                };
                r.run();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private ProfileAsync profileAsync;

    @Override
    public void finish() {
        super.finish();
        if (profileAsync != null && !profileAsync.isCancelled()) {
            profileAsync.cancel(true);
        }
    }

    void startVPN() {
        btnConnect.setMode(ActionProcessButton.Mode.ENDLESS);
        btnConnect.setProgress(1);
        try {
            ProfileManager pm = ProfileManager.getInstance(this);
            VpnProfile profile = pm.getProfileByName(Build.MODEL);//
            startVPNConnection(profile);
        } catch (Exception ex) {
            App.isStart = false;
        }
    }

    void stopVPN() {
        stopVPNConnection();
        btnConnect.setMode(ActionProcessButton.Mode.ENDLESS);
        btnConnect.setProgress(0);
        btnConnect.setText(getString(R.string.connect));
        layoutSpeedMeter.setVisibility(View.INVISIBLE);
    }


    // ------------- Functions Related to OpenVPN-------------
    public void startVPNConnection(VpnProfile vp) {
        Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);
    }

    public void stopVPNConnection() {
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        if (mService != null) {
            try {
                mService.stopVPN(false);
            } catch (RemoteException e) {
//                VpnStatus.logException(e);
            }
        }
    }

    @Override
    public void updateByteCount(long ins, long outs, long diffIns, long diffOuts) {
        final long diffIn = diffIns;
        final long diffOut = diffOuts;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textDownload.setText(String.format("↓: %s", humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, true, getResources())));
                textUpload.setText(String.format("↑: %s", humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, true, getResources())));
            }
        });
    }

    void setConnected() {
        btnConnect.setMode(ActionProcessButton.Mode.ENDLESS);
        btnConnect.setProgress(0);
        btnConnect.setText(getString(R.string.connected));
        layoutSpeedMeter.setVisibility(View.VISIBLE);
    }

    void changeStateButton(Boolean state) {
        if (state) {
            btnConnect.setMode(ActionProcessButton.Mode.ENDLESS);
            btnConnect.setProgress(0);
            btnConnect.setText(getString(R.string.connected));
            layoutSpeedMeter.setVisibility(View.VISIBLE);
        } else {
            btnConnect.setMode(ActionProcessButton.Mode.ENDLESS);
            btnConnect.setProgress(0);
            btnConnect.setText(getString(R.string.connect));
            layoutSpeedMeter.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void updateState(final String state, String logmessage, int localizedResId, ConnectionStatus level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    App.isStart = true;
                    setConnected();
                    layoutSpeedMeter.setVisibility(View.VISIBLE);
                } else {
                    layoutSpeedMeter.setVisibility(View.INVISIBLE);
                }
                if (state.equals("AUTH_FAILED")) {
                    Toast.makeText(getApplicationContext(), "Wrong Username or Password!", Toast.LENGTH_SHORT).show();
                    changeStateButton(false);
                }
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {
    }
}
