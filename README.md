# android-SVN-Plugin
Android assembleRelease Package and auto commit to SVN
安卓打包并上传到svn的插件

---

直接复制repo文件夹到项目，并编辑项目根目录build.gradle

    buildscript {
         repositories {
             maven { url uri('./repo') }// 仓库路径指向repo文件夹
         }
         dependencies {
             classpath 'com.android.tools.build:gradle:3.2.1'
             classpath 'com.dede.svnplugin:svn-plugin:1.0.0'
         }
     }

修改module的build.gradle

    apply plugin: 'svn-plugin'// 应用插件

 编译完成后会生成group为svn-plugin的commitReleasePackage2SVN的task
