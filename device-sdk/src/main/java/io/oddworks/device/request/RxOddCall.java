package io.oddworks.device.request;

import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * This class is useful for any of the methods in this library that take OddCallbacks and using them with RxJava
 * instead.
 *
 * The main point of entry for most use cases will be RxOddCall.observableFrom
 *
 * @author Dan Pallas
 * @since 1.2 on 02/26/2016
 */
public class RxOddCall<T> implements Observable.OnSubscribe<T>{
    public static final String TAG = RxOddCall.class.getSimpleName();

    private final Action1<OddCallback<T>> f;

    public RxOddCall(Action1<OddCallback<T>> f) {
        this.f = f;
    }

    /** This is a shortcut to getting an Observable from a method taking an odd callback. Whenever possible, this
     * should be used instead of instantiating RxOddCall directly.
     *
     * ex:
     * <pre>
     * {@code
     *      RxOddCall.observableFrom(new Action1<OddCallback<OddView>>() {
     *          {@literal @}Override
     *          public void call(OddCallback<OddView> oddViewOddCallback) {
     *              RestServiceProvider.getInstance().getApiCaller().getView(id, oddViewOddCallback);
     *          }
     *      })
     *      .observeOn(AndroidSchedulers.mainThread())
     * }
     * </pre>
     *
     *  If you're using Kotlin, ex:
     *  <pre>
     *  {@code
     *      observableFrom<OddView> { RestServicesProvider.instance.apiCaller.getView(id, it) }
     *            .observeOn(AndroidSchedulers.mainThread())
     *  }
     *  </pre>
     *
     * @param f an action taking an OddCallback
     * @param <T> the type of the object that the OddCallback would be called with.
     * @return an observable that will emit 1 error, or 1 item and then be completed.
     */
    @NonNull public static <T> Observable<T> observableFrom(@NonNull Action1<OddCallback<T>> f) {
        return Observable.create(new RxOddCall<T>(f));
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        f.call(new OddCallback<T>() {
            @Override
            public void onSuccess(T item) {
                if(subscriber != null) {
                    subscriber.onNext(item);
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if(subscriber != null) {
                    try {
                        subscriber.onError(e);
                    } catch (Throwable err) {
                        Log.e(TAG, "call() failed to call subscriber.onError() with: " + err);
                        subscriber.onError(new IllegalStateException("This should never happen", err));
                    }
                    subscriber.onCompleted();
                }
            }
        });
    }
}
