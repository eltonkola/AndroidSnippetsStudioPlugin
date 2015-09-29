package com.eltonkola.androidsnippets.settings;
import com.eltonkola.androidsnippets.ASUtils;
import com.intellij.ide.util.PropertiesComponent;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class SearchSettings {

    private static SearchSettings instance = null;
    protected SearchSettings() {
        load();
    }
    public static SearchSettings getInstance() {
        if(instance == null) {
            instance = new SearchSettings();
        }
        return instance;
    }

    private SettingsObject mSettingsObject;


    public int getLimit() {
        if (this.mSettingsObject == null) {
            return ASUtils.LIMIT_CHARS;
        }
        return this.mSettingsObject.getCharLimit();
    }

    public String getServerUrl() {
        if (this.mSettingsObject == null) {
            return ASUtils.SERVER_URL;
        }
        return this.mSettingsObject.getServerUrl();
    }

    public boolean getCacheEnabled() {
        if (this.mSettingsObject == null) {
            return ASUtils.USE_CACHE;
        }
        return this.mSettingsObject.isEnableCache();
    }

    private void load(){
        if (this.mSettingsObject == null) {
            this.mSettingsObject = new SettingsObject();
            this.mSettingsObject.setServerUrl(PropertiesComponent.getInstance().getValue("AS_SERVER_URL", ASUtils.SERVER_URL));
            this.mSettingsObject.setCharLimit(PropertiesComponent.getInstance().getOrInitInt("AS_LIMIT", ASUtils.LIMIT_CHARS));
            this.mSettingsObject.setEnableCache(PropertiesComponent.getInstance().getBoolean("AS_CACHE", ASUtils.USE_CACHE));
        }
    }

    public void saveSate(String url, int chars, boolean cache) {
        if (this.mSettingsObject == null) {
            this.mSettingsObject = new SettingsObject();
        }
        this.mSettingsObject.setServerUrl(url);
        this.mSettingsObject.setCharLimit(chars);
        this.mSettingsObject.setEnableCache(cache);

        PropertiesComponent.getInstance().setValue("AS_SERVER_URL", url);
        PropertiesComponent.getInstance().setValue("AS_LIMIT", chars + "");
        PropertiesComponent.getInstance().setValue("AS_CACHE", cache + "");

    }

    class SettingsObject {

        private String mServerUrl;
        public int mCharLimit;
        private boolean mEnableCache;


        public SettingsObject() {
        }

        public String getServerUrl() {
            return mServerUrl;
        }

        public void setServerUrl(String mServerUrl) {
            this.mServerUrl = mServerUrl;
        }

        public int getCharLimit() {
            return mCharLimit;
        }

        public void setCharLimit(int mCharLimit) {
            this.mCharLimit = mCharLimit;
        }

        public boolean isEnableCache() {
            return mEnableCache;
        }

        public void setEnableCache(boolean mEnableCache) {
            this.mEnableCache = mEnableCache;
        }
    }

}

