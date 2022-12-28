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

class FindActivity : AppCompatActivity() {
	var rct: ReceiverTh? = null
	var txtEmail3:EditText? = null
	var txtEmail4:EditText? = null
	var btnFindSubmit: Button? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_find)

		txtEmail3 = findViewById(R.id.txtEmail3)
		btnFindSubmit = findViewById(R.id.btnFindSubmit)

		rct = ReceiverTh()
		rct!!.start()

		btnFindSubmit!!.setOnClickListener {
			object : Thread() {
				override fun run() {
					rct!!.dos!!.writeUTF("find/${txtEmail3!!.text}")
				}
			}.start()
		}
	}

	inner class ReceiverTh:Thread() {
		var dos: DataOutputStream? = null
		var dis: DataInputStream? = null
		var socket: Socket? = null

		override fun run() {
			socket = Socket("192.168.150.139", 7777)
			dos = DataOutputStream(socket!!.getOutputStream())
			dis = DataInputStream(socket!!.getInputStream())

			try {
				while (dis != null) {
					var msg = dis!!.readUTF()
					if (!msg.equals("실패")) {
						msg = "이메일이 발송되었습니다."
					} else {
						msg = "일치하는 정보가 없습니다."
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