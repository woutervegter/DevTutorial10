package me.moop.mytwitter.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.moop.mytwitter.R;
import me.moop.mytwitter.model.Tweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TweetsListAdapter extends BaseAdapter {
	
	ArrayList<Tweet> mTweets;
	LayoutInflater mInflater;
	
	public TweetsListAdapter(Context context, ArrayList<Tweet> tweets){
		mTweets = new ArrayList<Tweet>();
		mTweets.addAll(tweets);
		notifyDataSetChanged();
        mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return mTweets.size();
	}

	public Tweet getItem(int position) {
		return mTweets.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.tweet_listitem, null);

			holder = new ViewHolder();
			holder.mTxtvText = (TextView) convertView.findViewById(R.id.txtvText);
			holder.mTxtvCreatedAt = (TextView) convertView.findViewById(R.id.txtvCreatedAt);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Tweet tweet = getItem(position);

		holder.mTxtvText.setText(tweet.getText());

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateString = simpleDateFormat.format(tweet.getCreatedAt());
		holder.mTxtvCreatedAt.setText(dateString);

		return convertView;
	}

    private class ViewHolder {
        TextView mTxtvText;
        TextView mTxtvCreatedAt;
    }
}
