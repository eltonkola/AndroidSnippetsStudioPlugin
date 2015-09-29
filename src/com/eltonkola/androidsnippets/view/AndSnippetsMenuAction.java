package com.eltonkola.androidsnippets.view;

import com.eltonkola.androidsnippets.MainApplicaton;
import com.eltonkola.androidsnippets.model.SearchListModel;
import com.eltonkola.androidsnippets.model.CodeSnippet;
import com.eltonkola.androidsnippets.model.SnippetSearchTask;
import com.eltonkola.androidsnippets.ASUtils;
import com.eltonkola.androidsnippets.settings.SearchSettings;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.search.BooleanOptionDescription;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.OnOffButton;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.ui.popup.PopupPositionManager;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by Elton Kola on 9/10/15.
 */
public class AndSnippetsMenuAction extends AnAction {

    private RoundSearchTextField searchField;
    private volatile JBPopup myBalloon;
    private JBPopup popupWindow;
    private JBList myList;
    private SnippetListRenderer myRenderer;
    private int myPopupActualWidth;
    private Editor ta;
    private AnActionEvent myActionEvent;
    private Project project_poshte;
    private Editor editor_poshte;
    private Document document;
    private EditorFactory mEditorFactory;

    public void actionPerformed(AnActionEvent e) {
        internalActionPerformed(e, null, null);
    }

