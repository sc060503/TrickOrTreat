package com.example.trickortreat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trickortreat.databinding.ItemRecyclerBinding

class CustomAdapter: RecyclerView.Adapter<Holder>() {
	var listData = mutableListOf<Rank>()
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
	// 아이템 레이아웃을 생성하는 메소드(한 화면에 보이는 개수만큼 안드로이드가 이 메소드를 호출함)
		val binding =
			ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		// 생성된 뷰홀더를 화면에 보여주는 메소드
		val rank = listData.get(position)
		holder.setRank(rank)
	}

	override fun getItemCount(): Int {
		// 데이터의 총 개수를 리턴하는 메소드
		return listData.size
	}
}

class Holder(val binding: ItemRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
	fun setRank(rank: Rank) {
		binding.txtNum.text = if (rank.no < 10) {
			"0${rank.no}"
		} else {
			"${rank.no}"
		}
		binding.txtNick.text = rank.nickname

		var second = if(rank.record / 1000 % 60 < 10) {
			"0${rank.record / 1000 % 60}"
		} else {
			"${rank.record / 1000 % 60}"
		}
		var millisecond = if(rank.record % 1000 / 10 < 10) {
			"0${rank.record % 1000 / 10}"
		} else {
			"${rank.record % 1000 / 10}"
		}
		binding.txtRecord.text = "${second}.${millisecond}초"
	}
}