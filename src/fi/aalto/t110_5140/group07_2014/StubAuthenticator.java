package fi.aalto.t110_5140.group07_2014;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class StubAuthenticator extends AbstractAccountAuthenticator {

	public StubAuthenticator(Context context) {
		super(context);
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle addAccount(
			AccountAuthenticatorResponse r,
			String s,
			String s2,
			String[] strs,
			Bundle bundle) throws NetworkErrorException {
		
		return null;
	}

	@Override
	public Bundle confirmCredentials(
			AccountAuthenticatorResponse r,
			Account account,
			Bundle bundle) throws NetworkErrorException {
		
		return null;
	}

	@Override
	public Bundle getAuthToken(
			AccountAuthenticatorResponse r,
			Account account,
			String s,
			Bundle bundle) throws NetworkErrorException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthTokenLabel(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle updateCredentials(
			AccountAuthenticatorResponse r,
			Account account,
			String s,
			Bundle bundle) throws NetworkErrorException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle hasFeatures(
			AccountAuthenticatorResponse r,
			Account account,
			String[] strs) throws NetworkErrorException {
		
		throw new UnsupportedOperationException();
	}

}
