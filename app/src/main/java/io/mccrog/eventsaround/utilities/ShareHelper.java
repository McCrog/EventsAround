package io.mccrog.eventsaround.utilities;

import android.content.Context;
import android.content.Intent;

import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;

public class ShareHelper {
    private final Context mContext;

    public ShareHelper(Context context) {
        this.mContext = context;
    }

    public void shareEvent(Event event) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String[] shareMessage = prepareMessage(event);
        intent.putExtra(Intent.EXTRA_SUBJECT, shareMessage[0]);
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage[1]);
        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.choose_sharing_method)));
    }

    private String[] prepareMessage(Event event) {
        String[] message = new String[2];
        String title = mContext.getResources().getString(R.string.app_name) + ": " + event.getTitle();
        String type = EventTypeHelper.getEventType(mContext, event.getType());

        String endsDateTimeResourceText = mContext.getResources().getString(R.string.ends_date_time);
        long endsDateTime = event.getEndsDateTime();
        if (endsDateTime < DateTimeUtil.getCurrentDateTimeInMillis()) {
            endsDateTimeResourceText = mContext.getResources().getString(R.string.ended_date_time);
        }
        String endsDateTimeText = DateTimeUtil.getFormattedDateTimeResourcesString(
                endsDateTimeResourceText, endsDateTime);
        String bullet = mContext.getResources().getString(R.string.bullet);

        String body = event.getTitle() +
                "\n" +
                type + " " + bullet + " " + endsDateTimeText +
                "\n" +
                mContext.getString(R.string.address) + " " + event.getPlace().getAddress() +
                "\n" +
                mContext.getString(R.string.description_text) + " " + event.getDescription();

        message[0] = title;
        message[1] = body;
        return message;
    }
}
