## Download

[![Download](https://api.bintray.com/packages/oddnetworks/maven/device-sdk/images/download.svg)](https://bintray.com/oddnetworks/maven/device-sdk/_latestVersion) or grab via Maven:

```xml
<dependency>
  <groupId>io.oddworks</groupId>
  <artifactId>device-sdk</artifactId>
  <version>{version}</version>
</dependency>
```

or Gradle:

```
repositories {
    maven {
        url 'http://oddnetworks.bintray.org/maven'
    }
}

dependencies {
    compile 'io.oddworks:device-sdk:{version}'
}
```

## Configure

You will need to configure a few pieces of application meta-data in `AndroidManifest.xml` to get started.

First, you will need to specify the device-specific JSON Web Token (JWT) given by Oddworks.

```xml
<application>
    <meta-data
        android:name="io.oddworks.configJWT"
        android:value="the-device-specific-jwt-given-by-the-oddworks-server" />
</application>
```

Then, if you are using the enterprise Oddworks content service, you will need to add the `io.oddworks.apiBaseURL` meta-data. If you leave this out, the default endpoint will be used. See `Oddworks.DEFAULT_API_BASE_URL`.

```xml
<application>
    <meta-data
        android:name="io.oddworks.configJWT"
        android:value="the-device-specific-jwt-given-by-the-oddworks-server" />
    <meta-data
        android:name="io.oddworks.apiBaseURL"
        android:value="https://path-to-your-oddworks.com/version" />
</application>
```

Finally, if you are using the enterprise Oddworks analytics service, you will need to add the `io.oddworks.analyticsApiBaseURL` meta-data. If you leave this out, the default endpoint will be used. See `Oddworks.DEFAULT_ANALYTICS_API_BASE_URL`.

```xml
<application>
    <meta-data
        android:name="io.oddworks.configJWT"
        android:value="the-device-specific-jwt-given-by-the-oddworks-server" />
    <meta-data
        android:name="io.oddworks.apiBaseURL"
        android:value="https://path-to-your-oddworks-content-service.com/version" />
    <meta-data
        android:name="io.oddworks.analyticsApiBaseURL"
        android:value="https://path-to-your-oddworks-analytics-service.com" />
</application>
```