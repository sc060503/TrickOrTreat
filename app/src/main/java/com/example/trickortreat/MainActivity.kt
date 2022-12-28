package com.example.trickortreat

import android.Manifest
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
	var btnJoin:Button? = null
	var btnLogin:Button? = null
	var btnLogout:Button? = null
	var btnLobby:Button? = null
	var txtFind:TextView? = null
	var isLogin = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_main)
		requestPermission()

		txtFind = findViewById(R.id.txtFind)
		txtFind!!.paintFlags = Paint.UNDERLINE_TEXT_FLAG
		txtFind!!.setOnClickListener {
			val intent = Intent(applicationContext, FindActivity::class.java)
			startActivity(intent)
		}
		btnJoin = findViewById<Button>(R.id.btnJoin)
		btnJoin!!.setOnClickListener {
			val intent = Intent(applicationContext, JoinActivity::class.java)
			startActivity(intent)
		}
		btnLogin = findViewById<Button>(R.id.btnLogin)
		btnLogin!!.setOnClickListener {
			val intent = Intent(applicationContext, LoginActivity::class.java)
			startActivity(intent)
		}
		btnLobby = findViewById<Button>(R.id.btnLobby)
		btnLobby!!.setOnClickListener {
			val intent = Intent(applicationContext, LobbyMainActivity::class.java)
			startActivity(intent)
		}
		btnLogout = findViewById<Button>(R.id.btnLogout)
		val sharedPreference = getSharedPreferences("memInfo", 0)
		val uid = sharedPreference.getString("id", "")
		btnLogout!!.setOnClickListener {
			val edt = sharedPreference.edit()
			edt.clear()
			edt.apply()
			isLogin = false
			btnClear()
		}
		isLogin = !uid.equals("")
		btnClear()
	}


	fun requestPermission(){
		ActivityCompat.requestPermissions(
			this,
			arrayOf(Manifest.permission.CAMERA),
			1234
		)
	}

	override fun onResume() {
		super.onResume()
		val sharedPreference = getSharedPreferences("memInfo", 0)
		val uid = sharedPreference.getString("id", "")

		isLogin = !uid.equals("")
		btnClear()
	}

	fun btnClear() {
		if (isLogin) {
			btnJoin!!.visibility = View.INVISIBLE
			btnLogin!!.visibility = View.INVISIBLE
			txtFind!!.visibility = View.INVISIBLE
			btnLogout!!.visibility = View.VISIBLE
			btnLobby!!.visibility = View.VISIBLE
		} else {
			btnJoin!!.visibility = View.VISIBLE
			btnLogin!!.visibility = View.VISIBLE
			txtFind!!.visibility = View.VISIBLE
			btnLogout!!.visibility = View.INVISIBLE
			btnLobby!!.visibility = View.INVISIBLE
		}
	}
}