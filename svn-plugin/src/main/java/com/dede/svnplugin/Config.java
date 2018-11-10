package com.dede.svnplugin;

import com.dede.svnplugin.util.TextUtil;

import org.gradle.api.ProjectConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 一些SVN配置
 * <p>
 * 读取内容为项目根目录的svn-config.properties文件，使用utf-8编码
 * 文件内容如下：
 * <p>
 * # 关闭自动上传，默认值为true
 * svn.plugin.state=true
 * # svn用户名
 * svn.username=dede
 * # svn用户密码
 * svn.password=1234
 * # svn服务器地址
 * svn.url=https://xxx:443/svn/test/trunk
 * # 删除文件时的提交记录
 * svn.delete=可以不设置
 * # 导入文件时的提交记录
 * svn.import=可以不设置
 */
public class Config {

    public static String MSG_DELETE = "Delete APK package by svn-plugin";
    public static String MSG_IMPORT = "Import APK package by svn-plugin";

    private static Properties properties;

    static {
        properties = new Properties();
        flushConfig();
    }

    public static void flushConfig() {
        File file = new File("./svn-config.properties");
        try {
            properties.load(new InputStreamReader(new FileInputStream(file), "utf-8"));// 中文转码
            String message = properties.getProperty("svn.delete");
            MSG_DELETE = TextUtil.isNull(message) ? MSG_DELETE : message;
            message = properties.getProperty("svn.import");
            MSG_IMPORT = TextUtil.isNull(message) ? MSG_IMPORT : message;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ProjectConfigurationException("Plugin load config file " +
                    file.getAbsolutePath() + " error", e);
        }
    }

    public static boolean PLUGIN_STATE = Boolean.valueOf(properties.getProperty("svn.plugin.state", "true"));

    public static String SVN_USERNAME = properties.getProperty("svn.username");
    public static String SVN_PASSWORD = properties.getProperty("svn.password");
    public static String SVN_URL = properties.getProperty("svn.url");

}
