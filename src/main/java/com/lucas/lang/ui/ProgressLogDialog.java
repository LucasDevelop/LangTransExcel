package com.lucas.lang.ui;

import com.intellij.openapi.ui.Messages;
import com.lucas.lang.ext.LogExtKt;
import com.lucas.lang.utils.TransExcelToXml;
import com.lucas.lang.utils.TransXmlToExcel;
import com.lucas.lang.utils.ParserConfig;

import javax.swing.*;
import java.awt.event.*;

public class ProgressLogDialog extends JDialog {
    private JPanel contentPane;
    private JTextArea v_log;
    private JButton buttonOK;
//    private JButton buttonCancel;

    public ProgressLogDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        LogExtKt.setLogProxy(s -> {
            v_log.append(s + "\n");
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
        v_log.append(str);
    }

    public void startTask(boolean isSelectP2E, ParserConfig config) {
        try {
            new Thread(() -> {
                if (isSelectP2E) {
                    TransXmlToExcel.INSTANCE
                            .initConfig(config)
                            .start();
                } else {
                    TransExcelToXml.INSTANCE
                            .initConfig(config)
                            .start();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
            Messages.showErrorDialog(e.getMessage(), "发生错误！"+e.getMessage());
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
