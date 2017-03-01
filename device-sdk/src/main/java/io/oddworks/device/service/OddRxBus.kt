package io.oddworks.device.service

import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Bus implementation using RxJava
 *
 * @author Dan Pallas
 * *
 * @since v1.2 on 02/26/2016
 */
object OddRxBus {

    private val bus: SerializedSubject<OddRxBusEvent, OddRxBusEvent>

    init {
        bus = SerializedSubject(PublishSubject.create<OddRxBusEvent>())
        bus.onBackpressureBuffer()
    }


    @JvmStatic fun publish(event: OddRxBusEvent) {
        bus.onNext(event)
    }

    /** events will be emitted through this observable  */
    val observable: Observable<OddRxBusEvent>
        get() = bus.asObservable()

    /** for type safety and hinting  */
    interface OddRxBusEvent
}
