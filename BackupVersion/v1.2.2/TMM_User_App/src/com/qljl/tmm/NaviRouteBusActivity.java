package com.qljl.tmm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.qljl.tmm.bean.BusLine;
import com.qljl.tmm.util.Constants;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;

/**
 * 公交线路
 * 
 * @author lw
 *
 */
public class NaviRouteBusActivity extends Activity implements
		OnMapLoadedListener, OnClickListener {
	private Context context;
	private ImageView routebus_back_view;// 返回
	private MapView mMapView;
	private ListView busLineList;// bus路线集合
	// 地图导航资源
	private AMap mAmap;
	// private RouteOverLay mRouteOverLay;
	private BusRouteResult busRouteResult;// 公交模式查询结果
	public List<BusLine> busLine;
	BusRouteOverlay routeOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routebus);
		context = NaviRouteBusActivity.this;
		System.out.println("lw       NaviRouteBusActivity");
		initView(savedInstanceState);
	}

	/**
	 * 初始化控件
	 * 
	 * @param savedInstanceState
	 */
	private void initView(Bundle savedInstanceState) {
		routebus_back_view = (ImageView) findViewById(R.id.routebus_back_view);
		routebus_back_view.setOnClickListener(this);
		busLineList = (ListView) findViewById(R.id.busLineList);
		mMapView = (MapView) findViewById(R.id.routebusmap);
		mMapView.onCreate(savedInstanceState);
		mAmap = mMapView.getMap();
		mAmap.setOnMapLoadedListener(this);
		initLine();
		BusListAdapter blAdapter = new BusListAdapter(context, busLine);
		busLineList.setAdapter(blAdapter);
		busLineList.setOnItemClickListener(busOnItemClickListener);
	}

	/**
	 * 初始化线路
	 */
	private void initLine() {
		if (Constants.rCode == 0) {
			if (Constants.busRouteResult != null
					&& Constants.busRouteResult.getPaths() != null
					&& Constants.busRouteResult.getPaths().size() > 0) {
				busRouteResult = Constants.busRouteResult;
				BusPath busPath = busRouteResult.getPaths().get(0);
				mAmap.clear();// 清理地图上的所有覆盖物
				routeOverlay = new BusRouteOverlay(context, mAmap, busPath,
						busRouteResult.getStartPos(),
						busRouteResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();// 移动镜头到当前的视角
				System.out.println("lw     规划成功！");
				if (busLine == null) {
					busLine = new ArrayList<BusLine>();
					for (int i = 0; i < busRouteResult.getPaths().size(); i++) {
						System.out.println("lw     busLine----"
								+ busRouteResult.getPaths().get(i).getSteps()
										.get(0).getBusLine().getBusLineName());
						BusLine bl = new BusLine();
						bl.setBusLineName(busRouteResult.getPaths().get(i).getSteps()
										.get(0).getBusLine().getBusLineName());
						bl.setBusDetail(convert(busRouteResult.getPaths().get(i).getDuration())+" | "
								+busRouteResult.getPaths().get(i).getBusDistance()/2+"公里 | 步行"
								+busRouteResult.getPaths().get(i).getWalkDistance()+"米 | "
								+busRouteResult.getPaths().get(i).getCost()+"元 | "
								+busRouteResult.getPaths().get(i).getSteps().get(0).getBusLine().getPassStationNum()+"站");
						busLine.add(bl);
						//getPaths():getCost()返回公交换乘方案的花费，单位元;
						//getBusDistance()返回此方案的公交行驶的总距离 ，单位米;
						//getDuration()返回整条路线行驶时间;
						//getWalkDistance()返回此方案的总步行距离，单位米。
						
						//getSteps():getBusLines()返回此路段的公交导航信息;
						//
//						System.out.println("lw   Bus==="
//								+busRouteResult.getPaths().get(i).getBusDistance()+"|"
//								+busRouteResult.getPaths().get(i).getDuration()+"|"
//								+busRouteResult.getPaths().get(i).getWalkDistance()+"|"
//								+busRouteResult.getPaths().get(i).getCost()+"|"
//								+busRouteResult.getPaths().get(i).getSteps().get(0).getBusLine().getPassStationNum()
//								);
					}
				}
			} else {
				System.out.println("lw      noresult!");
			}
		}
	}
	
	/**
	 * 
	 * @param second
	 * @return
	 */
	public static String convert(long second){
		int h=0,d=0,s=0;
		s=(int) (second%60);
		second=second/60;
		d=(int) (second%60);
		h=(int) (second/60);
		return h+"时"+d+"分"+s+"秒";
	}

	/**
	 * 公交线路点击
	 */
	public OnItemClickListener busOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			System.out.println("lw        position===" + position);
			BusPath busPath = busRouteResult.getPaths().get(position);
			mAmap.clear();// 清理地图上的所有覆盖物
			routeOverlay = new BusRouteOverlay(context, mAmap, busPath,
					busRouteResult.getStartPos(), busRouteResult.getTargetPos());
			routeOverlay.removeFromMap();
			routeOverlay.addToMap();
			routeOverlay.zoomToSpan();
		}
	};

	@Override
	public void onMapLoaded() {
		// if (mRouteOverLay != null) {
		// mRouteOverLay.zoomToSpan();
		//
		// }
		initLine();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		initLine();
		
		TCAgent.onResume(this);
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		
		TCAgent.onPause(this);
		MobclickAgent.onPause(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	public class BusListAdapter extends BaseAdapter {
		private Context context;
		private List<BusLine> lt = new ArrayList<BusLine>();
		private LayoutInflater inflater;

		public BusListAdapter(Context context, List<BusLine> lt) {
			super();
			this.context = context;
			this.lt = lt;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return lt.size();
		}

		@Override
		public Object getItem(int position) {
			return lt.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.bus_item, null);
				viewHolder.busLineName = (TextView) convertView
						.findViewById(R.id.busLineName);
				viewHolder.busLineDetail =  (TextView) convertView.findViewById(R.id.busLineDetail);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.busLineName.setText(lt.get(position).getBusLineName());
			viewHolder.busLineDetail.setText(lt.get(position).getBusDetail());
			return convertView;
		}
	}

	class ViewHolder {
		TextView busLineName;
		TextView busLineDetail;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.routebus_back_view:
			finish();
			break;

		default:
			break;
		}

	}

}
