package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Dark styled monospace text area inside a styled scroll pane.
 */
public class StyledTextArea extends JScrollPane {

    private final JTextArea textArea;

    public StyledTextArea(int rows, int columns) {
        super();
        textArea = new JTextArea(rows, columns);
        textArea.setFont(ThemeFonts.CODE);
        textArea.setBackground(ThemeColors.INPUT_BG);
        textArea.setForeground(ThemeColors.TEXT_PRIMARY);
        textArea.setCaretColor(ThemeColors.SECONDARY);
        textArea.setSelectionColor(ThemeColors.SIDEBAR_ACTIVE);
        textArea.setSelectedTextColor(ThemeColors.TEXT_PRIMARY);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        textArea.setTabSize(4);

        setViewportView(textArea);
        setBorder(new LineBorder(ThemeColors.BORDER, 1, true));
        setBackground(ThemeColors.INPUT_BG);
        getViewport().setBackground(ThemeColors.INPUT_BG);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    public StyledTextArea() {
        this(6, 30);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public void setEditable(boolean editable) {
        textArea.setEditable(editable);
    }

    public void clear() {
        textArea.setText("");
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
