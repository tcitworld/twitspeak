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

public class AsocialException extends Exception
{
    public static final int EMPTY_RESP = -3;
    public static final int JSONE = -2;
    public static final int IOE = -1;

    public int errcode = 0;
    public String msg = null;

    public AsocialException(String msg)
    {
        super(msg);
        this.msg = msg;
    }

    public AsocialException(int errcode, String msg)
    {
        super(msg);
        this.errcode = errcode;
        this.msg = msg;
    }

    public String toString()
    {
        if(msg != null) return msg+"("+errcode+")";
        return super.toString();
    }
}
