package com.eltonkola.androidsnippets.settings;

import com.eltonkola.androidsnippets.ASUtils;
import com.eltonkola.androidsnippets.model.SearchCache;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class Preferencat implements Configurable {

    private JPanel panelPrfContainer;

    private JTextField serverUrl;
    private JCheckBox useCacheCheckBox;
    private JComboBox comboBoxChars;

    private JButton buttonDeleteCache;

    private JButton saveButton;
    private JButton resetButton;
    private JButton helpButton;

    private final SearchSettings myPersistence;

    public Preferencat() {
        ASUtils.log("Settings - Preferencat constructor");
        this.myPersistence = SearchSettings.getInstance();
    }


    @Nls
    @Override
    public String getDisplayName() {
        ASUtils.log("Settings - getDisplayName");
        return "Android Snippet Search";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        ASUtils.log("Settings - getHelpTopic");
        return "Your snippet search preferences";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        ASUtils.log("Settings - createComponent");
        this.resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reseto();

            }
        });

        this.helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("http://eltonkola.com/androidsnippets/android-studio-plugin/");
            }
        });

        this.buttonDeleteCache.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCache();
            }
        });


        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    apply();
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });


        Integer[] vals = new Integer[]{0,1,2,3,4,5};


        this.comboBoxChars.setModel(new DefaultComboBoxModel<Integer>((Integer[]) vals));

        this.serverUrl.setText(myPersistence.getServerUrl());
        this.useCacheCheckBox.setSelected(myPersistence.getCacheEnabled());
        this.comboBoxChars.setSelectedItem((Object) this.myPersistence.getLimit());


        return panelPrfContainer;
    }

    @Override
    public boolean isModified() {
        ASUtils.log("Settings - isModified");
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        ASUtils.log("Settings - apply");
        myPersistence.saveSate(this.serverUrl.getText(), this.comboBoxChars.getSelectedIndex(), this.useCacheCheckBox.isSelected());

    }

    @Override
    public void reset() {
        ASUtils.log("Settings - reset");

        LafManager lafManager = LafManager.getInstance();

        this.serverUrl.setText(myPersistence.getServerUrl());
        this.useCacheCheckBox.setSelected(myPersistence.getCacheEnabled());
        this.comboBoxChars.setSelectedItem((Object) this.myPersistence.getLimit());

        lafManager.updateUI();

    }

    public void reseto() {
        ASUtils.log("Settings - reset");

        LafManager lafManager = LafManager.getInstance();

        myPersistence.saveSate(ASUtils.SERVER_URL, ASUtils.LIMIT_CHARS, ASUtils.USE_CACHE);

        this.serverUrl.setText(myPersistence.getServerUrl());
        this.useCacheCheckBox.setSelected(myPersistence.getCacheEnabled());
        this.comboBoxChars.setSelectedItem((Object) this.myPersistence.getLimit());

        lafManager.updateUI();

    }


    @Override
    public void disposeUIResources() {
        ASUtils.log("Settings - disposeUIResources");

    }

    private void deleteCache(){
        ASUtils.log("deleteCache");
        SearchCache.getInstance().cleanCache();
    }

}
