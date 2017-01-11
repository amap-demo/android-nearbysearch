package com.amap.android_nearby_search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.nearby.NearbyInfo;
import com.amap.api.services.nearby.NearbySearch;
import com.amap.api.services.nearby.NearbySearchFunctionType;
import com.amap.api.services.nearby.NearbySearchResult;
import com.amap.api.services.nearby.UploadInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NearbySearch.NearbyListener, AMapLocationListener, LocationSource, AMap.OnMarkerClickListener {
    private MapView mapView;
    private AMap aMap;
    private Button mUploadButton,mSearchButton, mChangeUserButton;
    private TextView mResultText;
    private LatLonPoint mlcoation ;
    private RelativeLayout resultLayout;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;
    private String UserID = "XiaoHong";
    private NearbyOverlay nearbyOverlay;
    private int resultcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        //设置附近监听
        NearbySearch.getInstance(getApplicationContext()).addNearbyListener(this);
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种

        aMap.setOnMarkerClickListener(this);

        mUploadButton = (Button)findViewById(R.id.uploadbtn);
        mSearchButton = (Button)findViewById(R.id.searchbtn);
        mResultText = (TextView) findViewById(R.id.search_result);
        resultLayout = (RelativeLayout) findViewById(R.id.search_detail) ;
        mUploadButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
//        mChangeUserButton = (Button)findViewById(R.id.useridbtn);
//        mChangeUserButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadbtn:
                uploadlocation();
                break;
            case R.id.searchbtn:
                searchnearby();
                break;
        }
    }

    /**
     * 搜索附近的用户
     */
    private void searchnearby() {
        //设置搜索条件
        NearbySearch.NearbyQuery query = new NearbySearch.NearbyQuery();
        //设置搜索的中心点
        query.setCenterPoint(mlcoation);
        //设置搜索的坐标体系
        query.setCoordType(NearbySearch.AMAP);
        //设置搜索半径
        query.setRadius(10000);
        //设置查询的时间
        query.setTimeRange(10000);
        //设置查询的方式驾车还是距离
        query.setType(NearbySearchFunctionType.DISTANCE_SEARCH);
        //调用异步查询接口
        NearbySearch.getInstance(getApplicationContext())
                .searchNearbyInfoAsyn(query);
    }

    /**
     * 上传我的位置
     */
    private void uploadlocation() {
        //构造上传位置信息
        UploadInfo loadInfo = new UploadInfo();
        //设置上传位置的坐标系支持AMap坐标数据与GPS数据
        loadInfo.setCoordType(NearbySearch.AMAP);
        //设置上传数据位置,位置的获取推荐使用高德定位sdk进行获取
        if (mlcoation != null){
            loadInfo.setPoint(mlcoation);
        }else{
            Toast.makeText(MainActivity.this, "定位位置为空", Toast.LENGTH_SHORT).show();
        }
        //设置上传用户id，一个NearbySearch单一只有唯一的ID，上传的位置需要保证userID是一样的
        loadInfo.setUserID(UserID);
        //调用异步上传接口
        NearbySearch.getInstance(getApplicationContext())
                .uploadNearbyInfoAsyn(loadInfo);

    }

    /**
     * 开始定位。
     */
    private void startlocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置允许模拟位置，用于测试
            mLocationOption.setMockEnable(true);
            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
    }

    /**
     * 删除附近回调
     * @param resultCode 错误码
     */
    @Override
    public void onUserInfoCleared(int resultCode) {

    }

    /**
     * 附近搜索结果回调
     * @param nearbySearchResult 附近搜索结果
     * @param resultCode 错误码
     */
    @Override
    public void onNearbyInfoSearched(NearbySearchResult nearbySearchResult, int resultCode) {
        resultLayout.setVisibility(View.VISIBLE);
        if(resultCode == AMapException.CODE_AMAP_SUCCESS){
            if (nearbySearchResult != null
                    && nearbySearchResult.getNearbyInfoList() != null
                    && nearbySearchResult.getNearbyInfoList().size() > 0) {
                resultcount = nearbySearchResult.getNearbyInfoList().size();
                //清理之前搜索结果的marker
                aMap.clear(true);
                nearbyOverlay = new NearbyOverlay(aMap,nearbySearchResult.getNearbyInfoList());
                nearbyOverlay.addToMap();
                showResult(nearbySearchResult.getNearbyInfoList().get(0));

            } else {
                mResultText.setText("附近搜索结果为空");
            }
        } else if(resultCode == AMapException.CODE_AMAP_NEARBY_KEY_NOT_BIND){
            mResultText.setText("App key未开通“附近”功能,请注册附近KEY");
        } else{
            mResultText.setText("附近搜索出现异常，异常码为："+resultCode);
        }
    }

    private void showResult(NearbyInfo nearbyInfo) {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time = nearbyInfo.getTimeStamp();
        String date = format.format(new Date(time*1000));//时间戳转年月日
        mResultText.setText(
                        "附近搜索结果: "
                        + resultcount + "个  "
                        +"用户ID：" + nearbyInfo.getUserID()
                        + " \n距离：" + nearbyInfo.getDistance()
                        + "米 \n信息上传时间： " + date + " \n位置： "
                        + nearbyInfo.getPoint().toString());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nearbyInfo.getPoint()
                .getLatitude(), nearbyInfo .getPoint().getLongitude()),15 ));
    }

    /**
     * 附近搜索上传回调
     * @param rCode 错误码
     */
    @Override
    public void onNearbyInfoUploaded(int rCode) {
        //上传位置信息回调处理
        resultLayout.setVisibility(View.VISIBLE);
        if (rCode == AMapException.CODE_AMAP_SUCCESS){
            mResultText.setText("上传位置成功");
        } else if(rCode == AMapException.CODE_AMAP_NEARBY_KEY_NOT_BIND){
            mResultText.setText("App key未开通“附近”功能,请注册附近KEY");
        } else if(rCode == AMapException.CODE_AMAP_CLIENT_NEARBY_NULL_RESULT){
            mResultText.setText("NearbyInfo对象为空");
        } else if(rCode == AMapException.CODE_AMAP_CLIENT_USERID_ILLEGAL){
            mResultText.setText("USERID非法");
        } else if(rCode == AMapException.CODE_AMAP_CLIENT_UPLOAD_TOO_FREQUENT){
            mResultText.setText("两次单次上传的间隔低于7秒");
        } else if(rCode == AMapException.CODE_AMAP_CLIENT_UPLOAD_LOCATION_ERROR){
            mResultText.setText("Point为空，或与前次上传的相同");
        } else {
            mResultText.setText("附近搜索出现异常，异常码为："+ rCode);
        }
    }

    /**
     * 定位结果回调
     * @param aMapLocation 定位结果
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mlcoation = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": "
                        + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        startlocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        NearbyInfo nearbyinfo = (NearbyInfo) marker.getObject();
        if (nearbyinfo != null){
            showResult(nearbyinfo);
            resultLayout.setVisibility(View.VISIBLE);
        }
        return false;
    }
}
