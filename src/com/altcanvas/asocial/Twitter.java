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


package com.altcanvas.asocial;

import org.apache.commons.codec.binary.Base64;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import android.util.Log;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Twitter
{
    private static final String TAG = "asocial.Twitter";

    private static final String OAUTH_CONSUMER_KEY=
        "KEY GOES HERE";
    private static final String OAUTH_CONSUMER_SECRET=
        "SECRET GOES HERE";
    private static final String HMACSHA1="HmacSHA1";
    private static final String OAUTH_SIGNATURE_METHOD="HMAC-SHA1";

    private static final String twitterURL  = "http://twitter.com/";
    private static final String updateURL = twitterURL+"statuses/update.json";
    private static final String reqtokenURL =
        "http://twitter.com/oauth/request_token";
    private static final String authorizeURL =
        "http://twitter.com/oauth/authorize";
    private static final String accessTokenURL = 
        "http://twitter.com/oauth/access_token";
    private static final String friendsTimelineURL = 
            twitterURL + "statuses/friends_timeline.json";

    private static final String SOURCE = "TwitSpeak";
    private static final String VERSION = "1.0";
    private static final String SOURCE_URL = 
        "http://www.altcanvas.com/android/twitspeak";
    private static final String USER_AGENT = 
        "TwitSpeak http://www.altcanvas.com/android/twitspeak/ /"+VERSION;

    private HashMap<String, String> headers;

    public String token = null;
    public String tokenSecret = null;

    public Twitter()
    {
        headers = new HashMap<String, String>();
        headers.put("X-Twitter-Client",         SOURCE);
        headers.put("X-Twitter-Client-Version", VERSION);
        headers.put("X-Twitter-Client-URL",     SOURCE_URL);
    }

    /*
     * Twitter Authentication methods
     */
    public Twitter setBasicAuth(String username, String password)
    {
        headers.put("Authorization", "Basic "+new String(
                Base64.encodeBase64((username+":"+password).getBytes())));
        return this;
    }

    public Twitter setOAuth(
        String method, String url, 
        Map<String, String> postParams)
    {
        headers.put("Authorization", "OAuth "+
            generateAuthorizationHeader(method, url, postParams));
        return this;
    }

    public void setOAuthAccessToken(String token, String tokenSecret)
    {
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    public void resetOAuthTokens()
    {
        this.token = null;
        this.tokenSecret = null;
    }

    public void getRequestToken()
        throws AsocialException 
    {
        Map<String, String> noPostParams = new HashMap<String, String>();

        setOAuth("POST", reqtokenURL, noPostParams);

        try {
            String response = Http.post(reqtokenURL, headers, noPostParams);
            if(response == null) {
                throw new AsocialException(AsocialException.EMPTY_RESP,
                                            "Empty response");
            }
            HashMap<String, String> map = parseResponse(response);
            this.token = map.get("oauth_token");
            this.tokenSecret = map.get("oauth_token_secret");
        } catch(HttpException he) {
            throw new AsocialException(he.responseCode, he.toString());
        } catch (IOException ioe) {
            throw new AsocialException(AsocialException.IOE, ioe.toString());
        }
    }

    public String generateAuthURL()
    {
        return authorizeURL+"?oauth_token="+token;
    }

    public void getAccessToken(String pin)
        throws AsocialException
    {
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("oauth_verifier", pin);

        setOAuth("POST", accessTokenURL, postParams);

        try {
            String response = Http.post(accessTokenURL, headers, postParams);
            if(response == null) {
                throw new AsocialException(AsocialException.EMPTY_RESP,
                                            "Empty response");
            }
            HashMap<String, String> map = parseResponse(response);
            this.token = map.get("oauth_token");
            this.tokenSecret = map.get("oauth_token_secret");
        } catch(HttpException he) {
            throw new AsocialException(he.responseCode, he.toString());
        } catch (IOException ioe) {
            throw new AsocialException(AsocialException.IOE, ioe.toString());
        }
    }

    public HashMap<String, String> parseResponse(String response)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] pairs = response.split("&");
        for(String pairStr : pairs) {
            String[] pair = pairStr.split("=");
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    public JSONArray getFriendsTimeline()
        throws AsocialException
    {
        return getFriendsTimeline(null);
    }

    public JSONArray getFriendsTimeline(String maxid) 
        throws AsocialException
    {
        Map<String, String> noPostParams = new HashMap<String, String>();

        String urlsuffix = "";
        ArrayList<String> params = new ArrayList<String>();
        if(maxid != null) {
            params.add("max_id="+maxid);
        }
        if(params.size() > 0) {
            int i=0;
            for(String param : params) {
                urlsuffix += ((i++ == 0) ? "?":"&") + param;
            }
        }
        String url = friendsTimelineURL + urlsuffix;

        setOAuth("GET", url, noPostParams);
        try {
            String response = Http.get(url, headers);
            if(response == null) throw new AsocialException("Empty response");
            return new JSONArray(response);
        } catch(HttpException he) {
            throw new AsocialException(he.responseCode, he.toString());
        } catch(IOException e) {
            // possible n/w timeout
            throw new AsocialException("IOException: "+e); 
        } catch(JSONException e) {
            throw new AsocialException("JSONException: "+e); 
        }
    }

    public JSONObject update(String status) throws AsocialException
    {
        HashMap<String, String> postParams = new HashMap<String, String>();
        postParams.put("status", status);
        postParams.put("source", SOURCE);

        setOAuth("POST", updateURL, postParams);

        try {
            return new JSONObject(
                Http.post(updateURL, headers, postParams));
        } catch(HttpException he) {
            throw new AsocialException(he.responseCode, he.toString());
        } catch (JSONException je) {
            throw new AsocialException(AsocialException.JSONE, je.toString());
        } catch (IOException ioe) {
            throw new AsocialException(AsocialException.IOE, ioe.toString());
        }
    }

    public JSONObject verifyCreds() throws AsocialException
    {
        try {
            JSONObject json = new JSONObject(
                Http.get(twitterURL+"account/verify_credentials.json",
                        headers));

            // Authentication failed
            if(json.optString("error",null) != null) return null;

            return json;

        } catch(HttpException he) {
            throw new AsocialException(he.responseCode, he.toString());
        } catch (JSONException je) {
            throw new AsocialException(AsocialException.JSONE, je.toString());
        } catch (IOException ioe) {
            throw new AsocialException(AsocialException.IOE, ioe.toString());
        }
    }

    /*
     * Internal helper methods
     */
    private String generateAuthorizationHeader(
        String method,
        String url,
        Map<String, String> postParams
    )
    {
        HashMap<String, String> oauthHeaderParams = 
            new HashMap<String, String>();

        // Insert hardcoded oauth entries

        long timestamp = System.currentTimeMillis()/1000;
        long nonce = timestamp + 5;

        oauthHeaderParams.put(
            "oauth_consumer_key", OAUTH_CONSUMER_KEY);
        oauthHeaderParams.put(
            "oauth_signature_method", OAUTH_SIGNATURE_METHOD);
        oauthHeaderParams.put(
            "oauth_timestamp", ""+timestamp);
        oauthHeaderParams.put(
            "oauth_nonce", ""+nonce);
        oauthHeaderParams.put(
            "oauth_version", "1.0");
        if(this.token != null) {
            oauthHeaderParams.put(
                "oauth_token", this.token);
        }

        // Insert entries from post parameters

        for(Map.Entry<String, String> e : postParams.entrySet()) {
            oauthHeaderParams.put(e.getKey(), e.getValue());
        }

        // Parse and insert GET parameters 
        parseGetParams(url, oauthHeaderParams);

        StringBuffer base = new StringBuffer(method).append("&")
            .append(Http.encode(extractBaseUrl(url))).append("&")
            .append(Http.encode(Http.encodeParams(
                        new TreeMap(oauthHeaderParams),"&",false)));

        String signature = new String(generateSignature(base.toString()));

        oauthHeaderParams.put("oauth_signature", signature);
        
        for(Map.Entry<String, String> e : postParams.entrySet()) {
            oauthHeaderParams.remove(e.getKey());
        }
        return Http.encodeParams(oauthHeaderParams, ",", true);
    }

    private String extractBaseUrl(String url)
    {
        int qpos = url.indexOf("?");
        if(qpos > 0) {
            return url.substring(0,qpos);
        } else {
            return url;
        }
    }

    private void parseGetParams(String url, Map<String, String> map)
    {
        int qpos = url.indexOf("?");
        if(qpos != -1) {
            String[] paramStrs = url.substring(qpos + 1).split("&");
            try {
                for(String param : paramStrs) {
                    String[] pair = param.split("=");
                    if(pair.length == 2) {
                        map.put(URLDecoder.decode(pair[0], "UTF-8"),
                                URLDecoder.decode(pair[1], "UTF-8"));
                    } else {
                        map.put(URLDecoder.decode(pair[0], "UTF-8"), "");
                    }
                }
            } catch (UnsupportedEncodingException never) {}
        }
    }

    private byte[] generateSignature(String data)
    {
        SecretKeySpec spec = null;
        if(this.tokenSecret == null)
        {
            spec = new SecretKeySpec(
                    (Http.encode(OAUTH_CONSUMER_SECRET)+"&").getBytes(), HMACSHA1);
        } else {
            spec = new SecretKeySpec(
                    (Http.encode(OAUTH_CONSUMER_SECRET)+"&"+Http.encode(tokenSecret))
                    .getBytes(), HMACSHA1);
        }

        try {
            Mac mac = Mac.getInstance(HMACSHA1);
            mac.init(spec);
            byte[] byteHMAC = mac.doFinal(data.getBytes());
            return Base64.encodeBase64(byteHMAC);
        } catch(NoSuchAlgorithmException nsae) {
        } catch(InvalidKeyException ike) {
        }
        return null;
    }

}
