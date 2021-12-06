package com.lucas.lang.v2.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.BooleanEnum;
import com.lucas.lang.v2.confoig.Constant;
import com.lucas.lang.v2.ext.ProviderExtKt;

import java.util.TreeMap;

public class RowBean {

    private TreeMap<String, String> langs = new TreeMap();

    private RowAttr rowAttr;

    public TreeMap<String, String> getLangs() {
        return langs;
    }

    public void setLangs(TreeMap<String, String> langs) {
        this.langs = langs;
    }

    public RowAttr getRowAttr() {
        if (rowAttr != null) return rowAttr;
        if (langs.containsKey(Constant.FIELD_PROPERTY)) {
            String property = langs.get(Constant.FIELD_PROPERTY);
            try {
                rowAttr = ProviderExtKt.getGson().fromJson(property, RowAttr.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rowAttr;
    }

    public void setRowAttr(RowAttr rowAttr) {
        try {
            String property = ProviderExtKt.getGson().toJson(rowAttr);
            langs.put(Constant.FIELD_PROPERTY, property);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncRowAttr() {
        setRowAttr(rowAttr);
    }

    @Override
    public String toString() {
        return "RowBean{" +
                "langs=" + langs +
                ", rowAttr=" + rowAttr +
                '}';
    }
}
