package com.eltonkola.androidsnippets.model;

import java.lang.reflect.Field;
import java.util.Vector;
import javax.swing.DefaultListModel;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class SearchListModel extends DefaultListModel {

    private Vector myDelegate;

    public SearchListModel() {
        try {
            Field field = DefaultListModel.class.getDeclaredField("delegate");
            field.setAccessible(true);
            this.myDelegate = (Vector)field.get(this);
        }catch (Exception ignore) {

        }
    }

    public void addElement(Object obj) {
        this.myDelegate.add(obj);
    }

    public void update() {
        this.fireContentsChanged(this, 0, this.getSize() - 1);
    }
}

