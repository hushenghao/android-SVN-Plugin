package com.dede.svnplugin.task;

import com.dede.svnplugin.Config;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;

/**
 * 上传文件到svn
 */
public class Commit2SVN extends DefaultTask {

    @Input
    public File input;

    @TaskAction
    public void action() {
        System.out.println("SVN-Plugin =====>>>> Commit2SVN");
        if (!Config.PLUGIN_STATE) {
            System.out.println("SVN-Plugin is Disable!!!");
            return;
        }

        if (input == null) {
            throw new IllegalArgumentException("Commit2SVN: input APK file is null");
        }
        if (!input.exists()) {
            throw new IllegalArgumentException("Commit2SVN: input APK file un exists :" + input.getAbsolutePath());
        }
        if (input.isDirectory()) {
            throw new IllegalArgumentException("Commit2SVN: input APK file isDirectory :" + input.getAbsolutePath());
        }

        Config.flushConfig();// 刷新配置

        String svnPath = Config.SVN_URL + "/" + input.getName();
        System.out.println("Commit2SVN svnPath ===>>> " + svnPath);
        SVNURL svnUri = parseSVN_URI(svnPath);
        System.out.println("Commit2SVN svnUri ===>>> " + svnUri.getPath());

        SVNNodeKind nodeKind = checkPath(svnUri);// 检测文件是否存在
        if (nodeKind == null) {
            throw new IllegalStateException("Commit2SVN: check SVN Repository url error :" + svnUri.getPath());
        }
        System.out.println("Check result:" + nodeKind);
        if (nodeKind == SVNNodeKind.FILE) {
            doDelete(svnUri);// 删除已存在的文件
        } else if (nodeKind == SVNNodeKind.DIR) {
            throw new IllegalStateException("Commit2SVN: check SVN Repository url :" + svnUri.getPath() + " isDirectory");
        }

        doImport(input, svnUri);
    }

    /**
     * 导入文件到SVN仓库
     *
     * @param source  文件
     * @param svnPath svn路径
     * @return
     */
    private static boolean doImport(File source, SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                Config.SVN_USERNAME, Config.SVN_PASSWORD);
        try {
            SVNCommitInfo commitInfo = svnClientManager.getCommitClient().doImport(source, svnPath,
                    Config.MSG_IMPORT, null, false,
                    false, SVNDepth.INFINITY);
            System.out.println("Import file to SVN ==>>> Author:" + commitInfo.getAuthor() +
                    ", Data:" + commitInfo.getDate() +
                    ", NewVersion:" + commitInfo.getNewRevision());
            return commitInfo.getNewRevision() > 0;
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测SVN仓库当前路径文件状态
     *
     * @param svnPath svn文件地址
     * @return {@link SVNNodeKind#NONE},{@link SVNNodeKind#FILE},{@link SVNNodeKind#DIR}
     */
    private static SVNNodeKind checkPath(SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                Config.SVN_USERNAME, Config.SVN_PASSWORD);
        try {
            SVNRepository repository = svnClientManager.createRepository(svnPath, true);
            return repository.checkPath("", SVNRepository.INVALID_REVISION);
        } catch (SVNException e) {
            e.printStackTrace();
            throw new IllegalStateException("Commit2SVN: check SVN Repository url :" + svnPath.getPath() + " error");
        }
    }

    /**
     * 删除SVN已经存在的文件
     *
     * @param svnPath svn路径
     * @return
     */
    private static boolean doDelete(SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                Config.SVN_USERNAME, Config.SVN_PASSWORD);
        try {
            SVNCommitInfo commitInfo = svnClientManager.getCommitClient().doDelete(new SVNURL[]{svnPath}, Config.MSG_DELETE);
            System.out.println("Delete file from SVN ==>>> Author:" + commitInfo.getAuthor() +
                    ", Data:" + commitInfo.getDate() +
                    ", NewVersion:" + commitInfo.getNewRevision());
            return commitInfo.getNewRevision() > 0;
        } catch (SVNException e) {
            e.printStackTrace();
            throw new IllegalStateException("Commit2SVN: delete SVN Repository file :" + svnPath.getPath() + " error");
        }
    }

    /**
     * 解析SVN路径
     *
     * @param uri 字符串地址
     * @return
     */
    private static SVNURL parseSVN_URI(String uri) {
        try {
            return SVNURL.parseURIEncoded(uri);
        } catch (SVNException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Commit2SVN: parse SVN Repository url error :" + uri);
        }
    }
}
