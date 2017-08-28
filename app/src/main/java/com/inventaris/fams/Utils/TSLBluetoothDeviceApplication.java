package com.inventaris.fams.Utils;

import android.app.Application;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

/**
 * Created by mwildani on 25/08/2017.
 */

public class TSLBluetoothDeviceApplication extends Application {
    private static AsciiCommander commander = null;

    /// Returns the current AsciiCommander
    public AsciiCommander getCommander() {
        return commander;
    }

    /// Sets the current AsciiCommander
    public void setCommander(AsciiCommander _commander) {
        commander = _commander;
    }

}
