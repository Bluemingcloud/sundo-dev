package com.example.liststart

import android.app.Application

class MyApplication :Application() {

    //sharedpreference vs application
    var a: Int? = null // test 용 변수
    fun ex(): Int? {
        return a
    }
    fun setEx(data: Int) {
        a = data
    }

    // 사업 목록 리스트 저장
    private var itemList: ArrayList<Item> = arrayListOf()

    // getter setter
    fun getItemList(): ArrayList<Item> {
        return itemList
    }
    fun setItemList(data: ArrayList<Item>) {
        itemList = data
    }

    // itemList에 새로운 아이템 추가
    fun addItem(item: Item) {
        itemList.add(item)
    }

    // 체크된 아이템 삭제
    fun deleteCheckedItems() {
        itemList = itemList.filter { !it.isChecked } as ArrayList<Item> // 체크되지 않은 아이템만 남기고 리스트 갱신
    }

    // 아이템 필터링
    fun filterItem(query: String): ArrayList<Item> {
        return if (query.isBlank()) {
            getItemList() // 검색어가 없으면 전체 리스트 반환
        } else {
            ArrayList(itemList.filter { it.title.contains(query, ignoreCase = true) }) // 검색된 항목만 반환
        }
    }

    // 모든 아이템의 체크 상태를 초기화
    fun resetCheckStatus() {
        itemList.forEach { it.isChecked = false }
    }
}