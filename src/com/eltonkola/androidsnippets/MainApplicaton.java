package com.eltonkola.androidsnippets;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class MainApplicaton implements ApplicationComponent {

    private static int INTERNET_CHECK_TIMEOUT = 10;
    private static int INTERNET_CHECK_TIMER = 5 * 60 * 1000; // cdo 5 minuta

    class CheckConnections extends TimerTask{
        public void run() {
            ASUtils.log("Check internet connection! mConnected:" + mConnected);
            mConnected = ASUtils.ping("http://google.com", INTERNET_CHECK_TIMEOUT * 1000);
            ASUtils.log("mConnected:" + mConnected);
        }
    }

    private static boolean mConnected =  true;
    private Timer mTimer;

    public void initComponent() {
        mTimer= new Timer();
        mTimer.schedule(new CheckConnections(), 0, INTERNET_CHECK_TIMER);
    }

    public static boolean hasInternet() {
        return mConnected;
    }

    public void disposeComponent() {
        mTimer.cancel();
        mTimer = null;
    }

    @NotNull
    public String getComponentName() {
        return "AndroidSnippets";
    }

}

