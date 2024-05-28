package net.crow31415.taoshira

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.pow
import kotlin.math.sqrt


class ShockDetectService : Service(), SensorEventListener {
    val TAG = ShockDetectService::class.java.simpleName

    // private val THRETHOLD = 30  // Product
    private val THRESHOLD = 20  // Staging

    private lateinit var mSensorManager: SensorManager

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Started measuring acceleration.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")

        // 通知設定
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name = "倒れたら知らせるんです"
        val channelId = "measurement_foreground"

        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, StopServiceReceiver::class.java).let {
            PendingIntent.getBroadcast(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("衝撃監視中")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(stopIntent)
            .build()

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(1, notification)
        }

        return START_STICKY
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
            if (acceleration >= THRESHOLD) {
                Log.d(TAG, "acceleration: $acceleration")
                Log.i(TAG, "A shock was detected.")
                mSensorManager.unregisterListener(this)
                val intent = Intent(this, ShockDetectedActivity::class.java)
                    .setFlags(FLAG_ACTIVITY_SINGLE_TOP)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                stopSelf()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do not anything
    }
}
