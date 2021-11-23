package com.lucas.lang.ext

import java.awt.Dimension
import javax.swing.JComponent

fun JComponent.labelSize(): JComponent {
    minimumSize = Dimension(100,30)
    return this
}

fun JComponent.contentSize(): JComponent {
    maximumSize = Dimension(200,30)
    return this
}