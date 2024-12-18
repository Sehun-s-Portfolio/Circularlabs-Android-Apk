package com.ubx.rfid_demo.ui.main;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.rfid_demo.BaseApplication;
import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagScan;
import com.ubx.rfid_demo.utils.ByteUtils;
import com.ubx.usdk.RFIDSDKManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.util.SoundTool;

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
 * Use the {@link TagScanFragmentNew#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagScanFragmentNew extends Fragment {
    public static final String TAG = "usdk-" + TagScanFragmentNew.class.getSimpleName();

    public List<TagScan> data;
    public HashMap<String, TagScan> mapData;
    private ScanCallback callback;
    private SendDialog sendDialog;
    private DiscardDialog discardDialog;
    static ScanListAdapterRv scanListAdapterRv;
    static MainActivity mActivity;
    private int tagTotal = 0;
    private int errorCount = 0;
    private HashMap<String, Integer> productCodes = new HashMap<>();
    private List<String> eachScanProducts = new ArrayList<>();
    public ListView listView;
    public SettingFragment settingFragment;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * 7
     *
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagScanFragmentNew newInstance(MainActivity activity) {
        mActivity = activity;
        TagScanFragmentNew fragment = new TagScanFragmentNew();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_scan, container, false);
    }

    public Button scanStartBtn, clearBtn, sendBtn, discardBtn;
    private RecyclerView scanListRv;
    static TextView scanCountText, scanTotalText;
    public TextView textFirmware;
    @SuppressLint("StaticFieldLeak")
    static TextView expressSupplierName;
    static String supplierCode;
    static String setDeviceId;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanStartBtn = view.findViewById(R.id.scan_start_btn);
        sendBtn = view.findViewById(R.id.btn_send);
        discardBtn = view.findViewById(R.id.btn_discard);
        clearBtn = view.findViewById(R.id.btn_clear);
        scanListRv = view.findViewById(R.id.scan_list_rv);
        scanCountText = view.findViewById(R.id.scan_count_text);
        //scanTotalText = view.findViewById(R.id.scan_total_text);
        //textFirmware = view.findViewById(R.id.text_firmware);
        expressSupplierName = view.findViewById(R.id.express_supplier_name);


        SharedPreferences sharedPref = getActivity().getSharedPreferences("RfidAccessPreference", Context.MODE_PRIVATE);

        String defaultValue = "NON_EXIST_PREFERENCE";

        if(!sharedPref.getAll().isEmpty()){

            String accessPreference =  sharedPref.getString("1", defaultValue);
            String deviceCodePreference = sharedPref.getString("device_code", defaultValue);

            System.out.println("자동 로그인 판별 데이터 존재1 : " + accessPreference);
            System.out.println("자동 로그인 판별 데이터 존재2 : " + deviceCodePreference);
            System.out.println(sharedPref);
            System.out.println("모든 데이터 : " + sharedPref.getAll());

            if (!accessPreference.equals(defaultValue) && accessPreference != null &&
                    !deviceCodePreference.equals(defaultValue) && deviceCodePreference != null) {
                String[] accessInfo = accessPreference.split("-");
                setDeviceId = accessInfo[1];

                List<HashMap<String, String>> clientList = new ArrayList<>();
                HashMap<String, String> clientMap = new HashMap<>();

                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Request request = new Request.Builder()
                                    .url("http://localhost:8090/cl/device/supplier?dc=" + accessInfo[1])
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

                                    List<String> clients = new ArrayList<>();
                                    clients.add("==== 고객사 ====");

                                    List<String> clientCodes = new ArrayList<>();
                                    clientCodes.add("CODE");

                                    HashMap<String, String> productInfo = new HashMap<>();

                                    // 수신된 JSON 데이터 디버그 로그로 출력
                                    try {
                                        //System.out.println("반환 데이터 : " + response.body().string());
                                        String responseData = response.body().string();
                                        JSONObject jsonResponseData = new JSONObject(responseData);

                                        String supplierCode = jsonResponseData.getString("supplierCode");
                                        Long supplierId = jsonResponseData.getLong("supplierId");
                                        String supplierName = jsonResponseData.getString("supplierName");

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

                                    SettingFragment.staticClients = clients;
                                    SettingFragment.staticClientCodes = clientCodes;
                                    SettingFragment.staticProductsInfo = productInfo;
                                    setDeviceId = accessInfo[1];

                                    SettingFragment.edtDeviceID.setText(deviceCodePreference);
                                    SettingFragment.edtDeviceID.setEnabled(false);
                                    SettingFragment.btnGetDeviceID.setText("수정");

                                    SettingFragment.deviceCodeText = deviceCodePreference;
                                    SettingFragment.deviceCodeButtonText = "수정";
                                }
                            });

                        } catch (Exception e) {
                            Log.i("tag", "error :" + e);
                        }
                    }
                });
                th.start();

            } else {
                SettingFragment.edtDeviceID.setEnabled(true);
                SettingFragment.btnGetDeviceID.setText("완료");

                settingFragment.deviceCodeButtonText = "완료";
            }
        }else{
            System.out.println("공유 환경설정에 데이터가 등록되지 않았을 경우 : " + sharedPref.getAll());
        }

        scanStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expressSupplierName.getText().equals("-") || expressSupplierName.getText() == null) {
                    Toast.makeText(mActivity, "기기 코드 및 공급사 코드를 확인해주십시오.", Toast.LENGTH_SHORT).show();
                } else {
                    if (mActivity.RFID_INIT_STATUS) {
                        System.out.println("스캔 버튼 실행 시 1단계 진입");

                        if (scanStartBtn.getText().equals(getString(R.string.btInventory))) {
                            System.out.println("스캔 버튼 실행 2단계 진입 - 텍스트가 start일 경우");

                            setCallback();
                            scanStartBtn.setText(getString(R.string.btn_stop_Inventory));
                            setScanStatus(true);
                        } else {
                            System.out.println("스캔 버튼 실행 2단계 진입 - 텍스트가 start가 아닐 경우");

                            scanStartBtn.setText(getString(R.string.btInventory));
                            setScanStatus(false);
                        }
                    } else {
                        Log.d(TAG, "scanStartBtn  RFID未初始化 ");
                        Toast.makeText(getActivity(), "RFID 이니셜라이즈 되지 않음..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 데이터 전송 버튼 동작
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (expressSupplierName.getText().equals("-") || expressSupplierName.getText() == null) {
                    Toast.makeText(getActivity(), "저장 작업 이전에 공급사 / 기기 확정 여부를 확인해주십시오.", Toast.LENGTH_SHORT).show();
                }
                /**
                else if (scanListRv.getChildCount() == 0) {
                    Toast.makeText(getActivity(), "스캔 데이터를 확인해주십시오.", Toast.LENGTH_SHORT).show();
                }
                 **/

                else {
                    openModal(view);
                }
            }
        });

        // 폐기 버튼 동작
        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expressSupplierName.getText().equals("-") || expressSupplierName.getText() == null) {
                    Toast.makeText(getActivity(), "폐기 처리 이전에 공급사 / 기기 확정 여부를 확인해주십시오.", Toast.LENGTH_SHORT).show();
                } else if (scanListRv.getChildCount() == 0) {
                    Toast.makeText(getActivity(), "스캔 데이터를 확인해주십시오.", Toast.LENGTH_SHORT).show();
                } else {
                    openDiscardModal(view);
                }
            }
        });

        // 스캔 초기화 버튼 동작
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tagTotal = 0;

                if (mapData != null) {
                    mapData.clear();
                }
                if (mActivity.mDataParents != null) {
                    mActivity.mDataParents.clear();
                }
                if (mActivity.tagScanSpinner != null) {
                    mActivity.tagScanSpinner.clear();
                }
                if (data != null) {
                    data.clear();
                    scanListAdapterRv.setData(data);
                }

                showView();

            }
        });

        mapData = new HashMap<>();

        scanListRv.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        scanListRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        scanListAdapterRv = new ScanListAdapterRv(null, getActivity());
        System.out.println("스캔 리스트 어댑터 RV - " + scanListAdapterRv);

        scanListRv.setAdapter(scanListAdapterRv);
        System.out.println("스캔 리스트 RV - " + scanListRv);

    }

    private final int MSG_UPDATE_UI = 0;
    private final int MSG_STOP_INVENTORY = 1;

    private final int MSG_UPDATE_SINGLE_INV = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_UI:
                    scanListAdapterRv.notifyDataSetChanged();
                    handlerUpdateUI();
                    break;
                case MSG_STOP_INVENTORY:
                    scanListAdapterRv.notifyDataSetChanged();
                    mHandler.removeCallbacksAndMessages(null);
                    if (scanStartBtn.getText().equals(mActivity.getString(R.string.btn_stop_Inventory))) {
                        scanStartBtn.setText(getContext().getString(R.string.btInventory));
                    }
                    break;

                case MSG_UPDATE_SINGLE_INV:
                    RFIDSDKManager.getInstance().getRfidManager().inventorySingle();
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SINGLE_INV, 200);
                    break;
            }

        }
    };

    // SETTING 반영 후 SEND 버튼 클릭 시 열리는 모달 창
    public void openModal(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendDialog = new SendDialog(getContext(), "기기 ID : ", scanCountText.getText().toString(), scanListAdapterRv.getData(), setDeviceId, supplierCode, mActivity);
                sendDialog.show();
                break;
        }
    }

    public void openDiscardModal(View view) {
        discardDialog = new DiscardDialog(getContext(), "기기 ID : ", scanCountText.getText().toString(), scanListAdapterRv.getData(), setDeviceId, supplierCode, mActivity);
        discardDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setScanStatus(boolean isScan) {

        if (isScan) {
            Log.v(TAG, "--- startInventory()   ----");
            handlerUpdateUI();
            try {
                RFIDSDKManager.getInstance().getRfidManager().startRead();//少量标签盘点建议使用：0；盘点标签超过 100-200建议使用：1.
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.v(TAG, "--- stopInventory()   ----");
            if (RFIDSDKManager.getInstance().getRfidManager() != null) {
                RFIDSDKManager.getInstance().getRfidManager().stopInventory();
            }
            handlerStopUI();
        }

    }

    /**
     * 控制A/B面读取
     */
    private void myScan() {

//               int ret = RFIDSDKManager.getInstance().getRfidManager().inventoryOnce((byte)1,(byte)4,(byte)0,(byte)6,(byte)0,(byte)50);//自定义扫描
//                Log.v(TAG, " inventoryOnce()   ret == "+ret);

        byte[] readAdr = new byte[]{0x00, 0x00};//固定2字节,第一个字节是高位，第2字节是低位
        int ret = RFIDSDKManager.getInstance().getRfidManager().inventoryOnceMix((byte) 1, (byte) 4, (byte) 0, (byte) 50, (byte) 2, readAdr, (byte) 6, ByteUtils.hexStringToBytes("00000000"));//自定义扫描
        Log.v(TAG, " InventoryOnceMix()   ret == " + ret);

    }

    private void handlerUpdateUI() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 500);
        }
    }

    private void handlerStopUI() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_STOP_INVENTORY, 200);
        }
    }


    private long time = 0l;

    /**
     * 单个读取EPC 或tid
     */
    private void inventorySingle() {
        RFIDSDKManager.getInstance().getRfidManager().inventorySingle();
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_STOP_INVENTORY, 10);
        }
    }


    /**
     * 设置掩码（标签过滤盘存）
     */
    private void setTagMask() {
        RFIDSDKManager.getInstance().getRfidManager().addMask(2, 24, 16, "7020");
    }


    /**
     * 通过TID写入标签数据
     *
     * @param TID     选中的TID
     * @param Mem     标签区域：0-密码区，前2个字是销毁密码，后2个字是访问密码      1-EPC区   2-TID区    3-用户区
     * @param WordPtr 写入的起始字地址
     * @param pwd     密码
     * @param datas   待写入数据
     */
    private void writeTagByTid(String TID, byte Mem, byte WordPtr, byte[] pwd, String datas) {
//                String TID = "E280110C20007642903D094D";
//                byte[] pwd = hexStringToBytes("00000000");
//                String datas = "1111111111111111";
        int ret = RFIDSDKManager.getInstance().getRfidManager().writeTagByTid(TID, (byte) 1, (byte) 2, pwd, datas);
        if (ret == -6) {
            Toast.makeText(mActivity, getContext().getString(R.string.gj_no_support), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 随机对一张标签写入EPC
     *
     * @param epc      待写入的EPC值 16进制字符串
     * @param password 标签访问密码
     */
    private void writeEpcString(String epc, String password) {
        RFIDSDKManager.getInstance().getRfidManager().writeEpcString(epc, password);
    }


    class ScanCallback implements IRfidCallback {
        @Override
        public void onInventoryTag(String EPC, final String TID, final String strRSSI) {
            Log.e(TAG, "onInventoryTag:............... epc:" + EPC + "    tid:" + TID);
            notiyDatas(EPC, TID, strRSSI);


        }

        /**
         * 盘存结束回调(Inventory Command Operate End)
         */
        @Override
        public void onInventoryTagEnd() {
            Log.d(TAG, "onInventoryTagEnd()");

        }
    }

    int num = 0;

    private void notiyDatas(String s2, String TID, final String strRSSI) {
        final String mapContainStrFinal = s2 + TID;
        Log.d(TAG, "onInventoryTag: EPC: " + s2);

        SoundTool.getInstance(BaseApplication.getContext()).playBeep(1);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    if (mapData.containsKey(mapContainStrFinal)) {
                        TagScan tagScan = mapData.get(mapContainStrFinal);
                        tagScan.setCount(mapData.get(mapContainStrFinal).getCount() + 1);

                        tagScan.setRssi(strRSSI);
                        //mapData.put(mapContainStrFinal, tagScan);
                    } else {
                        mActivity.mDataParents.add(s2);

                        TagScan tagScan = new TagScan(s2, TID, strRSSI, 1);

                        if (tagScan.getEpc().substring(0, 8).contains("CCA2310")) {
                            mapData.put(mapContainStrFinal, tagScan);
                            mActivity.tagScanSpinner.add(tagScan);
                        }
                    }

                    tagTotal++;
                    data = new ArrayList<>(mapData.values());
//                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));

                    showView();
                    /*long nowTime = System.currentTimeMillis();
                    if ((nowTime - time)>500){
                        time = nowTime;
                        data = new ArrayList<>(mapData.values());
//                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));
                        scanListAdapterRv.setData(data);
                    }*/


                }
            }
        });
    }

    public void showView() {
        //scanTotalText.setText(tagTotal + "");
        scanListAdapterRv.setData(data);
        scanCountText.setText(mapData.keySet().size() + "");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ........");

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setCallback();
        } else {
            if (RFIDSDKManager.getInstance().getRfidManager() != null) {
                scanStartBtn.setText(getContext().getString(R.string.btInventory));
                setScanStatus(false);
            }
//            Log.e(TAG, "setUserVisibleHint: 扫描释放声音资源..........." );
//            SoundTool.getInstance(BaseApplication.getContext()).release();

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ........");
        if (RFIDSDKManager.getInstance().getRfidManager() != null) {
            setScanStatus(false);
        }

    }


    public void setCallback() {
        if (RFIDSDKManager.getInstance().getRfidManager() != null) {
            System.out.println("스캔 시작을 거쳐 콜백 함수 호출 시 진입 확인 - " + RFIDSDKManager.getInstance().getRfidManager());
            System.out.println("스캔 콜백 데이터 - " + callback);

            if (callback == null) {
                callback = new ScanCallback();
            }
            RFIDSDKManager.getInstance().getRfidManager().registerCallback(callback);
        }
    }

//-----------------------------------------

}