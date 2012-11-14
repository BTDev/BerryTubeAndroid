/*
 * BerryTubeChat android client
 * Copyright (C) 2012 Daniel Triendl <trellmor@trellmor.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trellmor.BerryTubeChat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.trellmor.BerryTube.ChatMessage;

/**
 * This class is used to inflate and populate a <code>View</code> to display a
 * <code>ChatMessage</code>
 * 
 * @author Daniel Triendl
 * @see com.trellmor.BerryTube.ChatMessage
 */
public class ChatMessageFormatter {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private FlairGetter mFlairGetter = null;
	private String mNick = null;

	public ChatMessageFormatter(Context contest, LayoutInflater inflater) {
		mContext = contest;
		mInflater = inflater;

		mFlairGetter = new FlairGetter(mContext.getResources());
	}

	public View format(View view, ChatMessage message) {

		switch (message.getEmote()) {
		case ChatMessage.EMOTE_DRINK:
			return formatDrinks(view, message);
		case ChatMessage.EMOTE_ACT:
		case ChatMessage.EMOTE_REQUEST:
		case ChatMessage.EMOTE_POLL:
		case ChatMessage.EMOTE_RCV:
			return formatEmote(view, message);
		default:
			return formatDefault(view, message);
		}
	}

	public void setNick(String nick) {
		mNick = nick;
	}

	private View formatDrinks(View view, ChatMessage message) {
		view = mInflater.inflate(R.layout.chat_item_drink, null);
		TextView textChatMessage = (TextView) view
				.findViewById(R.id.text_chat_message);
		TextView textChatMultiple = (TextView) view
				.findViewById(R.id.text_chat_drink_multiple);

		textChatMessage.setText(message.getNick() + ": " + message.getMsg() + " " + mContext.getString(R.string.chat_drink));

		if (message.getMulti() > 1) {
			textChatMultiple
					.setText(Integer.toString(message.getMulti()) + "x");
			textChatMultiple.setVisibility(View.VISIBLE);
		}

		return view;
	}

	private View formatDefault(View view, ChatMessage message) {
		view = mInflater.inflate(R.layout.chat_item, null);

		TextView textChatMessage = (TextView) view
				.findViewById(R.id.text_chat_message);

		textChatMessage.setText(formatChatMsg(message));

		return view;
	}

	private View formatEmote(View view, ChatMessage message) {

		view = mInflater.inflate(R.layout.chat_item, null);
		TextView textChatMessage = (TextView) view
				.findViewById(R.id.text_chat_message);

		switch (message.getEmote()) {
		case ChatMessage.EMOTE_REQUEST:
			textChatMessage.setTextColor(Color.BLUE);
			textChatMessage.setText(Html.fromHtml(createTimestamp(message.getTimestamp()) + "<b><i>" + message.getNick() + 
					" requests " + message.getMsg() + "</i></b>"));
			break;
		case ChatMessage.EMOTE_ACT:
			textChatMessage.setTextColor(Color.GRAY);
			textChatMessage.setText(Html.fromHtml(createTimestamp(message.getTimestamp()) + "<i>" + message.getNick() + " " +
					message.getMsg() + "</i>"));
			break;
		case ChatMessage.EMOTE_POLL:
			textChatMessage.setTextColor(Color.parseColor("#008000"));
			textChatMessage.setTextSize(18);
			textChatMessage.setGravity(Gravity.CENTER_HORIZONTAL);
			textChatMessage.setText(Html.fromHtml("<b>" + message.getNick() + " created a new poll \"" + message.getMsg() + "\"</b>"));
			break;
		case ChatMessage.EMOTE_RCV:
			textChatMessage.setTextColor(Color.RED);
			textChatMessage.setTextSize(18);
			textChatMessage
					.setText(createTimestamp(message.getTimestamp()) + message.getNick() + ": " + message.getMsg());
		default:
			textChatMessage.setText(formatChatMsg(message));
			break;

		}

		return view;
	}

	private String highlightNick(String msg) {
		if (mNick == null)
			return msg;
		return msg.replace(mNick, "<font color=\"#ff0000\">" + mNick + "</font>");
	}
	
	private String createTimestamp(long timeStamp) {
		String result = "";
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (prefs.getBoolean(MainActivity.KEY_TIMESTAMP, false) && timeStamp > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ", Locale.ENGLISH);
			result = sdf.format(new Date(timeStamp));
		}
		
		return result;
	}

	private Spanned formatChatMsg(ChatMessage message) {
		StringBuffer sb = new StringBuffer();
		sb.append(createTimestamp(message.getTimestamp()));
		sb.append("<b>").append(message.getNick()).append("</b>");
		if (message.getFlair() > 0) {
			sb.append("<img src=\"").append(message.getFlair()).append("\" />");
		}
		sb.append(": ");

		// flutter shit
		String m = message.getMsg().replaceAll(
				"<span class=\"flutter\">(.*)</span>",
				"<font color=\"#FF5499\">$1</font>");

		// implying
		if (m.startsWith("&gt;"))
			m = "<font color=\"#789922\">" + m + "</font>";

		if (message.isHighlightable()) {
			sb.append(highlightNick(m));
		} else {
			sb.append(m);
		}

		return Html.fromHtml(sb.toString(), mFlairGetter, null);
	}
}
