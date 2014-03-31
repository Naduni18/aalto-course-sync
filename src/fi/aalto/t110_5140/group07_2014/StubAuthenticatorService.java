package fi.aalto.t110_5140.group07_2014;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StubAuthenticatorService extends Service {

	private StubAuthenticator authenticator;

	@Override
	public void onCreate() {
		Log.d("StubAuthenticatorService", "onCreate()");
		authenticator = new StubAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("StubAuthenticatorService", "onBind(" + intent +")");
		return authenticator.getIBinder();
	}

}
