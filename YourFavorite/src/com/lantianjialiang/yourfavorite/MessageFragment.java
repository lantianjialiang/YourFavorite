package com.lantianjialiang.yourfavorite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MessageFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "message_section_number";

	private final List<String> list = new ArrayList<String>();
	private final List<Message> messages = new ArrayList<Message>();
	private final List<MessageSummary> summaryList = new ArrayList<MessageSummary>();
	private static int MAX = 10;

	private MyAdapter adapter = null;

	public MessageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_message, container,
				false);

		adapter = new MyAdapter();
		adapter.setList(list);
		ListView lv = (ListView) rootView.findViewById(R.id.messageList);
		lv.setAdapter(adapter);

		getAllSms();

		return rootView;
	}

	private void getAllSms() {
		ContentResolver cr = this.getActivity().getContentResolver();
		Uri uri = Uri.parse("content://sms/");
		Message message = null;
		messages.clear();

		Cursor c = cr.query(uri, null, null, null, null);
		int totalSms = c.getCount();
		if (totalSms <= 0) {
			return;
		}

		if (c.moveToFirst()) {
			for (int i = 0; i < totalSms; i++) {

				message = new Message();
				if (c.getString(
						c.getColumnIndexOrThrow(Telephony.Sms.Inbox.TYPE))
						.contains("1")) {
					message.type = "inbox";
				} else {
					message.type = "send";
				}
				// message.phoneNumber = c.getString(c
				// .getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS));

				message.phoneNumber = c.getString(c
						.getColumnIndexOrThrow("address"));

				message.name = getNameByPhoneNumber(cr, message.phoneNumber);

				messages.add(message);

				c.moveToNext();
			}
		}

		c.close();

		calculateSummary();

	}

	@Override
	public void onResume() {
		super.onResume();
		getAllSms();
	}

	private String getNameByPhoneNumber(ContentResolver resolver,
			String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = resolver.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		String name = "";
		if (cursor.moveToFirst()) {
			name = cursor.getString(cursor
					.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
		} else {
			name = "";
		}

		return name;
	}

	private void calculateSummary() {
		summaryList.clear();

		MessageSummary ms = null;
		for (int i = 0; i < messages.size(); i++) {
			ms = null;

			for (int j = 0; j < summaryList.size(); j++) {
				if (messages.get(i).phoneNumber
						.equals(summaryList.get(j).phoneNumber)) {
					ms = summaryList.get(j);
					ms.addToCount(1);
					break;
				}
			}

			if (ms == null) {
				ms = new MessageSummary();
				ms.phoneNumber = messages.get(i).phoneNumber;
				ms.name = messages.get(i).name;
				ms.addToCount(1);
				summaryList.add(ms);
			}
		}

	}

	public void sortByCount() {
		list.clear();
		Collections.sort(summaryList, new CountComparator());

		int count = MAX;
		MessageSummary ms = null;
		if (summaryList.size() < MAX) {
			count = summaryList.size();
		}

		String prefix = "";
		for (int i = 0; i < count; i++) {
			ms = summaryList.get(i);
			prefix = ms.name;
			if (ms.name.equals("")) {
				prefix = ms.phoneNumber;
			}
			list.add(prefix + ": " + ms.totalCount + " messages");
		}

		adapter.notifyDataSetChanged();
	}

	private static class MessageSummary {
		public String phoneNumber = null;
		public String name = null;
		public int totalCount = 0;

		public void addToCount(int count) {
			totalCount = totalCount + count;
		}
	}

	private class CountComparator implements Comparator<MessageSummary> {
		@Override
		public int compare(MessageSummary a, MessageSummary b) {
			if (a.totalCount > b.totalCount) {
				return -1;
			} else if (a.totalCount < b.totalCount) {
				return 1;
			}

			return 0;
		}
	}

	private class MyAdapter extends BaseAdapter {
		private List<String> list;

		public void setList(List<String> list) {
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
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView textView = null;
			if (convertView == null) { // item View
				textView = new TextView(getActivity());
			} else {
				textView = (TextView) convertView;
			}
			textView.setText(list.get(position));
			textView.setPadding(20, 20, 20, 20);
			textView.setBackgroundColor(Color.GREEN);
			return textView;
		}
	}
}