package com.kevin.imageuploadclient.fragment;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bumptech.glide.Glide;
import com.kevin.imageuploadclient.R;
import com.kevin.imageuploadclient.fragment.basic.PictureSelectFragment;
import com.kevin.imageuploadclient.util.Constant;

import java.io.File;

import butterknife.Bind;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 版权所有：XXX有限公司
 *
 * MainFragment
 *
 * @author zhou.wenkai ,Created on 2016-5-5 10:25:49
 * 		   Major Function：<b>MainFragment</b>
 *
 *         注:如果您修改了本类请填写以下内容作为记录，如非本人操作劳烦通知，谢谢！！！
 * @author mender，Modified Date Modify Content:
 */
public class MainFragment extends PictureSelectFragment implements
        LocationSource, AMapLocationListener, AMap.OnMapLongClickListener, GeocodeSearch.OnGeocodeSearchListener{

    /** start 高德地图 */
    private MapView mapView;
    private AMap map;

    private LocationSource.OnLocationChangedListener locationChangedListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;

    private int cLocation = 0;

    //选中位置的数据
    private double zLongtitude;
    private double zLatitude;
    private String zAddress = "";

    private int cMapLongClick = 0;
    private Marker marker;
    private GeocodeSearch geocodeSearch;

    private View mapLayout = null;
    /** end 高德地图 */


    /** Toolbar */
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    //选择图片imageView注册
    @Bind(R.id.main_frag_picture_iv)
    ImageView mPictureIv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        if (mapLayout == null) {
            Log.i("sys", "MF onCreateView() null");
            mapLayout = super.onCreateView(inflater, container, savedInstanceState);
            mapView = (MapView) mapLayout.findViewById(R.id.map_frag_view);
            mapView.onCreate(savedInstanceState);
            if (map == null) {
                map = mapView.getMap();
                initMap();
            }
        }else {
            if (mapLayout.getParent() != null) {
                ((ViewGroup) mapLayout.getParent()).removeView(mapLayout);
            }
        }
        return mapLayout;
    }

    private void initMap() {
        /*八种定位模式
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
 //以下三种模式从5.1.0版本开始提供
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
 myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。
         */
        map.setLocationSource(this);// 设置定位监听
        //map.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        map.setMyLocationEnabled(true);// 表示显示定位层并可触发定位
        // 定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        //map.setMyLocationType(AMap.MAP_TYPE_BUS);
        //AMap.LOCATION_TYPE_MAP_ROTATE    地图转动，图标位于中心点

        //美化定位蓝点及范围
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类            //LOCATION_TYPE_FOLLOW_NO_CENTER随位置移动而移动，
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //myLocationStyle.strokeColor(Color.RED);//设置定位蓝点精度圆圈的边框颜色的方法。
        //myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色的方法。
        //BitmapDescriptor bitmapDescriptor_icon = BitmapDescriptorFactory.fromResource(R.mipmap.my_location);
        BitmapDescriptor bitmapDescriptor_icon = BitmapDescriptorFactory.fromView(getActivity().getLayoutInflater().inflate(R.layout.my_localtion,null));
        myLocationStyle.myLocationIcon(bitmapDescriptor_icon);
        myLocationStyle.anchor(0.5f,0.6f);//自定义定位蓝点图标的锚点
        map.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style

        map.setOnMapLongClickListener(this);

        geocodeSearch = new GeocodeSearch(this.getActivity());
        geocodeSearch.setOnGeocodeSearchListener(this);

        //界面美化
        UiSettings uiSettings =  map.getUiSettings();
        uiSettings.setLogoBottomMargin(-50);//隐藏logo
        uiSettings.setZoomControlsEnabled(false);//隐藏缩放按钮
        uiSettings.setRotateGesturesEnabled(false);//禁止地图旋转手势
        uiSettings.setScaleControlsEnabled(true);//是否显示比例尺
        uiSettings.setMyLocationButtonEnabled(false);//是否显示默认定位按钮
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

        locationChangedListener = onLocationChangedListener;

        if (locationClient == null) {

            locationClient = new AMapLocationClient(this.getActivity());
            locationClientOption = new AMapLocationClientOption();

            locationClient.setLocationListener(this);

            locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            locationClient.setLocationOption(locationClientOption);
            locationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {

        locationChangedListener = null;
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (locationChangedListener != null && aMapLocation != null) {

            if (locationClient != null && aMapLocation.getErrorCode() == 0) {

                locationChangedListener.onLocationChanged(aMapLocation);// 显示系统小蓝点

                //如果是第一次定位成功 就缩放到级别18
                if (cLocation == 0) {
                    map.moveCamera(CameraUpdateFactory.zoomTo(17));
                    cLocation++;
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        zLatitude = latLng.latitude;
        zLongtitude = latLng.longitude;

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker_location_center);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(bitmapDescriptor);
        markerOptions.position(latLng);

        if (cMapLongClick == 0) {
            marker = map.addMarker(markerOptions);
            cMapLongClick++;
        } else {
        }
        marker.setPosition(latLng);

        // 长按时中心点切换
        map.animateCamera(CameraUpdateFactory.changeLatLng(latLng), 500, null);

        LatLonPoint latLonPoint = new LatLonPoint(zLatitude, zLongtitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocodeSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int code) {

        if (code == AMapException.CODE_AMAP_SUCCESS) {

            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                zAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress()
                        + "   ";
            } else {
                zAddress = "未知位置";
            }

        } else {
            zAddress = "未知位置";
        }

        showLocationInfo();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    private void showLocationInfo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setIcon(R.mipmap.marker_location_center);
        builder.setTitle("地址查询结果");

        String mess = "经度：" + zLongtitude + "\n纬度：" + zLatitude + "\n地址：" + zAddress;

        builder.setMessage(mess);
        builder.setPositiveButton("我知道了", null);
        //builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    @Override
    public void onResume() {
        Log.i("sys", "mf onResume");
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onPause() {
        Log.i("sys", "mf onPause");
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("sys", "mf onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onDestroy() {
        Log.i("sys", "mf onDestroy");
        super.onDestroy();
        mapView.onDestroy();
        if (null != locationClient) {
            locationClient.onDestroy();
        }
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_main;
    }

    @Override
    public void initViews(View view) {
        initToolbar(toolbar);
    }

    @Override
    public void initEvents() {
        // 设置图片点击监听
        mPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
        // 设置裁剪图片结果监听
        setOnPictureSelectedListener(new OnPictureSelectedListener() {
            @Override
            public void onPictureSelected(Uri fileUri, Bitmap bitmap) {
//                mPictureIv.setImageBitmap(bitmap);

                String filePath = fileUri.getEncodedPath();
                final String imagePath = Uri.decode(filePath);

                uploadImage(imagePath);

            }
        });
    }

    /**
     * 上传图片
     * @param imagePath
     */
    private void uploadImage(String imagePath) {
        new NetworkTask().execute(imagePath);
    }

    /**
     * 访问网络AsyncTask,访问网络在子线程进行并返回主线程通知访问的结果
     */
    class NetworkTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return doPost(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(!"error".equals(result)) {
                Log.i(TAG, "图片地址 " + Constant.BASE_URL + result);
                Glide.with(mContext)
                        .load(Constant.BASE_URL + result)
                        .into(mPictureIv);
            }
        }
    }

    private String doPost(String imagePath) {
        OkHttpClient mOkHttpClient = new OkHttpClient();

        String result = "error";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
//        builder.addFormDataPart("userId", "20160519142605");
        builder.addFormDataPart("image", imagePath,
                RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)));

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL + "/uploadimage")
                .post(requestBody)
                .build();

        Log.d(TAG, "请求地址 " + Constant.BASE_URL + "/uploadimage");
        try{
            Response response = mOkHttpClient.newCall(request).execute();
            Log.d(TAG, "响应码 " + response.code());
            if (response.isSuccessful()) {
                String resultValue = response.body().string();
                Log.d(TAG, "响应体 " + resultValue);
                return resultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
