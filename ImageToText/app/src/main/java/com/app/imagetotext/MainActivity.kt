package com.app.imagetotext

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : BaseActivity() {
    lateinit  var appUpdateManager : AppUpdateManager

    var type = -1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        title = getString(R.string.app_name)

        loadAds()


        checkforUpdate()


        MobileAds.initialize(this) {}


        loadInterstitialAd()



        //MediationTestSuite.launch(MainActivity@this);
    }

    var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() === InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbarForCompleteUpdate()
                } else if (state.installStatus() === InstallStatus.INSTALLED) {
                    if (appUpdateManager != null) {
                        appUpdateManager.unregisterListener(this)
                    }
                } else {
                }
            }
        }

    private fun checkforUpdate() {
         appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.registerListener(installStateUpdatedListener);
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

       // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                requestUpdate(appUpdateManager, appUpdateInfo)
            }else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            }
        }
    }

    private fun requestUpdate(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            AppUpdateType.FLEXIBLE,
            // The current activity making the update request.
            this,
            // Include a request code to later monitor this update request.
            101
        )
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == 101){
            if (requestCode != RESULT_OK){
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        val snackbar: Snackbar = Snackbar.make(
            findViewById(R.id.root),
            "New app is ready!",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Install") { view ->
            if (appUpdateManager != null) {
                appUpdateManager.completeUpdate()
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            snackbar.setActionTextColor(resources.getColor(R.color.colorPrimary, null))
        }else{
            snackbar.setActionTextColor(resources.getColor(R.color.colorPrimary))
        }
        snackbar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    fun pickFromGallery(view: View?) {
        if(type == -1){
            type = 1
            show()
        }else{
            type = -1
            ImagePicker.with(this)
                .galleryOnly()
                .crop()    //User can only select image from Gallery
                .start { resultCode, data ->
                    handleImage(resultCode, data)
                }
        }
    }

    fun takePhoto(view: View?) {
        if(type == -1){
            type = 2
            show()
        }else {
            type = -1
            ImagePicker.with(this)
                .cameraOnly()
                .crop()//User can only select image from Gallery
                .start { resultCode, data ->
                    handleImage(resultCode, data)
                }
        }
    }

    fun pickFromCamera(view: View?) {
        if(type == -1){
            type = 3
            show()
        }else {
            type = -1
            if (isCameraPermissionGranted()) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                requestTakePhotoPermissions()
            }

        }
    }



    private fun handleImage(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)

            //You can get File object from intent
            val file: File? = ImagePicker.getFile(data)

            //You can also get File Path from intent
            //val filePath:String? = ImagePicker.getFilePath(data)
            findViewById<View>(R.id.layoutButton).visibility = View.GONE
            findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {


                val imageText = readTextFromImage(file)
                launch(Dispatchers.Main) {
                    findViewById<View>(R.id.layoutButton).visibility = View.VISIBLE
                    findViewById<View>(R.id.progressBar).visibility = View.GONE

                    val intent = Intent(this@MainActivity, DisplayText::class.java)
                    intent.putExtra("imageText", imageText)
                    this@MainActivity.startActivity(intent)

                    //Toast.makeText(this@MainActivity, imageText, Toast.LENGTH_SHORT).show()
                }
            }.invokeOnCompletion {
                file?.delete()
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            // Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readTextFromImage(file: File?): String {
        val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()

        val imageFrame: Frame = Frame.Builder()
            .setBitmap(getBitmapFromFile(file)) // your image bitmap
            .build()

        var imageText = ""


        val textBlocks: SparseArray<TextBlock> = textRecognizer.detect(imageFrame)

        for (i in 0 until textBlocks.size()) {
            val textBlock: TextBlock = textBlocks[textBlocks.keyAt(i)]
            textBlock.getValue()?.let {
                imageText = imageText.plus(textBlock.getValue())
            }
        }



        return imageText;
    }


    /**
     * return bitmap from file
     */
    fun getBitmapFromFile(photoFile: File?): Bitmap? {
        try {
            photoFile?.let {
                var ins: InputStream = photoFile.inputStream()
                var photoByteArray = ins.readBytes()
                return BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
            }
        } catch (FileNotFoundException: FileNotFoundException) {

        }
        return null
    }

    private fun isCameraPermissionGranted(): Boolean {
        var result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        return result == PackageManager.PERMISSION_GRANTED
    }

    fun requestTakePhotoPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA
            ), 101
        );
    }

    /** checks user permission results */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (isPermissionGranted(grantResults)) {
                    // if permission granted, take the user to camera
                    pickFromCamera(null)
                }
            }
        }
    }

    fun isPermissionGranted(grantResults: IntArray): Boolean {
        var isPermissionGranted: Boolean = true
        if (grantResults.isNotEmpty()) {
            loop@ for (result in grantResults) {
                if (result === PackageManager.PERMISSION_DENIED) {
                    isPermissionGranted = false
                    break@loop
                }
            }
        }
        return isPermissionGranted
    }

    private final var TAG = "MCsample"
    private var mInterstitialAd: InterstitialAd? = null
    private fun loadInterstitialAd() {
        var adRequest = AdRequest.Builder().build()

        //add unit id from admob
        //InterstitialAd.load(this,"ca-app-pub-1786194561317410/9296633458", adRequest, object : InterstitialAdLoadCallback() {
        InterstitialAd.load(this,"ca-app-pub-1786194561317410/5015320040", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
                when (type) {
                    1 -> {
                        pickFromGallery(null)
                    }
                    2 -> {
                        takePhoto(null)
                    }
                    3 -> {
                        pickFromCamera(null)
                    }
                }
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd

                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        when (type) {
                            1 -> {
                                pickFromGallery(null)
                            }
                            2 -> {
                                takePhoto(null)
                            }
                            3 -> {
                                pickFromCamera(null)
                            }
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        Log.d(TAG, "Ad failed to show.")
                        when (type) {
                            1 -> {
                                pickFromGallery(null)
                            }
                            2 -> {
                                takePhoto(null)
                            }
                            3 -> {
                                pickFromCamera(null)
                            }
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }
            }
        })
    }

    fun show() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            loadInterstitialAd()
        } else {
            loadInterstitialAd()
            when (type) {
                1 -> {
                    pickFromGallery(null)
                }
                2 -> {
                    takePhoto(null)
                }
                3 -> {
                    pickFromCamera(null)
                }
            }
        }
    }
}