package com.altcanvas.twitspeak;

import android.os.Bundle;
import android.preference.*;
import android.app.*;
import android.content.*;
import com.altcanvas.asocial.Twitter;
import android.widget.*;
import android.view.*;

public class SettingsActivity extends PreferenceActivity
{
    private static final String TAG = "SettingsActivity";

    public Database db = null;
    private Dialog twtabtDlg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        G.init(this);
        db = new Database(this);

        findPreference("refresh").setOnPreferenceClickListener(
            new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref)
                {
                    Twitter twitter = G.checkTwitterCreds();
                    if(twitter == null) {
                        startActivityForResult(new Intent(
                            SettingsActivity.this, TwitterLoginActivity.class),
                            G.REQCODE_TWITTER_LOGIN);
                    } else {
                        G.toast(SettingsActivity.this, 
                                getString(R.string.syncstr));
                        new TwitterTask().execute(
                            new TwitterTask.Payload(
                                TwitterTask.FRIENDS_TIMELINE_DLOAD2,
                                new Object[] {
                                    SettingsActivity.this, twitter, db})
                        );
                    }
                    return true;
                }
            }
        );

        findPreference("comment").setOnPreferenceClickListener(
            new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref)
                {
                    Twitter twitter = G.checkTwitterCreds();
                    if(twitter == null) {
                        startActivityForResult(new Intent(
                            SettingsActivity.this, TwitterLoginActivity.class),
                            G.REQCODE_TWITTER_LOGIN);
                        return true;
                    }

                    twtabtDlg = new Dialog(SettingsActivity.this);
                    twtabtDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    twtabtDlg.setContentView(R.layout.twitabout);

                    ((Button) twtabtDlg.findViewById(R.id.postbutton))
                        .setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                update();
                            }
                        }
                    );
                    twtabtDlg.show();
                    return true;
                }
            }
        );
    }

    private void update()
    {
        EditText status = (EditText) twtabtDlg.findViewById(R.id.twittext);
        if(status.getText().toString().length() > 140) {
            G.toast(this, "Exceeds 140 chars");
            return;
        }
        Twitter twitter = G.checkTwitterCreds();
        new TwitterTask().execute(
            new TwitterTask.Payload(TwitterTask.UPDATE,
                new Object[] { this, twitter, status.getText().toString() })
        );
        twtabtDlg.cancel();
    }

    @Override
    public void onDestroy() {
        if(db != null) db.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) 
    {
        if(requestCode == G.REQCODE_TWITTER_LOGIN) {
            Twitter twitter = G.checkTwitterCreds();
            if(twitter != null) {
                new TwitterTask().execute(
                    new TwitterTask.Payload(
                        TwitterTask.FRIENDS_TIMELINE_DLOAD2,
                        new Object[] {
                            SettingsActivity.this, twitter, db })
                );
            }
        }
    }

}
