/*
 * Copyright (C) 2010 Jayesh Salvi (http://www.altcanvas.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.altcanvas.twitspeak;

import com.altcanvas.asocial.*;
import android.os.AsyncTask;
import android.app.Activity;
import org.json.*;

public class TwitterTask extends 
    AsyncTask<TwitterTask.Payload, Object, TwitterTask.Payload>
{
    public static final String TAG = "TwitterTask";

    public static final int FRIENDS_TIMELINE_DLOAD  = 1001;
    public static final int FRIENDS_TIMELINE_DLOAD2  = 1002;
    public static final int UPDATE = 1003;

    public void onPostExecute(TwitterTask.Payload payload)
    {
        Object[] data = (Object[]) payload.data;
        switch(payload.taskType) {
        case FRIENDS_TIMELINE_DLOAD2:
            Activity activity = (Activity) data[0];
            if(payload.result == null) {
                String msg = (payload.exception == null) ? 
                    activity.getString(R.string.unkexcstr) : 
                    payload.exception.toString();
                G.toast(activity, msg);
            } else {
                G.toast(activity, activity.getString(R.string.donestr));
            }
            break;

        case FRIENDS_TIMELINE_DLOAD:
            TwitSpeakActivity app = (TwitSpeakActivity) data[0];
            if(payload.result == null) {
                String msg = (payload.exception == null) ? 
                    app.getString(R.string.unkexcstr) : 
                    payload.exception.toString();
                G.toast(app, msg);
            } else {
                Twit twit = app.db.getLatestTwit();
                app.db.markShown(twit);
                app.speak(twit);
            }
            break;
        case UPDATE:
            activity = (Activity) data[0];
            if(payload.result == null) {
                G.toast(activity, "Update failed");
            } else {
                G.toast(activity, "Update posted");
            }
            break;
        }
    }
    
    public void onProgressUpdate(Object... value) {}

    public TwitterTask.Payload doInBackground(TwitterTask.Payload... params)
    {
        TwitterTask.Payload payload = params[0];
        Object[] data = (Object[]) payload.data;

        switch(payload.taskType) {
        case FRIENDS_TIMELINE_DLOAD:
        case FRIENDS_TIMELINE_DLOAD2:
            try {
                Database db = (Database) data[2];
                Twitter twitter = (Twitter) data[1];

                JSONArray timeline = twitter.getFriendsTimeline();

                if(timeline.length() > 0) {
                    // We have new twits, delete all old ones
                    db.deleteAllTwits(null);
                }

                for(int i=0; i < timeline.length(); i++)
                {
                    JSONObject twit = timeline.getJSONObject(i);

                    Twit t = new Twit();
                    t.text = twit.getString("text");
                    t.timestamp = Twit.sanitizeTimestamp(
                                    twit.getString("created_at"));
                    JSONObject user = twit.optJSONObject("user");
                    if(user != null) {
                        t.username = user.getString("name");
                        t.screenname = user.getString("screen_name");
                    }
                    db.insertTwit(t);
                }

                payload.result = new Object();

            } catch(AsocialException ae) {
                payload.exception = ae;
                payload.result = null;
            } catch(JSONException je) {
                payload.exception = je;
                payload.result = null;
            }
            break;
        case UPDATE:
            try {
                Twitter twitter = (Twitter) data[1];
                String update = (String) data[2];
                payload.result = twitter.update(update);
            } catch(AsocialException ae) {
                payload.exception = ae;
                payload.result = null;
            }
            break;
        }
        return payload;
    }

    public static class Payload 
    {
        public int taskType;
        public Object data;
        public Object result;
        public Exception exception;

        public Payload(int taskType, Object data) {
            this.taskType = taskType;
            this.data = data;
        }
    }
}
