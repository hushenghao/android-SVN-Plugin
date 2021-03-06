package com.dede.svnplugin.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

/**
 * SVN插件
 */
class SVNPluginImpl implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // check plugin
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new ProjectConfigurationException("Plugin requires the 'com.android.application' plugin to be configured.", null)
        }

        // check build version
        String version = null
        try {
            def clazz = Class.forName("com.android.builder.Version")
            def field = clazz.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            field.setAccessible(true)
            version = field.get(null)
        } catch (ClassNotFoundException ignore) {
        } catch (NoSuchFieldException ignore) {
        }
        if (version == null) {
            try {
                def clazz = Class.forName("com.android.builder.model.Version")
                def field = clazz.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
                field.setAccessible(true)
                version = field.get(null)
            } catch (ClassNotFoundException ignore) {
            } catch (NoSuchFieldException ignore) {
            }
        }
        if (version != null && versionCompare(version, "3.0.0") < 0) {
            throw new ProjectConfigurationException("Plugin requires the 'com.android.tools.build:gradle' version 3.0.0 or above to be configured.", null)
        }

        applyExtension(project)

        applyTask(project)
    }

    private void applyExtension(Project project) {
        project.extensions.create("svn_plugin", Extension.class, project)
    }

    private void applyTask(Project project) {
        project.afterEvaluate {
            Extension config = Extension.getConfig(project)

            project.android.applicationVariants.all { BaseVariant variant ->
                if (config.ignoreDebug && variant.buildType.name != 'release') return// 忽略debug task

                def variantName = variant.name.capitalize()
                com.dede.svnplugin.task.Commit2SVN task = project.tasks.create("assemble${variantName}AndCommitSVN",
                        com.dede.svnplugin.task.Commit2SVN.class)// don't remove package
                task.setGroup("svn")
                task.targetProject = project
                task.variant = variant

                // 执行上传task前先执行打包task
                if (variant.hasProperty('assembleProvider')) {
                    task.dependsOn variant.assembleProvider.get()
                } else {
                    task.dependsOn variant.assemble
                }
            }
        }
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    private static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("-")[0].split("\\.")
        String[] vals2 = str2.split("-")[0].split("\\.")
        int i = 0
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i] == vals2[i]) {
            i++
        }

        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]) <=> Integer.valueOf(vals2[i])
            return Integer.signum(diff)
        }

        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length)
        }
    }
}