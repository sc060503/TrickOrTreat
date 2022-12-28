package com.example.trickortreat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class HelpFragment : DialogFragment() {

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment

		// 레이아웃 배경을 투명하게 해줌, 필수 아님
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		dialog?.setCancelable(false)

		var res = inflater.inflate(R.layout.fragment_help, container, false)

		var btn = res.findViewById<Button>(R.id.btn)
		btn.setOnClickListener {
			dismiss()
		}

		return res
	}
}