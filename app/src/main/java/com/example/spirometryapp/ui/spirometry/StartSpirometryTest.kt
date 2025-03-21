package com.example.spirometryapp.ui.spirometry

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R

class StartSpirometryTest : AppCompatActivity() {

    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var ball: View
    private lateinit var timerLabel: TextView

    private var blowTime = 5000L // Maximum blow time (5 seconds)
    private var isBlowing = false
    private var countDownTimer: CountDownTimer? = null
    private var ballAnimator: ValueAnimator? = null
    private var startTime: Long = 0L // Track when the blowing started

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_spirometry_test)

        // Initialize UI components
        submitButton = findViewById(R.id.btnSubmit)
        backButton = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        ball = findViewById(R.id.blowBall)
        timerLabel = findViewById(R.id.timerLabel)

        progressBar.max = 100

        // Back button logic
        backButton.setOnClickListener { finish() }

        // Ball touch listener to start/stop blowing
        ball.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startBlowAnimation()
                MotionEvent.ACTION_UP -> stopBlowing()
            }
            true
        }

        // Submit button logic
        submitButton.setOnClickListener {
            val intent = Intent(this, SpirometryTestRecord::class.java)
            startActivity(intent)
        }
    }

    private fun startBlowAnimation() {
        if (isBlowing) return

        isBlowing = true
        startTime = System.currentTimeMillis() // Start tracking time

        // Ball animation moves dynamically
        ballAnimator = ValueAnimator.ofFloat(0f, -400f) // Move up by 400px
        ballAnimator?.duration = blowTime
        ballAnimator?.interpolator = AccelerateDecelerateInterpolator()
        ballAnimator?.addUpdateListener { animation ->
            ball.translationY = animation.animatedValue as Float
        }
        ballAnimator?.start()

        // Countdown Timer to update the UI
        countDownTimer = object : CountDownTimer(blowTime, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000).toInt()
                val milliseconds = ((elapsedTime % 1000) / 10).toInt()
                val formattedTime = String.format("%02d:%02d", seconds, milliseconds)

                runOnUiThread {
                    timerLabel.text = formattedTime
                    progressBar.progress = (elapsedTime * 100 / blowTime).toInt()
                }
            }

            override fun onFinish() {
                stopBlowing()
            }
        }.start()
    }

    private fun stopBlowing() {
        if (!isBlowing) return

        isBlowing = false
        countDownTimer?.cancel()
        ballAnimator?.cancel()

        val totalBlowTime = System.currentTimeMillis() - startTime
        val finalSeconds = (totalBlowTime / 1000).toInt()
        val finalMilliseconds = ((totalBlowTime % 1000) / 10).toInt()
        val finalTime = String.format("%02d:%02d", finalSeconds, finalMilliseconds)

        // Update UI with final values
        runOnUiThread {
            timerLabel.text = finalTime
            progressBar.progress = (totalBlowTime * 100 / blowTime).toInt()
        }

        // Reset ball position
        resetBallPosition()
    }

    private fun resetBallPosition() {
        val ballResetAnimator = ValueAnimator.ofFloat(ball.translationY, 0f)
        ballResetAnimator.duration = 800
        ballResetAnimator.interpolator = AccelerateDecelerateInterpolator()
        ballResetAnimator.addUpdateListener { animation ->
            ball.translationY = animation.animatedValue as Float
        }
        ballResetAnimator.start()

        progressBar.progress = 0
    }
}
