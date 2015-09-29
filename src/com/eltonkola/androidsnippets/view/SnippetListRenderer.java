package com.eltonkola.androidsnippets.view;

import com.intellij.ui.ColoredListCellRenderer;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Elton Kola on 9/10/15.
 */
public abstract class SnippetListRenderer extends ColoredListCellRenderer {
    @Nullable
    public String myLocationString;
    @Nullable
    public Icon myLocationIcon;
    public final JPanel myMainPanel = new JPanel(new BorderLayout());
    public final JLabel myTitle = new JLabel();

    public abstract void recalculateWidth();
}

