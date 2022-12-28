package com.example.trickortreat

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class LobbyMainActivity : AppCompatActivity() {

	var bgmOnOff = true

	var btnOption: Button? = null
	var btnRanking: Button? = null
	var mediaPlayer: MediaPlayer? = null
	var sharedPreferences2: SharedPreferences? = null

	var btnMode: ToggleButton? = null
	var rdoGroup: RadioGroup? = null
	var rdoEasy: RadioButton? =  null
	var rdoNormal: RadioButton? =  null
	var rdoHard: RadioButton? =  null

	var btnHelp: Button? = null
	var socket: Socket? = null
	var rct: ReceiverTh? = null
	var record = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_lobby_main)
		sharedPreferences2 = getSharedPreferences("bgm", 0)
		var bgm = sharedPreferences2!!.getBoolean("onOff",true)
		if(bgm) {
			mediaPlayer = MediaPlayer.create(this, R.raw.music)
			mediaPlayer!!.setLooping(true) //무한재생
			mediaPlayer!!.start()
		}

		rct = ReceiverTh()
		rct!!.start()

		btnOption = findViewById(R.id.btnOption)
		btnOption!!.setOnClickListener {
			val dialog = OptionFragment()
			dialog.show(supportFragmentManager, "OptionFragment")
		}

		btnHelp = findViewById(R.id.btnHelp)
		btnHelp!!.setOnClickListener {
			val dialog2 = HelpFragment()
			dialog2.show(supportFragmentManager, "HelpFragment")
		}

		val sharedPreference = getSharedPreferences("memInfo", 0)
		val nickname = sharedPreference.getString("nickname", "")

		btnRanking = findViewById(R.id.btnRanking)
		btnRanking!!.setOnClickListener {
			object : Thread() {
				override fun run() {
					rct!!.dos!!.writeUTF("rankingView/$nickname")
				}
			}.start()
		}

		rdoGroup = findViewById(R.id.rdoGroup)
		rdoEasy = findViewById(R.id.rdoEasy)
		rdoNormal = findViewById(R.id.rdoNormal)
		rdoHard = findViewById(R.id.rdoHard)
		btnMode = findViewById(R.id.btnMode)
		val btnStart = findViewById<Button>(R.id.btnStart)
		btnStart!!.setOnClickListener {
			var level = ""
			when(rdoGroup!!.checkedRadioButtonId) {
				rdoEasy!!.id -> level = "Easy"
				rdoNormal!!.id -> level = "Normal"
				rdoHard!!.id -> level = "Hard"
			}
			Log.d("level 여", level)
			val builder = AlertDialog.Builder(this)
			var msg = ""
			if (btnMode!!.isChecked) {
				msg = "랭킹모드"
			} else {
				msg = "일반모드"
			}
			builder.setTitle("Message").setMessage("${msg}로 진행하시겠습니까?")
				.setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
				.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
					val intent = Intent(applicationContext, GameMainActivity::class.java)
					intent.putExtra("mode", btnMode!!.isChecked)
					intent.putExtra("level", level)
					startActivity(intent)
				})
			builder.create()
			builder.show()
		}
	}

	override fun onStop() {
		super.onStop()
		mediaPlayer?.pause()
	}

	override fun onRestart() {
		super.onRestart()
		Log.d("onoff 여","$bgmOnOff")
		if(bgmOnOff) {
			mediaPlayer?.start()
		}else{
			mediaPlayer?.pause()
		}
	}

	override fun onResume() {
		super.onResume()
		if (btnMode!!.isChecked) {
			rdoGroup!!.visibility = View.INVISIBLE
		} else {
			rdoGroup!!.visibility = View.VISIBLE
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		mediaPlayer?.release()
		mediaPlayer = null
	}

	inner class ReceiverTh: Thread() {
		var dis: DataInputStream? = null
		var dos: DataOutputStream? = null

		override fun run() {
			socket = Socket("192.168.219.107", 7777)
			dis = DataInputStream(socket!!.getInputStream())
			dos = DataOutputStream(socket!!.getOutputStream())

			try {
				while (dis != null) {
					record = dis!!.readUTF()
					runOnUiThread {
						val intent = Intent(applicationContext, RankingActivity::class.java)
						intent.putExtra("record", record)
						startActivity(intent)
					}
				}
			} catch (e:Exception) {
				e.printStackTrace()
			} finally {
				close()
			}
		}

		fun close() {
			try {
				dis!!.close()
				dos!!.close()
				socket!!.close()
				dos = null
				dis = null
				rct = null
			} catch (e:Exception) {
				e.printStackTrace()
			}

		}
	}
	fun viewGo(v: View) {
		if (btnMode!!.isChecked) {
			rdoGroup!!.visibility = View.INVISIBLE
		} else {
			rdoGroup!!.visibility = View.VISIBLE
		}
	}
}