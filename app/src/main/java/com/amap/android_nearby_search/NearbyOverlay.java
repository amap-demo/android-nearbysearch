package com.amap.android_nearby_search;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.nearby.NearbyInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by my94493 on 2017/1/11.
 */

public class NearbyOverlay {
    private AMap aMap;
    private List<NearbyInfo> mNearbys;
    private ArrayList<Marker> mUserMarks = new ArrayList<Marker>();
    public NearbyOverlay(AMap amap , List<NearbyInfo> nearbyInfos) {
        aMap = amap;
        mNearbys = nearbyInfos;
    }
    /**
     * 添加Marker到地图中。
     */
    public void addToMap() {
        for (int i = 0; i < mNearbys.size(); i++) {
            Marker marker = aMap.addMarker(getMarkerOptions(i));
            NearbyInfo item = mNearbys.get(i);
            marker.setObject(item);
            mUserMarks.add(marker);
        }
    }
    /**
     * 去掉UserOverlay上所有的Marker。
     *
     */
    public void removeFromMap() {
        for (Marker mark : mUserMarks) {
            mark.remove();
        }
    }

    private MarkerOptions getMarkerOptions(int index) {
        return new MarkerOptions()
                .position(
                        new LatLng(mNearbys.get(index).getPoint()
                                .getLatitude(), mNearbys.get(index)
                                .getPoint().getLongitude()))
                .title(getTitle(index)).snippet(getSnippet(index))
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
    }
    protected String getTitle(int index) {
        return mNearbys.get(index).getUserID();
    }

    protected String getSnippet(int index) {
        int distance = mNearbys.get(index).getDistance();
        String mydistance = "距离："+distance+"米";
        return mydistance;
    }

    //    /**
//     * 移动镜头到当前的视角。
//     */
//    public void zoomToSpan() {
//        if (mNearbys != null && mNearbys.size() > 0) {
//            if (aMap == null)
//                return;
//            LatLngBounds bounds = getLatLngBounds();
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//        }
//    }
//
//    private LatLngBounds getLatLngBounds() {
//        LatLngBounds.Builder b = LatLngBounds.builder();
//        for (int i = 0; i < mNearbys.size(); i++) {
//            b.include(new LatLng(mNearbys.get(i).getPoint().getLatitude(),
//                    mNearbys.get(i).getPoint().getLongitude()));
//        }
//        return b.build();
//    }
}
