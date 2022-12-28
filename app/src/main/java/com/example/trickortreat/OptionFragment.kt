package com.example.trickortreat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment

class OptionFragment : DialogFragment() {
	var lobbyMain:LobbyMainActivity? = null
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment


		// 레이아웃 배경을 투명하게 해줌, 필수 아님
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		dialog?.setCancelable(false)

		lobbyMain = activity as LobbyMainActivity

		var res = inflater.inflate(R.layout.fragment_option, container, false)

		var on = res.findViewById<Button>(R.id.btnBgmOn)
		var off = res.findViewById<Button>(R.id.btnBgmOff)
		var edit = lobbyMain!!.sharedPreferences2!!.edit()

		on.setOnClickListener {
			lobbyMain!!.mediaPlayer?.start()
			lobbyMain!!.bgmOnOff = true
			edit.putBoolean("onOff",true)
			edit.apply()
			if (lobbyMain!!.mediaPlayer == null) {
				lobbyMain!!.mediaPlayer = MediaPlayer.create(lobbyMain, R.raw.music)
				lobbyMain!!.mediaPlayer!!.setLooping(true) //무한재생
				lobbyMain!!.mediaPlayer!!.start()
			}
		}
		off.setOnClickListener {
			lobbyMain!!.mediaPlayer?.pause()
			lobbyMain!!.bgmOnOff = false
			edit.putBoolean("onOff",false)
			edit.apply()
		}

		var btn = res.findViewById<Button>(R.id.btn)
		btn.setOnClickListener {
			dismiss()
		}

		return res
	}
}