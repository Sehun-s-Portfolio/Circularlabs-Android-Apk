package com.ubx.rfid_demo.ui.main;

import static com.ubx.rfid_demo.ui.main.TagScanFragmentNew.TAG;

import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagScan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendDialog extends Dialog implements AdapterView.OnItemSelectedListener {
    private TextView txt_contents_value, total_scan_data_value;
    private Button productOutBtn, productInBtn, productReturnBtn, productCleaningBtn, productDiscardBtn;
    private Spinner customer_spinner_inventory_mode;
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS).build();
    private ViewPager viewPager;
    private TabLayout tabs;
    private List<Fragment> modalFragments;
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String deviceCode;
    private String selectClientCode;
    private ArrayAdapter arrayAdapter;
    private List<TagScan> scanDatas;
    private HashMap<String, Integer> productCodes = new HashMap<>();
    private List<String> eachScanProducts = new ArrayList<String>();
    private HashMap<String, String> productNameInfo;
    public Button firstSectionButton, secondSectionButton;
    private String withDrawalClientCheck = "";

    @SuppressLint("MissingInflatedId")
    public SendDialog(Context context, String contents, String scanCount, List<TagScan> scanData, String deviceCode, String supplierCode, MainActivity mActivity) {
        super(context);
        setContentView(R.layout.send_dialog);
        this.deviceCode = deviceCode;
        this.scanDatas = scanData;

        productNameInfo = SettingFragment.staticProductsInfo;

        // 기기 ID
        txt_contents_value = findViewById(R.id.txt_contents_value);
        txt_contents_value.setText(this.deviceCode);

        // 스캔한 데이터 총 수량
        total_scan_data_value = findViewById(R.id.total_scan_data_value);
        total_scan_data_value.setText(scanCount);

        // 카테고리에 따른 상태 처리 버튼
        /**
         productOutBtn = findViewById(R.id.btn_product_out);
         final String outTag = productOutBtn.getTag().toString();
         **/

        productInBtn = findViewById(R.id.btn_product_in);
        final String inTag = productInBtn.getTag().toString();

        productReturnBtn = findViewById(R.id.btn_product_return);
        final String returnTag = productReturnBtn.getTag().toString();

        /**
         productCleaningBtn = findViewById(R.id.btn_product_cleaning);
         final String cleaningTag = productCleaningBtn.getTag().toString();
         **/

        // 고객사 select 선택 항목
        customer_spinner_inventory_mode = findViewById(R.id.customer_spinner_inventory_mode);
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, SettingFragment.staticClients);
        customer_spinner_inventory_mode.setAdapter(arrayAdapter);
        customer_spinner_inventory_mode.setVerticalScrollBarEnabled(true);

        final String machineId = txt_contents_value.getText().toString();

        firstSectionButton = findViewById(R.id.first_section);
        LinearLayout.LayoutParams buttonParam1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParam1.weight = 1;
        firstSectionButton.setLayoutParams(buttonParam1);
        firstSectionButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
        firstSectionButton.setTextColor(Color.WHITE);

        secondSectionButton = findViewById(R.id.second_section);
        LinearLayout.LayoutParams buttonParam2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParam2.weight = 1;
        secondSectionButton.setLayoutParams(buttonParam2);
        secondSectionButton.setBackgroundColor(Color.parseColor("#D2D1D1"));
        secondSectionButton.setTextColor(Color.BLACK);

        // 출고 / 입고 화면 전환
        firstSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 상태 섹션 버튼 클릭 시 색상 변경
                firstSectionButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                firstSectionButton.setTextColor(Color.WHITE);
                secondSectionButton.setBackgroundColor(Color.parseColor("#D2D1D1"));
                secondSectionButton.setTextColor(Color.BLACK);

                // 출고, 입고 버튼 + 고객사 선택 스피너 노출
                findViewById(R.id.ll_i).setVisibility(View.VISIBLE);
                findViewById(R.id.category1).setVisibility(View.VISIBLE);

                // 회수, 세척 버튼 감추기
                findViewById(R.id.category2).setVisibility(View.GONE);
            }
        });

        // 회수 / 세척 화면 전환
        secondSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 상태 섹션 버튼 클릭 시 색상 변경
                firstSectionButton.setBackgroundColor(Color.parseColor("#D2D1D1"));
                firstSectionButton.setTextColor(Color.BLACK);
                secondSectionButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                secondSectionButton.setTextColor(Color.WHITE);

                // 출고, 입고 버튼 + 고객사 선택 스피너 감추기
                findViewById(R.id.ll_i).setVisibility(View.GONE);
                findViewById(R.id.category1).setVisibility(View.GONE);

                // 회수, 세척 버튼 노출
                findViewById(R.id.category2).setVisibility(View.VISIBLE);
            }
        });


        /**
         // 태스트 용 데이터
         String eachProductCountInfo1 = "A00-제품1-2000";
         String eachProductCountInfo2 = "AA1-제품2-2000";
         String eachProductCountInfo3 = "BB1-제품3-2000";
         eachScanProducts.add(eachProductCountInfo1);
         eachScanProducts.add(eachProductCountInfo2);
         eachScanProducts.add(eachProductCountInfo3);
         **/


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // 실제 운영 스캔 데이터
        for (int i = 0; i < scanData.size(); i++) {
            if (scanData.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
                String productCode_ = scanData.get(i).getEpc().substring(8, 11);

                if (!productCodes.containsKey(productCode_)) {
                    productCodes.put(productCode_, 1);
                } else {
                    productCodes.put(productCode_, productCodes.get(productCode_) + 1);
                }
            }
        }

        String[] productsArray = productCodes.keySet().toArray(new String[productCodes.size()]);

        for (int j = 0; j < productsArray.length; j++) {
            String eachProductCountInfo = "";
            eachProductCountInfo += productsArray[j] + "-" + productNameInfo.get(productsArray[j]) + "-" + productCodes.get(productsArray[j]);

            eachScanProducts.add(eachProductCountInfo);
        }


        LinearLayout productLinearLayout = findViewById(R.id.product_listup);
        productLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int k = 0; k < eachScanProducts.size(); k++) {
            String[] productInfo = eachScanProducts.get(k).split("-");
            View addLayoutView = inflater.inflate(R.layout.each_tag_scan_item, null);

            // 제품 명
            TextView productName = addLayoutView.findViewById(R.id.each_product_name);
            productName.setText(productInfo[1]);

            // 제품 수량
            TextView productCount = addLayoutView.findViewById(R.id.each_product_count);
            productCount.setText(productInfo[2]);

            productLinearLayout.addView(addLayoutView);
        }

        customer_spinner_inventory_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("[하위]");
                System.out.println("선택한 아이템 인덱스 : " + i);

                selectClientCode = SettingFragment.staticClientCodes.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

