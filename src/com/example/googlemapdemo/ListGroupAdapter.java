package com.example.googlemapdemo;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListGroupAdapter extends BaseAdapter {

	private Context context;

	private ArrayList<ItemContent> list;

	public ListGroupAdapter(Context context, ArrayList<ItemContent> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		// 1. inflateһ��layout���������layout.xml���涨��ľ���һ��listԪ�ش���Ļ����ṹ������ViewHolder��Ӧ
		// 2. ʵ������ViewHolder����Ҫ��groupItem�����ݣ�����layout.xml�е�һ��id��Դ��Ȼ����convertView���������holder��setTag
		// 3. ���convertView�Ѿ���ʼ����getTag
		// 4. ��ʼ��ViewHolder.groupItem����color��text��
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.group_item_view, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
			holder.text_body = (TextView) convertView.findViewById(R.id.text_body);
			holder.text_header = (TextView) convertView.findViewById(R.id.text_header);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
//		holder.groupItem.setTextColor(Color.BLACK);
		holder.text_header.setText(list.get(position).text_header);
		holder.text_body.setText(list.get(position).text_body);
		
		return convertView;
	}

	static class ViewHolder {
		TextView text_header;
		TextView text_body;
	}
	public static class ItemContent {
		public String text_header;
		public String text_body;
		public ItemContent(String header, String body)
		{
			text_header = header;
			text_body = body;
		}
	}
}
