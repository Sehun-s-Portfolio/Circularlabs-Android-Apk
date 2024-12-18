//package com.ubx.rfid_demo.ui.main;
//
//
//import android.bluetooth.le.ScanCallback;
//import android.device.ScanManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.os.SystemClock;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.ubx.rfid_demo.BaseApplication;
//import com.ubx.rfid_demo.MainActivity;
//import com.ubx.rfid_demo.R;
//import com.ubx.rfid_demo.pojo.TagScan;
//
//import com.ubx.usdk.USDKManager;
//import com.ubx.usdk.bean.RfidParameter;
//import com.ubx.usdk.rfid.aidl.IRfidCallback;
//import com.ubx.usdk.util.SoundTool;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link TagScanFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class TagScanFragment extends Fragment {
//
//    public static final String TAG = "usdk-" + TagScanFragment.class.getSimpleName();
//    private List<TagScan> data;
//    private HashMap<String, TagScan> mapData;
//    private ScanCallback callback;
//    private ScanListAdapterRv scanListAdapterRv;
//    private static MainActivity mActivity;
//    private int tagTotal = 0;
//    private int errorCount = 0;
//
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @return A new instance of fragment ScanFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static TagScanFragment newInstance(MainActivity activity) {
//        mActivity = activity;
//        TagScanFragment fragment = new TagScanFragment();
//        return fragment;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_tag_scan, container, false);
//    }
//
//    public Button scanStartBtn;
//    public Button sendBtn;
//    private RecyclerView scanListRv;
//    private TextView scanCountText, scanTotalText;
//    public TextView textFirmware;
//    private Spinner spinnerMode;
//    static String setDeviceId;
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        //spinnerMode = view.findViewById(R.id.spinner_inventory_mode);
//        scanStartBtn = view.findViewById(R.id.scan_start_btn);
//        sendBtn = view.findViewById(R.id.btn_send);
//        scanListRv = view.findViewById(R.id.scan_list_rv);
//        scanCountText = view.findViewById(R.id.scan_count_text);
//        //scanTotalText = view.findViewById(R.id.scan_total_text);
//        textFirmware = view.findViewById(R.id.text_firmware);
//
//
//        scanStartBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mActivity.RFID_INIT_STATUS) {
//
//                    if (scanStartBtn.getText().equals(getString(R.string.btInventory))) {
//                        setCallback();
//                        scanStartBtn.setText(getString(R.string.btn_stop_Inventory));
//                        setScanStatus(true);
//                    } else {
//                        scanStartBtn.setText(getString(R.string.btInventory));
//                        setScanStatus(false);
//                    }
//                } else {
//                    Log.d(TAG, "scanStartBtn  RFID未初始化 ");
//                    Toast.makeText(getActivity(), "RFID 이니셜라이즈 되지 않음", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//
//        // 데이터 전송 버튼 동작
//        sendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Log.d(TAG, "send 버튼 동작 확인");
//                System.out.println("send 동작 버튼 활성화");
//                Toast.makeText(getActivity(), "end 버튼 동작 확인", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        mapData = new HashMap<>();
//        /**
//        spinnerMode.setOnItemSelectedListener(
//                new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        if (mActivity.mRfidManager != null) {
//                            mActivity.mRfidManager.setQueryMode(position);
//                            if (position == 1) {
////                                SystemClock.sleep(10);
////                                RfidParameter rfidParameter = mActivity.mRfidManager.getInventoryParameter();
////                                rfidParameter.IvtType = 1;
////                                rfidParameter.Memory = 0x02;
////                                rfidParameter.WordPtr = 0x00;
////                                rfidParameter.Length = 4;
////                                mActivity.mRfidManager.setInventoryParameter(rfidParameter);
//
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//
//                    }
//                }
//        );
//         **/
//
//
//        scanListRv.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
//        scanListRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
//        scanListAdapterRv = new ScanListAdapterRv(null, getActivity());
//        scanListRv.setAdapter(scanListAdapterRv);
//
//        containerMap();
//    }
//
//    private final int MSG_UPDATE_UI = 0;
//    private final int MSG_STOP_INVENTORY = 1;
//
//    private final int MSG_UPDATE_SINGLE_INV = 2 ;
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case MSG_UPDATE_UI:
//                    scanListAdapterRv.notifyDataSetChanged();
//                    handlerUpdateUI();
//                    break;
//                case MSG_STOP_INVENTORY:
//                    scanListAdapterRv.notifyDataSetChanged();
//                    mHandler.removeCallbacksAndMessages(null);
//                    if (scanStartBtn.getText().equals(getString(R.string.btn_stop_Inventory))) {
//                        scanStartBtn.setText(getContext().getString(R.string.btInventory));
//                    }
//                    break;
//
//                case MSG_UPDATE_SINGLE_INV:
//                    mActivity.mRfidManager.inventorySingle();
//                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SINGLE_INV,200);
//                    break;
//            }
//
//        }
//    };
//
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//
//    }
//
//    private void setScanStatus(boolean isScan) {
//
//        if (isScan) {
//            tagTotal = 0;
////            if (mapData != null) {
////                mapData.clear();
////            }
////            if (mActivity.mDataParents != null) {
////                mActivity.mDataParents.clear();
////            }
////            if (mActivity.tagScanSpinner != null) {
////                mActivity.tagScanSpinner.clear();
////            }
////            if (data != null) {
////                data.clear();
////                scanListAdapterRv.setData(data);
////            }
//
//            Log.v(TAG, "--- startInventory()   ----");
//            handlerUpdateUI();
//            inventorySingle();//            inventorySingle();//读单个标签
//
////            mActivity.mRfidManager.startRead();//少量标签盘点建议使用：0；盘点标签超过 100-200建议使用：1.
//
////            startRead4or6TID();
//
////            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SINGLE_INV,200);
//        } else {
//            Log.v(TAG, "--- stopInventory()   ----");
//            mActivity.mRfidManager.stopInventory();
//
////            stopRead4or6TID();
//
////            mHandler.removeCallbacksAndMessages(null);
//
//            handlerStopUI();
//        }
//
//    }
//
//    private void handlerUpdateUI() {
//        if (mHandler != null) {
//            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 500);
//        }
//    }
//
//    private void handlerStopUI() {
//        if (mHandler != null) {
//            mHandler.sendEmptyMessageDelayed(MSG_STOP_INVENTORY, 200);
//        }
//    }
//
//
//    private long time = 0l;
//
//    /**
//     * 单个读取EPC 或tid
//     */
//    private void inventorySingle() {
//        mActivity.mRfidManager.inventorySingle();
//        if (mHandler != null) {
//            mHandler.sendEmptyMessageDelayed(MSG_STOP_INVENTORY, 10);
//        }
//    }
//
//
//    /**
//     * 设置掩码（标签过滤盘存）
//     */
//    private void setTagMask() {
//        mActivity.mRfidManager.addMask(2, 24, 16, "7020");
//    }
//
//
//    /**
//     * 通过TID写入标签数据
//     *
//     * @param TID     选中的TID
//     * @param Mem     标签区域：0-密码区，前2个字是销毁密码，后2个字是访问密码      1-EPC区   2-TID区    3-用户区
//     * @param WordPtr 写入的起始字地址
//     * @param pwd     密码
//     * @param datas   待写入数据
//     */
//    private void writeTagByTid(String TID, byte Mem, byte WordPtr, byte[] pwd, String datas) {
////                String TID = "E280110C20007642903D094D";
////                byte[] pwd = hexStringToBytes("00000000");
////                String datas = "1111111111111111";
//        int ret = mActivity.mRfidManager.writeTagByTid(TID, (byte) 1, (byte) 2, pwd, datas);
//        if (ret == -6) {
//            Toast.makeText(mActivity, getContext().getString(R.string.gj_no_support), Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    /**
//     * 随机对一张标签写入EPC
//     *
//     * @param epc      待写入的EPC值 16进制字符串
//     * @param password 标签访问密码
//     */
//    private void writeEpcString(String epc, String password) {
//        mActivity.mRfidManager.writeEpcString(epc, password);
//    }
//
//
//    class ScanCallback implements IRfidCallback {
//        @Override
//        public void onInventoryTag(String EPC, final String TID, final String strRSSI) {
//            notiyDatas(EPC, TID, strRSSI);
////            dealDatas(EPC, TID, strRSSI);
//            Log.e(TAG, "onInventoryTag:............... epc:" + EPC + "    tid:" + TID);
//
//        }
//
//        /**
//         * 盘存结束回调(Inventory Command Operate End)
//         */
//        @Override
//        public void onInventoryTagEnd() {
//            Log.i(TAG,"OendO     onInventoryTagEnd()  " +inventoryIng);
//            Log.d(TAG, "onInventoryTagEnd()");
////            if (typeInventory == 0){
////                read4Tid();
////            }else if (typeInventory == 1){
////                read6Tid();
////            }
//
//
//        }
//    }
//
//
//    private void notiyDatas(  String s2,   String TID, final String strRSSI) {
//        final String mapContainStrFinal = s2+TID;
//        Log.d(TAG, "onInventoryTag: EPC: " + s2);
//
//        SoundTool.getInstance(BaseApplication.getContext()).playBeep(1);
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                {
//                    if (mapData.containsKey(mapContainStrFinal)) {
//                        TagScan tagScan = mapData.get(mapContainStrFinal);
//                        tagScan.setCount(mapData.get(mapContainStrFinal).getCount() + 1);
////                    tagScan.setTid(TID);
//                        tagScan.setRssi(strRSSI);
//                        mapData.put(mapContainStrFinal, tagScan);
//                    } else {
//                        mActivity.mDataParents.add(s2);
//
//                        TagScan tagScan = new TagScan(s2, TID, strRSSI, 1);
//                        mapData.put(mapContainStrFinal, tagScan);
//                        mActivity.tagScanSpinner.add(tagScan);
//                    }
//
//                    scanTotalText.setText(++tagTotal + "");
//                    data = new ArrayList<>(mapData.values());
////                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));
//                    scanListAdapterRv.setData(data);
//                    scanCountText.setText(mapData.keySet().size() + "");
//                    /*long nowTime = System.currentTimeMillis();
//                    if ((nowTime - time)>500){
//                        time = nowTime;
//                        data = new ArrayList<>(mapData.values());
////                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));
//                        scanListAdapterRv.setData(data);
//                    }*/
//
//
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.e(TAG, "onResume: ........");
//
//    }
//
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            setCallback();
//        } else {
//            if (mActivity.mRfidManager != null) {
//                scanStartBtn.setText(getContext().getString(R.string.btInventory));
//                setScanStatus(false);
//            }
////            Log.e(TAG, "setUserVisibleHint: 扫描释放声音资源..........." );
////            SoundTool.getInstance(BaseApplication.getContext()).release();
//
//        }
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.e(TAG, "onPause: ........");
//        if (mActivity.mRfidManager != null) {
//            setScanStatus(false);
//        }
//
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.e(TAG, "onStop: .........");
//    }
//
//    public void setCallback() {
//        if (mActivity.mRfidManager != null) {
//
//            if (callback == null) {
//                callback = new ScanCallback();
//            }
//            mActivity.mRfidManager.registerCallback(callback);
//        }
//    }
//
//    /**
//     * 将Hex String转换为Byte数组
//     *
//     * @param hexString the hex string
//     * @return the byte [ ]
//     */
//    public static byte[] hexStringToBytes(String hexString) {
//        hexString = hexString.toLowerCase();
//        final byte[] byteArray = new byte[hexString.length() >> 1];
//        int index = 0;
//        for (int i = 0; i < hexString.length(); i++) {
//            if (index > hexString.length() - 1) {
//                return byteArray;
//            }
//            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
//            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
//            byteArray[i] = (byte) (highDit << 4 | lowDit);
//            index += 2;
//        }
//        return byteArray;
//    }
////------------------------------------------------------------------------
//
//    private HashMap<String,Integer> hashMap4wei = new HashMap<>();
//
//    private void containerMap(){
//        hashMap4wei.put("E2006003",4);
//        hashMap4wei.put("E2006004",4);
//        hashMap4wei.put("E282403B",4);
//        hashMap4wei.put("E282402D",4);
//        hashMap4wei.put("E200B080",4);
//    }
//
//private void dealDatas(String epc ,String tid ,String rssi){
//
//    if (!TextUtils.isEmpty(tid)){
//        int  length = tid.length();
//        if (length == 16){
//            String sub = tid.substring(0,8);
//            if (hashMap4wei.containsKey(sub)){//TODO 说明是4位长标签，数据可用，返回到上层
//                notiyDatas(epc, tid, rssi);
//            }else {//TODO 说明不是4位长标签，数据不可用，不作处理
//
//            }
//        }else  if (length == 24){
//            notiyDatas(epc, tid, rssi);
//        }else {
//            notiyDatas(epc, tid, rssi);
//        }
//
//    }else {
//        notiyDatas(epc, tid, rssi);
//    }
//}
//    private boolean inventoryIng = false;//是否正在盘点
//    private int typeInventory = 0;//0:6位， 1:4位
//
//    private void read4Tid(){
//        if (inventoryIng) {
//            typeInventory = 1;
//            RfidParameter rfidParameter =    mActivity.mRfidManager.getInventoryParameter();
//            rfidParameter.IvtType = 1;
//            rfidParameter.Memory = 0x02;
//            rfidParameter.WordPtr = 0x00;
//            rfidParameter.Session = 1;
//            rfidParameter.Length = 4;
//            mActivity.mRfidManager.setInventoryParameter(rfidParameter);
//            Log.i(TAG,"tid    4" );
//            handler.sendEmptyMessageDelayed(MSG_4_TID,70);
//        }
//
//    }
//    private void read6Tid(){
//        if (inventoryIng) {
//            typeInventory = 0;
//            RfidParameter rfidParameter =    mActivity.mRfidManager.getInventoryParameter();
//            rfidParameter.IvtType = 1;
//            rfidParameter.Memory = 0x02;
//            rfidParameter.WordPtr = 0x00;
//            rfidParameter.Session = 1;
//            rfidParameter.Length = 6;
//            mActivity.mRfidManager.setInventoryParameter(rfidParameter);
//            Log.i(TAG,"tid    6" );
//            handler.sendEmptyMessageDelayed(MSG_6_TID,70);
//        }
//
//
//
//
//    }
//
//    private void startRead4or6TID() {
//        inventoryIng = true;
//        read6Tid();
//
//    }
//    private void stopRead4or6TID() {
//        inventoryIng = false;
//        if (handler != null) {
//            handler.sendEmptyMessage(MSG_STOP);
//        }
//
//    }
//
//
//    private final int MSG_4_TID = 0;
//    private final int MSG_6_TID = 1;
//    private final int MSG_STOP = 2;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == MSG_4_TID){
//                if (inventoryIng) {
//                    mActivity.mRfidManager.startRead();
//                    handler.sendEmptyMessageDelayed(MSG_STOP,800);
//                }
//            }else if (msg.what == MSG_6_TID){
//                if (inventoryIng) {
//                    mActivity.mRfidManager.startRead();
//                    handler.sendEmptyMessageDelayed(MSG_STOP,600);
//                }
//            }
//            else if (msg.what == MSG_STOP){
//                     Log.i(TAG,"OendO     stopInventory()" );
//                     handler.removeCallbacksAndMessages(null);
//                    mActivity.mRfidManager.stopInventory();
//
//            }
//
//
//        }
//    };
//
//
//}