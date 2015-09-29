package com.eltonkola.androidsnippets.model;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class CodeSnippet {

    private String title;
    private String link;
    private String description;

    private final boolean mFake;

    public CodeSnippet() {
        mFake = false;
    }

    public CodeSnippet(final boolean fake) {
        mFake = fake;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isFake() {
        return mFake;
    }

}