/**
 // 출고 버튼 동작
 productOutBtn.setOnClickListener(new View.OnClickListener() {
@Override public void onClick(View v) {

if (!selectClientCode.equals("CODE")) {
AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
confirmDialog.setMessage("출고 처리를 진행합니다.\n!! 한 번 진행하면 취소할 수 없습니다.");
confirmDialog.setPositiveButton("예",
new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int which) {
// HttpUrlConnection
Thread th = new Thread(new Runnable() {
@Override public void run() {
try {
List<JSONObject> sendProductCodes = new ArrayList<>();
HashMap<String, Integer> productCodeList = new HashMap<>();
List<JSONObject> eachProductsSize = new ArrayList<>();


// RFID 기기가 존재하고 스캔한 데이터들이 있을 경우에 해제
if (!scanDatas.isEmpty()) {
for (int i = 0; i < scanDatas.size(); i++) {
if (scanDatas.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
String rfidChipCode = scanDatas.get(i).getEpc().substring(22);
String filteringCode = scanDatas.get(i).getEpc().substring(0, 8);
String productCode_ = scanDatas.get(i).getEpc().substring(8, 11);
String productSerialCode_ = scanDatas.get(i).getEpc().substring(8);

if (!productCodeList.containsKey(productCode_)) {
productCodeList.put(productCode_, 1);
} else {
productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
}

JSONObject eachTestData = new JSONObject();
eachTestData.put("rfidChipCode", "RFID" + rfidChipCode + "CHIP");
eachTestData.put("filteringCode", filteringCode);
eachTestData.put("productCode", productCode_);
eachTestData.put("productSerialCode", productSerialCode_);

sendProductCodes.add(eachTestData);
}
}

String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

for (int j = 0; j < productsArray.length; j++) {
JSONObject productStatusCount = new JSONObject();
productStatusCount.put("product", productsArray[j]);
productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

eachProductsSize.add(productStatusCount);
}
}
 **/
        ///////////////////////////////////////////////////////////////////////////


                                                /*
                                                 // 테스트 용 스캔 데이터 총 1500개 - A00 / AA1 / BB1 제품군 (실제 스캔 데이터를 활용할 경우 해당 코드 주석 처리 필요)
                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "A00";
                                                 String productSerialCode_ = (i + 1) + "D240313";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "AA1";
                                                 String productSerialCode_ = (i + 1) + "D240313";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "BB1";
                                                 String productSerialCode_ = (i + 1) + "D240313";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

                                                 for (int j = 0; j < productsArray.length; j++) {
                                                 JSONObject productStatusCount = new JSONObject();
                                                 productStatusCount.put("product", productsArray[j]);
                                                 productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

                                                 eachProductsSize.add(productStatusCount);
                                                 }
                                                 */


        ///////////////////////////////////////////////////////////////////////////
