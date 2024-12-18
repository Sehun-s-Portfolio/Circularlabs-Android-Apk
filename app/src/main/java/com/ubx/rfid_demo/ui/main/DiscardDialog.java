package com.ubx.rfid_demo.ui.main;

import static com.ubx.rfid_demo.ui.main.TagScanFragmentNew.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagScan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiscardDialog extends Dialog implements AdapterView.OnItemSelectedListener {
    private TextView device_code_value, total_discard_data_value;
    private Button productDiscardBtn;
    private Spinner customer_discard_spinner_inventory_mode;
    private OkHttpClient client = new OkHttpClient();
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String deviceCode;
    private String selectClientCode;
    private ArrayAdapter arrayAdapter;
    private List<TagScan> scanDatas;
    private HashMap<String, Integer> productCodes = new HashMap<>();
    private List<String> eachScanProducts = new ArrayList<String>();
    private HashMap<String, String> productNameInfo;

    @SuppressLint("MissingInflatedId")
    public DiscardDialog(Context context, String contents, String scanCount, List<TagScan> scanData, String deviceCode, String supplierCode, MainActivity mActivity) {
        super(context);
        setContentView(R.layout.discard_dialog);
        this.deviceCode = deviceCode;
        this.scanDatas = scanData;

        productNameInfo = SettingFragment.staticProductsInfo;

        // 기기 ID
        device_code_value = findViewById(R.id.device_code_value);
        device_code_value.setText(this.deviceCode);

        // 스캔한 데이터 총 수량
        total_discard_data_value = findViewById(R.id.total_discard_data_value);
        total_discard_data_value.setText(scanCount);

        productDiscardBtn = findViewById(R.id.btn_product_discard);
        final String discardTag = productDiscardBtn.getTag().toString();

        // 고객사 select 선택 항목
        customer_discard_spinner_inventory_mode = findViewById(R.id.customer_discard_spinner_inventory_mode);
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, SettingFragment.staticClients);
        customer_discard_spinner_inventory_mode.setAdapter(arrayAdapter);
        customer_discard_spinner_inventory_mode.setVerticalScrollBarEnabled(true);

        final String machineId = device_code_value.getText().toString();


        /**
         // 태스트 용 데이터
         String eachProductCountInfo1 = "A00-제품1-2";
         String eachProductCountInfo2 = "AA1-제품2-2";
         String eachProductCountInfo3 = "BB1-제품3-2";
         eachScanProducts.add(eachProductCountInfo1);
         eachScanProducts.add(eachProductCountInfo2);
         eachScanProducts.add(eachProductCountInfo3);
         **/

        //////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////


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


        LinearLayout productLinearLayout = findViewById(R.id.discard_product_listup);
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

        customer_discard_spinner_inventory_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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


        // 폐기 버튼 동작
        productDiscardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectClientCode.equals("CODE")) {
                    AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                    confirmDialog.setMessage("폐기 처리를 진행합니다.\n!! 한 번 진행하면 취소할 수 없습니다.");
                    confirmDialog.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    // HttpUrlConnection
                                    Thread th = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                List<JSONObject> sendProductCodes = new ArrayList<>();

                                                // RFID 기기가 존재하고 스캔한 데이터들이 있을 경우에 해제
                                                if (!scanDatas.isEmpty()) {
                                                    for (int i = 0; i < scanDatas.size(); i++) {
                                                        if (scanDatas.get(i).getEpc().substring(0, 8).contains("CCA2310")) {
                                                            String rfidChipCode = scanDatas.get(i).getEpc().substring(22);
                                                            String filteringCode = scanDatas.get(i).getEpc().substring(0, 8);
                                                            String productCode_ = scanDatas.get(i).getEpc().substring(8, 11);
                                                            String productSerialCode_ = scanDatas.get(i).getEpc().substring(8);

                                                            JSONObject eachTestData = new JSONObject();
                                                            eachTestData.put("rfidChipCode", "RFID" + rfidChipCode + "CHIP");
                                                            eachTestData.put("filteringCode", filteringCode);
                                                            eachTestData.put("productCode", productCode_);
                                                            eachTestData.put("productSerialCode", productSerialCode_);

                                                            sendProductCodes.add(eachTestData);
                                                        }
                                                    }
                                                }

                                                ///////////////////////////////////////////////////////////////////////////

                                                /**
                                                 // 테스트 용 스캔 데이터 100개 (실제 스캔 데이터를 활용할 경우 해당 코드 주석 처리 필요)
                                                 for (int k = 2; k < 4; k++) {
                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", "RFID" + k + "CHIP");
                                                 eachTestData.put("filteringCode", "CCA2310");
                                                 eachTestData.put("productCode", "A00");
                                                 eachTestData.put("productSerialCode", "A00" + (k + 1) + "D240313");

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 for (int k = 2; k < 4; k++) {
                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", "RFID" + k + "CHIP");
                                                 eachTestData.put("filteringCode", "CCA2310");
                                                 eachTestData.put("productCode", "AA1");
                                                 eachTestData.put("productSerialCode", "AA1" + (k + 1) + "D240313");

                                                 sendProductCodes.add(eachTestData);
                                                 }

                                                 for (int k = 2; k < 4; k++) {
                                                 JSONObject eachTestData = new JSONObject();
                                                 eachTestData.put("rfidChipCode", "RFID" + k + "CHIP");
                                                 eachTestData.put("filteringCode", "CCA2310");
                                                 eachTestData.put("productCode", "BB1");
                                                 eachTestData.put("productSerialCode", "BB1" + (k + 1) + "D240313");

                                                 sendProductCodes.add(eachTestData);
                                                 }
                                                 **/


                                                ///////////////////////////////////////////////////////////////////////////

                                                JSONObject json = new JSONObject();
                                                json.put("machineId", machineId);
                                                json.put("tag", discardTag);
                                                json.put("selectClientCode", selectClientCode);
                                                json.put("supplierCode", supplierCode);
                                                json.put("productCodes", new JSONArray(sendProductCodes));

                                                JSONObject requestJson = new JSONObject(json.toString());

                                                // RequestBody 생성
                                                RequestBody body = RequestBody.create(requestJson.toString(), JSON);

                                                Request request = new Request.Builder()
                                                        .url("http://localhost:8090/rfid/discard")
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
                                                             String discardAt = realResponseData.getString("discardAt");
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

    }

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int itemIndex = customer_discard_spinner_inventory_mode.getSelectedItemPosition();
            String itemText = (String) customer_discard_spinner_inventory_mode.getSelectedItem();
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
        int itemIndex = customer_discard_spinner_inventory_mode.getSelectedItemPosition();
        String itemText = (String) customer_discard_spinner_inventory_mode.getSelectedItem();
        selectClientCode = SettingFragment.staticClientCodes.get(itemIndex);

        System.out.println("[상위]");
        System.out.println("선택한 고객사 - 인덱스 : " + itemIndex + " / 텍스트 값 : " + itemText + " / 고객사 코드 : " + selectClientCode);

        Log.i(TAG, "Spinner selected item = " + itemIndex + " - " + itemText);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
