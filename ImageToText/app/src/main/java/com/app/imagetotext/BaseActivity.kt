package com.app.imagetotext

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import java.util.*

open class BaseActivity : AppCompatActivity() {

    fun loadAds() {
        
        MobileAds.initialize(
            this
        ) {

        }


        findViewById<AdView>(R.id.adView).adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                findViewById<AdView>(R.id.adView).setVisibility(View.VISIBLE)
                Log.d("AdListener", "onAdLoaded")
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            /* override fun onAdFailedToLoad(errorCode: Int) {
                 Log.d("AdListener", "onAdFailedToLoad")
             }

             override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                 val error = "domain: ${loadAdError.domain}, code: ${loadAdError.code}, " +
                         "message: ${loadAdError.message}"
                 Log.d("AdListener", "onAdFailedToLoad " + error)
             }

             override fun onAdLeftApplication() {
                 super.onAdLeftApplication()
             }

             override fun onAdOpened() {
                 Log.d("AdListener", "add clicked")
             }

             override fun onAdLoaded() {
                 super.onAdLoaded()
                 findViewById<AdView>(R.id.adView).setVisibility(View.VISIBLE)
                 Log.d("AdListener", "onAdLoaded")
             }*/
        }

        val adRequest = AdRequest.Builder()
            /* .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
             .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")*/
            .build()
        findViewById<AdView>(R.id.adView).loadAd(adRequest)
    }
}