/**
 JSONObject json = new JSONObject();
 json.put("machineId", machineId);
 json.put("tag", outTag);
 json.put("selectClientCode", selectClientCode);
 json.put("supplierCode", supplierCode);
 json.put("productCodes", new JSONArray(sendProductCodes));
 json.put("eachProductCount", new JSONArray(eachProductsSize));

 JSONObject requestJson = new JSONObject(json.toString());

 // RequestBody 생성
 RequestBody body = RequestBody.create(requestJson.toString(), JSON);


 Request request = new Request.Builder()
 .url("http://localhost:8090/rfid/out")
 .post(body)
 .build();

 /**
 // 비동기 방식으로 요청 전송
 client.newCall(request).enqueue(new Callback() {
@Override public void onFailure(Call call, IOException e) {
withDrawalClientCheck = "Y";
//Toast.makeText(mActivity, "고객사가 탈퇴된 상태입니다.", Toast.LENGTH_SHORT).show();
e.printStackTrace();
}

@Override public void onResponse(Call call, Response response) throws IOException {
if (!response.isSuccessful()) {
throw new IOException("Unexpected code " + response);
}

// 수신된 JSON 데이터 디버그 로그로 출력
try {
String responseData = response.body().string();
JSONObject receivedJson = new JSONObject(responseData);
String withDrawalCheck = receivedJson.getJSONObject("data").toString();
//JSONObject realResponseData = new JSONObject(receivedJson.getJSONObject("data").toString());
withDrawalClientCheck = "N";


} catch (JSONException e) {
e.printStackTrace();
}
}
});

 } catch (Exception e) {
 Log.i("tag", "error :" + e);
 }
 }
 });

 th.start();

 // 작업 처리 후 데이터 리셋
 List<TagScan> resetTagScan = new ArrayList<>();
 TagScanFragmentNew.scanCountText.setText("0");
 //TagScanFragmentNew.scanTotalText.setText("0");
 TagScanFragmentNew.scanListAdapterRv.setData(resetTagScan);

 // dialog 닫기
 dismiss();
 th.interrupt();
 }
 });

 confirmDialog.setNegativeButton("아니오",
 new DialogInterface.OnClickListener() {
 public void onClick(DialogInterface dialog, int which) {
 dialog.dismiss();
 }
 });

 confirmDialog.show();
 } else {
 Toast.makeText(mActivity, "고객사를 설정해주십시오.", Toast.LENGTH_SHORT).show();
 }

 }
 });
 **/

        // 입고 버튼 동작
        productInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectClientCode.equals("CODE")) {
                    AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                    confirmDialog.setMessage("입고 처리를 진행합니다.\n!! 한 번 진행하면 취소할 수 없습니다.");
                    confirmDialog.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // HttpUrlConnection
                                    Thread th = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                List<JSONObject> sendProductCodes = new ArrayList<>();
                                                HashMap<String, Integer> productCodeList = new HashMap<>();
                                                List<JSONObject> eachProductsSize = new ArrayList<>();

                                                // RFID 기기가 존재하고 스캔한 데이터들이 있을 경우에 해제
                                                if (!scanDatas.isEmpty()) {
                                                    for (int i = 0; i < scanDatas.size(); i++) {
                                                        if (scanDatas.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
                                                            String rfidChipCode = scanDatas.get(i).getEpc().substring(22);
                                                            String filteringCode = scanDatas.get(i).getEpc().substring(0, 8);
                                                            String productCode_ = scanDatas.get(i).getEpc().substring(8, 11);
                                                            String productSerialCode_ = scanDatas.get(i).getEpc().substring(8);

                                                            if (!productCodeList.containsKey(productCode_)) {
                                                                productCodeList.put(productCode_, 1);
                                                            } else {
                                                                productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                            }

                                                            JSONObject eachTestData = new JSONObject();
                                                            eachTestData.put("rfidChipCode", "RFID" + rfidChipCode + "CHIP");
                                                            eachTestData.put("filteringCode", filteringCode);
                                                            eachTestData.put("productCode", productCode_);
                                                            eachTestData.put("productSerialCode", productSerialCode_);

                                                            sendProductCodes.add(eachTestData);
                                                        }
                                                    }

                                                    String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

                                                    for (int j = 0; j < productsArray.length; j++) {
                                                        JSONObject productStatusCount = new JSONObject();
                                                        productStatusCount.put("product", productsArray[j]);
                                                        productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

                                                        eachProductsSize.add(productStatusCount);
                                                    }
                                                }


                                                ///////////////////////////////////////////////////////////////////////////


                                                // 테스트 용 스캔 데이터 총 1500개 - A00 / AA1 / BB1 제품군 (실제 스캔 데이터를 활용할 경우 해당 코드 주석 처리 필요)
                                                /**
                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "000";
                                                 String productSerialCode_ = (i + 1) + "D240313TEST";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }


                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "AA1";
                                                 String productSerialCode_ = (i + 1) + "D240313TEST";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 for (int i = 0; i < 2000; i++) {
                                                 String rfidChipCode = "RFID" + i + "CHIP";
                                                 String filteringCode = "CCA2310";
                                                 String productCode_ = "BB1";
                                                 String productSerialCode_ = (i + 1) + "D240313TEST";

                                                 if (!productCodeList.containsKey(productCode_)) {
                                                 productCodeList.put(productCode_, 1);
                                                 } else {
                                                 productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                 }

                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", rfidChipCode);
                                                 eachTestData.put("filteringCode", filteringCode);
                                                 eachTestData.put("productCode", productCode_);
                                                 eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

                                                 for (int j = 0; j < productsArray.length; j++) {
                                                 JSONObject productStatusCount = new JSONObject();
                                                 productStatusCount.put("product", productsArray[j]);
                                                 productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

                                                 eachProductsSize.add(productStatusCount);
                                                 }
                                                 **/

                                                ///////////////////////////////////////////////////////////////////////////

                                                JSONObject json = new JSONObject();
                                                json.put("machineId", machineId);
                                                json.put("tag", inTag);
                                                json.put("selectClientCode", selectClientCode);
                                                json.put("supplierCode", supplierCode);
                                                json.put("productCodes", new JSONArray(sendProductCodes));
                                                json.put("eachProductCount", new JSONArray(eachProductsSize));

                                                JSONObject requestJson = new JSONObject(json.toString());

                                                // RequestBody 생성
                                                RequestBody body = RequestBody.create(requestJson.toString(), JSON);

                                                // 써큘 개발 서버 용 [개선]
                                                Request request = new Request.Builder()
                                                        .url("http://localhost:8090/rfid/in")
                                                        .post(body)
                                                        .build();

                                                // 비동기 방식으로 요청 전송
                                                client.newCall(request).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        if (!response.isSuccessful()) {
                                                            throw new IOException("Unexpected code " + response);
                                                        }

                                                        // 수신된 JSON 데이터 디버그 로그로 출력
                                                        try {
                                                            String responseData = response.body().string();
                                                            JSONObject receivedJson = new JSONObject(responseData);
                                                            JSONObject realResponseData = new JSONObject(receivedJson.getJSONObject("data").toString());

                                                            /**
                                                             String deviceCode = realResponseData.getString("machineId");
                                                             String outTag = realResponseData.getString("tag");
                                                             String selectClientCode = realResponseData.getString("selectClientCode");
                                                             String supplierCode = realResponseData.getString("supplierCode");
                                                             String status = realResponseData.getString("status");
                                                             String latestReadingAt = realResponseData.getString("latestReadingAt");
                                                             JSONArray productCodes = realResponseData.getJSONArray("productDetails");

                                                             for (int i = 0; i < productCodes.length(); i++) {
                                                             JSONObject eachProduct = productCodes.getJSONObject(i);

                                                             Long productDetailId = eachProduct.getLong("productDetailId");
                                                             String rfidChipCode = eachProduct.getString("rfidChipCode");
                                                             String productSerialCode = eachProduct.getString("productSerialCode");
                                                             String productCode = eachProduct.getString("productCode");
                                                             String supplyCode = eachProduct.getString("supplierCode");
                                                             String clientCode = eachProduct.getString("clientCode");
                                                             String statusNow = eachProduct.getString("status");
                                                             int cycle = eachProduct.getInt("cycle");
                                                             String latestReadingAtNow = eachProduct.getString("latestReadingAt");

                                                             }
                                                             **/

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                });

                                            } catch (Exception e) {
                                                Log.i("tag", "error :" + e);
                                            }
                                        }
                                    });
                                    th.start();

                                    // 작업 처리 후 데이터 리셋
                                    List<TagScan> resetTagScan = new ArrayList<>();
                                    TagScanFragmentNew.scanCountText.setText("0");
                                    //TagScanFragmentNew.scanTotalText.setText("0");
                                    TagScanFragmentNew.scanListAdapterRv.setData(resetTagScan);

                                    // dialog 닫기
                                    dismiss();
                                }
                            });

                    confirmDialog.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    confirmDialog.show();
                } else {
                    Toast.makeText(mActivity, "고객사를 설정해주십시오.", Toast.LENGTH_SHORT).show();
                }
            }

        });


        // 회수 버튼 동작
        productReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                confirmDialog.setMessage("회수 처리를 진행합니다.\n!! 한 번 진행하면 취소할 수 없습니다.");
                confirmDialog.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // HttpUrlConnection
                                Thread th = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            List<JSONObject> sendProductCodes = new ArrayList<>();
                                            HashMap<String, Integer> productCodeList = new HashMap<>();
                                            List<JSONObject> eachProductsSize = new ArrayList<>();

                                            // RFID 기기가 존재하고 스캔한 데이터들이 있을 경우에 해제
                                            if (!scanDatas.isEmpty()) {
                                                for (int i = 0; i < scanDatas.size(); i++) {
                                                    if (scanDatas.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
                                                        String rfidChipCode = scanDatas.get(i).getEpc().substring(22);
                                                        String filteringCode = scanDatas.get(i).getEpc().substring(0, 8);
                                                        String productCode_ = scanDatas.get(i).getEpc().substring(8, 11);
                                                        String productSerialCode_ = scanDatas.get(i).getEpc().substring(8);

                                                        if (!productCodeList.containsKey(productCode_)) {
                                                            productCodeList.put(productCode_, 1);
                                                        } else {
                                                            productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                                        }

                                                        JSONObject eachTestData = new JSONObject();
                                                        eachTestData.put("rfidChipCode", "RFID" + rfidChipCode + "CHIP");
                                                        eachTestData.put("filteringCode", filteringCode);
                                                        eachTestData.put("productCode", productCode_);
                                                        eachTestData.put("productSerialCode", productSerialCode_);

                                                        sendProductCodes.add(eachTestData);
                                                    }
                                                }

                                                String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

                                                for (int j = 0; j < productsArray.length; j++) {
                                                    JSONObject productStatusCount = new JSONObject();
                                                    productStatusCount.put("product", productsArray[j]);
                                                    productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

                                                    eachProductsSize.add(productStatusCount);
                                                }
                                            }

                                            ///////////////////////////////////////////////////////////////////////////

                                            // 테스트 용 스캔 데이터 총 1500개 - A00 / AA1 / BB1 제품군 (실제 스캔 데이터를 활용할 경우 해당 코드 주석 처리 필요)
                                            /**
                                             for (int i = 0; i < 2000; i++) {
                                             String rfidChipCode = "RFID" + i + "CHIP";
                                             String filteringCode = "CCA2310";
                                             String productCode_ = "000";
                                             String productSerialCode_ = (i + 1) + "D240313TEST";

                                             if (!productCodeList.containsKey(productCode_)) {
                                             productCodeList.put(productCode_, 1);
                                             } else {
                                             productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                             }

                                             JSONObject eachTestData = new JSONObject();
                                             eachTestData.put("rfidChipCode", rfidChipCode);
                                             eachTestData.put("filteringCode", filteringCode);
                                             eachTestData.put("productCode", productCode_);
                                             eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                             sendProductCodes.add(eachTestData);
                                             }

                                             for (int i = 0; i < 2000; i++) {
                                             String rfidChipCode = "RFID" + i + "CHIP";
                                             String filteringCode = "CCA2310";
                                             String productCode_ = "AA1";
                                             String productSerialCode_ = (i + 1) + "D240313TEST";

                                             if (!productCodeList.containsKey(productCode_)) {
                                             productCodeList.put(productCode_, 1);
                                             } else {
                                             productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                             }

                                             JSONObject eachTestData = new JSONObject();
                                             eachTestData.put("rfidChipCode", rfidChipCode);
                                             eachTestData.put("filteringCode", filteringCode);
                                             eachTestData.put("productCode", productCode_);
                                             eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                             sendProductCodes.add(eachTestData);
                                             }

                                             for (int i = 0; i < 2000; i++) {
                                             String rfidChipCode = "RFID" + i + "CHIP";
                                             String filteringCode = "CCA2310";
                                             String productCode_ = "BB1";
                                             String productSerialCode_ = (i + 1) + "D240313TEST";

                                             if (!productCodeList.containsKey(productCode_)) {
                                             productCodeList.put(productCode_, 1);
                                             } else {
                                             productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
                                             }

                                             JSONObject eachTestData = new JSONObject();
                                             eachTestData.put("rfidChipCode", rfidChipCode);
                                             eachTestData.put("filteringCode", filteringCode);
                                             eachTestData.put("productCode", productCode_);
                                             eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

                                             sendProductCodes.add(eachTestData);
                                             }

                                             String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

                                             for (int j = 0; j < productsArray.length; j++) {
                                             JSONObject productStatusCount = new JSONObject();
                                             productStatusCount.put("product", productsArray[j]);
                                             productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

                                             eachProductsSize.add(productStatusCount);
                                             }
                                             **/


                                            ///////////////////////////////////////////////////////////////////////////

                                            JSONObject json = new JSONObject();
                                            json.put("machineId", machineId);
                                            json.put("tag", returnTag);
                                            //json.put("selectClientCode", selectClientCode);
                                            json.put("supplierCode", supplierCode);
                                            json.put("productCodes", new JSONArray(sendProductCodes));
                                            json.put("eachProductCount", new JSONArray(eachProductsSize));

                                            JSONObject requestJson = new JSONObject(json.toString());

                                            // RequestBody 생성
                                            RequestBody body = RequestBody.create(requestJson.toString(), JSON);

                                            Request request = new Request.Builder()
                                                    .url("http://localhost:8090/rfid/return")
                                                    .post(body)
                                                    .build();

                                            // 비동기 방식으로 요청 전송
                                            client.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    if (!response.isSuccessful()) {
                                                        throw new IOException("Unexpected code " + response);
                                                    }

                                                    // 수신된 JSON 데이터 디버그 로그로 출력
                                                    try {
                                                        String responseData = response.body().string();
                                                        JSONObject receivedJson = new JSONObject(responseData);
                                                        JSONObject realResponseData = new JSONObject(receivedJson.getJSONObject("data").toString());

                                                        /**
                                                         String deviceCode = realResponseData.getString("machineId");
                                                         String outTag = realResponseData.getString("tag");
                                                         String selectClientCode = realResponseData.getString("selectClientCode");
                                                         String supplierCode = realResponseData.getString("supplierCode");
                                                         String status = realResponseData.getString("status");
                                                         String latestReadingAt = realResponseData.getString("latestReadingAt");
                                                         JSONArray productCodes = realResponseData.getJSONArray("productDetails");

                                                         for (int i = 0; i < productCodes.length(); i++) {
                                                         JSONObject eachProduct = productCodes.getJSONObject(i);

                                                         Long productDetailId = eachProduct.getLong("productDetailId");
                                                         String rfidChipCode = eachProduct.getString("rfidChipCode");
                                                         String productSerialCode = eachProduct.getString("productSerialCode");
                                                         String productCode = eachProduct.getString("productCode");
                                                         String supplyCode = eachProduct.getString("supplierCode");
                                                         String clientCode = eachProduct.getString("clientCode");
                                                         String statusNow = eachProduct.getString("status");
                                                         int cycle = eachProduct.getInt("cycle");
                                                         String latestReadingAtNow = eachProduct.getString("latestReadingAt");

                                                         }
                                                         **/

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });

                                        } catch (Exception e) {
                                            Log.i("tag", "error :" + e);
                                        }
                                    }
                                });
                                th.start();

                                // 작업 처리 후 데이터 리셋
                                List<TagScan> resetTagScan = new ArrayList<>();
                                TagScanFragmentNew.scanCountText.setText("0");
                                //TagScanFragmentNew.scanTotalText.setText("0");
                                TagScanFragmentNew.scanListAdapterRv.setData(resetTagScan);

                                // dialog 닫기
                                dismiss();
                            }
                        });

                confirmDialog.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                confirmDialog.show();
            }
        });

