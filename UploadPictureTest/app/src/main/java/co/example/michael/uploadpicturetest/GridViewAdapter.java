package co.example.michael.uploadpicturetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.ViewDragHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Michael on 2015/1/28.
 */
public class GridViewAdapter extends BaseAdapter {

    private ArrayList<Bitmap> bitmapLists;

    private Context context;

    public GridViewAdapter(Context context,ArrayList<Bitmap> bitmapLists){
        this.bitmapLists = bitmapLists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return bitmapLists.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bitmap bitmap = (Bitmap)getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(R.layout.image_layout,null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView)view.findViewById(R.id.image_view);

            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();

            resetViewHolder(viewHolder);
        }

        viewHolder.imageView.setImageBitmap(bitmap);

        return view;

    }

    private void resetViewHolder(ViewHolder holder){
        holder.imageView.setImageBitmap(null);
    }

    private class ViewHolder{
        private ImageView imageView;
    }
}
