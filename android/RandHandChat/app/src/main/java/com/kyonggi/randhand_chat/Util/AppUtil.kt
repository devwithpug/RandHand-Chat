package com.kyonggi.randhand_chat.Util

import android.app.Application
import android.content.Context

class AppUtil : Application() {
    companion object {
        lateinit var prefs: PrefsManager
    }

    override fun onCreate() {
        prefs = PrefsManager(applicationContext)
        super.onCreate()
    }
}
class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("pref_name", Context.MODE_PRIVATE)

    // 저장할 변수 설정
    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    // 값 가져오기
    fun getString(key: String, defValue: String?) : String {
        return prefs.getString(key, defValue).toString()
    }
    // 값 초기화
    fun clear() {
        prefs.edit().clear().apply()
    }
}