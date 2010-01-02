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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class Http
{
    private static final String TAG = "asocial.Http";

    private static final int RESPONSE_OK = 200;

    private static final int readTimeout = 25*1000;
    private static final int connTimeout = 10*1000;

    public static String get(
        String url, 
        Map<String, String> headers) 
    throws IOException, HttpException
    {
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = createConnection(url, headers, "GET");

        int responseCode = conn.getResponseCode();
        if(responseCode != RESPONSE_OK) {
            throw new HttpException(responseCode, conn.getResponseMessage());
        }

        return read(conn);
    }

    public static HttpURLConnection getConnection(
        String url,
        Map<String, String> headers) 
    throws IOException, HttpException
    {
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = createConnection(url, headers, "GET");

        int responseCode = conn.getResponseCode();
        if(responseCode != RESPONSE_OK) {
            throw new HttpException(responseCode, conn.getResponseMessage());
        }

        return conn;
    }

    public static String post(
        String url, 
        Map<String, String> headers,
        String postParam)
    throws IOException, HttpException
    {
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = createConnection(url, headers, "POST");

        conn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        byte[] bytes = postParam.getBytes("UTF-8");
        conn.setRequestProperty("Content-Length",
            Integer.toString(bytes.length));
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if(responseCode != RESPONSE_OK) {
            throw new HttpException(responseCode, conn.getResponseMessage());
        }

        return read(conn);
    }

    public static String post(
        String url, 
        Map<String, String> headers,
        Map<String, String> postParams)
    throws IOException, HttpException
    {
        String postParam = encodeParams(postParams, "&", false);

        return post(url, headers, postParam);
    }

    private static String read(HttpURLConnection conn) throws IOException
    {
        InputStream is = conn.getInputStream();

        BufferedReader br = 
            new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer buf = new StringBuffer();
        String line;
        while (null != (line = br.readLine())) {
            buf.append(line).append("\n");
        }
        return buf.toString();
    }

    private static HttpURLConnection createConnection(
        String url, 
        Map<String, String> headers,
        String type)
    throws IOException
    {
        HttpURLConnection conn = 
            (HttpURLConnection) new URL(url).openConnection();

        conn.setRequestMethod(type);
        conn.setConnectTimeout(connTimeout);
        conn.setReadTimeout(readTimeout);

        for(String key: headers.keySet()) {
            conn.addRequestProperty(key, headers.get(key));
        }
        return conn;
    }

    public static String encodeParams(
        Map<String, String> postParams,
        String delimiter,
        boolean quote)
    {
        StringBuffer buf = new StringBuffer();
        int i=0;
        for(String key : postParams.keySet())
        {
            if(i++ != 0) buf.append(delimiter);
            buf.append(encode(key)).append("=");
            if(quote) buf.append("\"");
            buf.append(encode(postParams.get(key)));
            if(quote) buf.append("\"");
        }
        return buf.toString();
    }

    public static String encode(String s) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(s, "UTF-8");
        } catch(UnsupportedEncodingException uee) {
        }
        StringBuffer buf = new StringBuffer(encoded.length());
        char c;
        for (int i = 0; i < encoded.length(); i++) {
            c = encoded.charAt(i);
            if (c == '*') {
                buf.append("%2A");
            } else if (c == '+') {
                buf.append("%20");
            } else if (c == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' 
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
