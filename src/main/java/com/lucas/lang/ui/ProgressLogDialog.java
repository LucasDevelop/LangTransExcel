package com.lucas.lang.ui;

import com.lucas.lang.ext.LogExtKt;
import com.lucas.lang.utils.ParserConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProgressLogDialog extends JDialog {
    private JPanel contentPane;
    private JTextArea v_log;
    private JScrollPane v_scroll;
    private JButton buttonOK;
//    private JButton buttonCancel;

    public static ProgressLogDialog getInstance(String title) {
        ProgressLogDialog progressLogDialog = new ProgressLogDialog();
        progressLogDialog.setTitle(title);
        progressLogDialog.setMinimumSize(new Dimension(600, 300));
        progressLogDialog.setLocationRelativeTo(null); //剧中
        return progressLogDialog;
    }

    public ProgressLogDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        LogExtKt.setLogProxy(s -> {
            addLog(s);
            return null;
        });

//        buttonCancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void addLog(String str) {
        SwingUtilities.invokeLater(() -> {
            v_log.append(str + "\n");
            int maximum = v_scroll.getVerticalScrollBar().getMaximum();
            v_scroll.getViewport().setViewPosition(new Point(0, maximum));
            v_scroll.updateUI();
        });
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
