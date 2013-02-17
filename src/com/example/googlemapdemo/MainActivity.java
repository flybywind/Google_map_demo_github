package com.example.googlemapdemo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//import com.example.popupwindowdemo.PopupWindowDemo;
import com.example.googlemapdemo.ListGroupAdapter.ItemContent;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public final class MainActivity extends Activity  {

//	private LocationManager locationManager;
	// 长按地图弹窗，显示区域内所有日志
	public View mPopupView;	
	// 编辑日志弹窗，编辑文本
	public View mEditorPopup;
	public PopupWindow mPopupWindow;
	public SQLdb mSql = new SQLdb(this);
	public int win_width;
	public int win_height;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		setContentView(R.layout.popup);
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	private GoogleMap mGMap;
	private MapFragment mFragmentMap;
	// 长按ListView Item弹窗，显示删除，编辑，分享等按钮：
	private View mNoteOptionPopup;
	private MainActivity mainActivity = this;
	private LatLng mCurTapPoint;
//	private LandMarker mLandMarder;
	private void init()
	{	
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		win_width = windowManager.getDefaultDisplay().getWidth();
		win_height = windowManager.getDefaultDisplay().getHeight();
		mFragmentMap = (MapFragment)  getFragmentManager().findFragmentById(R.id.map);
		mGMap = mFragmentMap.getMap();	
		

		if( mGMap != null )
		{
			// 设置google map
			mGMap.setMyLocationEnabled(true);			
			mGMap.getUiSettings().setMyLocationButtonEnabled(true);
			mGMap.getUiSettings().setRotateGesturesEnabled(true);
			mGMap.getUiSettings().setCompassEnabled(true);
			Activity parent = mFragmentMap.getActivity();
			mPopupWindow = new PopupWindow(parent);
			// 在map上添加所有marker：
			addAllMarker();
			
			// 添加监听程序：
			mGMap.setOnMapLongClickListener (new GoogleMap.OnMapLongClickListener(){
				@SuppressWarnings("deprecation")
				@Override
				public void onMapLongClick(LatLng point) {
					// TODO Auto-generated method stub
					mCurTapPoint = point;
					mPopupView = getLayoutInflater().inflate(R.layout.popup, null);
					// 注册按钮响应程序
					set_btn_listener(R.id.add_text_btn);
					mPopupWindow.setContentView(mPopupView);
					// connect list_view and array_list:		
					ArrayList<ListGroupAdapter.ItemContent> diary_group = load_diary_at_tap(mCurTapPoint);
					ListGroupAdapter adapter = new ListGroupAdapter(mainActivity, diary_group);
						
					show_popup_window(adapter);
				}
			});
			mGMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				
				@Override
				public boolean onMarkerClick(Marker marker) {
					// TODO Auto-generated method stub
					mPopupView = getLayoutInflater().inflate(R.layout.popup, null);
					mCurTapPoint = marker.getPosition();
					// 注册按钮响应程序
					set_btn_listener(R.id.add_text_btn);
					mPopupWindow.setContentView(mPopupView);
					// connect list_view and array_list:		
					ArrayList<ItemContent> diary_group = load_diary_at_tap(marker.getPosition(), 1);
					Log.i("load_one_marker", marker.getPosition().latitude+","+marker.getPosition().longitude);
					ListGroupAdapter adapter = new ListGroupAdapter(mainActivity, diary_group);
					show_popup_window(adapter);
					return false;
				}
			});
		}	
	}
	// 显示长按地图后的弹窗，ListView显示当前视野内的所有日志。
	private void show_popup_window(final ListGroupAdapter adapter)
	{
		if( mPopupWindow != null ) mPopupWindow.dismiss();
		ListView list_view = (ListView) mPopupView.findViewById(R.id.listview_diary);
		
		list_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				mEditorPopup = getLayoutInflater().inflate(R.layout.textedit_popup, null);
				set_btn_listener(R.id.save_btn);
				ItemContent items = (ItemContent) adapter.getItem(pos);
				show_text_edit_window(items.text_header, items.text_body);
			}
			
		});
		
		list_view.setAdapter(adapter);
		mPopupWindow.setContentView(mPopupView);
		mPopupWindow.setHeight(win_height/2);
		mPopupWindow.setWidth(win_width);
		
		View anchor = mainActivity.findViewById(R.id.title_show);
		// 使其聚集
		mPopupWindow.setFocusable(true);
		// 设置允许在外点击消失
		mPopupWindow.setOutsideTouchable(false);
		mPopupWindow.showAsDropDown(anchor);
		
		mPopupWindow.update();
	}
	private void addAllMarker()
	{
		// 如果数据库比较大，这个操作最好放在后台运行。
		SQLiteDatabase db = mSql.getReadableDatabase();
		String[] projection = {
			    DBConstant.DIARY_PRECISE_LATITUDE,
			    DBConstant.DIARY_PRECISE_LONGITUDE,
			    DBConstant.DIARY_REAL_LOCATION
		};
		try{
			Cursor lc = db.query(
					DBConstant.TABLE_DIARY,
					projection, 
					null, null, null, null, null);
			lc.moveToPosition(-1);
			while(lc.moveToNext())
			{
				double latitude = lc.getDouble(lc.getColumnIndex(DBConstant.DIARY_PRECISE_LATITUDE));
				double longitude = lc.getDouble(lc.getColumnIndex(DBConstant.DIARY_PRECISE_LONGITUDE));
				String location = lc.getString(lc.getColumnIndex(DBConstant.DIARY_REAL_LOCATION));
				Log.i("LoadAllMarker", "lat,lng = "+latitude+","+longitude);
				if( location.equals("unknown") )
				{
					mGMap.addMarker(new MarkerOptions()
								.draggable(true)
								.position(new LatLng(latitude, longitude)));
				}else{
					mGMap.addMarker(new MarkerOptions()
								.draggable(true)
								.position(new LatLng(latitude, longitude))
								.title(location));	
				}
			}
		}catch(SQLiteException e)
		{
			// 先删除，再新建
			db = mSql.getReadableDatabase();
			mSql.onUpgrade(db, 1, 2);
			Log.i("SQL Notice", e.getMessage());
		}finally{
			db.close();
		}
	}
	private ArrayList<ItemContent> load_diary_at_tap(LatLng point)
	{
		return load_diary_at_tap(point, 0);
	}
	/// show_type: 
	/// 0: 加载点击中心处，视野内的所有日志
	/// 1: 仅加载点击处的日志。
	private ArrayList<ItemContent> load_diary_at_tap(LatLng point, int show_type)
	{
		ArrayList<ItemContent> diary_group = new ArrayList<ItemContent>();
//		int coarse_lat = (int) (point.latitude*50);
//		int coarse_lng = (int) (point.longitude*50);
//		String key = "\""+coarse_lat+","+coarse_lng+"\"";
		
		// 取出哪些列：
		String[] projection = {
			    DBConstant.DIARY_PRECISE_LATITUDE,
			    DBConstant.DIARY_PRECISE_LONGITUDE,
			    DBConstant.DIARY_TEXT,
			    DBConstant.DIARY_REAL_LOCATION,
			    DBConstant.DIARY_TEXT_CREATE_TIME
		};
		try{
			SQLiteDatabase db = mSql.getReadableDatabase();
			/* 
			 * 获得当前视野
			 */
			LatLngBounds bounds = mGMap.getProjection().getVisibleRegion().latLngBounds;
			double northest = bounds.northeast.latitude;
			double southest = bounds.southwest.latitude;
			double eastest = bounds.northeast.longitude;
			double westest = bounds.southwest.longitude;
			Cursor c = db.query(
				    DBConstant.TABLE_DIARY,  			// The table to query
				    projection,                         		// The columns to return
				    DBConstant.DIARY_PRECISE_LATITUDE + " >= ? AND " +
				    DBConstant.DIARY_PRECISE_LATITUDE + " <= ? AND " +
				    DBConstant.DIARY_PRECISE_LONGITUDE + " >= ? AND " +
				    DBConstant.DIARY_PRECISE_LONGITUDE + " <= ? ",   // The columns for the WHERE clause
				    new String[]{ 
				    		Double.toString(southest), 
				    		Double.toString(northest), 
				    		Double.toString(westest), 
				    		Double.toString(eastest) },                            // The values for the WHERE ? clause
				    null,                                     // don't group the rows
				    null,                                     // don't filter by row groups
				    DBConstant.DIARY_TEXT_CREATE_TIME+" DESC"   // The sort order
				    );
			c.moveToPosition(-1);
//			String precise_latlng = "\""+point.latitude+","+point.longitude+"\"";
			while(c.moveToNext())
			{
				if( show_type == 1 ){
					double diary_lat = c.getDouble(c.getColumnIndex(DBConstant.DIARY_PRECISE_LATITUDE));
					double diary_long =  c.getDouble(c.getColumnIndex(DBConstant.DIARY_PRECISE_LONGITUDE));
					if( diary_lat == point.latitude && diary_long == point.longitude )
					{
						String date_time = c.getString(c.getColumnIndex(DBConstant.DIARY_TEXT_CREATE_TIME));
						String text_body = c.getString(c.getColumnIndex(DBConstant.DIARY_TEXT));
						String text_header = c.getString(c.getColumnIndex(DBConstant.DIARY_REAL_LOCATION)); 
						diary_group.add(new ItemContent(text_header,
								text_body+"\n写于 "+date_time));
					}
				}else
				{
					String date_time = c.getString(c.getColumnIndex(DBConstant.DIARY_TEXT_CREATE_TIME));
					String text_body = c.getString(c.getColumnIndex(DBConstant.DIARY_TEXT));
					String text_header = c.getString(c.getColumnIndex(DBConstant.DIARY_REAL_LOCATION)); 
					diary_group.add(new ItemContent(text_header,
								text_body+"\n写于 "+date_time));

				} 
			}
			db.close();
		}catch(SQLiteException e)
		{
			Log.i("SQL Notice", e.getMessage());
		}
		
		return diary_group;
	}
