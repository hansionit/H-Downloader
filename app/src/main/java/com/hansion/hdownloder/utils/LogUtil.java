/*
 *  Copyright (C)  2016 android@19code.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.hansion.hdownloder.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class LogUtil {

    //TODO 是否在Debug模式   上线前需要更改为false
    public static boolean LOG_DEBUG = true;

    //默认TAG
    public static String TAG = "robosea";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    //Log类型
    private static final int VERBOSE = 2;
    private static final int DEBUG = 3;
    private static final int INFO = 4;
    private static final int WARN = 5;
    private static final int ERROR = 6;
    private static final int ASSERT = 7;
    private static final int JSON = 8;
    private static final int XML = 9;
    private static final int JSON_INDENT = 4;

    public static void v(Object msg) {
        log(VERBOSE, null,  "" + msg);
    }

    public static void d(Object msg) {
        log(DEBUG, null,  "" + msg);
    }

    public static void i(Object... msg) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : msg) {
            sb.append(obj + ",");
        }
        log(INFO, null, String.valueOf(sb));
    }

    public static void w(Object msg) {
        log(WARN, null, "" + msg);
    }

    public static void e(Object msg) {
        log(ERROR, null, "" + msg);
    }


    public static void a(Object msg) {
        log(ASSERT, null,  "" + msg);
    }

    public static void json(String json) {
        log(JSON, null, json);
    }

    public static void json(String tag, String json) {
        log(JSON, tag, json);
    }

    public static void xml(String xml) {
        log(XML, null, xml);
    }

    public static void xml(String tag, String xml) {
        log(XML, tag, xml);
    }

    private static void log(int logType, String tagStr, Object objects) {
        String[] contents = wrapperContent(tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        if (LOG_DEBUG) {
            switch (logType) {
                case VERBOSE:
                case DEBUG:
                case INFO:
                case WARN:
                case ERROR:
                case ASSERT:
                    printDefault(logType, tag, headString + msg);
                    break;
                case JSON:
                    printJson(tag, msg, headString);
                    break;
                case XML:
                    printXml(tag, msg, headString);
                    break;
                default:
                    break;
            }
        }
    }

    private static void printDefault(int type, String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        int index = 0;
        int maxLength = 4000;
        int countOfSub = msg.length() / maxLength;

        if (countOfSub > 0) {  // The log is so long
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + maxLength);
                printSub(type, tag, sub);
                index += maxLength;
            }
            //printSub(type, msg.substring(index, msg.length()));
        } else {
            printSub(type, tag, msg);
        }

    }

    private static void printSub(int type, String tag, String sub) {
        if (tag == null) {
            tag = TAG;
        }
        switch (type) {
            case VERBOSE:
                Log.v(tag, sub);
                break;
            case DEBUG:
                Log.d(tag, sub);
                break;
            case INFO:
                Log.i(tag, sub);
                break;
            case WARN:
                Log.w(tag, sub);
                break;
            case ERROR:
                Log.e(tag, sub);
                break;
            case ASSERT:
                Log.wtf(tag, sub);
                break;
        }
    }

    private static void printJson(String tag, String json, String headString) {
        if (TextUtils.isEmpty(json)) {
            d("Empty/Null json content");
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        String message;

        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                message = jsonObject.toString(JSON_INDENT);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = json;
            }
        } catch (JSONException e) {
            message = json;
        }

        printLine(tag, true);
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "|" + line);
        }
        printLine(tag, false);
    }

    private static void printXml(String tag, String xml, String headString) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (xml != null) {
            try {
                Source xmlInput = new StreamSource(new StringReader(xml));
                StreamResult xmlOutput = new StreamResult(new StringWriter());
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(xmlInput, xmlOutput);
                xml = xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            xml = headString + "\n" + xml;
        } else {
            xml = headString + "Log with null object";
        }

        printLine(tag, true);
        String[] lines = xml.split(LINE_SEPARATOR);
        for (String line : lines) {
            if (!TextUtils.isEmpty(line)) {
                Log.d(tag, "|" + line);
            }
        }
        printLine(tag, false);
    }

    private static String[] wrapperContent(String tag, Object... objects) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[5];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + ".java";
        }
        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String msg = (objects == null) ? "Log with null object" : getObjectsString(objects);
        String headString = "[(" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {

        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append("null").append("\n");
                } else {
                    stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? "null" : object.toString();
        }
    }

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static void crash2File(Context context, String msg) {
        File file = new File(context.getExternalFilesDir("crash_" + getDate() + ".log").getAbsolutePath());
        FileOutputStream trace = null;
        try {
            trace = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(trace, "utf-8");
            writer.write("#_" + getDate() + "\n");
            writer.write(msg + "\n");
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDate() {
        return new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
    }
}
