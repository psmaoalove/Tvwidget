
package com.stv.widget.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stv.widget.R;

public class GridViewHolder extends OpenPresenter.ViewHolder {

    public ImageView iv;
    public TextView tv;
    public TextView head_tv;

    public GridViewHolder(View itemView) {
        super(itemView);
        // iv = (ImageView)itemView.findViewById(R.id.);
        tv = (TextView) itemView.findViewById(R.id.textView);
    }

}
