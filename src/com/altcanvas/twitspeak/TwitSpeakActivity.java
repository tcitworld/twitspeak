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

import android.app.*;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import android.content.Intent;
import android.media.AudioManager;
import android.content.*;

import java.util.*;
import java.io.*;

import com.altcanvas.asocial.*;
import org.json.*;

public class TwitSpeakActivity extends Activity 
    implements TextToSpeech.OnInitListener, 
                TextToSpeech.OnUtteranceCompletedListener
{
    private static final String TAG = "TwitSpeakActivity";


    private TextView twitBox = null;
    private ImageView settingsIcon = null;
    private TextToSpeech mTts;
    public Database db = null;

    private Thread.UncaughtExceptionHandler systemDefaultUEH = null;
    public Twitter twitter = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        systemDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
            new TopExceptionHandler(this));

        setContentView(R.layout.main);

        boolean hasStackTrace = checkStackTrace();
        if(!hasStackTrace) continueOnCreate();
    }

    public void continueOnCreate()
    {
        G.init(this);
        db = new Database(this);

        twitBox = (TextView) findViewById(R.id.twitbox);
        settingsIcon = (ImageView) findViewById(R.id.settings);
        twitBox.setText("Loading");

        twitBox.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    speakTwit();
                }
            }
        );

        settingsIcon.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    Intent iSettings = new Intent();
                    iSettings.setClassName("com.altcanvas.twitspeak",
                                            SettingsActivity.class.getName());
                    TwitSpeakActivity.this.startActivity(iSettings);
                }
            }
        );

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, G.REQCODE_CHECK_TTS);
    }

    private void promptTTSInstall()
    {
        AlertDialog ttsInstallDlg = new AlertDialog.Builder(this).create(); 
        ttsInstallDlg.setMessage(getString(R.string.tts_install_msg));
        ttsInstallDlg.setTitle(getString(R.string.tts_install_title));

        ttsInstallDlg.setButton(DialogInterface.BUTTON_POSITIVE, 
            getResources().getString(R.string.proceed_str),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
            }
        );

        ttsInstallDlg.setButton(DialogInterface.BUTTON_NEGATIVE, 
            getResources().getString(R.string.cancel_str),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) 
                { 
                    dialog.cancel();
                    finish();
                }
            }
        );
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) 
    {
        if (requestCode == G.REQCODE_CHECK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                init();
            } else {
                // missing data, install it
                promptTTSInstall();
            }
        } else if(requestCode == G.REQCODE_TWITTER_LOGIN) {
            this.twitter = G.checkTwitterCreds();
            if(this.twitter != null) {
                speakTwit();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        if(db != null) db.close();
        super.onDestroy();
    }

    public void init()
    {
        mTts = new TextToSpeech(this, this);
        mTts.setOnUtteranceCompletedListener(this);
    }

    public void speakTwit()
    {
        Twit twit = db.getLatestTwit();
        if(twit != null) {
            db.markShown(twit);
            speak(twit);
        } else {
            twitBox.setText(getString(R.string.syncstr));
            new TwitterTask().execute(
                new TwitterTask.Payload(
                    TwitterTask.FRIENDS_TIMELINE_DLOAD,
                    new Object[] {
                        TwitSpeakActivity.this, 
                        TwitSpeakActivity.this.twitter,
                        TwitSpeakActivity.this.db })
            );
        }
    }

    public void speak(Twit twit)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UTTID");
        mTts.speak(twit.getSpeech(), TextToSpeech.QUEUE_FLUSH, params);
        twitBox.setText("@"+twit.screenname+": "+twit.text);
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            } else {
                this.twitter = G.checkTwitterCreds();

                if(twitter == null) {
                    startActivityForResult(
                        new Intent(this, TwitterLoginActivity.class),
                                            G.REQCODE_TWITTER_LOGIN);
                } else {
                    speakTwit();
                }
            }
        } else {
            Log.e(TAG, "TTS init error: "+status);
        }
    }

    public void onUtteranceCompleted (String utteranceId) {
        Log.i(TAG, "utterance completed");
    }

    public boolean checkStackTrace()
    {
        FileInputStream traceIn = null;
        try {
            traceIn = openFileInput("stack.trace");
            traceIn.close();
        } catch(FileNotFoundException fnfe) {
            // No stack trace available
            return false;
        } catch(IOException ioe) {
            return false;
        }

        AlertDialog alert = new AlertDialog.Builder(this).create(); 
        alert.setMessage(getResources().getString(R.string.crashreport_msg));

        alert.setButton(DialogInterface.BUTTON_POSITIVE, 
            getResources().getString(R.string.emailstr),
            new DialogInterface.OnClickListener() {
                public void onClick(
                    DialogInterface dialog, int which)
                {
                    String trace = "";
                    String line = null;
                    try {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(TwitSpeakActivity.this
                                            .openFileInput("stack.trace")));
                        while((line = reader.readLine()) != null) {
                            trace += line+"\n";
                        }
                    } catch(FileNotFoundException fnfe) {
                        Log.logException(TAG, fnfe);
                    } catch(IOException ioe) {
                        Log.logException(TAG, ioe);
                    }

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    String subject = "Error report";
                    String body = 
                                getResources().getString(
                                    R.string.mailthisto_msg)+
                                " jayesh@altcanvas.com: "+
                                "\n\n"+
                                G.getPhoneInfo()+
                                "\n\n"+
                                "TwitSpeak ["+G.VERSION_STRING+"]"+
                                "\n\n"+
                                trace+
                                "\n\n";

                    sendIntent.putExtra(Intent.EXTRA_EMAIL, 
                            new String[] {"jayesh@altcanvas.com"});
                    sendIntent.putExtra(Intent.EXTRA_TEXT, body);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    sendIntent.setType("message/rfc822");

                    TwitSpeakActivity.this.startActivityForResult(
                        Intent.createChooser(sendIntent, 
                        getResources().getString(R.string.emailstr)),
                        G.REQCODE_EMAIL_STACK_TRACE); 

                    TwitSpeakActivity.this.deleteFile("stack.trace");
                }
            }
        );
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, 
            getResources().getString(R.string.cancelstr),
            new DialogInterface.OnClickListener() {
                public void onClick(
                    DialogInterface dialog, int which) 
                { 
                    TwitSpeakActivity.this.deleteFile("stack.trace");
                    TwitSpeakActivity.this.continueOnCreate();
                }
            }
        );

        alert.setOnCancelListener(
            new DialogInterface.OnCancelListener()
            {
                public void onCancel(DialogInterface dialog)
                {
                    TwitSpeakActivity.this.deleteFile("stack.trace");
                    TwitSpeakActivity.this.continueOnCreate();
                }
            }
        );

        alert.show();

        return true;
    }

}
