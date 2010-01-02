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

import java.util.regex.*;
import java.util.*;

public class Twit
{
    public int id;
    public String text;
    public String timestamp;
    public String username;
    public String screenname;

    private static final Pattern timeStampPattern = 
        Pattern.compile("[a-zA-Z]+\\s+([a-zA-Z]+)\\s+(\\d+)\\s+"+
            "(\\d+):(\\d+):(\\d+)\\s+\\+\\d+\\s+(\\d+)");
    private static final Pattern scrnamePattern =
        Pattern.compile("@(\\S+)");
    public Twit() {}

    public Twit(String text, String timestamp, String username, String scrname)
    {
        this.text = text;
        this.timestamp = timestamp;
        this.username = username;
        this.screenname = scrname;
    }

    public String getSpeech()
    {
        // eat URLs
        Matcher m = G.urlPattern.matcher(text);
        String speech = m.replaceAll("");

        // replace @screenname by screenname
        m = scrnamePattern.matcher(speech);
        if(m != null && m.find()){
            String name = m.group(1); 
            speech = m.replaceAll(name);
        }

        return username+" says: "+speech;
    }


    public static String sanitizeTimestamp(String rawTS)
    {
        Matcher m = timeStampPattern.matcher(rawTS);
        if(m != null && m.find() && m.groupCount() == 6) {
            int[] t = new int[] {
                Integer.parseInt(m.group(6)),          // year
                getMonthMap().get(m.group(1)).intValue(), // month
                Integer.parseInt(m.group(2)),           // day
                Integer.parseInt(m.group(3)),           // hour
                Integer.parseInt(m.group(4)),           // minute
                Integer.parseInt(m.group(5))            // second
            };
            return new Formatter().format(
                "%4d%2d%2d%2d%2d%2d",t[0],t[1],t[2],t[3],t[4],t[5]).toString();
        }
        return "";
    }

    private static HashMap<String,Integer> monthMap = null;
    private static HashMap<String,Integer> getMonthMap()
    {
        if(monthMap == null)
        {
            monthMap = new HashMap<String,Integer>(12);
            monthMap.put("Jan",new Integer(1));
            monthMap.put("Feb",new Integer(2));
            monthMap.put("Mar",new Integer(3));
            monthMap.put("Apr",new Integer(4));
            monthMap.put("May",new Integer(5));
            monthMap.put("Jun",new Integer(6));
            monthMap.put("Jul",new Integer(7));
            monthMap.put("Aug",new Integer(8));
            monthMap.put("Sep",new Integer(9));
            monthMap.put("Oct",new Integer(10));
            monthMap.put("Nov",new Integer(11));
            monthMap.put("Dec",new Integer(12));
        }

        return monthMap;
    }
}
