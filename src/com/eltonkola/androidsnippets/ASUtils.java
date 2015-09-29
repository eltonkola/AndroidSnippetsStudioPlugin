package com.eltonkola.androidsnippets;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.Gradient;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class ASUtils {

    //preferencat default
    public static final int LIMIT_CHARS = 2;
    public static final String SERVER_URL = "http://eltonkola.com/androidsnippets/";
    public static final boolean USE_CACHE = true;

    public static void log(String l){
//        System.out.println(">>>>>:" + l);
    }

    @NotNull
    public static Gradient getGradientColors() {
        Gradient gradient = new Gradient((Color)new JBColor(new Color(101, 147, 242), new Color(64, 80, 94)), (Color)new JBColor(new Color(46, 111, 205), new Color(53, 65, 87)));
        return gradient;
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     * @param url The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     * the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     */
    public static boolean ping(String url, int timeout) {
        // Otherwise an exception may be thrown on invalid SSL certificates:
        url = url.replaceFirst("^https", "http");

        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }

    public static String pastroKodin(String codeToadd){
        codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "<p>", "");
        codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "</p>", "");
        codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "<br />", "");
        codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "&#8220;", "\"");
        codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "&#8221;", "\"");
        return codeToadd;

    }

}


