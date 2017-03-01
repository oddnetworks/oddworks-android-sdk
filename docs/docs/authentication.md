This SDK offers an `OddAuthenticator` service that can be configured to handle authentication via Oddworks.

An authentication workflow can also be manually created via `OddRequest.Builder`.

## OddAuthenticatorService

__Step 1:__ `AndroidManifest.xml`

To utilize OddAuthenticatorService, you must first declare it within the `<application>` block of `AndroidManifest.xml`

```xml
<application>
  <activity android:name="io.oddworks.device.authentication.OddAuthenticationActivity" 
              android:label="@string/login_label" />
  <service android:name="io.oddworks.device.authentication.OddAuthenticatorService">
      <intent-filter>
          <action android:name="android.accounts.AccountAuthenticator" />
      </intent-filter>
      <meta-data android:name="android.accounts.AccountAuthenticator"
                       android:resource="@xml/oddworks_authenticator" />
  </service>
</application>
```

There are also some permissions needed. Depending upon the SDK level you are targeting, you may need to request these permissions at runtime.

```xml
<uses-permission android:name="android.permission.USE_CREDENTIALS"/>
<uses-permission android:name="android.permission.GET_ACCOUNTS"/>
<uses-permission android:name="android.permission.MANAGE_ACCOUNTS"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
```

__Step 2:__ `res/strings.xml`

Next, you should override a few strings in your application's `res/strings.xml` file.

- `@string/oddworks_account_type` - This should be a string that is unique to your application and will distinguish your Accounts from others on the device.
- `@string/oddworks_account_label` - This will be the label displayed when listing the device's Accounts.

!["OddSample"](http://oddworks-android-sdk.s3.amazonaws.com/android-device-account-list.png)

__Step 3:__ `res/mipmap/ic_launcher.png|xml`

Be sure that you have an `ic_launcher` icon set in your application's `res/mipmap` directory(ies). This will be the icon displayed when listing the device's Accounts.

## OddAuthenticationActivity

When creating a new Account with OddAuthenticationService, an OddAuthenticationActivity is started, which you will need to declare this activity within your `AndroidManifest.xml`:

![OddAuthenticationActivity](http://oddworks-android-sdk.s3.amazonaws.com/android-device-odd-authentication-activity.png)

This view is highly customizable and can be done so by overriding the following styles:

```xml
<style name="OddAuthenticationTheme" parent="@style/Theme.AppCompat.NoActionBar">
<style name="OddAuthenticationTheme.LinearLayout" parent="OddAuthenticationTheme">
<style name="OddAuthenticationTheme.MessageTextView" parent="OddAuthenticationTheme">
<style name="OddAuthenticationTheme.EditText" parent="@style/Widget.AppCompat.EditText">
<style name="OddAuthenticationTheme.EmailEditText" parent="OddAuthenticationTheme.EditText">
<style name="OddAuthenticationTheme.PasswordEditText" parent="OddAuthenticationTheme.EditText">
<style name="OddAuthenticationTheme.Button" parent="@style/Widget.AppCompat.Button.Colored">
<style name="OddAuthenticationTheme.ProgressBar" parent="@style/Widget.AppCompat.ProgressBar">
```

## Handling Accounts

Ultimately, how you choose to handle your Account(s) is up to you.

Authenticating an OddRequest with an Account can be done using the `OddRequest.Builder` function `account(Account)`.

If the Account's `authToken` is valid, it will be used to make the request.

If the Account's `authToken` is invalid or missing, the user will be prompted to re-authenticate.

If the Authenticated request responds with a `401` code, the Account is removed.