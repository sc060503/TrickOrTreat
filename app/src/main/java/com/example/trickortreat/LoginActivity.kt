package com.example.trickortreat

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class LoginActivity : AppCompatActivity() {
	var rct:ReceiverTh? = null
	var txtLoginUid:EditText? = null
	var txtLoginPwd:EditText? = null
	var btnLoginSubmit:Button? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_login)

		txtLoginUid = findViewById(R.id.txtLoginUid)
		txtLoginPwd = findViewById(R.id.txtLoginPwd)
		btnLoginSubmit = findViewById(R.id.btnLoginSubmit)

		rct = ReceiverTh()
		rct!!.start()

		btnLoginSubmit!!.setOnClickListener {
			object : Thread() {
				override fun run() {
					rct!!.dos!!.writeUTF("login/${txtLoginUid!!.text}`${txtLoginPwd!!.text}")
				}
			}.start()
		}
	}

	inner class ReceiverTh:Thread() {
		var dos: DataOutputStream? = null
		var dis: DataInputStream? = null
		var socket: Socket? = null

		override fun run() {
			socket = Socket("192.168.219.107", 7777)
			dos = DataOutputStream(socket!!.getOutputStream())
			dis = DataInputStream(socket!!.getInputStream())

			try {
				while (dis != null) {
					var msg = dis!!.readUTF()
					if (!msg.equals("로그인 실패")) {
						val sharedPreference = getSharedPreferences("memInfo", 0)
						val editor = sharedPreference.edit()
						val memInfo = msg.split("/")
						editor.putString("id", memInfo[0])
						editor.putString("pw", memInfo[1])
						editor.putString("email", memInfo[2])
						editor.putString("nickname", memInfo[3])
						editor.putString("status", memInfo[4])
						editor.putString("join", memInfo[5])
						editor.apply()
						msg = "로그인 성공"
					}
					sendMessageAlert(msg, Intent(applicationContext, MainActivity::class.java))
				}
			} catch (e:Exception) {
				e.printStackTrace()
			}
		}
	}

	fun sendMessageAlert(message: String, intent: Intent? = null) {
		val builder = AlertDialog.Builder(this)
		runOnUiThread {
			builder.setTitle("Message").setMessage(message).setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
				if (intent != null) {
					startActivity(intent)
				}
			})
			builder.create()
			builder.show()
		}
	}
}