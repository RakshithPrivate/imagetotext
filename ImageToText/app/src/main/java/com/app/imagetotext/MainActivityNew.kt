package com.app.imagetotext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.facebook.ads.AdSettings
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import com.google.android.ads.mediationtestsuite.MediationTestSuiteListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivityNew : AppCompatActivity() {
    private final var TAG = "MCsample"

    private var mInterstitialAd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // AdSettings.addTestDevice("cc6937dc-fe78-43d8-b819-da344c7bd4e4")
        //AdSettings.setTestMode(true)
        MobileAds.initialize(this) {}






        //MediationTestSuite.launch(MainActivity@this);

        //ca-app-pub-1786194561317410/9296633458
    }

    fun load(view: View) {
        var adRequest = AdRequest.Builder().build()

        //add unit id from admob
        //InterstitialAd.load(this,"ca-app-pub-1786194561317410/9296633458", adRequest, object : InterstitialAdLoadCallback() {
            InterstitialAd.load(this,"ca-app-pub-1786194561317410/5015320040", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd

                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        Log.d(TAG, "Ad failed to show.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }
            }
        })

    }
    fun show(view: View) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }
}