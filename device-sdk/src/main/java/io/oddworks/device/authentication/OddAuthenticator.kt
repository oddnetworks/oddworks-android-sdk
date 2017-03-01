package io.oddworks.device.authentication

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.oddworks.device.R

class OddAuthenticator(val context: Context) : AbstractAccountAuthenticator(context) {

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle {
        Log.d(TAG, "addAccount")
        val accounts = AccountManager.get(context).getAccountsByType(context.getString(R.string.oddworks_account_type))
        if (accounts.size > 0) {
            val bundle = Bundle()
            bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_REMOTE_EXCEPTION)
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, context.getString(R.string.oddworks_single_account_only))
            return bundle
        }

        val intent = Intent(context, OddAuthenticationActivity::class.java)
        intent.putExtra(OddAuthenticationActivity.ARG_ACCOUNT_TYPE, accountType)
        intent.putExtra(OddAuthenticationActivity.ARG_AUTH_TYPE, authTokenType)
        intent.putExtra(OddAuthenticationActivity.ARG_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle? {
        Log.d(TAG, "getAuthToken")

        if (account == null) {
            val bundle = Bundle()
            bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_AUTHENTICATION)
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid account")
            return bundle
        }

        // Ensure the caller requested an authToken type that we support
        if (authTokenType != AUTH_TOKEN_TYPE_ODDWORKS_DEVICE) {
            val bundle = Bundle()
            bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_AUTHENTICATION)
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType")
            return bundle
        }

        val accountManager = AccountManager.get(context)

        val authToken = accountManager.peekAuthToken(account, authTokenType)
        Log.d(TAG, "peekAuthToken: $authToken")

        // normally you would use the password to refresh the authToken
        // however, authToken == password in our case

        if (authToken != null && !authToken.isEmpty()) {
            val bundle = Bundle()
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return bundle
        }

        val intent = Intent(context, OddAuthenticationActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(OddAuthenticationActivity.ARG_ACCOUNT_TYPE, account.type)
        intent.putExtra(OddAuthenticationActivity.ARG_AUTH_TYPE, authTokenType)
        intent.putExtra(OddAuthenticationActivity.ARG_ACCOUNT_EMAIL, account.name)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        if (authTokenType == AUTH_TOKEN_TYPE_ODDWORKS_DEVICE) {
            return context.getString(R.string.oddworks_account_label)
        }
        return "$authTokenType (Label)"
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return bundle
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
        return null
    }

    override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle? {
        return null
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String?, options: Bundle?): Bundle? {
        return null
    }

    companion object {
        private val TAG = OddAuthenticator::class.java.simpleName
        @JvmField val AUTH_TOKEN_TYPE_ODDWORKS_DEVICE = "Oddworks Device"
    }
}
