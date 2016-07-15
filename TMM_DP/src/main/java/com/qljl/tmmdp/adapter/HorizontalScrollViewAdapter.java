package com.qljl.tmmdp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qljl.tmmdp.R;
import com.qljl.tmmdp.modle.imageloader.ImageViewNetwork;
import com.qljl.tmmdp.modle.imageloader.NetworkPhotoTask;
import com.qljl.tmmdp.util.DPHelper;

import java.util.List;

public class HorizontalScrollViewAdapter
{
	private Context context;
	private LayoutInflater mInflater;
	private List<String> list;
	private float width,height,marginLeft,marginRight,marginTop,marginBottom;

	public HorizontalScrollViewAdapter(Context ctx, List<String> list,float width,float height,float marginLeft,
									   float marginTop,float marginRight,float marginBottom) {
		this.context = ctx;
		this.list = list;
		this.list = list;
		mInflater = LayoutInflater.from(ctx);
		this.width = width;
		this.height = height;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
	}

	public int getCount()
	{
		return list.size();
	}

	public Object getItem(int position)
	{
		return list.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.image, parent, false);
			viewHolder.imgView = (ImageViewNetwork) convertView
					.findViewById(R.id.imgView);
			viewHolder.textView = (TextView) convertView.findViewById(R.id.textView);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		RelativeLayout.LayoutParams lp;
		lp= (RelativeLayout.LayoutParams) viewHolder.imgView.getLayoutParams();
		lp.width = DPHelper.px2dip(context, width);
		lp.height = DPHelper.px2dip(context, height);
		lp.setMargins(DPHelper.px2dip(context, marginLeft),DPHelper.px2dip(context,marginTop),DPHelper.px2dip(context, marginRight),
				DPHelper.px2dip(context,marginBottom));
		NetworkPhotoTask task = NetworkPhotoTask.build();
		task.url = list.get(position);
		task.width = DPHelper.px2dip(context, width);
		task.height = DPHelper.px2dip(context, height);
		//开始加载图片显示的
		task.startDrawId = R.mipmap.ic_launcher;
		//图片加载失败显示的照片
		task.errorDrawId = R.mipmap.ic_launcher;
		viewHolder.imgView.setImageParams(task);
		viewHolder.imgView.setLayoutParams(lp);
		return convertView;
	}

	private class ViewHolder
	{
		ImageViewNetwork imgView;
		TextView textView;
	}

	/*@Override
	protected View newView(int position, Object element) {
		return mInflater.inflate(R.layout.image,null);
	}*/

	/*@Override
	protected void bindView(View view, int position, Object element) {
		ImageViewNetwork imgView = (ImageViewNetwork) view.findViewById(R.id.imgView);
		LinearLayout.LayoutParams lp;
		lp= (LinearLayout.LayoutParams) imgView.getLayoutParams();
		lp.width = DPHelper.px2dip(context, width);
		lp.height = DPHelper.px2dip(context, height);
		lp.setMargins(DPHelper.px2dip(context, marginLeft),DPHelper.px2dip(context,marginTop),DPHelper.px2dip(context, marginRight),
				DPHelper.px2dip(context,marginBottom));
		NetworkPhotoTask task = NetworkPhotoTask.build();
		WindowManager wm = ((Activity)context).getWindowManager();
		task.url = (String) list.get(position);
		task.width = DPHelper.px2dip(context, width);
		task.height = DPHelper.px2dip(context, height);
		//开始加载图片显示的
		task.startDrawId = R.mipmap.ic_launcher;
		//图片加载失败显示的照片
		task.errorDrawId = R.mipmap.ic_launcher;
		imgView.setImageParams(task);
		imgView.setLayoutParams(lp);
	}*/


}
