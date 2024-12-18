package com.ubx.rfid_demo.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.utils.ToastUtil;
import com.ubx.usdk.bean.CustomRegionBean;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.rfid.aidl.RfidDate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SettingFragment.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();

    private Button btnGetPower, btnSetPower, btnCfFrency, btnGetFrency, btnCfMask, btnGetMask, btnSetRange, btnGetRange;
    static Button btnGetDeviceID;
    private EditText etSetPower, edtStartFrency, edtFrencyStep, edtFrencyNumber, edtMaskArea, edtMaskStartAddr, edtMaskLen, edtMaskData, edtRange;
    static EditText edtDeviceID;
    private TextView tvGetPower, deviceCode;
    private Spinner maskAreaSpinner, spinnerMaskData;
    private List<String> maskData = new ArrayList<>();
    static List<String> staticClients;
    static List<String> staticClientCodes;
    static HashMap<String, String> staticProductsInfo;
    static String deviceCodeText = "";
    static String deviceCodeButtonText = "";


    public static SettingFragment newInstance(MainActivity activity) {
        mActivity = activity;
        deviceCodeButtonText = "완료";
        deviceCodeText = "CODE";
        return new SettingFragment();
    }

    private Callback callback = new Callback();
    private static MainActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnGetPower = view.findViewById(R.id.btn_getPower);
        btnSetPower = view.findViewById(R.id.btn_setPower);
        etSetPower = view.findViewById(R.id.et_setPower);
        tvGetPower = view.findViewById(R.id.tv_getPower);
        btnGetDeviceID = view.findViewById(R.id.btn_deviceID);
        edtDeviceID = view.findViewById(R.id.et_deviceID);
        deviceCode = view.findViewById(R.id.txt_contents_value);

        /**
         edtStartFrency = view.findViewById(R.id.edt_startFrency);
         edtFrencyStep = view.findViewById(R.id.edt_frencyStep);
         edtFrencyNumber = view.findViewById(R.id.edt_frencyNumber);
         btnCfFrency = view.findViewById(R.id.btn_configFrency);
         btnGetFrency = view.findViewById(R.id.btn_getFrency);

         maskAreaSpinner = view.findViewById(R.id.spinner_mask_area);
         edtMaskStartAddr = view.findViewById(R.id.edt_maskStartAddr);
         edtMaskLen = view.findViewById(R.id.edt_maskLength);
         edtMaskData = view.findViewById(R.id.edt_maskData);
         btnCfMask = view.findViewById(R.id.btn_configMask);
         btnGetMask = view.findViewById(R.id.btn_getMask);

         edtRange = view.findViewById(R.id.edt_rangeValue);
         btnGetRange = view.findViewById(R.id.btn_getRange);
         btnSetRange = view.findViewById(R.id.btn_setRange);
         spinnerMaskData = view.findViewById(R.id.spinner_mask_data);
         spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, maskData);
         spinnerMaskData.setAdapter(spinnerAdapter);
         **/

        btnGetDeviceID = view.findViewById(R.id.btn_deviceID);

        settingButtonActive(view);
        initEvents();
        //onClick(view);
    }

    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }


    // 기기 확정 버튼 동작 함수
    private void settingButtonActive(View view) {

        btnGetDeviceID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("버튼 텍스트 완료 확인 : " + btnGetDeviceID.getText());

                // 스캔 시 같이 보여지게 될 기기 ID를 세팅
                ScanListAdapterRv.inputScanDeviceCode = edtDeviceID.getText().toString();
                TagScanFragmentNew.setDeviceId = edtDeviceID.getText().toString();

                List<HashMap<String, String>> clientList = new ArrayList<>();
                HashMap<String, String> clientMap = new HashMap<>();

                // HttpUrlConnection
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (deviceCodeButtonText.equals("완료")) {
                            try {

                                Request request = new Request.Builder()
                                        .url("http://localhost:8090/cl/device/supplier?dc=" + edtDeviceID.getText())
                                        .get()
                                        .build();

                                // 비동기 방식으로 요청 전송
                                client.newCall(request).enqueue(new okhttp3.Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (!response.isSuccessful()) {
                                            throw new IOException("Unexpected code " + response);
                                        }

                                        System.out.println("네트워크 Response : " + response.networkResponse());
                                        System.out.println("기기 확정 후 받은 Supplier 헤더 : " + response.header("supplier"));
                                        System.out.println("기기 확정 후 받은 Cookie 헤더 : " + response.header("Set-Cookie"));

                                        List<String> clients = new ArrayList<>();
                                        clients.add("==== 고객사 ====");

                                        List<String> clientCodes = new ArrayList<>();
                                        clientCodes.add("CODE");

                                        HashMap<String, String> productInfo = new HashMap<>();

                                        SharedPreferences rfidSharedPreference = getContext().getSharedPreferences("RfidAccessPreference", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor edit = rfidSharedPreference.edit();

                                        // 수신된 JSON 데이터 디버그 로그로 출력
                                        try {
                                            //System.out.println("반환 데이터 : " + response.body().string());
                                            String responseData = response.body().string();
                                            JSONObject jsonResponseData = new JSONObject(responseData);

                                            String supplierCode = jsonResponseData.getString("supplierCode");
                                            Long supplierId = jsonResponseData.getLong("supplierId");
                                            String supplierName = jsonResponseData.getString("supplierName");

                                            edit.putString("1", supplierId + "-" + response.header("device") + "-" + response.header("supplier"));
                                            edit.putString("device_code", edtDeviceID.getText().toString());
                                            edit.apply();
                                            edit.commit();

                                            JSONArray jsonClients = jsonResponseData.getJSONArray("clients");
                                            JSONArray jsonProducts = jsonResponseData.getJSONArray("productsInfo");

                                            for (int i = 0; i < jsonClients.length(); i++) {
                                                JSONObject eachClient = jsonClients.getJSONObject(i);

                                                String clientName = eachClient.getString("companyName");
                                                String clientCode = eachClient.getString("classificationCode");

                                                clients.add(clientName);
                                                clientCodes.add(clientCode);

                                                clientMap.put(clientCode, clientName);
                                                clientList.add(clientMap);
                                            }

                                            TagScanFragmentNew.supplierCode = supplierCode;
                                            TagScanFragmentNew.expressSupplierName.setText(supplierName);

                                            for (int j = 0; j < jsonProducts.length(); j++) {
                                                JSONObject eachProduct = jsonProducts.getJSONObject(j);

                                                String productCode = eachProduct.getString("productCode");
                                                String productName = eachProduct.getString("productName");

                                                productInfo.put(productCode, productName);
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        Message msg = handler.obtainMessage();
                                        handler.sendMessage(msg);

                                        staticClients = clients;
                                        staticClientCodes = clientCodes;
                                        staticProductsInfo = productInfo;
                                    }
                                });

                            } catch (Exception e) {
                                Log.i("tag", "error :" + e);
                            }


                        } else if (deviceCodeButtonText.equals("수정")) {
                            System.out.println("버튼 텍스트 수정 확인 : " + btnGetDeviceID.getText());

                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                        }

                    }

                });

                th.start();

            }
        });

    }

    // 메인 스레드를 재차 사용하기 위한 핸들러
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (deviceCodeButtonText.equals("완료")) {
                deviceCodeText = edtDeviceID.getText().toString();
                edtDeviceID.setText(deviceCodeText);
                edtDeviceID.setEnabled(false);

                deviceCodeButtonText = "수정";
                btnGetDeviceID.setText(deviceCodeButtonText);

                LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                btnGetDeviceID.setLayoutParams(buttonParam);

            } else if (deviceCodeButtonText.equals("수정")) {
                System.out.println("버튼 텍스트 수정 확인 : " + btnGetDeviceID.getText());

                edtDeviceID.setEnabled(true);
                btnGetDeviceID.setText("완료");

                deviceCodeButtonText = "완료";

                LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                btnGetDeviceID.setLayoutParams(buttonParam);

            }
        }
    };


    private void initEvents() {
        btnGetPower.setOnClickListener(this);
        btnSetPower.setOnClickListener(this);
        /**
         btnGetDeviceID.setOnClickListener(this);
         btnCfFrency.setOnClickListener(this);
         btnGetFrency.setOnClickListener(this);
         btnCfMask.setOnClickListener(this);
         btnGetMask.setOnClickListener(this);
         btnSetRange.setOnClickListener(this);
         btnGetRange.setOnClickListener(this);
         **/
    }

    private void btnGetPower() {
        setCallback();
        int outputPower = mActivity.mRfidManager.getOutputPower();
        tvGetPower.setText(getString(R.string.current_power) + outputPower);
    }

    // 스캔 범위 제어
    private void btnSetPower() {
        setCallback();
        String str = etSetPower.getText().toString().trim();

        if (TextUtils.isEmpty(str)) {
            toast(getString(R.string.content_not_null));
            return;
        }

        int power = Integer.parseInt(str);

        if (power < 0 || power > 33) {
            toast(getString(R.string.power_value_not_allow));
            return;
        }

        int ret = mActivity.mRfidManager.setOutputPower((byte) power);

        if (ret != 0) {
            toast(getString(R.string.set_power_fail));
        } else {
            SharedPreferences rfid_demo = getContext().getSharedPreferences("rfid_demo", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = rfid_demo.edit();
            edit.putInt("power", power);
            edit.apply();
        }
    }

    // setting 에서 기기 ID 확정
    private void btnGetDeviceID() {
        //String deviceId = mActivity.mRfidManager.getDeviceId();
        //edtDeviceID.setText(deviceId+"");
        deviceCode.setText(edtDeviceID.getText());

    }


    private void btnCfFrency() {
        String StartFrency = edtStartFrency.getText().toString();
        String step = edtFrencyStep.getText().toString();
        String number = edtFrencyNumber.getText().toString();
        if (StartFrency.equals("") || step.equals("") || number.equals(""))
            return;
        int i = mActivity.mRfidManager.setCustomRegion((byte) 0, Integer.parseInt(step), Integer.parseInt(number), Integer.parseInt(StartFrency));
        if (i == 0) {
            toast(getContext().getString(R.string.set_success));
        } else {
            toast(getContext().getString(R.string.set_fail));
        }
    }


    private void btnGetFrency() {
        CustomRegionBean customRegion = mActivity.mRfidManager.getCustomRegion();
        RfidDate frequencyRegion = mActivity.mRfidManager.getFrequencyRegion();
        if (frequencyRegion != null) {
            Log.e(TAG, "onClick: start：" + frequencyRegion.btFrequencyStart + "end:" + frequencyRegion.btFrequencyEnd + "region:" + frequencyRegion.btRegion);
        }
        if (customRegion != null) {
            edtStartFrency.setText(customRegion.StartFre[0] + "");
            edtFrencyNumber.setText(customRegion.FreNum[0] + "");
            edtFrencyStep.setText(customRegion.FreSpace[0] + "");

        } else {
            toast(getContext().getString(R.string.get_fail));
        }
    }


    private void btnCfMask() {
        int area = maskAreaSpinner.getSelectedItemPosition() + 1;
        String startAddr = edtMaskStartAddr.getText().toString().trim();
        String len = edtMaskLen.getText().toString().trim();
        String data = edtMaskData.getText().toString().trim();
        if (startAddr.equals("") || data.equals("")) {
            return;
        }
        int i = mActivity.mRfidManager.addMask(area, Integer.parseInt(startAddr), data.length(), data);
        if (i == 0) {
            maskData.add(area + "," + startAddr + "," + data);
            toast(getContext().getString(R.string.set_success));
        } else {
            toast(getContext().getString(R.string.set_fail));
        }

    }


    private void btnGetMask() {
        int i = mActivity.mRfidManager.clearMask();
        if (i == 0) {
            edtMaskData.setText("");
            edtMaskStartAddr.setText("");
            edtMaskLen.setText("");
            maskData.clear();
            toast("clear successfully");
        } else {
            toast("clear failed");
        }

    }


    private void btnSetRange() {
        String range = edtRange.getText().toString();
        if (range.equals("")) {
            return;
        }
        int i = mActivity.mRfidManager.setRange(Integer.parseInt(range));
        if (i == 0) {
            toast(getContext().getString(R.string.set_success));
        } else {
            toast(getContext().getString(R.string.set_fail));
        }
    }


    private void btnGetRange() {


        int range1 = mActivity.mRfidManager.getRange();
        if (range1 != -1) {
            edtRange.setText(range1 + "");
        } else {
            toast(getContext().getString(R.string.get_fail));
        }
    }


    @Override
    public void onClick(View view) {

        if (mActivity.mRfidManager == null) {
            toast(getString(R.string.rfid_not_init));
            return;
        }

        if (view == btnGetPower) {
            btnGetPower();
        } else if (view == btnSetPower) {
            btnSetPower();
        } else if (view == btnCfFrency) {
            btnCfFrency();
        } else if (view == btnGetFrency) {
            btnGetFrency();
        } else if (view == btnCfMask) {
            btnCfMask();
        } else if (view == btnGetMask) {
            btnGetMask();
        } else if (view == btnSetRange) {
            btnSetRange();
        } else if (view == btnGetRange) {
            btnGetRange();
        }
        /**
         else if (view == btnGetDeviceID) {
         settingButtonActive(view);
         }
         **/

    }

    private class Callback implements IRfidCallback {


        @Override
        public void onInventoryTag(String EPC, String TID, String strRSSI) {

        }

        @Override
        public void onInventoryTagEnd() {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setCallback();
        }
    }

    private void setCallback() {
        if (mActivity.RFID_INIT_STATUS) {
            if (mActivity.mRfidManager != null) {
                if (callback == null) {
                    callback = new Callback();
                }
                mActivity.mRfidManager.registerCallback(callback);
            }
        }
    }

    private void toast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toast(message);
            }
        });
    }
}