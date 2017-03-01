There are two ways of sending analytics metrics.

## Sending OddMetric requests manually

Note: Be careful about making requests on the main thread.

```java
// from within your activity

OddCallback<OddMetric> metricCallback = new OddCallback<OddMetric>() {
    @Override
    public void onSuccess(OddMetric resource) {
        Log.d(TAG, "handleOddMetric: SUCCESS $resource}")
    }

    @Override
    public void onFailure(@NotNull Exception exception) {
        Log.d(TAG, "handleOddMetric: FAILURE $exception")
        if (exception is BadResponseCodeException) {
            Log.d(TAG, "handleOddMetric code: ${exception.code} errors: ${exception.oddErrors}")
        }
    }
};

OddRequest.Builder(context, OddResourceType.EVENT)
                        .event(event)
                        .build()
                        .enqueueRequest(oddMetricCallback)
```

## Sending OddMetric requests via the OddRxBus

You will first need to enable the OddMetricHandler. A good place to do this is in your Application class.

```java

public class YourApp extends Application {
  @Override
  public void onCreate() {
      super.onCreate();

      // Enable handling of published analytics events
      OddMetricHandler.enable(this);
  }
}
```

Using the OddMetricHandler ensures the POST requests are sent on an IO Scheduler thread.

Next, when you are ready to enqueue an OddMetric request, simply use the `OddRxBus.publish()` function.

```java

// from within an activity
OddAppInitMetric metric = new OddAppInitMetric();

OddRxBus.publish(metric);
```
