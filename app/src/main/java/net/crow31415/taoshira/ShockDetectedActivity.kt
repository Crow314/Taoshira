package net.crow31415.taoshira

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale


class ShockDetectedActivity : AppCompatActivity(), LocationListener {
    val TAG = ShockDetectService::class.java.simpleName
    val COUNT_TIME = 30 * 1000L // 30s

    private var mLocationManager: LocationManager? = null

    private lateinit var mVibrator: Vibrator
    private lateinit var mRingtone: Ringtone

    private lateinit var mLatitudeTextView: TextView
    private lateinit var mLongitudeTextView: TextView
    private lateinit var mAltitudeTextView: TextView

    private lateinit var mAlertCountTextView: TextView

    private var mAccuracy = Float.MAX_VALUE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shock_detected)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mVibrator = getSystemService(Vibrator::class.java)

        mLatitudeTextView = findViewById(R.id.gpsLatitudeTextView)
        mLongitudeTextView = findViewById(R.id.gpsLongitudeTextView)
        mAltitudeTextView = findViewById(R.id.gpsAltitudeTextView)

        mAlertCountTextView = findViewById(R.id.alertCountText)

        val cancelButton: Button = findViewById(R.id.cancelBotton)
        cancelButton.setOnClickListener {
            val serviceIntent = Intent(this, ShockDetectService::class.java)
            startForegroundService(serviceIntent)

            finish()
        }

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //スリープ復帰処理
        val window = window
        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //通知音設定
        mRingtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        )

        //バイブ鳴動
        val timings = longArrayOf(1000, 1500)
        val vibrationEffect = VibrationEffect.createWaveform(timings, 0)
        mVibrator.vibrate(vibrationEffect)

        val timer = object : CountDownTimer(COUNT_TIME, 100) {
            override fun onFinish() {
                sendNotification()
                finish()
            }

            override fun onTick(millisUntilFinished: Long) {
                val ss = millisUntilFinished / 1000 % 60
                var ms = millisUntilFinished - ss * 1000
                ms = ms / 100 //表示桁数減らし

                mAlertCountTextView.setText(String.format(Locale.JAPAN, "%2d.%d", ss, ms))

                if (ms in 0..1) {
                    mRingtone.play()
                }
            }
        }
        timer.start()

        Log.d(TAG, "Countdown start")

        if (checkPermissionDenied()) {
            return
        }

        mLocationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            this
        )

        mLocationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "plog onLocationChanged()")

        // 精度が10倍以下に悪化する場合は無視
        if (location.accuracy / 10 > mAccuracy) {
            return
        }

        mLatitudeTextView.text = location.latitude.toString()
        mLongitudeTextView.text = location.longitude.toString()
        mAltitudeTextView.text = location.altitude.toString()
        mAccuracy = location.accuracy
    }

    override fun finish() {
        mLocationManager!!.removeUpdates(this)
        mVibrator.cancel()

        super.finish()
    }

    private fun sendNotification() {
        // TODO
    }

    private fun checkPermissionDenied(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }
}
