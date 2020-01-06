package com.dede.svnplugin.task;

import com.android.build.OutputFile;
import com.android.build.gradle.api.BaseVariant;
import com.dede.svnplugin.plugin.Extension;
import com.meituan.android.walle.ChannelInfo;
import com.meituan.android.walle.ChannelReader;
import com.meituan.android.walle.ChannelWriter;
import com.meituan.android.walle.SignatureNotFoundException;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
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
import java.io.IOException;
import java.util.Iterator;

/**
 * 上传文件到svn
 */
public class Commit2SVN extends DefaultTask {

    @Input
    public BaseVariant variant;
    @Input
    public Project targetProject;

    private Extension config;

    @TaskAction
    public void action() {
        System.out.println("SVN-Plugin =====>>>> Commit2SVN");

        config = Extension.getConfig(this.targetProject);

        if (!config.getPluginState()) {
            System.out.println("SVN-Plugin is Disable!!!");
            return;
        }

        Iterator iterator = variant.getOutputs().iterator();
        while (iterator.hasNext()) {
            OutputFile next = (OutputFile) iterator.next();
            File apkFile = next.getOutputFile();

            checkFile(apkFile);

            writeChannel(apkFile);

            System.out.println("Commit2SVN filePath ===>>> " + apkFile.getAbsolutePath());

            String svnPath = config.getSvnUrl() + "/" + apkFile.getName();
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

            doImport(apkFile, svnUri);
        }
    }

    /**
     * 写入美团walle渠道信息
     */
    private void writeChannel(File apkFile) {
        System.out.println("Walle plugin state :" + config.getWalleState());
        if (!config.getWalleState()) return;
        System.out.println("Walle =====>>>> start writer channel :" + config.getWalleChannel());
        try {
            ChannelWriter.put(apkFile, config.getWalleChannel());
            System.out.println("Walle =====>>>> writer channel completed");
        } catch (IOException | SignatureNotFoundException e) {
            e.printStackTrace();
            System.out.println("Walle =====>>>> writer channel error");
        }
        ChannelInfo channelInfo = ChannelReader.get(apkFile);
        if (channelInfo != null) {
            String channel = channelInfo.getChannel();
            System.out.println("Walle =====>>>> reader channel :" + channel);
        } else {
            System.out.println("Walle =====>>>> reader channel error");
        }
    }

    /**
     * 检测文件
     */
    private void checkFile(File apkFile) {
        if (apkFile == null) {
            throw new IllegalArgumentException("Commit2SVN: apkFile APK file is null");
        }
        if (!apkFile.exists()) {
            throw new IllegalArgumentException("Commit2SVN: apkFile APK file un exists :" + apkFile.getAbsolutePath());
        }
        if (apkFile.isDirectory()) {
            throw new IllegalArgumentException("Commit2SVN: apkFile APK file isDirectory :" + apkFile.getAbsolutePath());
        }
    }

    /**
     * 导入文件到SVN仓库
     *
     * @param source  文件
     * @param svnPath svn路径
     * @return
     */
    private boolean doImport(File source, SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                config.getSvnUserName(), config.getSvnPassword());
        try {
            SVNCommitInfo commitInfo = svnClientManager.getCommitClient().doImport(source, svnPath,
                    config.getMsgImport(), null, false,
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
    private SVNNodeKind checkPath(SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                config.getSvnUserName(), config.getSvnPassword());
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
    private boolean doDelete(SVNURL svnPath) {
        DAVRepositoryFactory.setup();
        SVNClientManager svnClientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),
                config.getSvnUserName(), config.getSvnPassword());
        try {
            SVNCommitInfo commitInfo = svnClientManager.getCommitClient().doDelete(new SVNURL[]{svnPath}, config.getMsgDelete());
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
