package com.lucas.lang.ui;

import android.text.TextUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lucas.lang.utils.CacheUtil;
import com.lucas.lang.utils.FileUtil;
import com.lucas.lang.utils.ParserConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class InitConfig extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
//        InitConfigDialog dialog = new InitConfigDialog(e);
//        dialog.setTitle("配置参数");
//        dialog.setMinimumSize(new Dimension(800,350));
//        dialog.setLocationRelativeTo(null);//剧中
//        dialog.setVisible(true);

        ParserConfig config = CacheUtil.INSTANCE.readConfig(e.getProject().getBasePath());
        if (config ==null){
            scanProject(e, parserConfig -> {
                ConfigDialog.Companion.show(parserConfig,e);
                return null;
            });
        }else {
            ConfigDialog.Companion.show(config,e);
        }
    }

    public static void scanProject(AnActionEvent e, Function1<ParserConfig,Unit> block) {
        ProgressLogDialog dialog = new ProgressLogDialog();
        dialog.setTitle("扫描项目");
        dialog.setMinimumSize(new Dimension(600, 300));
        dialog.setLocationRelativeTo(null);//剧中
        new Thread(() -> {
            ParserConfig parserConfig = initProjectConfig(e,dialog);
            CacheUtil.INSTANCE.saveConfig(parserConfig);
            SwingUtilities.invokeLater(() ->{
                block.invoke(parserConfig);
            });
        }).start();
        dialog.setVisible(true);
    }

    private static ParserConfig initProjectConfig(AnActionEvent e, ProgressLogDialog dialog) {

        ParserConfig parserConfig = new ParserConfig(
                e.getProject().getName(),
                "/Users/lucas/Documents/developer/android/EgyptOutfield/egypt-outfield-android",
//                e.getProject().getBasePath(),
                "",
                true,
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                "zh",
                "(values-[a-z]{2,3})|(values)$",
                new ArrayList<>()
        );
        //扫描项目--module
        List<File> allModuleDir = FileUtil.INSTANCE.findAllModuleDir(new File(parserConfig.getProjectPath()), new Function1<File, Unit>() {
            @Override
            public Unit invoke(File file) {
                dialog.addLog(file.getAbsolutePath());
                return null;
            }
        });
        parserConfig.getModuleDir().clear();
        parserConfig.getModuleDir().addAll(allModuleDir);
        //扫描项目--lang
        HashSet<String> langs = new HashSet<>();
        for (int i = 0; i < allModuleDir.size(); i++) {
            File valuesDir = new File(allModuleDir.get(i), "src/main/res");
            if (valuesDir.exists() && valuesDir.isDirectory()) {//查找语言类型
                for (File file : valuesDir.listFiles()) {
                    String fileName = file.getName();
                    if (file.isDirectory() && fileName.contains("-")) {
                        String langName = fileName.substring(fileName.indexOf("-") + 1);
                        if (!TextUtils.isEmpty(langName) &&
                                !langName.contains("dpi") &&
                                Pattern.compile("[a-z]{0,4}").matcher(langName).matches()) {
                            langs.add(langName);
                        }
                    }
                }
            }
        }
        parserConfig.getLangTypes().clear();
        parserConfig.getLangTypes().addAll(langs);
        dialog.setVisible(false);
        return parserConfig;
    }
}
