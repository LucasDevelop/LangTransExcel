package com.lucas.lang.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.lucas.lang.utils.ParserConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class InitConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField v_project_name;
    private JLabel Jlable2;
    private JTextField v_project_path;
    private JButton v_excel_path;
    private JCheckBox v_is_conver_key;
    private JTextField v_lang_types;
    private JRadioButton v_project_to_excel;
    private JRadioButton v_excel_to_project;
    private JTextField v_dir_pattern;

    public InitConfigDialog(AnActionEvent e) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        Project project = e.getData(PlatformDataKeys.PROJECT);
        v_project_name.setText(project.getName());
        v_project_path.setText(project.getBasePath());
        v_project_to_excel.addActionListener(actionEvent -> v_excel_to_project.setSelected(false));
        v_excel_to_project.addActionListener(actionEvent -> v_project_to_excel.setSelected(false));
        v_excel_path.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fileChooser.showOpenDialog(InitConfigDialog.this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.exists()) {
                    v_excel_path.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        //检查参数
        String projectName = v_project_name.getText();
        String projectPath = v_project_path.getText();
        String excelPath = v_excel_path.getText();
        String langTypes = v_lang_types.getText();
        String dirPattern = v_dir_pattern.getText();
        boolean isCoverKey = v_is_conver_key.isSelected();
        String[] split = null;
        if (langTypes != null && !langTypes.equals("")) {
            split = langTypes.split(",");
        }
        if (projectName == "") {
            Messages.showInfoMessage("请填写项目名称！", "错误");
            return;
        }
        if (projectPath == "") {
            Messages.showInfoMessage("请填写项目路径！", "错误");
            return;
        }
        if (excelPath == ""||excelPath == "选择文件或者文件夹") {
            Messages.showInfoMessage("请选择文件或者文件夹！", "错误");
            return;
        }
        if (langTypes == "") {
            Messages.showInfoMessage("请输入支持的语言类型简写(以逗号分隔)！", "错误");
            return;
        }
        ParserConfig config = new ParserConfig(projectName, projectPath, excelPath, isCoverKey, new ArrayList<>(Arrays.asList(split)), dirPattern);
        dispose();
        ProgressLogDialog dialog = new ProgressLogDialog();
        dialog.setTitle("转换进度");
        dialog.setMinimumSize(new Dimension(600, 300));
        dialog.setLocationRelativeTo(null);//剧中
        dialog.startTask(v_project_to_excel.isSelected(), config);
        dialog.setVisible(true);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
//        InitConfigDialog dialog = new InitConfigDialog(e);
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
    }
}
