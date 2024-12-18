package com.ubx.rfid_demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.ubx.rfid_demo.pojo.TagScan;
import com.ubx.rfid_demo.ui.main.CustomLoadingDialog;
import com.ubx.rfid_demo.ui.main.SectionsPagerAdapter;

import com.ubx.rfid_demo.ui.main.SettingFragment;

import com.ubx.rfid_demo.ui.main.TagScanFragmentNew;
import com.ubx.rfid_demo.utils.ByteUtils;
import com.ubx.usdk.RFIDSDKManager;
import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.util.QueryMode;
import com.ubx.usdk.util.SoundTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "usdk";

    public boolean RFID_INIT_STATUS = false;
    public RfidManager mRfidManager;
    public List<String> mDataParents;
    public List<TagScan> tagScanSpinner;
    private List<Fragment> fragments;
    private ViewPager viewPager;
    private TabLayout tabs;
    public int readerType = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         customLoadingDialog = new CustomLoadingDialog(this);
         customLoadingDialog.show();
         **/

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SoundTool.getInstance(BaseApplication.getContext());
        mDataParents = new ArrayList<>();
        tagScanSpinner = new ArrayList<>();

        fragments = Arrays.asList(TagScanFragmentNew.newInstance(MainActivity.this)
                , SettingFragment.newInstance(MainActivity.this));

/**
 fragments = Arrays.asList(TagScanFragmentNew.newInstance(MainActivity.this)
 , TagManageFragment.newInstance(MainActivity.this)
 , SettingFragment.newInstance(MainActivity.this));
 **/

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), fragments);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        initRfid();

        //customLoadingDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String s7 = "1111222233334444555566667777";
        String s8 = "11112222333344445555666677778888";
        String s9 = "111122223333444455556666777788889999";
        String s10 = "1111222233334444555566667777888899990000";
        String s7Llen = ByteUtils.getPC(s7);
        String s8Llen = ByteUtils.getPC(s8);
        String s9Llen = ByteUtils.getPC(s9);
        String s10Llen = ByteUtils.getPC(s10);
        Log.d(TAG, "onResume()  s7Llen:" + s7Llen + "    s8Llen:" + s8Llen + "    s9Llen:" + s9Llen + "    s10Llen:" + s10Llen);
    }


    public void initRfid() {
        // 在异步回调中拿到RFID实例
        RFIDSDKManager.getInstance().power(true);
        System.out.println("RFID 인스턴스 생성 여부 확인 : " + RFIDSDKManager.getInstance());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean connect = RFIDSDKManager.getInstance().connect();

                if (connect) {
                    Log.d(TAG, "initRfid()  success.");

                    RFID_INIT_STATUS = true;
                    readerType = RFIDSDKManager.getInstance().getRfidManager().getReaderType();//80为短距，其他为长距
                    mRfidManager = RFIDSDKManager.getInstance().getRfidManager();
                    String firmware = RFIDSDKManager.getInstance().getRfidManager().getFirmwareVersion();

                    try {
                        ((TagScanFragmentNew) fragments.get(0)).textFirmware.setText(getString(R.string.firmware) + firmware);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "initRfid: GetReaderType() = " + readerType);
                    System.out.println("initRfid - 리더 타입 : " + readerType);

                } else {
                    Log.d(TAG, "initRfid  fail.");
                    System.out.println("initRfid 실패");
                }

            }
        }, 1500);
    }

    /**
     public void initRfid() {
     // 在异步回调中拿到RFID实例
     USDKManager.getInstance().init(new  InitListener() {
    @Override public void onStatus(boolean status) {
    if ( status ) {
    Log.d(TAG, "initRfid()  success.");
    RFID_INIT_STATUS = true;
    mRfidManager =   USDKManager.getInstance().getRfidManager();
    readerType =  mRfidManager.getReaderType();//80为短距，其他为长距
    runOnUiThread(new Runnable() {
    @Override public void run() {
    String firmware =   mRfidManager.getFirmwareVersion();
    try {
    ((TagScanFragment)fragments.get(0)).textFirmware.setText(getString(R.string.firmware) + firmware);
    } catch (Exception e) {
    e.printStackTrace();
    }
    }
    });
    getScanInteral();
    Log.d(TAG, "initRfid: GetReaderType() = " +readerType );
    }else {
    Log.d(TAG, "initRfid  fail.");
    }
    }
    });

     }
     **/

    /**
     * 设置查询模式
     *
     * @param mode
     */
    private void setQueryMode(int mode) {
        mRfidManager.setQueryMode(QueryMode.EPC_TID);
    }

    /**
     * 通过TID写标签
     */
    private void writeTagByTid() {
        //写入方法（不需要先选中）
        String tid = "24 length TID";
        String writeData = "need write EPC datas ";
        mRfidManager.writeTagByTid(tid, (byte) 0, (byte) 2, "00000000".getBytes(), writeData);
    }

    @Override
    protected void onDestroy() {

        RFID_INIT_STATUS = false;
        USDKManager.getInstance().release();
        if (mRfidManager != null) {
            mRfidManager.disConnect();
            mRfidManager.release();
            Log.d(TAG, "onDestroyView: rfid close");
        }
        SoundTool.getInstance(BaseApplication.getContext()).release();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        SoundTool.getInstance(BaseApplication.getContext()).release();
        super.onPause();
    }

    /**
     * 设置盘存时间
     *
     * @param interal 0-200 ms
     */
    private void setScanInteral(int interal) {
        int setScanInterval = mRfidManager.setScanInterval(interal);
        Log.v(TAG, "--- setScanInterval()   ----" + setScanInterval);
    }

    /**
     * 获取盘存时间
     */
    private void getScanInteral() {
        int getScanInterval = mRfidManager.getScanInterval();
        Log.v(TAG, "--- getScanInterval()   ----" + getScanInterval);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);//弹出Menu前调用的方法
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_rate_main, menu);
        return true;
    }

    // 상단의 ... 6B Tag 설정 메뉴 버튼
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         int id = item.getItemId();
         switch (id) {
         case R.id.action_6b:
         Intent intent = new Intent(MainActivity.this, Activity6BTag.class);
         startActivity(intent);
         break;
         }
         **/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        Log.e(TAG, " finish: .......");
        SoundTool.getInstance(BaseApplication.getContext()).release();
        USDKManager.getInstance().release();
        super.finish();
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        if((event.getKeyCode() == 520||event.getKeyCode() == 521) &&  event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0){
//            //TODO 按下 扫描键
//            return true;
//        }else  if((event.getKeyCode() == 520||event.getKeyCode() == 521) &&  event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0){
//            //TODO 抬起 扫描键
//            return true;
//        }
//        return super.dispatchKeyEvent(event);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == 523 && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            //TODO 按下 开启盘点

            if (viewPager.getCurrentItem() == 0) {
                TagScanFragmentNew fragment = (TagScanFragmentNew) fragments.get(0);
                fragment.scanStartBtn.callOnClick();
            }

            return true;
        } else if (event.getKeyCode() == 523 && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) {
            //TODO 抬起 停止盘点
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}