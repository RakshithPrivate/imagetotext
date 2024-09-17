package com.app.imagetotext

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import com.app.imagetotext.databinding.ActivityCameraBinding
import com.app.imagetotext.databinding.ContentCameraBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CameraActivity : BaseActivity() {

    private var _binding : ActivityCameraBinding? = null
    private val binding get() = _binding!!

    private var _contentCameraBinding : ContentCameraBinding? = null
    private val contentCameraBinding get() = _contentCameraBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCameraBinding.inflate(layoutInflater)
        _contentCameraBinding = ContentCameraBinding.bind(binding.contentCamera.root)

        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        title = getString(R.string.app_name)

         setUp(this)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val intent = Intent(this,DisplayText::class.java)
            intent.putExtra("imageText",contentCameraBinding.tvResult.text)
            startActivity(intent)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _contentCameraBinding = null
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
        contentCameraBinding.surfaceCameraPreview.holder.addCallback(object : SurfaceHolder.Callback {


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
                        mCameraSource.start(contentCameraBinding.surfaceCameraPreview.holder)
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

                    contentCameraBinding.tvResult.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }
                        contentCameraBinding.tvResult.text = stringBuilder.toString()
                    }
                }
            })

        }



}
