package com.app.imagetotext

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.content_camera.*

class CameraActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        setSupportActionBar(findViewById(R.id.toolbar))

        title = getString(R.string.app_name)

         setUp(this)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val intent = Intent(this,DisplayText::class.java)
            intent.putExtra("imageText",tv_result.text)
            startActivity(intent)

        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val edit_item: MenuItem = menu.add(0, 1,0, "Share")
        edit_item.setIcon(android.R.drawable.ic_menu_share)
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            1 -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, "ImageToText")
                i.putExtra(Intent.EXTRA_TEXT, tv_result.text)
                startActivity(Intent.createChooser(i, "Share via"))
                true
            }
            else -> false
        }
    }*/

    private fun setUp(context : Context) {
            val textRecognizer = TextRecognizer.Builder(this).build()
            if (!textRecognizer.isOperational) {
                // toast("Dependencies are not loaded yet...please try after few moment!!")
                //Logger.d("Dependencies are downloading....try after few moment")
                return
            }
//  Init camera source to use high resolution and auto focus
            val mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()
            surface_camera_preview.holder.addCallback(object : SurfaceHolder.Callback {


                override fun surfaceCreated(p0: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        mCameraSource.start(surface_camera_preview.holder)
                    } catch (e: Exception) {

                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

                }

                override fun surfaceDestroyed(p0: SurfaceHolder) {
                    mCameraSource.stop()
                }
            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems

                    if (items.size() <= 0) {
                        return
                    }

                    tv_result.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }
                        tv_result.text = stringBuilder.toString()
                    }
                }
            })

        }



}
