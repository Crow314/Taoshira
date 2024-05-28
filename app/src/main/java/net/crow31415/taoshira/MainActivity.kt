package net.crow31415.taoshira

import android.app.ActivityManager
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mAccelerationTextView: TextView

    private lateinit var mSensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAccelerationTextView = findViewById(R.id.accelerationTextView)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI)

        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            val serviceIntent = Intent(this, ShockDetectService::class.java)
            startForegroundService(serviceIntent)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            //ベクトル合成
            val acceleration = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

            mAccelerationTextView.text = acceleration.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}