# android-SVN-Plugin
[![GitHub stable release version](https://img.shields.io/github/release/hushenghao/android-SVN-Plugin.svg?label=android-SVN-Plugin &maxAge=600)](https://github.com/hushenghao/android-SVN-Plugin/releases/latest) [![License](https://img.shields.io/github/license/hushenghao/android-SVN-Plugin.svg?label=License &maxAge=2592000)](https://github.com/hushenghao/android-SVN-Plugin/blob/master/LICENSE)

Android assembleRelease Package and auto commit to SVN
安卓打包并上传到svn的插件

---

#### 使用

* 在项目根目录创建文件[svn-config.properties](https://github.com/hushenghao/android-SVN-Plugin/blob/master/svn-config.properties)，内容如下：

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

* 依赖插件，三种方式

    [**jcenter**](https://bintray.com/dede/AndroidLib/com.dede.svnplugin) 依赖，编辑项目根目录build.gradle

        buildscript {
            dependencies {
                classpath 'com.dede.svnplugin:svn-plugin:0.0.7+'
            }
        }

    [**gradle**](https://plugins.gradle.org/plugin/com.dede.svn-plugin) 依赖，编辑项目根目录build.gradle

        buildscript {
            repositories {
                maven { url "https://plugins.gradle.org/m2/" }
            }
            dependencies {
                classpath 'gradle.plugin.com.dede.svnplugin:svn-plugin:0.0.7+'
            }
        }

    ~~本地仓库~~ 依赖(不推荐)，复制repo文件夹到项目，并编辑项目根目录build.gradle

        buildscript {
            repositories {
                maven { url uri('./repo') }// 仓库路径指向repo文件夹
            }
            dependencies {
                classpath 'com.dede.svnplugin:svn-plugin:0.0.7'
            }
        }

+ 修改module的build.gradle

        apply plugin: 'com.dede.svn-plugin'// 应用插件

    同步完成后会生成group为svn plugin的commitReleasePackage2SVN的task


