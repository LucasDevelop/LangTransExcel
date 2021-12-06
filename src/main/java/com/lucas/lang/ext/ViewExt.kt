package com.lucas.lang.ext

import java.awt.Dimension
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JScrollPane

fun JComponent.labelSize(): JComponent {
    minimumSize = Dimension(100, 30)
    return this
}

fun JComponent.contentSize(): JComponent {
    maximumSize = Dimension(200, 30)
    return this
}

fun createCheckBoxListView(list: List<String>,onCreateCheckBox:(cb:JCheckBox,text:String)->Unit): JScrollPane {
    val box = Box.createVerticalBox()
    list.forEach {
        val jCheckBox = JCheckBox(it)
        onCreateCheckBox(jCheckBox,it)
        box.add(jCheckBox)
    }
    val jScrollPane = JScrollPane(box)
    jScrollPane.preferredSize = Dimension(150, 100)
    jScrollPane.minimumSize = Dimension(150, 100)
    return jScrollPane
}
