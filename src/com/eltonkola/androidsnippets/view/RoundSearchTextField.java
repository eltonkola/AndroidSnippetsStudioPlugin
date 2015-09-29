package com.eltonkola.androidsnippets.view;

import com.intellij.ide.ui.laf.darcula.ui.DarculaTextBorder;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTextFieldUI;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.Gray;
import com.intellij.ui.SearchTextField;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.TextUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class RoundSearchTextField extends SearchTextField implements DataProvider, Disposable {
    public RoundSearchTextField() {
        super(false);
        this.getTextEditor().setOpaque(false);
        this.getTextEditor().setUI((TextUI)((DarculaTextFieldUI)DarculaTextFieldUI.createUI((JComponent)this.getTextEditor())));
        this.getTextEditor().setBorder((Border)new DarculaTextBorder());
        this.getTextEditor().putClientProperty("JTextField.Search.noBorderRing", Boolean.TRUE);
        if (UIUtil.isUnderDarcula()) {
            this.getTextEditor().setBackground((Color)Gray._45);
            this.getTextEditor().setForeground((Color)Gray._240);
        }
    }

    public void setText(String aText) {
        this.getTextEditor().setText(aText);
    }

    protected boolean isSearchControlUISupported() {
        return true;
    }

    protected boolean hasIconsOutsideOfTextField() {
        return false;
    }

    protected void showPopup() {
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
        if (PlatformDataKeys.PREDEFINED_TEXT.is(dataId)) {
            return this.getTextEditor().getText();
        }
        return null;
    }

    public void dispose() {
    }
}

