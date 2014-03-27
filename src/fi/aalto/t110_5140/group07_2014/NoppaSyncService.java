package fi.aalto.t110_5140.group07_2014;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NoppaSyncService extends Service {

	/** Storage for an instance of the sync adapter */
	private static NoppaSyncAdapter sSyncAdapter = null;

	/** Object to use as a thread-safe lock */
	private static final Object sSyncAdapterLock = new Object();

	@Override
	public void onCreate() {
		// Create the sync adapter as a singleton.
		// Set the sync adapter as syncable
		// Disallow parallel syncs
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new NoppaSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// Get the object that allows external processes
		// to call onPerformSync(). The object is created
		// in the base class code when the SyncAdapter
		// constructors call super()
		return sSyncAdapter.getSyncAdapterBinder();
	}

}
