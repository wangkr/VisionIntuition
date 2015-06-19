package com.kairong.horizonListView;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.viSize;
import com.kairong.vision_recognition.FaceMatActivity;
import com.kairong.vision_recognition.R;

import java.util.List;
import java.util.Vector;

public class HorizontalListViewAdapter extends BaseAdapter{
    private int iconId;
    private List<String> filepaths;
    private Context mContext;
    private LayoutInflater mInflater;
    private Vector<Integer> imageStatus;
    Bitmap iconBitmap = null;
    Bitmap faceBitmap = null;
    private int selectIndex = -1;
    private final int LEFT_FACE_IMAGE_VIEW = 1;
    private final int RIGHT_FACE_IMAGE_VIEW = 2;
    private final int NONE_FACE_IMAGE = -1;

    public HorizontalListViewAdapter(Context context, List<String> filepaths,Vector<Integer> imageStatus,int iconId){
        this.mContext = context;
        this.filepaths = filepaths;
        this.imageStatus = imageStatus;
        this.iconId = iconId;
        mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
    }
    public void setFilepaths(List<String> filepaths){
        this.filepaths = filepaths;
    }
    @Override
    public int getCount() {
        return filepaths.size()+1;
    }
    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView==null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.horizontal_list_item, null);
            holder.mImage=(ImageView)convertView.findViewById(R.id.img_list_item);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        if(position == selectIndex){
            convertView.setSelected(true);
        }else{
            convertView.setSelected(false);
        }
        int w = mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
        int h = mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);
        if(position == filepaths.size()){
            iconBitmap = getPropThumnail(iconId);
            holder.mImage.setImageBitmap(iconBitmap);
        }else{
            // 为了避免大内存分配，采取加载samplesize的图片
            viSize reqSize = BitmapUtil.getImageReq(filepaths.get(position));
            Bitmap temp = BitmapUtil.decodeSampledBitmapFromFile(filepaths.get(position),reqSize);
            faceBitmap = ThumbnailUtils.extractThumbnail(temp,w,h,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            holder.mImage.setImageBitmap(faceBitmap);
            // 设置选中背景
            if(imageStatus.get(position)>0) {
                if (imageStatus.get(position) == LEFT_FACE_IMAGE_VIEW) {
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.hList_left_face_item_bk_color));
                } else {
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.hList_right_face_item_bk_color));
                }
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        private ImageView mImage;
    }
    private Bitmap getPropThumnail(int id){
        Drawable d = mContext.getResources().getDrawable(id);
        Bitmap b = BitmapUtil.drawableToBitmap(d);
        int w = mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
        int h = mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);

        return ThumbnailUtils.extractThumbnail(b, w, h,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }
    public void setSelectIndex(int i){
        selectIndex = i;
    }
}
