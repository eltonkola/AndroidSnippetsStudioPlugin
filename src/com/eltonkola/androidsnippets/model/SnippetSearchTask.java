package com.eltonkola.androidsnippets.model;

import com.eltonkola.androidsnippets.ASUtils;
import com.eltonkola.androidsnippets.settings.SearchSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class SnippetSearchTask extends SwingWorker<Void, Void> {


    public interface SearchEvents{
        public void onError();
        public void onNoResults();
        public void onResults(ArrayList<CodeSnippet> answers);
    }

    private String question;
    private SearchEvents mSearchEvents;

    private ArrayList<CodeSnippet> answers = new ArrayList<CodeSnippet>();

    public SnippetSearchTask(String question, SearchEvents searchEvents) {
        this.question = question;
        this.mSearchEvents= searchEvents;
        ASUtils.log("task new SearchWorkerTask:" + question);
    }

    private boolean error = false;

    @Override
    protected Void doInBackground() throws Exception {
        ASUtils.log("task doInBackground");

        if(SearchSettings.getInstance().getCacheEnabled()){
            ASUtils.log("perdor cache");
            //shiko a kam gje ne cache
            ArrayList<CodeSnippet> ngaKesh =  SearchCache.getInstance().fromCache(question);
            ASUtils.log("ne kesh kam:" + ngaKesh);
            if(ngaKesh != null){
                answers =  ngaKesh;
                ASUtils.log(">> mbarova, perdora cache");
            }else{
                ASUtils.log(">> cache ska gje, kerkoj online");
                searchOnline();
            }
        }else {
            ASUtils.log("mos perdor cache");
            searchOnline();
        }
        return null;
    }

    @Override
    protected void done() {
        ASUtils.log("task done");
        if(error){
            mSearchEvents.onError();
        }else{
            if(answers.size()> 0){
                mSearchEvents.onResults(answers);
            }else{
                mSearchEvents.onNoResults();
            }
        }
    }



    ////////// online search //////

    private void searchOnline(){
        error = false;
        final HttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpGet = new HttpGet( SearchSettings.getInstance().getServerUrl() + "wp-json/posts?filter[s]=" + question);

        ASUtils.log("httpGet:" + httpGet);
        try {

            //Execute and return the response
            final HttpEntity entity = httpclient.execute(httpGet).getEntity();

            if (entity != null) {
                    InputStream instream = entity.getContent();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    String line;
                    final StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    JsonParser parser = new JsonParser();
                    JsonArray json = (JsonArray) parser.parse(builder.toString());

                    for (int i = 0; i < json.size(); i++) {
                        JsonObject result = (JsonObject) json.get(i);
                        CodeSnippet res= new CodeSnippet();
                        res.setTitle( result.get("title").getAsString());
                        res.setDescription(ASUtils.pastroKodin(result.get("excerpt").getAsString()));
                        res.setLink(result.get("link").getAsString());

                        answers.add(res);
                    }

                SearchCache.getInstance().put(question, answers);
            }
        } catch (IOException e) {
            ASUtils.log("Failed to get answer for question " + question);
            ASUtils.log(e.getMessage());
            e.printStackTrace();
            error = true;
        }
    }

}
