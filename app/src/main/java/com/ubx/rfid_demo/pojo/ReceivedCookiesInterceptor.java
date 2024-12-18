package com.ubx.rfid_demo.pojo;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {

    public SharedPreferenceBase sharedPreferenceBase;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }

            // Preference에 cookies를 넣어주는 작업을 수행
            sharedPreferenceBase.putSharedPreference(originalResponse.header("supplier"), cookies);
        }

        return originalResponse;
    }

}
