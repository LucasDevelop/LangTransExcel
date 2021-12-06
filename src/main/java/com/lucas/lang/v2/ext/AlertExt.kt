package com.lucas.lang.v2.ext

import javax.swing.JOptionPane

fun String.showMessage() {
    JOptionPane.showMessageDialog(null, this, "提示", JOptionPane.PLAIN_MESSAGE)
}

fun String.showWaning() {
    JOptionPane.showMessageDialog(null, this, "警告", JOptionPane.WARNING_MESSAGE)
}

fun String.showError() {
    JOptionPane.showMessageDialog(null, this, "错误", JOptionPane.ERROR_MESSAGE)
}