/**
 // 세척 버튼 동작
 productCleaningBtn.setOnClickListener(new View.OnClickListener() {
@Override public void onClick(View v) {
AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
confirmDialog.setMessage("세척 처리를 진행합니다.\n!! 한 번 진행하면 취소할 수 없습니다.");
confirmDialog.setPositiveButton("예",
new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int which) {

// HttpUrlConnection
Thread th = new Thread(new Runnable() {
@Override public void run() {
try {
List<JSONObject> sendProductCodes = new ArrayList<>();
HashMap<String, Integer> productCodeList = new HashMap<>();
List<JSONObject> eachProductsSize = new ArrayList<>();

// RFID 기기가 존재하고 스캔한 데이터들이 있을 경우에 해제
if (!scanDatas.isEmpty()) {
for (int i = 0; i < scanDatas.size(); i++) {
if (scanDatas.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
String rfidChipCode = scanDatas.get(i).getEpc().substring(22);
String filteringCode = scanDatas.get(i).getEpc().substring(0, 8);
String productCode_ = scanDatas.get(i).getEpc().substring(8, 11);
String productSerialCode_ = scanDatas.get(i).getEpc().substring(8);

if (!productCodeList.containsKey(productCode_)) {
productCodeList.put(productCode_, 1);
} else {
productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
}

JSONObject eachTestData = new JSONObject();
eachTestData.put("rfidChipCode", "RFID" + rfidChipCode + "CHIP");
eachTestData.put("filteringCode", filteringCode);
eachTestData.put("productCode", productCode_);
eachTestData.put("productSerialCode", productSerialCode_);

sendProductCodes.add(eachTestData);
}
}

String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

for (int j = 0; j < productsArray.length; j++) {
JSONObject productStatusCount = new JSONObject();
productStatusCount.put("product", productsArray[j]);
productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

eachProductsSize.add(productStatusCount);
}
}
 **/
        ///////////////////////////////////////////////////////////////////////////

        /**
         // 테스트 용 스캔 데이터 총 1500개 - A00 / AA1 / BB1 제품군 (실제 스캔 데이터를 활용할 경우 해당 코드 주석 처리 필요)
         for (int i = 0; i < 500; i++) {
         String rfidChipCode = "RFID" + i + "CHIP";
         String filteringCode = "CCA2310";
         String productCode_ = "A00";
         String productSerialCode_ = (i + 1) + "D240313";

         if (!productCodeList.containsKey(productCode_)) {
         productCodeList.put(productCode_, 1);
         } else {
         productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
         }

         JSONObject eachTestData = new JSONObject();
         eachTestData.put("rfidChipCode", rfidChipCode);
         eachTestData.put("filteringCode", filteringCode);
         eachTestData.put("productCode", productCode_);
         eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

         sendProductCodes.add(eachTestData);
         }

         for (int i = 0; i < 500; i++) {
         String rfidChipCode = "RFID" + i + "CHIP";
         String filteringCode = "CCA2310";
         String productCode_ = "AA1";
         String productSerialCode_ = (i + 1) + "D240313";

         if (!productCodeList.containsKey(productCode_)) {
         productCodeList.put(productCode_, 1);
         } else {
         productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
         }

         JSONObject eachTestData = new JSONObject();
         eachTestData.put("rfidChipCode", rfidChipCode);
         eachTestData.put("filteringCode", filteringCode);
         eachTestData.put("productCode", productCode_);
         eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

         sendProductCodes.add(eachTestData);
         }

         for (int i = 0; i < 500; i++) {
         String rfidChipCode = "RFID" + i + "CHIP";
         String filteringCode = "CCA2310";
         String productCode_ = "BB1";
         String productSerialCode_ = (i + 1) + "D240313";

         if (!productCodeList.containsKey(productCode_)) {
         productCodeList.put(productCode_, 1);
         } else {
         productCodeList.put(productCode_, productCodeList.get(productCode_) + 1);
         }

         JSONObject eachTestData = new JSONObject();
         eachTestData.put("rfidChipCode", rfidChipCode);
         eachTestData.put("filteringCode", filteringCode);
         eachTestData.put("productCode", productCode_);
         eachTestData.put("productSerialCode", productCode_ + productSerialCode_);

         sendProductCodes.add(eachTestData);
         }

         String[] productsArray = productCodeList.keySet().toArray(new String[productCodeList.size()]);

         for (int j = 0; j < productsArray.length; j++) {
         JSONObject productStatusCount = new JSONObject();
         productStatusCount.put("product", productsArray[j]);
         productStatusCount.put("orderCount", productCodeList.get(productsArray[j]));

         eachProductsSize.add(productStatusCount);
         }
         **/


        ///////////////////////////////////////////////////////////////////////////
