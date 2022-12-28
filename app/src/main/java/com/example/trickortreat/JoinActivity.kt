package com.example.trickortreat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class JoinActivity : AppCompatActivity() {
	var txtEmail1:EditText? = null
	var txtCode:EditText? = null
	var txtUid:EditText? = null
	var txtPwd:EditText? = null
	var txtNickname:EditText? = null
	var btnVerify:Button? = null
	var btnVerifySubmit:Button? = null
	var btnJoinSubmit:Button? = null
	var btnBack:Button? = null

	var socket: Socket? = null
	var rct:ReceiverTh? = null
	var code = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_join)

		txtEmail1 = findViewById(R.id.txtEmail1)
		txtCode = findViewById(R.id.txtCode)
		txtUid = findViewById(R.id.txtUid)
		txtPwd = findViewById(R.id.txtPwd)
		txtNickname = findViewById(R.id.txtNickname)
		btnVerify = findViewById(R.id.btnVerify)
		btnVerifySubmit = findViewById(R.id.btnVerifySubmit)
		btnJoinSubmit = findViewById(R.id.btnJoinSubmit)
		btnBack = findViewById(R.id.btnBack)
		var isVerify = false

		rct = ReceiverTh()
		rct!!.start()


		btnBack!!.setOnClickListener {
			val intent = Intent(applicationContext, MainActivity::class.java)
			startActivity(intent)
		}

		btnVerify!!.setOnClickListener {
			val message = if (code.equals("")) {
				object : Thread() {
					override fun run() {
						rct!!.dos!!.writeUTF("email/${txtEmail1!!.text}")
					}
				}.start()
				"인증번호를 전송하였습니다."
			} else {
				"인증번호가 이미 전송되었습니다."
			}
			sendMessageAlert(message)
		}

		btnVerifySubmit!!.setOnClickListener {
			val message = if (!txtCode!!.text.toString().equals(code) || code.equals("")){
				"인증번호가 올바르지 않습니다."
			} else {
				txtEmail1!!.isEnabled = false
				txtCode!!.isEnabled = false
				btnVerify!!.visibility = View.INVISIBLE
				btnVerifySubmit!!.visibility = View.INVISIBLE
				isVerify = true
				rct = ReceiverTh()
				rct!!.start()
				"이메일 인증이 완료되었습니다."
			}
			sendMessageAlert(message)
		}

		btnJoinSubmit!!.setOnClickListener {
			 if (isVerify) {
				object: Thread() {
					override fun run() {
						rct!!.dos!!.writeUTF("join/${txtUid!!.text}`${txtPwd!!.text}`${txtEmail1!!.text}`${txtNickname!!.text}")
					}
				}.start()
			} else {
				sendMessageAlert("이메일 인증을 먼저 해주세요.")
			 }
		}
	}

	inner class ReceiverTh:Thread() {
		var dis: DataInputStream? = null
		var dos: DataOutputStream? = null

		override fun run() {
			socket = Socket("192.168.219.107", 7777)
			dis = DataInputStream(socket!!.getInputStream())
			dos = DataOutputStream(socket!!.getOutputStream())
			val builder = AlertDialog.Builder(this@JoinActivity)

			try {
				while (dis != null) {
					val msg = dis!!.readUTF()
					when (msg) {
						"성공", "실패" -> {
							sendMessageAlert("회원가입 $msg", Intent(applicationContext, MainActivity::class.java))
						}
						else -> {
							code = msg
						}
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