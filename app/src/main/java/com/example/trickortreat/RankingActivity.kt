package com.example.trickortreat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trickortreat.databinding.ActivityRankingBinding

class RankingActivity : AppCompatActivity() {
	var record: String? = null
	val Ranking: MutableList<Rank> = mutableListOf()
	var adapter1: CustomAdapter? = null
	var rcvRanking: RecyclerView? = null

	var txtMyRecord: TextView? = null
	val binding by lazy { ActivityRankingBinding.inflate(layoutInflater) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(binding.root)

		rcvRanking = findViewById(R.id.rcvRanking)
		txtMyRecord = findViewById(R.id.txtMyRecord)

		val intent = getIntent()
		record = intent.getStringExtra("record")
		loadData()
		adapter1 = CustomAdapter()
		adapter1!!.listData = Ranking
		binding.rcvRanking.adapter = adapter1
		binding.rcvRanking.layoutManager = LinearLayoutManager(this)
	}

	fun loadData() {
		var str = record!!.split("`")
		var ranking = str[0].split("|")
		for (i in 0 until ranking.size) {
			var str2 = ranking[i].split("/")
			val rank = Rank(i+1, str2[0], str2[1].toInt())
			Ranking.add(rank)
		}
		var myRecord = str[1].split("/")
		if (myRecord.size > 1) {
			var record = myRecord[1].toInt()
			var second = if(record / 1000 % 60 < 10) {
				"0${record / 1000 % 60}"
			} else {
				"${record / 1000 % 60}"
			}
			var millisecond = if(record % 1000 / 10 < 10) {
				"0${record % 1000 / 10}"
			} else {
				"${record % 1000 / 10}"
			}
			runOnUiThread {
				txtMyRecord!!.text = "내 최고기록 : ${second}.${millisecond}초"
			}
		} else {
			runOnUiThread {
				txtMyRecord!!.text = "내 최고기록 : 기록없음"
			}
		}
	}
}