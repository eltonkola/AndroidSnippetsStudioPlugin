package com.eltonkola.androidsnippets.model;
import com.eltonkola.androidsnippets.ASUtils;
import com.intellij.ide.util.PropertiesComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class SearchCache {

    private static SearchCache instance = null;
    protected SearchCache() {
        cache = new ConcurrentHashMap<String, ArrayList<CodeSnippet>>();
    }
    public static SearchCache getInstance() {
        if(instance == null) {
            instance = new SearchCache();
        }
        return instance;
    }

    private ConcurrentMap<String, ArrayList<CodeSnippet>> cache;

    protected ArrayList<CodeSnippet> fromCache(String question){

        Iterator entries = cache.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, ArrayList<CodeSnippet>> thisEntry = (Map.Entry<String, ArrayList<CodeSnippet>>) entries.next();
            String key = thisEntry.getKey();
            ASUtils.log(">>>>>>>>iterate: " + thisEntry + " - " + key);
            if(key != null && key.equalsIgnoreCase(question)) {
                return thisEntry.getValue();
            }
        }
        return null;
    }

    public void put(String question, ArrayList<CodeSnippet> answers) {
        cache.put(question, answers);
    }

    public void cleanCache(){
        cache.clear();
    }
}

