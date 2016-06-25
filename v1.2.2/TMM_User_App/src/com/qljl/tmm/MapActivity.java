package com.qljl.tmm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.search.poisearch.PoiSearch;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.qljl.tmm.util.AMapUtil;
import com.qljl.tmm.util.Constants;
import com.qljl.tmm.util.TTSController;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;

/**
 * 地图导航类
 * 
 * @author lw
 *
 */
public class MapActivity extends Activity implements OnClickListener,
		OnKeyListener,InfoWindowAdapter {
	private Context context;
	private Button driveBtn, walkBtn, busBtn;
	private TextView fromText, toText;
	private TextView map_back_view;//退出导航
	MapView mMapView;
	// 地图和导航核心逻辑类
	private AMap mAmap;
	private AMapNaviListener mAmapNaviListener;
	AMapNavi mAmapNavi;
	private LocationManagerProxy mAMapLocationManager;
	private LocationSource.OnLocationChangedListener mListener;
	// 定位传感器///////////////
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private long lastTime = 0;
	private final int TIME_SENSOR = 100;
	private float mAngle;
	/***
	 * 我的位置
	 */
	private Marker mGPSMarker;
	// ///////////////
	/**
	 * 是否中心定位过
	 */
	private boolean isMoveCamera = false;
	// 地理编码//////////////////
	private GeocodeSearch geocoderSearch;

	/**
	 * marker的集合
	 */
	private ArrayList<Marker> markerlst;
	/**
	 * 是否定位成功
	 */
	private boolean isLocationSuccess = false;

	/**
	 * 地图定位位置
	 */
	private AMapLocation myAMapLocation;

	// 驾车路径规划起点，终点的list
	private List<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
	private List<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
	private List<NaviLatLng> mWayPoints = new ArrayList<NaviLatLng>();
	// 记录起点、终点位置
	private NaviLatLng mStartPoint = new NaviLatLng();
	private NaviLatLng mEndPoint = new NaviLatLng();
	// 记录起点、终点在地图上添加的Marker
	private Marker mStartMarker;
	private Marker mEndMarker;
	// 记录导航种类，用于记录当前选择是驾车还是步行
	private int mTravelMethod = DRIVER_NAVI_METHOD;
	private static final int DRIVER_NAVI_METHOD = 0;// 驾车导航
	private static final int WALK_NAVI_METHOD = 1;// 步行导航
	// 计算路的状态
	private final static int GPSNO = 0;// 使用我的位置进行计算、GPS定位还未成功状态
	private final static int CALCULATEERROR = 1;// 启动路径计算失败状态
	private final static int CALCULATESUCCESS = 2;// 启动路径计算成功状态
	private ProgressDialog mProgressDialog;// 路径规划过程显示状态
	String mLatitude = "";//经度
	String mLongitude = "";//纬度
	String mAddress = "";//地址
	String cityCode = "";//城市编码
	String mCity = "";//城市
	
	private LinearLayout fromLinear,toLinear;
	LatLonPoint startLatLonPoint,endLatLonPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		context = MapActivity.this;
		Intent intent = getIntent();
		mLatitude = intent.getStringExtra("mLatitude");
		mLongitude = intent.getStringExtra("mLongitude");
		mAddress = intent.getStringExtra("mAddress");
		mCity = intent.getStringExtra("mCity");
		initView(savedInstanceState);
	}

	/**
	 * 初始化地图
	 * 
	 * @param savedInstanceState
	 */
	private void initView(Bundle savedInstanceState) {
		driveBtn = (Button) findViewById(R.id.driveBtn);
		walkBtn = (Button) findViewById(R.id.walkBtn);
		busBtn = (Button) findViewById(R.id.busBtn);
		driveBtn.setOnClickListener(this);
		walkBtn.setOnClickListener(this);
		busBtn.setOnClickListener(this);
		fromText = (TextView) findViewById(R.id.fromText);
		toText = (TextView) findViewById(R.id.toText);
		toText.setText(mAddress);
		map_back_view = (TextView) findViewById(R.id.map_back_view);
		map_back_view.setOnClickListener(this);
		fromLinear = (LinearLayout) findViewById(R.id.fromLinear);
		toLinear = (LinearLayout) findViewById(R.id.toLinear);
		fromLinear.setOnClickListener(this);
		toLinear.setOnClickListener(this);

		mMapView = (MapView) findViewById(R.id.map);

		mMapView.onCreate(savedInstanceState);
		mMapView.setKeepScreenOn(true);
		// 初始语音播报资源
		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
		mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
		TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
		ttsManager.init();
		mAmapNavi.setAMapNaviListener(ttsManager);// 设置语音模块播报
		if (mAmap == null) {
			mAmap = mMapView.getMap();
			mAmap.getUiSettings().setZoomControlsEnabled(false);
			mAmap.moveCamera(CameraUpdateFactory.zoomTo(12));
			// aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
			// mAmap.setMapType(AMap.MAP_TYPE_NIGHT);
			mAmap.setLocationSource(locationSource);// 设置定位监听
			mAmap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定
																	// 位按钮是否显示
			mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			mAmap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			mAmap.setOnMapClickListener(onMapClickListener);
			/** 标志物点击事件 */
			mAmap.setOnMarkerClickListener(onMarkerClickListener);
			// 初始化Marker添加到地图
			mStartMarker = mAmap
					.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
							.fromBitmap(BitmapFactory.decodeResource(
									getResources(), R.drawable.start))));
			mEndMarker = mAmap.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
							.decodeResource(getResources(), R.drawable.end))));
			mAmap.setInfoWindowAdapter(this);
			// 地理编码相关方法
			geocoderSearch = new GeocodeSearch(context);
			geocoderSearch.setOnGeocodeSearchListener(geocodeSearchListener);
			if (myAMapLocation != null) {
				LatLng latLng = new LatLng(myAMapLocation.getLatitude(),
						myAMapLocation.getLongitude());
				mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
			}
		}
		// 初始化传感器
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

	/***
	 * 定位变化监听
	 */
	private AMapLocationListener aMapLocationListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {
			if (mListener != null && aMapLocation != null) {
				if (aMapLocation != null
						&& aMapLocation.getAMapException().getErrorCode() == 0) {
					// System.out.println("lw     aMapLocation.getCityCode()="
					// + aMapLocation.getCityCode());
					// System.out.println("lw     aMapLocation.getCity()="
					// + aMapLocation.getCity());
					// showToast("定位成功 aMapLocation=" +
					// aMapLocation.toString());
					myAMapLocation = aMapLocation;
					if (!isLocationSuccess) {
//						mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
						NaviLatLng naviLatLng = new NaviLatLng(
								aMapLocation.getLatitude(),
								aMapLocation.getLongitude());
						mStartPoint = naviLatLng;
						LatLng latLng = new LatLng(aMapLocation.getLatitude(),
								aMapLocation.getLongitude());
						mStartMarker.setPosition(latLng);
						mStartPoints.clear();
						mStartPoints.add(mStartPoint);
						showLocation();
						LatLonPoint latLonPoint = new LatLonPoint(
								aMapLocation.getLatitude(),
								aMapLocation.getLongitude());
						startLatLonPoint = latLonPoint;
						getAddress(latLonPoint);
					}
					isLocationSuccess = true;
				} else {
					Log.e("AmapErr", "lw  Location ERR:"
							+ aMapLocation.getAMapException().getErrorCode());
				}
			}
		}

		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

	};

	private void showLocation() {
		if (myAMapLocation == null) {
			showToast("定位失败,请检查网络");
			return;
		}
		ArrayList<MarkerOptions> markerOptionlst = new ArrayList<MarkerOptions>();
		// 设置 我的位置
		MarkerOptions mGPSMarkerOption = getMyMarker(new LatLng(
				myAMapLocation.getLatitude(), myAMapLocation.getLongitude()));
		mGPSMarkerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
		markerOptionlst.add(mGPSMarkerOption);
		// if (BaseApplication.car.getPoi() != null) {
//		 MarkerOptions carMarkerOption = getEndMarker(new
//		 LatLng(getLatlon()));
//		 markerOptionlst.add(carMarkerOption);
//		 }
		// markerlst = aMap.addMarkers(markerOptionlst, true);
		// if (markerlst.size() == 1) {
		// LatLng latLng = new LatLng(myAMapLocation.getLatitude(),
		// myAMapLocation.getLongitude());
		// aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		// }
		//将中文编码改成地理坐标
		getLatlon(mAddress, mCity);
		getEndMarker();
	}

	/**
	 * 设置目的地的marker
	 */
	private void getEndMarker() {
		double latitude = 0.0;
		latitude = Double.valueOf(mLatitude.toString());
		double longitude = 0.0;
		longitude = Double.valueOf(mLongitude.toString());
		LatLonPoint latLonPoint = new LatLonPoint(latitude,
				longitude);
		mAmap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				AMapUtil.convertToLatLng(latLonPoint),
				15));
		endLatLonPoint = latLonPoint;
		mEndMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
		LatLng naviLatLng = new LatLng(latitude, longitude);
		// mEndMarker.remove();
