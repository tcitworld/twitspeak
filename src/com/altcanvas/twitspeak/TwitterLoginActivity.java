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

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.net.Uri;
import com.altcanvas.asocial.*;

public class TwitterLoginActivity extends Activity
{
    private static final String TAG = "TwitterLoginActivity";

    private Twitter twitter = null;

    private EditText pinBox = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.twitterlogin);

        twitter = new Twitter();

        pinBox = (EditText) findViewById(R.id.twitterPin);

        ((Button) findViewById(R.id.twitterAuthorize)).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v)
                {
                    try {
                        twitter.resetOAuthTokens();
                        twitter.getRequestToken();
                        String authUrl = twitter.generateAuthURL();
                        TwitterLoginActivity.this.startActivity( 
                            new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
                    } catch(AsocialException ae) {
                        G.alert(TwitterLoginActivity.this,
                            getResources().getString(
                            R.string.twitter_auth_errmsg1));
                    }
                }
            }
        );
        ((Button) findViewById(R.id.twitterDone)).setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v)
                {
                    try {
                        twitter.getAccessToken(pinBox.getText().toString());
                        G.prefsEd.putString("twitter_access_token", 
                            twitter.token).commit();
                        G.prefsEd.putString("twitter_access_token_secret", 
                            twitter.tokenSecret).commit();
                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    } catch(AsocialException ae) {
                        G.alert(TwitterLoginActivity.this,
                            getResources().getString(
                            R.string.twitter_auth_errmsg2));
                    }
                }
            }
        );
    }
}
