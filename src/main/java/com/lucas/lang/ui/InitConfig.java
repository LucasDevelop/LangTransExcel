package com.lucas.lang.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.awt.*;

public class InitConfig extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        InitConfigDialog dialog = new InitConfigDialog(e);
        dialog.setTitle("配置参数");
        dialog.setMinimumSize(new Dimension(600,300));
        dialog.setLocationRelativeTo(null);//剧中
        dialog.setVisible(true);
    }
}
