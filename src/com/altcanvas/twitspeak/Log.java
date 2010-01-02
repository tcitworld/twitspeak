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

import java.io.*;
import java.util.Calendar;
import android.content.Context;
import java.text.DateFormat;
import java.util.*;

public class Log
{
    public static String RED="\033[01;31m";
    public static String GREEN="\033[01;32m";
    public static String YELLOW="\033[01;33m";
    public static String BLUE="\033[01;34m";
    public static String PURPLE="\033[01;35m";
    public static String CYAN="\033[01;36m";
    public static String NORMAL="\033[00m";

    private Log() {}

    public static void e(String TAG, String msg)
    {
        android.util.Log.e(TAG, RED+msg+NORMAL);
    }

    public static void i(String TAG, String msg)
    {
        android.util.Log.i(TAG, BLUE+msg+NORMAL);
    }

    public static void d(String TAG, String msg)
    {
        android.util.Log.d(TAG, YELLOW+msg+NORMAL);
    }

    public static void logException(String TAG, Throwable e)
    {
        Log.e(TAG, RED);
        Log.e(TAG, "---------------------------");
        Log.e(TAG, e.toString());

        StackTraceElement[] arr = e.getStackTrace();
        for (int i=0; i<arr.length; i++)
        {
            Log.e(TAG, "    "+arr[i].toString());
        }
        Log.e(TAG, "---------------------------");
        Log.e(TAG, NORMAL);
    }
}
