package com.sanha.coronamap.ACTIVITY_MAP;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.MarkerIcons;
import com.sanha.coronamap.CLASS.MarkerData;
import com.sanha.coronamap.R;


/*
 이 파일은 현재 사용 되지 않는 파일입니다.

 기존에는, 좌표,주소 입력을 통해 네이버 지도에 마크를 입력해 주는 방법을 사용 하였습니다.

 그러나, 확진자 수가 기하급수적으로 늘어남에 따라, 다 표기 할 수도 없으며, 자료 정리를 제대로 할 수 가 없어서

 파기하고 다른 지도 표현 방식으로 변경 하였습니다. 

 */
public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    int n =0 ;
    MarkerData[] markData = new MarkerData[3000];
    Marker [] marker = new Marker[3000];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        NaverMapOptions options = new NaverMapOptions()
                .camera(new CameraPosition(new LatLng(37.478717, 126.668853), 8))
                .mapType(NaverMap.MapType.Basic)
                .compassEnabled(true);


        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(options);
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        setMap(naverMap);
    }


    private void setMap(@NonNull NaverMap naverMap) {

        // infowindow . 정보창
        InfoWindow infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return (CharSequence)infoWindow.getMarker().getTag();
            }
        });
        // 지도 빈공간 클릭시 정보창 꺼지도록
        naverMap.setOnMapClickListener((coord, point) -> {
            infoWindow.close();
        });

        // 마커를 클릭하면:
        Overlay.OnClickListener listener = overlay -> {
            Marker marker = (Marker)overlay;

            if (marker.getInfoWindow() == null) {
                // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                infoWindow.open(marker);
            } else {
                // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                infoWindow.close();
            }
            return true;
        };

        databaseReference.child("mark").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                makeMark(dataSnapshot,naverMap,n ,listener);
                n++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void makeMark(DataSnapshot dataSnapshot, @NonNull NaverMap naverMap, int k, Overlay.OnClickListener listener) {
        MarkerData tmpMark = dataSnapshot.getValue(MarkerData.class);

        markData[k] = new MarkerData(tmpMark.nNum,tmpMark.detail,tmpMark.mLatitude,tmpMark.mLongitude,tmpMark.marksNum,tmpMark.happenData);

        marker[k] = new Marker();
        marker[k].setPosition(new LatLng(markData[k].mLatitude,markData[k].mLongitude));
        String temp = markData[k].happenData +"\n" + markData[k].nNum + "번 확진자 \n" + markData[k].detail ;
        marker[k].setTag(temp);
        marker[k].setIcon(MarkerIcons.BLACK);
        // color : 0xFF000000 ~ 0xFFFFFFFF. 16777 = 0x00FFFFFF / 1000. 즉, 1000명치의 색상 자동 설정.
        marker[k].setIconTintColor(0xFFFFFFFF  - (Integer.parseInt( markData[k].nNum) *16777));
        marker[k].setMap(naverMap);
        marker[k].setOnClickListener(listener);
    }
}

/* 테스트용 데이터
mark[0] = new Marks("1","헬레니아 와 벨리카 사이",37.5702317, 126.9834601,0,"2020-02-05");
 mark[1] = new Marks("1","바로 거기",37.5603174, 126.9635914,1,"2020-02-05");
  mark[2] = new Marks("2","바로 거기 옆에 있는 거기",37.588469, 127.034147,2,"2020-02-05");
*/
