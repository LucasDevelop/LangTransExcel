# 开发一个Android studio 插件：项目国际化与Excel文件双向导入，并支持在线翻译。

- [功能说明](#功能说明)
- [安装使用方式](#安装使用方式)
- [插件开发流程](#插件开发流程)
- [国际化资源与Excel互导脚本开发](#国际化资源与Excel互导脚本开发)

----

## 功能说明

当一个项目从非国际化（只有单言语类型）转向国际化（多语言支持）亦或项目过于庞大Module数量众多每期迭代都需要从各个Module抽取和填补翻译时就会有了个很烦恼的事情，这么多的语言资源靠手动整理那得到猴年马月，等整理完也练就了一双麒麟臂了。（以上纯属抱怨可忽略）😄

言归正传，如同以上机械式工作作为一名程序猿当然是交给程序去处理喽，所以需要写一个能够遍历读取识别并提取项目中的语言资源文件，并将资源导出成Excel文件以方便交给其他工具人（手动狗头）进行翻译的脚本，翻译完成后还需要将Excel文件转换成项目中的资源格式并原路存储进去。为了更加方便的在各个项目中使用当然还是把上面写的脚本套上Intellij plugin的皮囊一起食用更佳。

#### 支持功能：
- [x] 将项目的res->values-xx-> strings.xml资源导出至excel文件（请勿修改表格的格式）
- [x] 将excel文件文件中的资源导回至项目中（资源会原路径写入）
- [x] 支持导出的资源使用颜色高亮状态<font color='green'>新增</font><font color='yellow'>覆盖</font><font color='red'>异常</font>
- [x] 支持导出资源时自动在线翻译缺失的语言类型资源，并写入Excel中。（在线翻译使用的百度API，大量翻译可能会限制IP）
- [x] 支持将Excel导回项目中时自动补全缺失的语言资源，以Excel中已存在的资源为准。

<font color='red'>Tip:当前插件v1.0版本未经过大量测试，所以使用前务必先提交项目代码，以防不必要的损失。</font>

## 安装使用方式

1. 安装插件
> 插件jar包下载地址：https://github.com/LucasDevelop/LangTransExcel/blob/master/LangTransExcel-V1.0.0.zip
使用本地安装,随后重启IDE。
![image.png](https://upload-images.jianshu.io/upload_images/9625409-947c55c6c231d8c9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)


2. 启动插件
> 打开Android stuido 菜单栏Tools->Android Lang Transform Excel就会看到下面的配置弹窗，填写好配置，一般情况项目名称和项目路径无需修改，插件会自动读取当前项目参数并填写。其他参数也是有默认值，除了需要我们自己选择下Excel的文件地址，Excel地址可以是个文件夹脚本会自动判断并新建一个Excel文件。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-e3b163fa18683f9f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

3. 运行脚本
> 点击OK后开始运行脚本，并实时显示运行进度以及资源数据，耐心等待运行结束即可，具体效果如下。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-5fb874645aeea34a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

4. 查看导出的Excel文件
> 如果配置时选择的是文件夹那么生成Excel 文件为【国际化翻译.xls】固定名称。剩下的就是翻译的事情了。

5. 翻译完成工作后将Excel导入项目
> 重复步骤2，修改配置只需选中文件导向【Excel->项目】和选择Excel文件路径即可，点击ok运行脚本并实时显示进度。

## 插件开发流程
1. 安装Intellij idea
> 下载地址：https://www.jetbrains.com/idea/ 安装最新的版本即可

2. 新建一个Plugin项目
> 选择Gradle项目以方便依赖三方lib，并且勾选java/Intellij Platfrom Plugin, kotlin是否勾选看个人开发语言习惯。然后下一步。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-142e9419015afe0e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 填写项目名称以及地址

![image.png](https://upload-images.jianshu.io/upload_images/9625409-1ba1d4cf429dbe93.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

3. 配置插件信息

![image.png](https://upload-images.jianshu.io/upload_images/9625409-74f9da772dbb8cf4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

4. 创建java文件夹

![image.png](https://upload-images.jianshu.io/upload_images/9625409-5559afb4b7c84f59.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

5. 配置Gradle
> 如果插件要可安装在最近android studio版本请务必配置红框内容，否则会导致无法安装在Android studio上。
intellij {
version = '2020.1' // 因为Android Studio 4.1是基于IDEA 2020.1 Community版本开发的，所以这里调试也指定为此版本
plugins = ['android']
}
patchPluginXml {
changeNotes = """
V1.0.0:实现基本双向导入功能.<br>
<em></em>"""
sinceBuild = '191' // 插件适用的IDEA版本范围，此范围基本涵盖了Android Studio最近两三年的版本
untilBuild = '212.*'
}

![image.png](https://upload-images.jianshu.io/upload_images/9625409-96447dab795f2bf8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

6. 创建插件入口，也就是为啥插件会在Tools->Android Lang Transform Excel上

![image.png](https://upload-images.jianshu.io/upload_images/9625409-37355a8ab41cfe7b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

![image.png](https://upload-images.jianshu.io/upload_images/9625409-69ed97e8a9f69199.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

7. 创建弹窗。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-20db40eae23e0184.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 创建完弹窗后可看到以下界面，最右侧可选择控件并拖动到中间的布局中。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-b4ec8601b8bc142c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

8. 运行插件，查看效果

![image.png](https://upload-images.jianshu.io/upload_images/9625409-7ed8d82e0cf7c4be.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 等待编译结束后会自动新开一个Idea程序窗口，在新开的窗口创建一个新项目或者选择一个项目进入。在新开的Idea窗口中找到我们的插件并打开。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-34c3a37b47eff5d5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

最后效果如下：

![image.png](https://upload-images.jianshu.io/upload_images/9625409-d9a4500dd11e2c58.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

9. 打包插件并安装在android studio上

> 打开右侧Gradle任务栏，运行buildPlugin

![image.png](https://upload-images.jianshu.io/upload_images/9625409-c87de927b7756072.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 等待编译结束后在build中找到我们的插件包

![image.png](https://upload-images.jianshu.io/upload_images/9625409-27235a35340f7969.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 在Android studio中选择本地安装我们的插件，然后重启Android studio即可。
当然，我们也可以把插件发布到官网仓库然后供其他人安装使用，具体方式大家各自查找吧，这里不讲解了。

![image.png](https://upload-images.jianshu.io/upload_images/9625409-c7623ddb445b7650.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)



## 国际化资源与Excel互导脚本开发
> 主要是基于poi和dom4j的API进行文件操作，API 对应的Gradle依赖如下
implementation group: 'org.apache.poi', name: 'poi', version: '3.9'
// https://mvnrepository.com/artifact/org.dom4j/dom4j
implementation group: 'org.dom4j', name: 'dom4j', version: '2.1.1'
implementation group: 'jaxen', name: 'jaxen', version: '1.2.0'

![image.png](https://upload-images.jianshu.io/upload_images/9625409-01effb4e9edbe068.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/740)

> 具体文件查找Excel读取/写入实现代码已上传至gayhub,在下就不在这里指指点点了。相信各位大佬对于这种级别的API还是能把握住的。

插件和脚本源码地址：https://github.com/LucasDevelop/LangTransExcel
