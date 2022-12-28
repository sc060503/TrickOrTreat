package com.example.trickortreat

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment



class PauseFragment : DialogFragment() {


	var gameMain:GameMainActivity? = null
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment


		// 레이아웃 배경을 투명하게 해줌, 필수 아님
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		dialog?.setCancelable(false)

		gameMain = activity as GameMainActivity

		var res = inflater.inflate(R.layout.fragment_pause, container, false)

		var btnResume = res.findViewById<Button>(R.id.btnResume)
		var btnReset = res.findViewById<Button>(R.id.btnReset)
		var btnGoLobby = res.findViewById<Button>(R.id.btnGoLobby)

		btnResume.setOnClickListener {
			dismiss()
			if(!gameMain!!.isFirstObjCreate) {
				gameMain!!.timefun()
			}
				gameMain!!.myGLRenderer?.pause = false
		}

		btnReset.setOnClickListener {
			gameMain!!.finish()
			val intent = Intent(gameMain!!.applicationContext, GameMainActivity::class.java)
			intent.putExtra("mode", gameMain!!.isRankingMode)
			intent.putExtra("level", gameMain!!.level)
			startActivity(intent)
		}

		btnGoLobby.setOnClickListener {
			dismiss()
			val intent = Intent(gameMain!!.applicationContext, LobbyMainActivity::class.java)
			startActivity(intent)
			gameMain!!.finish()
		}


		return res
	}
}