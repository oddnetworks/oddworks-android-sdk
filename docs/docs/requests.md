We need to build an `OddRequest` and enqueue it, passing along an `OddCallback`.

`OddRequest.Builder` can be used to do this. Minimally, pass the current application context and an `OddResourceType`

```java
// Create the OddCallback instance
OddCallback<OddConfig> configCallback = new OddCallback<OddConfig>() {
    @Override
    public void onSuccess(OddConfig resource) {
        // do what you need to do with the OddConfig resource
        Log.d(TAG, "request successful - id: " + resource.getIdentifier().getId());
    }

    @Override
    public void onFailure(@NotNull Exception exception) {
        Log.e(TAG, "request failed", exception);

        // if non 200 status code, Exception will be a BadResponseCodeException
        if (exception instanceof BadResponseCodeException) {
            // Useful for determining cause of bad response
            LinkedHashSet<OddError> errors = ((BadResponseCodeException) exception).oddErrors;
            Log.d(TAG, "parsed server errors: " + errors);
        }
    }
};

// Create the OddRequest instance
OddRequest request = new OddRequest.Builder(context, OddResourceType.CONFIG).build();

// Build and enqueue the OkHttp.Call using OddRequest and OddCallback
request.enqueueRequest(configCallback);
```

You can also wrap this in an `RxOddCall` if you are into [RxJava](https://github.com/ReactiveX/RxJava)

```java
RxOddCall
    .observableFrom(new Action1<OddCallback<OddConfig>>() {
        @Override
        public void call(OddCallback<OddConfig> oddCallback) {
            new OddRequest.Builder(context, OddResourceType.CONFIG)
                    .build()
                    .enqueueRequest(oddCallback);
        }
    })
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Action1<OddConfig>() {
        @Override
        public void call(OddConfig oddConfig) {
            // do what you need to do with the OddConfig resource
            Log.d(TAG, "request successful - id: " + resource.getIdentifier().getId());
        }
    }, new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Log.e(TAG, "request failed", throwable);

            // if non 200 status code, Throwable will be a BadResponseCodeException
            if (throwable instanceof BadResponseCodeException) {
                // Useful for determining cause of bad response
                LinkedHashSet<OddError> errors = ((BadResponseCodeException) throwable).oddErrors;
                Log.d(TAG, "parsed server errors: " + errors);
            }
        }
    });
```
