package com.ubx.rfid_demo.pojo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceBase implements SharedPreferences {

    // 접속한 쿠키 유지를 위한 SharedPreference 호출 및 저장
    public void putSharedPreference(String connectSupplier, HashSet<String> cookies){
        SharedPreferences sharedPreferences = this.getSharedPreference("ACCESS_PREFERENCE", Context.MODE_MULTI_PROCESS);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 쿠키 저장
        editor.putStringSet(connectSupplier, cookies);
        editor.commit();
    }

    // 유지 중인 쿠키 호출
    /**
    public Set<String> getSharedPreference(String connectSupplier, HashSet<String> cookies){
        Set<String> accessCookie = new HashSet<>();

        if (!this.getStringSet(connectSupplier, cookies).isEmpty()) {
            accessCookie = this.getStringSet(connectSupplier, cookies);
        }
        //this.getStringSet(connectSupplier, cookies);

        return accessCookie;
    }
     **/

    public SharedPreferences getSharedPreference(String connectSupplier, int mode){


        return null;
    }

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return null;
    }

    @Override
    public int getInt(String s, int i) {
        return 0;
    }

    @Override
    public long getLong(String s, long l) {
        return 0;
    }

    @Override
    public float getFloat(String s, float v) {
        return 0;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }
}
