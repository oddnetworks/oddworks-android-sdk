package io.oddworks.device.authentication

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import io.oddworks.device.R
import io.oddworks.device.exception.BadResponseCodeException
import io.oddworks.device.model.OddViewer
import io.oddworks.device.model.common.OddResourceType
import io.oddworks.device.request.OddRequest
import io.oddworks.device.request.RxOddCall
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class OddAuthenticationActivity : AccountAuthenticatorActivity() {
    lateinit var authTokenType: String
    lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.OddAuthenticationTheme)
        setContentView(R.layout.activity_odd_authentication)
        accountManager = AccountManager.get(baseContext)

        val accountEmail = intent.getStringExtra(ARG_ACCOUNT_EMAIL)
        authTokenType = intent.getStringExtra(ARG_AUTH_TYPE) ?: OddAuthenticator.AUTH_TOKEN_TYPE_ODDWORKS_DEVICE

        if (accountEmail != null) {
            (findViewById(R.id.odd_authentication_email) as TextView).text = accountEmail
        }
        (findViewById(R.id.odd_authentication_password) as EditText).setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == R.id.odd_authentication_sign_in || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                submit(textView)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    fun submit(view: View) {
        val email = (findViewById(R.id.odd_authentication_email) as TextView).text.toString()
        val password = (findViewById(R.id.odd_authentication_password) as TextView).text.toString()
        val accountType = intent.getStringExtra(ARG_ACCOUNT_TYPE)

        showProgress()

        RxOddCall
                .observableFrom<OddViewer> {
                    Log.d(TAG, "Started authenticating - $authTokenType -  $email")
                    OddRequest.Builder(baseContext, OddResourceType.VIEWER)
                            .login(email, password)
                            .build()
                            .enqueueRequest(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "login successful - $authTokenType -  $email")

                    finishLogin(accountType, it)
                },{
                    Log.d(TAG, "login failed - $authTokenType -  $email", it)
                    val message = when (it) {
                        is BadResponseCodeException -> {
                            var detail = ""
                            if (it.oddErrors.isNotEmpty()) {
                                val error = it.oddErrors.first()
                                if (error.detail.isNullOrBlank()) {
                                    detail = error.title
                                } else {
                                    detail = error.detail
                                }
                            }

                            getString(R.string.oddworks_authentication_failed_with_detail, detail)
                        }
                        else -> {
                            getString(R.string.oddworks_authentication_failed_no_detail)
                        }
                    }
                    Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                    hideProgress()
                })
    }

    private fun showProgress() {
        val button = findViewById(R.id.odd_authentication_button)
        val progress = findViewById(R.id.odd_authentication_progress)

        button.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        val button = findViewById(R.id.odd_authentication_button)
        val progress = findViewById(R.id.odd_authentication_progress)

        button.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

    private fun finishLogin(accountType: String, viewer: OddViewer) {
        val account = Account(viewer.id, accountType)
        if (intent.getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "finishLogin > addAccountExplicitly - $authTokenType -  ${viewer.id}")
            Log.d(TAG, "${account.name} - $authTokenType - ${viewer.jwt}")

            accountManager.addAccountExplicitly(account, viewer.jwt, null)
            accountManager.setAuthToken(account, authTokenType, viewer.jwt)
        } else {
            Log.d(TAG, "finishLogin > setPassword - $authTokenType -  ${viewer.id}")
            Log.d(TAG, "${account.name} - $authTokenType - ${viewer.jwt}")
            accountManager.setAuthToken(account, authTokenType, viewer.jwt)
            accountManager.setPassword(account, viewer.jwt)
        }

        val data = Bundle()
        val result = Intent()
        result.putExtras(data)
        setAccountAuthenticatorResult(data)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        private val TAG = OddAuthenticationActivity::class.java.simpleName
        val ARG_ACCOUNT_TYPE = "${OddAuthenticationActivity::class.java.name}.ARG_ACCOUNT_TYPE"
        val ARG_AUTH_TYPE = "${OddAuthenticationActivity::class.java.name}.ARG_AUTH_TYPE"
        val ARG_ACCOUNT_EMAIL = "${OddAuthenticationActivity::class.java.name}.ARG_ACCOUNT_EMAIL"
        val ARG_IS_ADDING_NEW_ACCOUNT = "${OddAuthenticationActivity::class.java.name}.ARG_IS_ADDING_NEW_ACCOUNT"
    }
}