package com.ubx.rfid_demo.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagScan;

import java.util.HashMap;
import java.util.List;

public class EachScanListAdapterRv extends RecyclerView.Adapter<EachScanListAdapterRv.ViewHolder> {

    private List<String> data;
    private Context context;

    public EachScanListAdapterRv(List<String> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public void setData(List<String> data) {
        this.data = data;

        if (this.data.size() > 0) {
            notifyItemRangeChanged(0, data.size());
        } else {
            notifyDataSetChanged();
        }
    }

    // 스캔한 태그 데이터 가져오기
    public List<String> getData() {
        return this.data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.each_tag_scan_item, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.refreshView(position, data.get(position));

    }

    @Override
    public int getItemCount() {
        return null != data ? data.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        TextView productName;
        TextView productCode;
        TextView productCount;
        HashMap<String, String> productsInfo;


        public ViewHolder(View view) {
            super(view.getRootView());
            this.mView = view;

            //productCode = mView.findViewById(R.id.each_product_code);
            productName = mView.findViewById(R.id.each_product_name);
            productCount = mView.findViewById(R.id.each_product_count);
            productsInfo = SettingFragment.staticProductsInfo;
        }

        private void refreshView(int position, String data) {
            String[] productInfo = data.split("|");
            String productCode_ = productInfo[0];
            String productCount_ = productInfo[1];
            String productName_ = productsInfo.get(productCode_);

            productName.setText(productName_); // 제품 명
            productCode.setText(productCode_); // 제품 코드
            productCount.setText(productCount_); // 제품 스캔 수량

            System.out.println("제품 코드 : " + productCode_ + " / 제품명 : " + productName_ + " / 스캔 수량 : " + productCount_ );
        }

    }
}