//		cityCode = address.getAdcode();
		//添加标记
		mEndMarker.setPosition(naviLatLng);
//		mEndMarker.setTitle(cityCode);//标题
		mEndMarker.setSnippet(mAddress);
		mEndMarker.setPerspective(true);
		mEndMarker.setDraggable(false);
		mEndMarker.setPeriod(50);
		mEndMarker.showInfoWindow();
		
		NaviLatLng naviLatLngs = new NaviLatLng(latitude, longitude);
		mEndPoint = naviLatLngs;
		mEndPoints.clear();
		mEndPoints.add(mEndPoint);
	}

	private MarkerOptions getMyMarker(LatLng myLatLng) {
		// 自定义图标
		// View view = inflater.inflate(R.layout.markericom, null);
		// CircleImageView head = (CircleImageView)
		// view.findViewById(R.id.head);
		// head.setImageResource(R.mipmap.abc_header);

		BitmapDescriptor viewIcon = BitmapDescriptorFactory
				.fromResource(R.drawable.start)/*
												 * fromView(view)
												 */;

		// 我的位置
		MarkerOptions mGPSMarkerOption = new MarkerOptions().icon(viewIcon)
				.anchor((float) 0.5, (float) 0.5).setFlat(true).draggable(true)
				.perspective(true).setInfoWindowOffset(0, 0).position(myLatLng);
		return mGPSMarkerOption;
	}

	/***
	 * 定位资源设置监听
	 */
	private LocationSource locationSource = new LocationSource() {
		@Override
		public void activate(OnLocationChangedListener onLocationChangedListener) {
			mListener = onLocationChangedListener;
			if (mAMapLocationManager == null) {
				mAMapLocationManager = LocationManagerProxy
						.getInstance(context);
				// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
				// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
				// 在定位结束后，在合适的生命周期调用destroy()方法
				// 其中如果间隔时间为-1，则定位只定一次
				// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
				mAMapLocationManager.requestLocationData(
						LocationProviderProxy.AMapNetwork, 30 * 60 * 1000, 10,
						aMapLocationListener);
			}
		}

		@Override
		public void deactivate() {
			stopLocation();
		}
	};

	/***
	 * 定位停止
	 */
	private void stopLocation() {
		mListener = null;
		if (mAMapLocationManager != null) {
			// mAMapLocationManager.removeUpdates(aMapLocationListener);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	/***
	 * 地图点击事件，添加标志物
	 */
	private AMap.OnMapClickListener onMapClickListener = new AMap.OnMapClickListener() {
		@Override
		public void onMapClick(LatLng latLng) {
			// showToast("你点击了地图,经度=" + latLng.longitude + ",维度="
			// + latLng.latitude);

		}
	};

	private AMap.OnMarkerClickListener onMarkerClickListener = new AMap.OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			LatLng latLng = marker.getPosition();
			// showToast("你点击了marker,经度=" + latLng.longitude + ",维度="
			// + latLng.latitude);
			// ScreenSwitch.startActivity(context, CarCheckHabitActivity.class,
			// null);
			return true;
		}
	};

	/****
	 * 箭头转向监听
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
				return;
			}
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ORIENTATION: {
				float x = event.values[0];
				x += getScreenRotationOnPhone(context);
				x %= 360.0F;
				if (x > 180.0F)
					x -= 360.0F;
				else if (x < -180.0F)
					x += 360.0F;
				if (Math.abs(mAngle - x) < 5.0f) {
					break;
				}
				mAngle = x;
				if (mGPSMarker != null) {
					mGPSMarker.setRotateAngle(-mAngle);
				}
				lastTime = System.currentTimeMillis();
			}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	/***
	 * 地理编码
	 */
	private GeocodeSearch.OnGeocodeSearchListener geocodeSearchListener = new GeocodeSearch.OnGeocodeSearchListener() {
		/**
		 * 逆地理编码回调 将地理坐标转换为中文地址（地名描述）的功能
		 */
		@Override
		public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
			if (rCode == 0) {
				if (result != null
						&& result.getRegeocodeAddress() != null
						&& result.getRegeocodeAddress().getFormatAddress() != null) {
					String addressName = result.getRegeocodeAddress()
							.getFormatAddress() + "附近";
					// showToast(addressName);
					 System.out.println("lw     addressName==" + result.getRegeocodeAddress());
//					fromText.setText(addressName);
				} else {
					showToast("对不起，没有搜索到相关数据！");
				}
			} else if (rCode == 27) {
				showToast("搜索失败,请检查网络连接！");
			} else if (rCode == 32) {
				showToast("key验证无效！");
			} else {
				showToast("未知错误，请稍后重试!错误码为" + rCode);
			}
		}

		/**
		 * 地理编码查询回调 将中文地址(或地名描述)转换为地理坐标的功能
		 */
		@Override
		public void onGeocodeSearched(GeocodeResult result, int rCode) {
			if (rCode == 0) {
				if (result != null && result.getGeocodeAddressList() != null
						&& result.getGeocodeAddressList().size() > 0) {
					GeocodeAddress address = result.getGeocodeAddressList()
							.get(0);
//					mAmap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//							AMapUtil.convertToLatLng(address.getLatLonPoint()),
//							15));
//					endLatLonPoint = address.getLatLonPoint();
//					mEndMarker.setPosition(AMapUtil.convertToLatLng(address
//							.getLatLonPoint()));
//					double a = 0.02;
//					LatLng naviLatLng = new LatLng(address.getLatLonPoint()
//							.getLatitude(), address.getLatLonPoint()
//							.getLongitude());
//					String addressName = "经纬度值:" + address.getLatLonPoint()
//							+ "\n位置描述:" + address.getFormatAddress();
					// showToast(addressName);
					// mEndMarker.remove();
					cityCode = address.getAdcode();
					//添加标记
//					mEndMarker.setPosition(naviLatLng);
//					mEndMarker.setTitle(cityCode);//标题
//					mEndMarker.setSnippet(mAddress);
//					mEndMarker.setPerspective(true);
//					mEndMarker.setDraggable(false);
//					mEndMarker.setPeriod(50);
//					mEndMarker.showInfoWindow();
					
//					NaviLatLng naviLatLngs = new NaviLatLng(address
//							.getLatLonPoint().getLatitude(), address
//							.getLatLonPoint().getLongitude());
//					mEndPoint = naviLatLngs;
//					mEndPoints.clear();
//					mEndPoints.add(mEndPoint);
					// // 规划路线
					// calculateRoute();
				} else {
					showToast("对不起，没有搜索到相关数据！");
				}
			} else if (rCode == 27) {
				showToast("搜索失败,请检查网络连接！");
			} else if (rCode == 32) {
				// showToast("key验证无效！");
			} else {
				showToast("未知错误，请稍后重试!错误码为" + rCode);
			}

		}
	};

	/**
	 * 算路的方法，根据选择可以进行行车和步行两种方式进行路径规划
	 */
	private void calculateRoute() {
		// System.out.println("lw    calculateRoute ");
		switch (mTravelMethod) {
		// 驾车导航
		case DRIVER_NAVI_METHOD:
			int driverIndex = calculateDriverRoute();
			if (driverIndex == CALCULATEERROR) {
				showToast("路线计算失败,检查参数情况");
				return;
			} else if (driverIndex == GPSNO) {
				System.out.println("lw     driverIndex == GPSNO");
				return;
			}
			break;
		// 步行导航
		case WALK_NAVI_METHOD:
			// System.out.println("lw     WALK_NAVI_METHOD");
			int walkIndex = calculateWalkRoute();
			if (walkIndex == CALCULATEERROR) {
				showToast("暂不支持长距离步行路线规划");
				return;
			} else if (walkIndex == GPSNO) {
				return;
			}
			break;
		}
		// 显示路径规划的窗体
		showProgressDialog();
	}

	/**
	 * 对行车路线进行规划
	 */
	private int calculateDriverRoute() {
		System.out.println("lw      calculateDriverRoute");
		int driveMode = AMapNavi.DrivingDefault;
		int code = CALCULATEERROR;

		if (mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints, mWayPoints,
				driveMode)) {
			code = CALCULATESUCCESS;
		} else {

			code = CALCULATEERROR;
		}

		return code;
	}

	/**
	 * 对步行路线进行规划
	 */
	private int calculateWalkRoute() {
		int code = CALCULATEERROR;
		if (mAmapNavi.calculateWalkRoute(mStartPoint, mEndPoint)) {
			code = CALCULATESUCCESS;
		} else {

			code = CALCULATEERROR;
		}

		return code;
	}
	
	private void calculateBusRoute() {
		RouteSearch routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(routeSearchListener);
		LatLonPoint startPoint = new LatLonPoint(mStartPoint.getLatitude(),
				mStartPoint.getLongitude());
		LatLonPoint endPoint = new LatLonPoint(mEndPoint.getLatitude(),
				mEndPoint.getLongitude());
		FromAndTo fromAndTo = new FromAndTo(startPoint, endPoint);
		// fromAndTo包含路径规划的起点和终点，RouteSearch.BusLeaseWalk表示公交查询模式
		// 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
		BusRouteQuery query = new BusRouteQuery(fromAndTo,
				RouteSearch.BusLeaseWalk, cityCode, 0);
		routeSearch.calculateBusRouteAsyn(query);// 开始规划路径
	}

	// private BusRouteResult busRouteResult;// 公交模式查询结果
	public OnRouteSearchListener routeSearchListener = new OnRouteSearchListener() {

		@Override
		public void onBusRouteSearched(BusRouteResult result, int rCode) {// code返回结果成功或者失败的响应码。0为成功，其他为失败
			// TODO Auto-generated method stub
			System.out.println("lw      BusRouteResult===" + result
					+ ",code===" + rCode);
			if (rCode == 0) {
				if (result != null && result.getPaths() != null
						&& result.getPaths().size() > 0) {
					Constants.busRouteResult = result;
					Constants.rCode = rCode;
					Intent intentBus = new Intent();
					intentBus.setClass(context, NaviRouteBusActivity.class);
					startActivity(intentBus);
				} else {
//					System.out.println("lw      noresult!");
					showToast("无公交线路可达！");
				}
			} else if (rCode == 27) {
				System.out.println("lw     网络错误！");
			} else if (rCode == 32) {
				System.out.println("lw     key无效！");
			} else {
				System.out.println("lw    其他错误" + rCode);
			}
		}

		@Override
		public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

	};

	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (mProgressDialog == null)
			mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setMessage("线路规划中");
		mProgressDialog.show();
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	// -------------生命周期必须重写方法----------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		if (mAmap != null) {
			mAmap.moveCamera(CameraUpdateFactory.zoomTo(12));

		}
		// 以上两句必须重写
		// 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
		if (mAmapNavi != null) {
			mAmapNavi.setAMapNaviListener(getAMapNaviListener());
		}
		TTSController.getInstance(this).startSpeaking();
		// 注册箭头移动事件
		registerSensorListener();
		
		TCAgent.onResume(this);
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		// 以上两句必须重写
		// 下边逻辑是移除监听
		if (mAmapNavi != null) {
			mAmapNavi.removeAMapNaviListener(getAMapNaviListener());
		}
		
		TCAgent.onPause(this);
		MobclickAgent.onPause(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		// 以上逻辑是必须重写的
		// 这是最后退出页，所以销毁导航和播报资源
		AMapNavi.getInstance(this).destroy();// 销毁导航
		stopLocation();
	}

	/**
	 * 导航回调函数
	 * 
	 * @return
	 */
	private AMapNaviListener getAMapNaviListener() {
		if (mAmapNaviListener == null) {

			mAmapNaviListener = new AMapNaviListener() {

				@Override
				public void onTrafficStatusUpdate() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStartNavi(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onReCalculateRouteForYaw() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onReCalculateRouteForTrafficJam() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLocationChange(AMapNaviLocation location) {

				}

				@Override
				public void onInitNaviSuccess() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onInitNaviFailure() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGetNavigationText(int arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onEndEmulatorNavi() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onCalculateRouteSuccess() {
					dissmissProgressDialog();
					Intent intent = new Intent(context, NaviRouteActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);

				}

				@Override
				public void onCalculateRouteFailure(int arg0) {
					dissmissProgressDialog();
					showToast("路径规划出错");
				}

				@Override
				public void onArrivedWayPoint(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onArriveDestination() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGpsOpenStatus(boolean arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onNaviInfoUpdated(AMapNaviInfo arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onNaviInfoUpdate(NaviInfo arg0) {

					// TODO Auto-generated method stub

				}
			};
		}
		return mAmapNaviListener;
	}

	/***
	 * 注册箭头监听
	 */
	public void registerSensorListener() {
		mSensorManager.registerListener(sensorEventListener, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	/***
	 * 注销箭头监听
	 */
	public void unRegisterSensorListener() {
		mSensorManager.unregisterListener(sensorEventListener, mSensor);
	}

	/**
	 * 响应地理编码. 将中文地址(或地名描述)转换为地理坐标的功能
	 */
	private void getLatlon(String address, String city) {
		if (TextUtils.isEmpty(city)) {
			showToast("定位失败");
			return;
		}
		GeocodeQuery query = new GeocodeQuery(address, city);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
	}

	/**
	 * 响应逆地理编码 坐标转成中文
	 */
	private void getAddress(LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	/**
	 * 获取当前屏幕旋转角度
	 *
	 * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
	 */
	public static int getScreenRotationOnPhone(Context context) {
		final Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return -90;
		}
		return 0;
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.driveBtn:// 驾车
			mTravelMethod = DRIVER_NAVI_METHOD;
			// showLocation();
			// 规划路线
			calculateRoute();
			break;
		case R.id.walkBtn:// 步行
			mTravelMethod = WALK_NAVI_METHOD;
			// showLocation();
			// 规划路线
			calculateRoute();
			break;
		case R.id.map_back_view:
			this.finish();
			break;
		case R.id.busBtn://公交路线
			calculateBusRoute();
			break;
		case R.id.fromLinear://跳转到出发点视角
			mAmap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					AMapUtil.convertToLatLng(startLatLonPoint),
					15));
			break;
		case R.id.toLinear://跳转到目的地视角
			mAmap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					AMapUtil.convertToLatLng(endLatLonPoint),
					15));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return false;
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		View infoWindow = getLayoutInflater().inflate(
				R.layout.info_window, null);

		render(marker, infoWindow);
		return infoWindow;
	}

	/**
	 * 自定义marker显示样式
	 * @param marker
	 * @param infoWindow
	 */
	private void render(Marker marker, View infoWindow) {
		String snippet = marker.getSnippet();
		TextView snippetText = ((TextView) infoWindow.findViewById(R.id.addressInfo));
		if (snippet != null) {
			snippetText.setText(snippet);
		}
		
	}

}
