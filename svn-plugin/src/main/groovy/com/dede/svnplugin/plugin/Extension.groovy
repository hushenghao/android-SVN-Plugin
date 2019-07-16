package com.dede.svnplugin.plugin

import com.dede.svnplugin.util.TextUtil
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

import java.nio.charset.StandardCharsets

class Extension {
    /**
     * plugin state, default true
     */
    boolean pluginState = true

    /**
     * 上传文件到svn时的日志
     */
    String msgImport = "Import APK package by svn-plugin"

    /**
     * 从svn删除文件时的日志
     */
    String msgDelete = "Delete APK package by svn-plugin"

    /**
     * svn路径
     */
    String svnUrl

    /**
     * svn user
     */
    String svnUserName

    /**
     * svn password
     */
    String svnPassword

    /**
     * 忽略debug task
     */
    boolean ignoreDebug = true

    /**
     * 是否打开walle渠道写入
     */
    boolean walleState

    /**
     * walle打开时写入的渠道
     */
    String walleChannel

    Extension(Project project) {
        File file = new File(project.getRootDir(), "local.properties")
        System.out.println("SVN-Plugin load config ===>>> " + file.getAbsolutePath())

        Properties properties = new Properties()
        InputStreamReader reader
        try {
            reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            properties.load(reader)
        } catch (IOException e) {
            throw new ProjectConfigurationException("Plugin load config file " +
                    file.getAbsolutePath() + " error", e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (IOException ignore) {
                }
            }
        }

        if (TextUtil.isNull(svnUserName)) {
            svnUserName = properties.getProperty("svn.username")
        }
        if (TextUtil.isNull(svnPassword)) {
            svnPassword = properties.getProperty("svn.password")
        }
        def value = properties.getProperty("svn.delete")
        if (!TextUtil.isNull(value)) {
            msgDelete = value
        }
        value = properties.getProperty("svn.import")
        if (!TextUtil.isNull(value)) {
            msgImport = value
        }
        value = properties.getProperty("svn.url")
        if (!TextUtil.isNull(value)) {
            svnUrl = value
        }

        // 兼容以前的配置
        value = properties.getProperty("svn.plugin.state")
        if (!TextUtil.isNull(value)) {
            pluginState = Boolean.valueOf(value)
        }
        value = properties.getProperty("svn.ignore.debug")
        if (!TextUtil.isNull(value)) {
            ignoreDebug = Boolean.valueOf(value)
        }
        value = properties.getProperty("svn.walle.state")
        if (!TextUtil.isNull(value)) {
            ignoreDebug = Boolean.valueOf(value)
        }
        value = properties.get("svn.walle.channel")
        if (!TextUtil.isNull(value)) {
            walleChannel = value
        }
    }

    static Extension getConfig(Project project) {
        def config = project.getExtensions().findByType(Extension.class)
        if (config == null) {
            config = new Extension(project)
        }
        return config
    }
}
