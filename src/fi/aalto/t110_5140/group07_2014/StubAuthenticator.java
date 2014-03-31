package fi.aalto.t110_5140.group07_2014;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class StubAuthenticator extends AbstractAccountAuthenticator {

	private static final String ACCOUNT_TYPE =
			"fi.aalto.t110_5140.group07_2014.noppacalendarsync.no_account";

	private final Context context;

	public StubAuthenticator(Context context) {
		super(context);
		this.context = context;
		Log.d("StubAuthenticator", "constructor");
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse res, String accountType) {
		Log.d("StubAuthenticator", "editProperties(...)");
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle addAccount(
			AccountAuthenticatorResponse res,
			String accountType,
			String authTokenType,
			String[] requiredFeatures,
			Bundle options) throws NetworkErrorException {
		
		Log.d("StubAuthenticator", "addAccount("+res+", "+accountType+", "+authTokenType+", "+requiredFeatures+", "+options+")");
		Bundle b = new Bundle();
		if (accountType.equals(ACCOUNT_TYPE)) {
			b.putString(AccountManager.KEY_ACCOUNT_NAME, "no_account");
			b.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
			b.putString(AccountManager.KEY_AUTHTOKEN, "-");
			
			Account acc = new Account("no_account", ACCOUNT_TYPE);
			AccountManager.get(context).addAccountExplicitly(acc, null, null);
		}
		else {
			b.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS);
			b.putString(AccountManager.KEY_ERROR_MESSAGE, "Bad account type: " + accountType);
		}
		return b;
	}

	@Override
	public Bundle confirmCredentials(
			AccountAuthenticatorResponse res,
			Account account,
			Bundle options) throws NetworkErrorException {
		
		Log.d("StubAuthenticator", "confirmCredentials("+res+", "+account+", "+options+")");
		return null;
	}

	@Override
	public Bundle getAuthToken(
			AccountAuthenticatorResponse res,
			Account account,
			String authTokenType,
			Bundle options) throws NetworkErrorException {
		
		Log.d("StubAuthenticator", "getAuthToken(...)");
//		throw new UnsupportedOperationException();
		Bundle b = new Bundle();
		b.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
		b.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
		b.putString(AccountManager.KEY_AUTHTOKEN, "-");
		return b;
	}

	@Override
	public String getAuthTokenLabel(String s) {
		Log.d("StubAuthenticator", "getAuthTokenLabel(" + s + ")");
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle updateCredentials(
			AccountAuthenticatorResponse res,
			Account account,
			String authTokenType,
			Bundle bundle) throws NetworkErrorException {
		
		Log.d("StubAuthenticator", "updateCredentials(...)");
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle hasFeatures(
			AccountAuthenticatorResponse r,
			Account account,
			String[] features) throws NetworkErrorException {
		
		Log.d("StubAuthenticator", "hasFeatures(...)");
		throw new UnsupportedOperationException();
	}

}
