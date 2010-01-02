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
import android.content.*;
import java.util.regex.*;
import com.altcanvas.asocial.Twitter;
import android.widget.Toast;
import android.os.Build;

public class G
{
    private static final String TAG = "G";

    public static final String VERSION_STRING = "1.0";

    public static final int REQCODE_CHECK_TTS = 1001;
    public static final int REQCODE_TWITTER_LOGIN = 1002;
    public static final int REQCODE_EMAIL_STACK_TRACE = 1003;

    public static SharedPreferences prefs = null;
    public static SharedPreferences.Editor prefsEd = null;

    public static final Pattern urlPattern = Pattern.compile(
    "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    public static void init(Context context)
    {
        prefs = context.getSharedPreferences(
                            "com.altcanvas.twitspeak_preferences", 0);
        prefsEd = prefs.edit();
    }

    public static String getPhoneInfo()
    {
        return Build.DEVICE + ", " + Build.MODEL + ", " + Build.PRODUCT;
    }

    public static AlertDialog alert(Context context, String msg)
    {
        AlertDialog a = new AlertDialog.Builder(context).create(); 
        a.setMessage(msg); 
        a.setCancelable(true); 
        a.show();
        return a;
    }

    public static void toast(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static Twitter checkTwitterCreds()
    {
        String acctoken = prefs.getString("twitter_access_token", null);
        String accsecret = prefs.getString("twitter_access_token_secret", null);

        if(acctoken != null && accsecret != null) {
            Twitter twt = new Twitter();
            twt.setOAuthAccessToken(acctoken, accsecret);
            return twt;
        } 
        return null;
    }

}