/**
 JSONObject json = new JSONObject();
 json.put("machineId", machineId);
 json.put("tag", cleaningTag);
 //json.put("selectClientCode", selectClientCode);
 json.put("supplierCode", supplierCode);
 json.put("productCodes", new JSONArray(sendProductCodes));
 json.put("eachProductCount", new JSONArray(eachProductsSize));

 JSONObject requestJson = new JSONObject(json.toString());

 // RequestBody 생성
 RequestBody body = RequestBody.create(requestJson.toString(), JSON);

        /**
         Request request = new Request.Builder()
         .url("http://localhost:8090/rfid/clean")
         .post(body)
         .build();
         **/

        /**
         // 비동기 방식으로 요청 전송
         client.newCall(request).enqueue(new Callback() {
        @Override public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
        }

        // 수신된 JSON 데이터 디버그 로그로 출력
        try {
        String responseData = response.body().string();
        JSONObject receivedJson = new JSONObject(responseData);
        JSONObject realResponseData = new JSONObject(receivedJson.getJSONObject("data").toString());
         **/
        /**
         String deviceCode = realResponseData.getString("machineId");
         String outTag = realResponseData.getString("tag");
         String selectClientCode = realResponseData.getString("selectClientCode");
         String supplierCode = realResponseData.getString("supplierCode");
         String status = realResponseData.getString("status");
         String latestReadingAt = realResponseData.getString("latestReadingAt");
         JSONArray productCodes = realResponseData.getJSONArray("productDetails");

         for (int i = 0; i < productCodes.length(); i++) {
         JSONObject eachProduct = productCodes.getJSONObject(i);

         Long productDetailId = eachProduct.getLong("productDetailId");
         String rfidChipCode = eachProduct.getString("rfidChipCode");
         String productSerialCode = eachProduct.getString("productSerialCode");
         String productCode = eachProduct.getString("productCode");
         String supplyCode = eachProduct.getString("supplierCode");
         String clientCode = eachProduct.getString("clientCode");
         String statusNow = eachProduct.getString("status");
         int cycle = eachProduct.getInt("cycle");
         String latestReadingAtNow = eachProduct.getString("latestReadingAt");

         }
         **/
