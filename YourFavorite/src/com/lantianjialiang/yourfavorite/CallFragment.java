package com.lantianjialiang.yourfavorite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CallFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "call_section_number";

	private final List<Call> calls = new ArrayList<Call>();
	private final List<CallSummary> summaryList = new ArrayList<CallSummary>();

	private final List<String> sortedResult = new ArrayList<String>();

	private static int MAX = 10;
	private MyAdapter adapter = null;
	private ListView lv = null;

	public CallFragment() {
	}

	private void getCallHistory() {
		calls.clear();

		Call call = null;
		Cursor cursor = this.getActivity().getContentResolver()
				.query(CallLog.Calls.CONTENT_URI, null, null, null, null);

		if (cursor.getCount() <= 0) {
			return;
		}

		cursor.moveToFirst();
		do {
			call = new Call();

			/* Reading Name */
			String nameTemp = cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.CACHED_NAME));
			if (nameTemp == null) {
				nameTemp = "";
			}

			if ("".equals(nameTemp)) {
				call.name = "";
			} else {
				call.name = nameTemp;
			}
			/* Reading Date */
			call.date = cursor.getLong(cursor
					.getColumnIndex(CallLog.Calls.DATE));

			/* Reading duration */
			call.duration = cursor.getLong(cursor
					.getColumnIndex(CallLog.Calls.DURATION));

			/* Reading Date */
			call.type = cursor
					.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));

			call.phoneNumber = cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.NUMBER));

			calls.add(call);
		} while (cursor.moveToNext());

		calculateSummary();
	}

	private void calculateSummary() {
		if (calls == null || calls.size() == 0) {
			return;
		}

		summaryList.clear();

		CallSummary cs = null;
		for (int i = 0; i < calls.size(); i++) {
			cs = null;

			for (int j = 0; j < summaryList.size(); j++) {
				if (calls.get(i).phoneNumber
						.equals(summaryList.get(j).phoneNumber)) {
					cs = summaryList.get(j);
					cs.addToCount(1);
					cs.addToDuration(calls.get(i).duration);
					break;
				}
			}

			if (cs == null) {
				cs = new CallSummary();
				cs.phoneNumber = calls.get(i).phoneNumber;
				cs.name = calls.get(i).name;
				cs.addToCount(1);
				cs.addToDuration(calls.get(i).duration);
				summaryList.add(cs);
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_call, container,
				false);

		adapter = new MyAdapter();
		adapter.setList(sortedResult);
		lv = (ListView) rootView.findViewById(R.id.callList);
		lv.setAdapter(adapter);

		getCallHistory();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		getCallHistory();
	}

	public void sortByDuration() {
		sortedResult.clear();

		Collections.sort(summaryList, new DurationComparator());

		int count = MAX;
		CallSummary cs = null;
		if (summaryList.size() < MAX) {
			count = summaryList.size();
		}

		String prefix = "";
		for (int i = 0; i < count; i++) {
			cs = summaryList.get(i);
			prefix = cs.name;
			if (cs.name.equals("")) {
				prefix = cs.phoneNumber;
			}

			String duration = "";
			if (cs.totalDuration > 60) {
				duration = (cs.totalDuration / 60) + " m";
			} else {
				duration = cs.totalDuration + " s";
			}
			sortedResult.add(prefix + ": " + duration);
		}
		adapter.notifyDataSetChanged();
	}

	public void sortByCount() {
		sortedResult.clear();

		Collections.sort(summaryList, new CountComparator());

		int count = MAX;
		CallSummary cs = null;
		if (summaryList.size() < MAX) {
			count = summaryList.size();
		}

		String prefix = "";
		for (int i = 0; i < count; i++) {
			cs = summaryList.get(i);
			prefix = cs.name;
			if (cs.name.equals("")) {
				prefix = cs.phoneNumber;
			}
			sortedResult.add(prefix + ": " + cs.totalCount + " times");
		}
		adapter.notifyDataSetChanged();
	}

	private class CallSummary {
		public String phoneNumber = null;
		public String name = null;
		public int totalCount = 0;
		public long totalDuration = 0;

		public void addToCount(int count) {
			totalCount = totalCount + count;
		}

		public void addToDuration(long duration) {
			totalDuration = totalDuration + duration;
		}
	}

	private class CountComparator implements Comparator<CallSummary> {
		@Override
		public int compare(CallSummary a, CallSummary b) {
			if (a.totalCount > b.totalCount) {
				return -1;
			} else if (a.totalCount < b.totalCount) {
				return 1;
			}

			return 0;
		}
	}

	private class DurationComparator implements Comparator<CallSummary> {
		@Override
		public int compare(CallSummary a, CallSummary b) {
			if (a.totalDuration > b.totalDuration) {
				return -1;
			} else if (a.totalDuration < b.totalDuration) {
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