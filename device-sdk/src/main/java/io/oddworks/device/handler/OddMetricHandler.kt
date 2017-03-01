package io.oddworks.device.handler

import android.content.Context
import android.util.Log
import io.oddworks.device.exception.BadResponseCodeException
import io.oddworks.device.metric.OddMetric
import io.oddworks.device.model.common.OddResourceType
import io.oddworks.device.request.OddCallback
import io.oddworks.device.request.OddRequest
import io.oddworks.device.service.OddRxBus
import rx.schedulers.Schedulers

/**
 * A singleton that handles the HTTP POST calls for OddMetric events.
 *
 * This class must be explicitly enabled, otherwise it will not listen
 * for events posted to the OddRxBus.
 *
 * Once enabled, any OddMetric object that is picked up on the OddRxBus
 * will automatically be sent back to Oddworks via an OddRequest.
 */
object OddMetricHandler {

    private val TAG = OddMetricHandler::class.java.simpleName

    /**
     * Registers the instance of OddMetricHandler on the OddRxBus
     * so it can begin receiving posted event objects.
     */
    @JvmStatic fun enable(context: Context) {
        val observable = OddRxBus.observable
        val oddMetricCallback = object : OddCallback<OddMetric> {
            override fun onSuccess(resource: OddMetric) {
                Log.d(TAG, "handleOddMetric: SUCCESS $resource}")
            }

            override fun onFailure(exception: Throwable) {
                Log.d(TAG, "handleOddMetric: FAILURE $exception")
                if (exception is BadResponseCodeException) {
                    Log.d(TAG, "handleOddMetric code: ${exception.code} errors: ${exception.oddErrors}")
                }
            }
        }

        observable.observeOn(Schedulers.io()).subscribe { event ->
            if (event is OddMetric) {
                OddRequest.Builder(context, OddResourceType.EVENT)
                        .event(event)
                        .build()
                        .enqueueRequest(oddMetricCallback)
            }
        }
    }
}