/**
 } catch (JSONException e) {
 e.printStackTrace();
 }

 }
 });

 } catch (Exception e) {
 Log.i("tag", "error :" + e);
 }
 }
 });
 th.start();

 // 작업 처리 후 데이터 리셋
 List<TagScan> resetTagScan = new ArrayList<>();
 TagScanFragmentNew.scanCountText.setText("0");
 //TagScanFragmentNew.scanTotalText.setText("0");
 TagScanFragmentNew.scanListAdapterRv.setData(resetTagScan);

 // dialog 닫기
 dismiss();
 }
 });

 confirmDialog.setNegativeButton("아니오",
 new DialogInterface.OnClickListener() {
 public void onClick(DialogInterface dialog, int which) {
 dialog.dismiss();
 }
 });

 confirmDialog.show();
 }
 });
 **/
    }

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int itemIndex = customer_spinner_inventory_mode.getSelectedItemPosition();
            String itemText = (String) customer_spinner_inventory_mode.getSelectedItem();
            selectClientCode = SettingFragment.staticClientCodes.get(itemIndex);

            System.out.println("[하위]");
            System.out.println("선택한 고객사 - 인덱스 : " + itemIndex + " / 텍스트 값 : " + itemText + " / 고객사 코드 : " + selectClientCode);

            Log.i(TAG, "Spinner selected item = " + itemIndex + " - " + itemText);
        }


        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int itemIndex = customer_spinner_inventory_mode.getSelectedItemPosition();
        String itemText = (String) customer_spinner_inventory_mode.getSelectedItem();
        selectClientCode = SettingFragment.staticClientCodes.get(itemIndex);

        System.out.println("[상위]");
        System.out.println("선택한 고객사 - 인덱스 : " + itemIndex + " / 텍스트 값 : " + itemText + " / 고객사 코드 : " + selectClientCode);

        Log.i(TAG, "Spinner selected item = " + itemIndex + " - " + itemText);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
