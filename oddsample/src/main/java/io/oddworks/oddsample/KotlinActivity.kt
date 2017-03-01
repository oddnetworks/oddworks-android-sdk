package io.oddworks.oddsample

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.oddworks.device.model.OddCollection
import io.oddworks.device.model.OddConfig
import io.oddworks.device.model.OddView
import io.oddworks.device.model.common.OddResourceType
import io.oddworks.device.request.OddRequest
import io.oddworks.device.request.RxOddCall
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class KotlinActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContentView(R.layout.activity_main)

        initializeOddData()
    }

    private fun initializeOddData() {
        RxOddCall
                .observableFrom<OddConfig> {
                    OddRequest.Builder(this, OddResourceType.CONFIG)
                            .build()
                            .enqueueRequest(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val viewId = it.views.get("crtv-home-view")!!
                    initializeHomeView(viewId)
                },{
                    Log.e(KotlinActivity::class.java.simpleName, "config fetch failed", it)
                })
    }

    private fun initializeHomeView(viewId: String) {
        RxOddCall
                .observableFrom<OddView> {
                    OddRequest.Builder(this, OddResourceType.VIEW)
                            .resourceId(viewId)
                            .include("personalities,promotion")
                            .build()
                            .enqueueRequest(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(KotlinActivity::class.java.simpleName, "view fetch success")
                    val personalities = it.getIncludedByRelationship("personalities")
                    val personality = personalities.first() as OddCollection
                    Log.d(KotlinActivity::class.java.simpleName, "first personality: ${personality.title}")
                },{
                    Log.e(KotlinActivity::class.java.simpleName, "view fetch failed", it)
                })
    }
}
