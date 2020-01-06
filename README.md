# android-SVN-Plugin
[![GitHub stable release version](https://img.shields.io/github/release/hushenghao/android-SVN-Plugin.svg?label=android-SVN-Plugin&maxAge=600)](https://github.com/hushenghao/android-SVN-Plugin/releases/latest) [![License](https://img.shields.io/github/license/hushenghao/android-SVN-Plugin.svg?label=License&maxAge=2592000)](https://github.com/hushenghao/android-SVN-Plugin/blob/master/LICENSE)

Android assembleRelease Package and auto commit to SVN
安卓打包并上传到svn的插件

---

#### 使用

* 在项目根目录文件local.properties中添加内容：

        # 设置插件状态false可以关闭自动上传
        svn.plugin.state=true
        # svn账号信息，记得将本文件添加忽略
        svn.username=dede
        svn.password=1234
        svn.url=https://wudi-001:8443/svn/test/trunk
        # 删除文件时的修改日志
        svn.delete=
        # 添加文件时的修改日志
        svn.import=
        # svn忽略debugTask
        svn.ignore.debug=true
        # 使用美团walle写入渠道信息
        svn.walle.state=false
        # walle渠道名，必须开启svn.walle.state
        svn.walle.channel=fortest
        
 * 或者在module.gradle中添加：
 
        // 优先级低于local.properties，可以把公共的配置声明在这里，私密配置声明在local.properties
        svn_plugin {
            pluginState = true
            svnUrl = "https://wudi-001:8443/svn/test/trunk"
            msgImport = "上传测试包"
            msgDelete = "删除测试包"
            ignoreDebug = true
            svnUserName = "dede"
            svnPassword = "1234"
            walleState = true
            walleChannel = "fortest"
        }
        

* 依赖插件

    [**jcenter**](https://bintray.com/dede/AndroidLib/com.dede.svnplugin) 依赖，编辑项目根目录build.gradle

        buildscript {
            dependencies {
                classpath 'com.dede.svnplugin:svn-plugin:0.1.1'
            }
        }

    [**gradle**](https://plugins.gradle.org/plugin/com.dede.svn-plugin) 依赖，编辑项目根目录build.gradle

        buildscript {
            repositories {
                maven { url "https://plugins.gradle.org/m2/" }
            }
            dependencies {
                classpath 'com.dede.svnplugin:svn-plugin:0.1.1'
            }
        }

+ 修改module的build.gradle

        apply plugin: 'com.dede.svn-plugin'// 应用插件

    同步完成后会生成group为svn的assembleReleaseAndCommitSVN的task，如果有多渠道打包会生成多个相应渠道的task

+ 运行task即可生成Release包并上传SVN

        ./gradlew assembleReleaseAndCommitSVN // or
        ./gradlew assemble${渠道名}ReleaseAndCommitSVN // 多渠道打包