    void internalActionPerformed(@Nullable AnActionEvent e, @Nullable MouseEvent me, @Nullable String suggestion) {

        project_poshte = e.getData(CommonDataKeys.PROJECT);
        editor_poshte = e.getData(CommonDataKeys.EDITOR);

        this.myActionEvent = e;

        searchField = new RoundSearchTextField();

        mEditorFactory = EditorFactory.getInstance();
        this.document = mEditorFactory.createDocument((CharSequence)"");
        this.ta = mEditorFactory.createEditor(this.document, this.project_poshte, FileTypeManager.getInstance().getFileTypeByExtension("java"), true);

        final JTextField editor = searchField.getTextEditor();
        editor.getDocument().addDocumentListener((DocumentListener)new DocumentAdapter(){

            protected void textChanged(DocumentEvent e) {
                String pattern = editor.getText();
                if (editor.hasFocus()) {
                    AndSnippetsMenuAction.this.rebuildList(pattern);
                }
            }
        });
        editor.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(@NotNull FocusEvent e) {
                editor.setColumns(25);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JComponent parent = (JComponent) editor.getParent();
                        parent.revalidate();
                        parent.repaint();
                    }
                });
            }

            @Override
            public void focusLost(@NotNull FocusEvent e) {
                if (AndSnippetsMenuAction.this.popupWindow instanceof AbstractPopup && AndSnippetsMenuAction.this.popupWindow.isVisible() && (AndSnippetsMenuAction.this.myList == e.getOppositeComponent() || ((AbstractPopup) AndSnippetsMenuAction.this.popupWindow).getPopupWindow() == e.getOppositeComponent())) {
                    return;
                }
            }
        });

        this.searchField.setOpaque(false);
        editor.setColumns(25);
        updateListComponent();

        JPanel panel = new JPanel(new BorderLayout()){

            @Override
            protected void paintComponent(@NotNull Graphics g) {
                Gradient gradient = ASUtils.getGradientColors();
                ((Graphics2D)g).setPaint(new GradientPaint(0.0f, 0.0f, gradient.getStartColor(), 0.0f, this.getHeight(), gradient.getEndColor()));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }

            @NotNull
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension = new Dimension(780, super.getPreferredSize().height);
                return dimension;
            }
        };

        JLabel title = new JLabel("Android Snippet Search:");
        NonOpaquePanel topPanel = new NonOpaquePanel((LayoutManager)new BorderLayout());
        title.setForeground((Color)new JBColor((Color) Gray._240, (Color)Gray._200));
        if (SystemInfo.isMac) {
            title.setFont(title.getFont().deriveFont(1, (float)title.getFont().getSize() - 1.0f));
        } else {
            title.setFont(title.getFont().deriveFont(1));
        }
        topPanel.add((Component)title, "West");
        panel.add((Component)this.searchField, "Center");
        panel.add((Component)topPanel, "North");
        panel.setBorder(IdeBorderFactory.createEmptyBorder((int) 3, (int) 5, (int) 4, (int) 5));
        ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder((JComponent)panel, (JComponent)editor);
        this.myBalloon = builder.setCancelOnClickOutside(true).setModalContext(false).setRequestFocus(true).createPopup();
        assert (this.myBalloon != null);
        this.myBalloon.getContent().setBorder(new EmptyBorder(0, 0, 0, 0));
        Window window2 = WindowManager.getInstance().suggestParentWindow(e.getProject());
        Component parent = UIUtil.findUltimateParent((Component)window2);
        RelativePoint showPoint;
        if (me != null) {
            Component label = me.getComponent();
            Container button = label.getParent();
            assert (button != null);
            showPoint = new RelativePoint((Component)button, new Point(button.getWidth() - panel.getPreferredSize().width, button.getHeight()));
        } else if (parent != null) {
            int height;
            int n = height = UISettings.getInstance().SHOW_MAIN_TOOLBAR ? 135 : 115;
            if (parent instanceof IdeFrameImpl && ((IdeFrameImpl)parent).isInFullScreen()) {
                height-=20;
            }
            showPoint = new RelativePoint(parent, new Point((parent.getSize().width - panel.getPreferredSize().width) / 2, height));
        } else {
            showPoint = JBPopupFactory.getInstance().guessBestPopupLocation(e.getDataContext());
        }
        this.myList.setFont(UIUtil.getListFont());
        assert (this.myBalloon != null);
        this.myBalloon.show(showPoint);
        this.initSearchActions(this.myBalloon, this.searchField);


        IdeFocusManager focusManager = IdeFocusManager.getInstance((Project)e.getProject());
        focusManager.requestFocus((Component) editor, true);
        FeatureUsageTracker.getInstance().triggerFeatureUsed("SearchEverywhere");

    }


    private SnippetSearchTask mASSearchTask;
    private SearchListModel myListModel;

    private void rebuildList(String pattern) {

        if(MainApplicaton.hasInternet()) {
            if(pattern.length()>  SearchSettings.getInstance().getLimit()){
                renderSingleLine("Searching..");

                if(mASSearchTask != null){
                    mASSearchTask.cancel(true);
                }

                mASSearchTask = new SnippetSearchTask(pattern, new SnippetSearchTask.SearchEvents() {
                    @Override
                    public void onError() {
                        renderSingleLine("Error getting results..");
                    }

                    @Override
                    public void onNoResults() {
                        renderSingleLine("Nothing found..");
                    }

                    @Override
                    public void onResults(ArrayList<CodeSnippet> answers) {
                        renderList(answers);
                    }
                });
                mASSearchTask.execute();

            }else{
                renderSingleLine("Write more than "+ SearchSettings.getInstance().getLimit() +" chars..");
            }
        } else {
            renderSingleLine("No internet..");
        }
    }

    private void renderList(ArrayList<CodeSnippet> answers){
        myListModel = new SearchListModel();
        for (int i = 0; i < answers.size(); i++) {
            CodeSnippet res = answers.get(i);
            this.myListModel.addElement(res);
        }
        this.myList.setModel(myListModel);
        updatePopup();
    }

    private void showCurrentPreview() {
        int index = AndSnippetsMenuAction.this.myList.getSelectedIndex();
        if (index != -1) {
            final CodeSnippet el = (CodeSnippet) myList.getModel().getElementAt(index);
            if (!el.isFake()) {
                ASUtils.log("element to show details for is:" + el);
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        document.setText(el.getDescription());
                    }
                });
                //ta.setText(el.element.getDescription());
                //ta.invalidate();
            }
        }
    }

    private boolean canShowPreview(){
        int index = AndSnippetsMenuAction.this.myList.getSelectedIndex();
        if (index != -1) {
            CodeSnippet el = (CodeSnippet) myList.getModel().getElementAt(index);
            if (!el.isFake()){
                return true;
            }
        }
        return false;
    }


    private void renderSingleLine(String what){
        myListModel = new SearchListModel();
        CodeSnippet el = new CodeSnippet(true);
        el.setTitle(what);
        this.myListModel.addElement(el);
        this.myList.setModel(myListModel);
        updatePopup();
        updatePopupBounds();
    }

    SearchTextField getField() {
        return this.searchField;
    }


    protected void updatePopup() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                AndSnippetsMenuAction.this.myListModel.update();
                AndSnippetsMenuAction.this.myList.revalidate();
                AndSnippetsMenuAction.this.myList.repaint();
                AndSnippetsMenuAction.this.myRenderer.recalculateWidth();
                if (AndSnippetsMenuAction.this.myBalloon == null || AndSnippetsMenuAction.this.myBalloon.isDisposed()) {
                    return;
                }
                if (!(AndSnippetsMenuAction.this.popupWindow != null && AndSnippetsMenuAction.this.popupWindow.isVisible())) {



                    JLabel titleLabel1 = new JLabel("Android Code Snippet Search");
                    titleLabel1.setFont(AndSnippetsMenuAction.getTitleFont());
                    titleLabel1.setForeground(UIUtil.getLabelDisabledForeground());


                    Color bg = UIUtil.getListBackground();

                    SeparatorComponent separatorComponent = new SeparatorComponent(titleLabel1.getPreferredSize().height / 2, (Color)new JBColor((Color)Gray._220, (Color)Gray._80), null);
                    JPanel panelTitulli = new JPanel(new BorderLayout());
                    panelTitulli.add((Component) titleLabel1, "West");
                    panelTitulli.add((Component) separatorComponent, "Center");
                    panelTitulli.setBackground(bg);


                    JLabel titleLabel2 = new JLabel("Ctrl + Enter to open on browser");
                    titleLabel2.setFont(AndSnippetsMenuAction.getTitleFont());
                    titleLabel2.setForeground(UIUtil.getLabelDisabledForeground());

                    SeparatorComponent separatorComponentPoshte = new SeparatorComponent(titleLabel2.getPreferredSize().height / 2, (Color)new JBColor((Color)Gray._220, (Color)Gray._80), null);
                    JPanel panelTitulliPoshte = new JPanel(new BorderLayout());
                    panelTitulliPoshte.add((Component) titleLabel2, BorderLayout.WEST);
                    panelTitulliPoshte.add((Component) separatorComponentPoshte, BorderLayout.CENTER);
                    panelTitulliPoshte.setBackground(bg);


                    JPanel pane = new JPanel(new BorderLayout());

                    JBScrollPane listContainer = new JBScrollPane(AndSnippetsMenuAction.this.myList);
                    JComponent editorPreviewContainer = AndSnippetsMenuAction.this.ta.getComponent();

                    JBSplitter splitPane = new JBSplitter(false, 1.0f);

                    splitPane.setFirstComponent((JComponent) listContainer);
                    splitPane.setSecondComponent((JComponent) editorPreviewContainer);

                    splitPane.setProportion(0.4f);

                    pane.add(panelTitulli, BorderLayout.PAGE_START);
                    pane.add(splitPane, BorderLayout.CENTER);
                    pane.add(panelTitulliPoshte, BorderLayout.PAGE_END);


                    final ActionCallback callback = ListDelegationUtil.installKeyboardDelegation((JComponent) AndSnippetsMenuAction.this.getField().getTextEditor(), (JList) AndSnippetsMenuAction.this.myList);
                    ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder((JComponent)pane, null);
                    AndSnippetsMenuAction.this.popupWindow = builder.setRequestFocus(false).setCancelKeyEnabled(false).setCancelCallback((Computable) new Computable<Boolean>() {

                        @NotNull
                        public Boolean compute() {
                            Boolean bl = AndSnippetsMenuAction.this.myBalloon == null || AndSnippetsMenuAction.this.myBalloon.isDisposed() || !AndSnippetsMenuAction.this.getField().getTextEditor().hasFocus();
                            return bl;
                        }
                    }).createPopup();
                    AndSnippetsMenuAction.this.popupWindow.getContent().setBorder(new EmptyBorder(0, 0, 0, 0));
                    Disposer.register((Disposable) AndSnippetsMenuAction.this.popupWindow, (Disposable) new Disposable() {

                        public void dispose() {
                            JLabel label;
                            Component component;
                            callback.setDone();
//                            AndSnippetsMenuAction.this.resetFields();
                            ActionToolbarImpl.updateAllToolbarsImmediately();
                            if (AndSnippetsMenuAction.this.myActionEvent != null && AndSnippetsMenuAction.this.myActionEvent.getInputEvent() instanceof MouseEvent && (component = AndSnippetsMenuAction.this.myActionEvent.getInputEvent().getComponent()) != null && (label = (JLabel) UIUtil.getParentOfType((Class) JLabel.class, (Component) component)) != null) {
                                label.setIcon(AllIcons.Actions.FindPlain);
                            }
                           mbylle();
                        }
                    });
                    AndSnippetsMenuAction.this.popupWindow.show(new RelativePoint((Component) AndSnippetsMenuAction.this.getField().getParent(), new Point(0, AndSnippetsMenuAction.this.getField().getParent().getHeight())));
                    AndSnippetsMenuAction.this.updatePopupBounds();
                    ActionManager.getInstance().addAnActionListener((AnActionListener) new AnActionListener.Adapter() {

                        public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                            if (action instanceof TextComponentEditorAction) {
                                return;
                            }
                            AndSnippetsMenuAction.this.popupWindow.cancel();
                        }
                    }, (Disposable) AndSnippetsMenuAction.this.popupWindow);
                } else {
                    AndSnippetsMenuAction.this.myList.revalidate();
                    AndSnippetsMenuAction.this.myList.repaint();
                }
                ListScrollingUtil.ensureSelectionExists((JList) AndSnippetsMenuAction.this.myList);
                if (AndSnippetsMenuAction.this.myList.getModel().getSize() > 0) {
                    AndSnippetsMenuAction.this.updatePopupBounds();
                }
            }

        });
    }

    private void updatePopupBounds() {

        if (!(this.popupWindow != null && this.popupWindow.isVisible())) {
            return;
        }
        Container parent = this.getField().getParent();
        Dimension size = popupWindow.getSize();
        size.width = parent.getWidth();
        size.height = 400;
        popupWindow.setSize(size);


        Dimension sizeLi = this.myList.getPreferredSize();
        sizeLi.width =  (int)(size.width * 0.4);
        this.myList.setSize(sizeLi);


        Dimension sizeTa = this.ta.getComponent().getPreferredSize();
        sizeTa.width =  (int)(size.width * 0.6);
        this.ta.getComponent().setSize(sizeTa);

    }

    private void adjustPopup() {
        Dimension d = PopupPositionManager.PositionAdjuster.getPopupSize((JBPopup) this.popupWindow);
        JComponent myRelativeTo = this.myBalloon.getContent();
        Point myRelativeOnScreen = myRelativeTo.getLocationOnScreen();
        Rectangle screen = ScreenUtil.getScreenRectangle((Point) myRelativeOnScreen);
        Rectangle popupRect = null;
        Rectangle r = new Rectangle(myRelativeOnScreen.x, myRelativeOnScreen.y + myRelativeTo.getHeight(), d.width, d.height);
        if (screen.contains(r)) {
            popupRect = r;
        }
        if (popupRect != null) {
            this.popupWindow.setLocation(new Point(r.x, r.y));
        } else {
            if (r.y + d.height > screen.y + screen.height) {
                r.height = screen.y + screen.height - r.y - 2;
            }
            if (r.width > screen.width) {
                r.width = screen.width - 50;
            }
            if (r.x + r.width > screen.x + screen.width) {
                r.x = screen.x + screen.width - r.width - 2;
            }
            this.popupWindow.setSize(r.getSize());
            this.popupWindow.setLocation(r.getLocation());
        }
    }

    void initSearchActions(@NotNull JBPopup balloon, @NotNull RoundSearchTextField searchTextField) {
        final JTextField editor = searchTextField.getTextEditor();

        //kur shtyping esc, e mbyllim
        new DumbAwareAction(){
            public void actionPerformed(@NotNull AnActionEvent e) {
                mbylle();
            }
        }.registerCustomShortcutSet((ShortcutSet) CustomShortcutSet.fromString((String[]) new String[]{"ESCAPE"}), (JComponent) editor, (Disposable) balloon);

        new DumbAwareAction(){
            public void actionPerformed(@NotNull AnActionEvent e) {
                int index = AndSnippetsMenuAction.this.myList.getSelectedIndex();
                if (index != -1) {
                    AndSnippetsMenuAction.this.handleListSelection(index);
                }
            }
        }.registerCustomShortcutSet((ShortcutSet) CustomShortcutSet.fromString((String[]) new String[]{"ENTER"}), (JComponent) editor, (Disposable) balloon);

        new DumbAwareAction(){
            public void actionPerformed(@NotNull AnActionEvent e) {
                int index = AndSnippetsMenuAction.this.myList.getSelectedIndex();
                if (index != -1) {
                    AndSnippetsMenuAction.this.handleListSelectionWeb(index);;
                }
            }
        }.registerCustomShortcutSet((ShortcutSet)  CustomShortcutSet.fromString((String[]) new String[]{"ctrl ENTER"}), (JComponent) editor, (Disposable) balloon);


    }


    protected SnippetListRenderer createListRenderer() {
        MyListRenderer myListRenderer = new MyListRenderer();
        return myListRenderer;
    }
    public void updateListComponent() {
        this.myRenderer = this.createListRenderer();
        this.myList = new JBList(){

            @NotNull
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                Dimension dimension = new Dimension(Math.min(size.width - 2, 800), size.height);
                return dimension;
            }
        };
        this.myList.setCellRenderer((ListCellRenderer) this.myRenderer);
        this.myList.addMouseListener((MouseListener) new MouseAdapter() {
            @Override
            public void mouseClicked(@NotNull MouseEvent e) {
                if(e.getClickCount() == 2) {
                    e.consume();
                    int i = AndSnippetsMenuAction.this.myList.locationToIndex(e.getPoint());
                    AndSnippetsMenuAction.this.handleListSelection(i);
                }
                super.mouseClicked(e);
            }
        });



        this.myList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showCurrentPreview();
            }
        });
        this.myList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                ASUtils.log("event key:" + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JList list = (JList) e.getSource();
                    int index = list.getSelectedIndex();
                    AndSnippetsMenuAction.this.handleListSelection(index);
                }else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    showCurrentPreview();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

    }

    private void handleListSelectionWeb(int position){
        CodeSnippet element = (CodeSnippet)myList.getModel().getElementAt(position);
        if(!element.isFake()) {
            ASUtils.log("----------> open browser:" + element.getLink());
            BrowserUtil.browse(element.getLink());
            mbylle();
        }
    }

    private void handleListSelection(int i) {
        ASUtils.log("handleListSelection:" + i);
        final CodeSnippet element = (CodeSnippet)myList.getModel().getElementAt(i);
        if(!element.isFake()){
            //fus ne editor tekstin qe kam


            if(editor_poshte!=null) {
                //Access document, caret, and selection
                final Document document = editor_poshte.getDocument();
                final SelectionModel selectionModel = editor_poshte.getSelectionModel();
                final int start = selectionModel.getSelectionStart();
                final int end = selectionModel.getSelectionEnd();
                System.out.println("selection text:" + selectionModel.getSelectedText());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        String codeToadd = element.getDescription();
                        ASUtils.log("kodi origjinal:" + codeToadd);
                        codeToadd = StringUtil.convertLineSeparators(codeToadd);

                        //codeToadd = StringUtil.replaceIgnoreCase(codeToadd, "<br>", "/n");
                        ASUtils.log("kodi origjinal:" + codeToadd);


                        if(selectionModel.getSelectedText() == null){
                            document.insertString(start, codeToadd);
                        }else{
                            document.replaceString(start, end, codeToadd);
                        }
                    }
                };
                //Making the replacement
                WriteCommandAction.runWriteCommandAction(project_poshte, runnable);
                selectionModel.removeSelection();
            }


        }

        mbylle();
    }

    private void mbylle(){
        if (this.myBalloon != null && this.myBalloon.isVisible()) {
            this.myBalloon.cancel();
        }
        if (this.popupWindow != null && this.popupWindow.isVisible()) {
            this.popupWindow.cancel();
        }

        AndSnippetsMenuAction.this.myActionEvent = null;

        try{
            if(mEditorFactory!=null)mEditorFactory.releaseEditor(this.ta);
        }catch (Exception e){

        }
    }

    static Font getTitleFont() {
        return UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize((UIUtil.FontSize)UIUtil.FontSize.SMALL));
    }

    /////////////////////////////////////////

    private class MyListRenderer
            extends SnippetListRenderer {
        final ColoredListCellRenderer myLocation;

        private MyListRenderer() {
            this.myLocation = new ColoredListCellRenderer(){

                protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                    this.setPaintFocusBorder(false);
                    this.append(MyListRenderer.this.myLocationString, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                    this.setIcon(MyListRenderer.this.myLocationIcon);
                }
            };
        }

        public void clear() {
            super.clear();
            this.myLocation.clear();
            this.myLocationString = null;
            this.myLocationIcon = null;
        }

        public void setLocationString(String locationString) {
            this.myLocationString = locationString;
        }

        @NotNull
        public Component getListCellRendererComponent(@NotNull JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Color bg;
            this.myLocationString = null;
            Component cmp = super.getListCellRendererComponent(list, value, index, isSelected, isSelected);
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(UIUtil.getListBackground((boolean)isSelected));
            p.add(cmp, "Center");
            cmp = p;
            if (this.myLocationString != null || value instanceof BooleanOptionDescription) {
                Component rightComponent;
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(UIUtil.getListBackground((boolean)isSelected));
                panel.add(cmp, "Center");
                if (value instanceof BooleanOptionDescription) {
                    OnOffButton button = new OnOffButton();
                    button.setSelected(((BooleanOptionDescription)value).isOptionEnabled());
                    rightComponent = button;
                } else {
                    rightComponent = this.myLocation.getListCellRendererComponent(list, value, index, isSelected, isSelected);
                }
                panel.add(rightComponent, "East");
                cmp = panel;
            }
            if ((bg = cmp.getBackground()) == null) {
                cmp.setBackground(UIUtil.getListBackground((boolean)isSelected));
                bg = cmp.getBackground();
            }
            this.myMainPanel.setBorder((Border)new CustomLineBorder(bg, 0, 0, 2, 0));
//            String title = AndSnippetsMenuAction.this.getModel().myTitleIndexes.getTitle(index);
            this.myMainPanel.removeAll();
//            if (title != null) {
//                this.myTitle.setText(title);
//                this.myMainPanel.add((Component)AndSnippetsMenuAction.createTitle(" " + title), "North");
//            }
            this.myMainPanel.add(cmp, "Center");
            int width = this.myMainPanel.getPreferredSize().width;
            if (width > AndSnippetsMenuAction.this.myPopupActualWidth) {
                AndSnippetsMenuAction.this.myPopupActualWidth = width;
            }
            JPanel jPanel = this.myMainPanel;
            return jPanel;
        }

        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            this.setPaintFocusBorder(false);
            this.setIcon(EmptyIcon.ICON_16);
            AccessToken token = ApplicationManager.getApplication().acquireReadActionLock();
            try {
                CodeSnippet t = (CodeSnippet)value;
                this.append(t.getTitle());
            }
            finally {
                token.finish();
            }
        }

        @Override
        public void recalculateWidth() {
            ListModel model = AndSnippetsMenuAction.this.myList.getModel();
            this.myTitle.setIcon(EmptyIcon.ICON_16);
            this.myTitle.setFont(AndSnippetsMenuAction.getTitleFont());
//            for (int index = 0; index < model.getSize(); ++index) {
//                String title = AndSnippetsMenuAction.this.getModel().myTitleIndexes.getTitle(index);
//                if (title == null) continue;
//                this.myTitle.setText(title);
//            }
            this.myTitle.setText("veriKerin");
            this.myTitle.setForeground((Color)Gray._122);
            this.myTitle.setAlignmentY(1.0f);
        }

    }

}

