package com.example.trickortreat

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.DialogFragment
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket


class ResultFragment : DialogFragment() {

	var socket: Socket? = null
	var rct: ReceiverTh? = null
	var gameMain:GameMainActivity? = null
	var msg = ""

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		// 레이아웃 배경을 투명하게 해줌, 필수 아님
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		dialog?.setCancelable(false)

		gameMain = activity as GameMainActivity

		var res = inflater.inflate(R.layout.fragment_result, container, false)

		var btnReset2 = res.findViewById<Button>(R.id.btnReset2)
		var btnGoLobby2 = res.findViewById<Button>(R.id.btnGoLobby2)
		var txtScore = res.findViewById<TextView>(R.id.txtScore)
		var txtGameMode = res.findViewById<TextView>(R.id.txtGameMode)

		if (gameMain!!.isRankingMode) {
			txtGameMode.text = "랭킹모드"
		} else {
			txtGameMode.text = "일반모드"
		}

		if (gameMain!!.victory) {
			txtScore.text = "${gameMain!!.record.toFloat() / 1000} 초"
		} else {
			txtScore.text = "게임 오버"
		}

		btnReset2.setOnClickListener {
			gameMain!!.finish()
			val intent = Intent(gameMain!!.applicationContext, GameMainActivity::class.java)
			intent.putExtra("mode", gameMain!!.isRankingMode)
			intent.putExtra("level", gameMain!!.level)
			startActivity(intent)
		}

		btnGoLobby2.setOnClickListener {
			dismiss()
			val intent = Intent(gameMain!!.applicationContext, LobbyMainActivity::class.java)
			startActivity(intent)
			gameMain!!.finish()
		}

		if (gameMain!!.isRankingMode) {
			rct = ReceiverTh()
			rct!!.start()
		}

		var btn = res.findViewById<Button>(R.id.buttonI)
		if (gameMain!!.isRankingMode && gameMain!!.victory) {
			btn.visibility = View.VISIBLE
		} else {
			btn.visibility = View.INVISIBLE
		}
		btn.setOnClickListener {
			if (gameMain!!.isRankingMode) {
				object : Thread() {
					override fun run() {
						rct!!.dos!!.writeUTF("rankingInsert/${gameMain!!.nickname}`${gameMain!!.record}")
					}
				}.start()
			}
			if (msg.equals("성공")) {
				Toast.makeText(gameMain,"랭킹 등록 성공",Toast.LENGTH_SHORT).show()
			}
		}

		return res
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
					msg = dis!!.readUTF()
					Log.d("결과 여", msg)
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
}