//	private OnClickListener prepare_write_text_btn_listener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			
//		}
//	};
	private OnClickListener add_text_btn_listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 1. 弹出编辑窗口
			// 2. 返回时更新ListView
			mEditorPopup = getLayoutInflater().inflate(R.layout.textedit_popup, null);
			set_btn_listener(R.id.save_btn);
			show_text_edit_window();
		}
	};
	private OnClickListener save_text_btn_listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			EditText text_view = (EditText)mEditorPopup.findViewById(R.id.editor_note);
			EditText header_view = (EditText)mEditorPopup.findViewById(R.id.real_world_location);
			SQLiteDatabase db = mSql.getWritableDatabase();
			String text_body = text_view.getText().toString();
			String text_header = header_view.getText().toString();
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");     
			String date = sDateFormat.format(new java.util.Date());  
			ContentValues insert_val = new ContentValues();
			insert_val.put(DBConstant.DIARY_PRECISE_LATITUDE, mCurTapPoint.latitude);
			insert_val.put(DBConstant.DIARY_PRECISE_LONGITUDE, mCurTapPoint.longitude);
			insert_val.put(DBConstant.DIARY_TEXT, text_body);
			insert_val.put(DBConstant.DIARY_REAL_LOCATION, text_header);
			insert_val.put(DBConstant.DIARY_TEXT_CREATE_TIME, date);
			
			db.insert(DBConstant.TABLE_DIARY, null, insert_val);
			db.close();
			// 更新popup window
			ArrayList<ItemContent> diary_group = load_diary_at_tap(mCurTapPoint);
			ListGroupAdapter adapter = new ListGroupAdapter(mainActivity, diary_group);
			show_popup_window(adapter);	
			// 增加LandMarker 
			if( text_header.equals("") )
			{
				mGMap.addMarker(new MarkerOptions()
					.draggable(true)
					.position(mCurTapPoint));	
			}else{
				mGMap.addMarker(new MarkerOptions()
					.draggable(true)
					.position(mCurTapPoint)
					.title(text_header));
			}
			
		}
	};
	private void show_text_edit_window()
	{
		if( mPopupWindow != null ) mPopupWindow.dismiss();
		// PopUpWindow --> ScrollView --> popup_view --> editor
		Activity parent = mFragmentMap.getActivity();
		ScrollView scroll_view = new ScrollView(parent);
		scroll_view.addView(mEditorPopup);
		mPopupWindow.setContentView(scroll_view);
		View anchor = mainActivity.findViewById(R.id.title_show);
		// 使其聚集
		mPopupWindow.setFocusable(true);
		// 设置允许在外点击消失
		mPopupWindow.setOutsideTouchable(false);
		mPopupWindow.showAsDropDown(anchor);
		mPopupWindow.update();
		Log.i("ViewHeight", "view height is "+mEditorPopup.getHeight());
	}
	private void show_text_edit_window(String text_header, String text_body)
	{
		if( mPopupWindow != null ) mPopupWindow.dismiss();
		// PopUpWindow --> ScrollView --> popup_view --> editor
		Activity parent = mFragmentMap.getActivity();
		ScrollView scroll_view = new ScrollView(parent);
		scroll_view.addView(mEditorPopup);
//		if( mEditorPopup == null )
//		{
//			mEditorPopup = getLayoutInflater().inflate(R.layout.textedit_popup, null);
//		}
		EditText header_view = (EditText)mEditorPopup.findViewById(R.id.real_world_location);
		EditText body_view = (EditText)mEditorPopup.findViewById(R.id.editor_note);
		header_view.setText(text_header);
		body_view.setText(text_body);
		
		mPopupWindow.setContentView(scroll_view);
		View anchor = mainActivity.findViewById(R.id.title_show);
		// 使其聚集
		mPopupWindow.setFocusable(true);
		// 设置允许在外点击消失
		mPopupWindow.setOutsideTouchable(false);
		mPopupWindow.showAsDropDown(anchor);
		mPopupWindow.update();
		Log.i("ViewHeight", "view height is "+mEditorPopup.getHeight());
	}
	
	// 设置各种按钮回调函数
	// 要求对应的view inflate之后才能执行
	private void set_btn_listener(int btn_id)
	{
		if(btn_id == R.id.add_text_btn)
		{
			Button btn = (Button) mPopupView.findViewById(btn_id); 
			btn.setClickable(true);
			btn.setOnClickListener(add_text_btn_listener);
		}
		if(btn_id == R.id.save_btn)
		{
			Button btn = (Button) mEditorPopup.findViewById(btn_id); 
			btn.setClickable(true);
			btn.setOnClickListener(save_text_btn_listener);
		}
	}
//	private void set_listview_item_listener(ListView lv)
//	{
//		// 添加ListView item marker
//		lv.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//				EditText editText = (EditText) mEditorPopup.findViewById(R.id.editor_note)	;
//				editText
//				
//			}
//		});
//	}
}
