package com.app.imagetotext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class DisplayText : BaseActivity() {
    lateinit var textToSpeech :TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_text)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        title = getString(R.string.app_name)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, "ImageToText")
            i.putExtra(Intent.EXTRA_TEXT, findViewById<TextView>(R.id.imageText).text)
            startActivity(Intent.createChooser(i, "Share via"))
        }

        intent.getStringExtra("imageText").let {
            findViewById<TextView>(R.id.imageText).text = it
        }

        loadAds()

        textToSpeech = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.UK)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {


        val speech: MenuItem = menu.add(0, 2, 0, "speech")
        speech.setIcon(R.mipmap.speech)
        speech.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val edit_item: MenuItem = menu.add(0, 1, 0, "Copy")
        edit_item.setIcon(R.mipmap.copy)
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            1 -> {
                copyToClipboard(this, findViewById<TextView>(R.id.imageText).text.toString());
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
                true
            }
            2 -> {
                val toSpeak: String = findViewById<TextView>(R.id.imageText).text.toString()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null,"1")
                }else{
                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
                }
                true
            }
            else -> false
        }
    }

    @SuppressLint("NewApi")
    fun copyToClipboard(context: Context, text: String?): Boolean {
        return try {
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = context
                    .getSystemService(CLIPBOARD_SERVICE) as android.text.ClipboardManager
                clipboard.text = text
            } else {
                val clipboard = context
                    .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData
                    .newPlainText(
                        "text", text
                    )
                clipboard.setPrimaryClip(clip)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}