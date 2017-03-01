package io.oddworks.device.authentication

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OddAuthenticatorService: Service() {
    override fun onBind(intent: Intent?): IBinder {
        val authenticator = OddAuthenticator(this)
        return authenticator.iBinder
    }
}
