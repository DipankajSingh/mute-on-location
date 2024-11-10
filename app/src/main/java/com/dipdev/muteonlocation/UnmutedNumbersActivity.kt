package com.dipdev.muteonlocation

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class UnmutedNumbersActivity : AppCompatActivity() {

    private lateinit var contactPermissionButton: Button
    private lateinit var callLogPermissionButton: Button
    private lateinit var phoneStatePermissionButton: Button
    private val permissionButtons = mutableMapOf<Int, Pair<Button, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unmuted_numbers)

        // Set status bar and action bar properties
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        supportActionBar?.apply {
            title = "Muted Locations"
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffd8d8")))
            setDisplayHomeAsUpEnabled(true)
            setTitleColor(Color.BLACK)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }

        // Initialize views
        contactPermissionButton = findViewById(R.id.contactPermissionButton)
        callLogPermissionButton = findViewById(R.id.callLogPermissionButton)
        phoneStatePermissionButton = findViewById(R.id.phoneStatePermissionButton)

        // Set up permission buttons
        setupPermissionButton(
            CONTACT_PERMISSION_REQUEST_CODE,
            contactPermissionButton,
            android.Manifest.permission.READ_CONTACTS,
            "Enable Contact Permission",
            "Contact Permission Enabled"
        )

        setupPermissionButton(
            PHONE_STATE_PERMISSION_REQUEST_CODE,
            phoneStatePermissionButton,
            android.Manifest.permission.READ_PHONE_STATE,
            "Enable Phone State Permission",
            "Phone State Permission Enabled"
        )

        setupPermissionButton(
            CALL_LOG_PERMISSION_REQUEST_CODE,
            callLogPermissionButton,
            android.Manifest.permission.READ_CALL_LOG,
            "Enable Call Log Permission",
            "Call Log Permission Enabled"
        )
    }

    private fun ActionBar.setTitleColor(color: Int) {
        val text = SpannableString(title ?: "")
        text.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        title = text
    }

    private fun setupPermissionButton(requestCode: Int ,button: Button, permission: String, enableText: String, enabledText: String) {
        permissionButtons[requestCode] = button to enabledText

        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            button.text = enableText
            button.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.red)
            button.setOnClickListener {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        } else {
            updatePermissionButtonUI(button, enabledText)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionButtons[requestCode]?.let { (button, enabledText) ->
                updatePermissionButtonUI(button, enabledText)
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()

        }
    }

    private fun updatePermissionButtonUI(button: Button, enabledText: String) {
        button.isEnabled = false
        button.text = enabledText
        button.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.real_grey)
    }

    // Handle the back button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    companion object {
        const val CONTACT_PERMISSION_REQUEST_CODE = 101
        const val PHONE_STATE_PERMISSION_REQUEST_CODE = 102
        const val CALL_LOG_PERMISSION_REQUEST_CODE = 103
    }
}
