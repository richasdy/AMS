package com.inventaris.fams;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.inventaris.fams.Fragment.AddData;
import com.inventaris.fams.Fragment.SearchData;
import com.inventaris.fams.Utils.TSLBluetoothDeviceActivity;
import com.inventaris.fams.Utils.TSLBluetoothDeviceApplication;
import com.pixplicity.easyprefs.library.Prefs;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import java.util.ArrayList;
import java.util.List;

public class HalamanUtama extends TSLBluetoothDeviceActivity {
    private String stat;
    private BluetoothAdapter mBluetoothAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FamsModel mModel;
    private int[] tabIcons = {
            R.drawable.addnewdata,
            R.drawable.search,
    };

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    private static final int REQUEST_ENABLE_BT = 3;

    public AsciiCommander getCommander() {
        return ((TSLBluetoothDeviceApplication) getApplication()).getCommander();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);


        //initialize shared pref
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        stat = Prefs.getString(Config.TOKEN_SHARED_PREF, "null");

        if (stat.equals("null")) {
            startActivity(new Intent(getApplicationContext(), HalamanLogin.class));
            finish();
        }


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setEnabled(false);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();


        AsciiCommander commander = getCommander();

        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is added first so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add a synchronous responder to handle synchronous commands
        commander.addSynchronousResponder();

        mModel = new FamsModel();
        mModel.setCommander(getCommander());
    }

    public void finDevice() {
        selectDevice();
    }

    public FamsModel getmModel() {
        return mModel;
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new AddData(), "ONE");
        adapter.addFrag(new SearchData(), "TWO");

        viewPager.setAdapter(adapter);
    }

    private MenuItem mReconnectMenuItem;
    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;
    private MenuItem mResetMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_halaman_utama, menu);

        mResetMenuItem = menu.findItem(R.id.reset_reader_menu_item);
        mReconnectMenuItem = menu.findItem(R.id.reconnect_reader_menu_item);
        mConnectMenuItem = menu.findItem(R.id.insecure_connect_reader_menu_item);
        mDisconnectMenuItem = menu.findItem(R.id.disconnect_reader_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.reconnect_reader_menu_item:
                Toast.makeText(this.getApplicationContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                reconnectDevice();
                return true;

            case R.id.insecure_connect_reader_menu_item:
                // Choose a device and connect to it
                selectDevice();
                return true;

            case R.id.disconnect_reader_menu_item:
                Toast.makeText(this.getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
                disconnectDevice();
                return true;

            case R.id.reset_reader_menu_item:
                resetReader();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);

        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    bluetoothNotAvailableError("Bluetooth was not enabled\nApplication Quitting...");
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, false);
                }
                break;
        }
    }
}
