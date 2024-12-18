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

public class ScanListAdapterRv extends RecyclerView.Adapter<ScanListAdapterRv.ViewHolder> {

    private List<TagScan> data;
    private Context context;
    public OnClickListener onClickListener;
    static String inputScanDeviceCode;

    public ScanListAdapterRv(List<TagScan> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public void setData(List<TagScan> data) {
        this.data = data;

        if (this.data.size()>0) {
            notifyItemRangeChanged(0, data.size());
        }else {
            notifyDataSetChanged();
        }
    }

    // 스캔한 태그 데이터 가져오기
    public List<TagScan> getData(){
        return this.data;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(  ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.tag_scan_item, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if(data.get(position).getEpc().substring(0, 8).contains("CCA2310")){
            if (onClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickListener.onSelectEpc(data.get(position), position);
                    }
                });
            }

            holder.refreshView(position, data.get(position));
        }
    }

    public interface OnClickListener {
        void onSelectEpc(TagScan data, int position);
    }

    @Override
    public int getItemCount() {
        return null != data ? data.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        TextView productName;
        TextView productCode ;
        TextView productSerialCode;
        TextView expressSupplierCode;

        HashMap<String, String> productsInfo;


        public ViewHolder( View  view) {
            super(view.getRootView());
            this.mView = view;

            productName = mView.findViewById(R.id.product_name);
            //productCode = mView.findViewById(R.id.product_code);
            productSerialCode = mView.findViewById(R.id.product_serial_code);
            //expressSupplierCode = TagScanFragmentNew.expressSupplierCode;
            productsInfo = SettingFragment.staticProductsInfo;
        }

        private void refreshView(int position, TagScan data) {

            String productCode_ = data.getEpc().substring(8, 11);
            String productSerialCode_ = data.getEpc().substring(11);

            if(data.getEpc().substring(0, 8).contains("CCA2310")){
                String relatedProductName = productsInfo.get(productCode_);

                productName.setText(relatedProductName); // 제품 명
                //productCode.setText(productCode_); // 제품 코드
                productSerialCode.setText(productSerialCode_); // 각 제품 분류 코드
            }
        }
    }
}
