/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.dm.thumbnails.video.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.api.Constants;
import org.jahia.dm.DocumentOperationException;
import org.jahia.dm.thumbnails.VideoThumbnailService;
import org.jahia.dm.utils.ProcessUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.StringOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * The video thumbnail generation service.
 * 
 * @author Cédric Mailleux
 * @author Sergiy Shyrkov
 */
public class VideoThumbnailServiceImpl implements VideoThumbnailService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(VideoThumbnailServiceImpl.class);

    private boolean autodetect;

    private boolean enabled;

    private String executablePath = "ffmpeg";

    private String parameters = "-y -itsoffset ${offset} -i ${input} -vcodec mjpeg -vframes 1 -an -f rawvideo -s ${size} ${output}";

    private File workingDir;

    public void afterPropertiesSet() throws Exception {
        if (autodetect) {
            doAutodetect();
        }
    }

    public boolean canHandle(JCRNodeWrapper fileNode) throws RepositoryException {
        if (!isEnabled()) {
            return false;
        }

        return fileNode.isNodeType("nt:file")
                && JCRContentUtils.isMimeTypeGroup(fileNode.getFileContent().getContentType(),
                        "video");
    }

    public boolean createThumbnailForNode(JCRNodeWrapper fileNode, String thumbnailName,
            int offsetSeconds, String thumbnailSize) throws RepositoryException,
            DocumentOperationException {
        if (!canHandle(fileNode)) {
            return false;
        }

        long timer = System.currentTimeMillis();

        JCRNodeWrapper thumbNode = null;

        File source = null;
        File thumbnail = null;
        try {
            source = File.createTempFile("video-source", null);
            JCRContentUtils.downloadFileContent(fileNode, source);
            thumbnail = generateThumbnail(source, offsetSeconds, thumbnailSize);

            if (thumbnail != null && thumbnail.isFile()) {
                thumbNode = storeThumbnailNode(fileNode, thumbnail, thumbnailName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Generated thumbnail {} for node {} in {} ms", new Object[] {
                            thumbNode.getPath(), fileNode.getPath(),
                            (System.currentTimeMillis() - timer) });
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            FileUtils.deleteQuietly(thumbnail);
            FileUtils.deleteQuietly(source);
        }

        return thumbNode != null;
    }

    protected void doAutodetect() {
        logger.info("Checking if the {} is present in the current path", executablePath);

        enabled = ProcessUtils.commandPresent(executablePath, workingDir);

        if (enabled) {
            logger.info("Found {} in the current system path." + " Service will be enabled.",
                    executablePath);
        } else {
            logger.info("Command {} cannot be found in the current system path."
                    + " The service will be disabled.", executablePath);
        }
    }

    public boolean generateThumbnail(File videoFile, File outputFile, int offsetSeconds, String size)
            throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("FFmpeg service is not enabled." + " Skip converting file {}", videoFile);

            return false;
        }

        long timer = System.currentTimeMillis();

        CommandLine cmd = getConvertCommandLine(videoFile, outputFile,
                String.valueOf(offsetSeconds), StringUtils.defaultIfEmpty(size, "320x240"));

        if (logger.isDebugEnabled()) {
            logger.debug("Execuiting thumbnail generation command: {}", cmd.toString());
        }

        int exitValue = 0;

        StringOutputStream err = new StringOutputStream();
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(null, err));
            if (workingDir != null) {
                if (workingDir.exists() || workingDir.mkdirs()) {
                    executor.setWorkingDirectory(workingDir);
                }
            }
            exitValue = executor.execute(cmd, System.getenv());
        } catch (Exception e) {
            throw new DocumentOperationException(e);
        } finally {
            if (exitValue > 0 && err.getLength() > 0) {
                logger.error("External process finished with error. Cause: {}", err.toString());
            }
            if (logger.isDebugEnabled() && err.getLength() > 0) {
                logger.debug(err.toString());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Generating thumbnail {} from {} done (exit code: {}) in {} ms",
                    new Object[] { outputFile, videoFile, exitValue,
                            (System.currentTimeMillis() - timer) });
        }

        return exitValue == 0;
    }

    public File generateThumbnail(File videoFile, int offsetSeconds, String size)
            throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("FFmpeg service is not enabled." + " Skip converting file {}", videoFile);

            return null;
        }

        File out = null;
        try {
            out = File.createTempFile("video-thumbnail", null);
            generateThumbnail(videoFile, out, offsetSeconds, size);
        } catch (DocumentOperationException e) {
            FileUtils.deleteQuietly(out);
            throw e;
        } catch (IOException e) {
            FileUtils.deleteQuietly(out);
            logger.error(
                    "Unable to create a temp file for video thumbnail generation operation. Cause: "
                            + e.getMessage(), e);
        }

        return out;
    }

    protected CommandLine getConvertCommandLine(File inputFile, File outputFile, String offset,
            String size) {
        CommandLine cmd = new CommandLine(executablePath);
        cmd.addArguments(parameters);

        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("offset", offset);
        params.put("input", inputFile);
        params.put("output", outputFile);
        params.put("size", size);

        cmd.setSubstitutionMap(params);

        return cmd;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the conversion service
     * 
     * @param activate
     *            set to <code>true</code> to enable the service; to <code>false</code> to disable it and to <code>auto</code> to
     *            auto-detect if the executable is present in the path and than enable the service.
     */
    public void setActivate(String activate) {
        activate = activate != null ? activate.trim() : activate;
        this.enabled = Boolean.valueOf(activate);
        if (!this.enabled && activate != null) {
            this.autodetect = "auto".equalsIgnoreCase(activate)
                    || "autodetect".equalsIgnoreCase(activate);
        }
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    protected JCRNodeWrapper storeThumbnailNode(JCRNodeWrapper fileNode, File thumbnail,
            String thumbnailName) throws RepositoryException, IOException {
        JCRNodeWrapper node = null;

        fileNode.getSession().checkout(fileNode);

        try {
            node = fileNode.getNode(thumbnailName);
        } catch (PathNotFoundException e) {
            node = fileNode.addNode(thumbnailName, Constants.JAHIANT_RESOURCE);
            node.addMixin(Constants.JAHIAMIX_IMAGE);
        }

        if (node.hasProperty(Constants.JCR_DATA)) {
            node.getProperty(Constants.JCR_DATA).remove();
        }

        Binary b = null;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(thumbnail));
            b = new BinaryImpl(is);
            node.setProperty(Constants.JCR_DATA, b);
        } finally {
            if (b != null) {
                b.dispose();
            }
            IOUtils.closeQuietly(is);
        }
        node.setProperty(Constants.JCR_MIMETYPE, "image/jpeg");
        Calendar lastModified = Calendar.getInstance();
        node.setProperty(Constants.JCR_LASTMODIFIED, lastModified);
        fileNode.setProperty(Constants.JCR_LASTMODIFIED, lastModified);

        return node;
    }
}
