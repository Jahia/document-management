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
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
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
 *
 */
package org.jahia.modules.dm.thumbnails.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.api.Constants;
import org.jahia.dm.DocumentOperationException;
import org.jahia.dm.thumbnails.DocumentThumbnailService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.JahiaImageService;
import org.jahia.services.image.JahiaImageService.ResizeType;
import org.jahia.services.transform.DocumentConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The document thumbnail generation service.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentThumbnailServiceImpl implements DocumentThumbnailService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentThumbnailServiceImpl.class);

    private DocumentConverterService documentConverter;

    private boolean enabled = true;

    private JahiaImageService imageService;

    private PDF2ImageConverter pdf2ImageConverter;

    private String[] supportedDocumentFormats;
    
    private boolean usePNGForThumbnailImage = true;

    public boolean canHandle(JCRNodeWrapper fileNode) throws RepositoryException {
        if (!isEnabled() || supportedDocumentFormats == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Thumbnails service is disabled"
                                + (supportedDocumentFormats == null ? " as no supported document formats are configured"
                                        : "") + ". Skip converting node {}", fileNode.getPath());
            }
            return false;
        }

        boolean canHandle = false;
        if (fileNode.isNodeType("nt:file")) {
            String mimeType = fileNode.getFileContent().getContentType();
            canHandle = JCRContentUtils.isMimeTypeGroup(mimeType, supportedDocumentFormats);
            if (canHandle && !JCRContentUtils.isMimeTypeGroup(mimeType, "pdf")) {
                // if the document is not a PDF and the document converter service is not enabled, we cannot handle the file
                canHandle = documentConverter != null && documentConverter.isEnabled();
            }
        }
        return canHandle;
    }

    public boolean createThumbnailForNode(JCRNodeWrapper fileNode, String thumbnailName,
            int thumbnailSize) throws RepositoryException, DocumentOperationException {
        if (!canHandle(fileNode)) {
            return false;
        }

        long timer = System.currentTimeMillis();

        JCRNodeWrapper thumbNode = null;

        BufferedImage image = null;
        BufferedImage thumbnail = null;
        try {
            image = getImageOfFirstPageForNode(fileNode);

            if (image != null) {
                thumbnail = imageService.resizeImage(image, thumbnailSize, thumbnailSize, ResizeType.ADJUST_SIZE);
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
            if (image != null) {
                image.flush();
            }
            if (thumbnail != null) {
                thumbnail.flush();
            }
        }

        return thumbNode != null;
    }

    public BufferedImage getImageOfFirstPageForNode(JCRNodeWrapper fileNode)
            throws RepositoryException, DocumentOperationException {
        BufferedImage image = null;

        long timer = System.currentTimeMillis();
        String sourceContentType = fileNode.getFileContent().getContentType();
        InputStream pdfInputStream = null;
        File pdfFile = null;
        try {
            if (JCRContentUtils.isMimeTypeGroup(sourceContentType, "pdf")) {
                pdfInputStream = fileNode.getFileContent().downloadFile();
            } else {
                if (documentConverter == null || !documentConverter.isEnabled()) {
                    logger.info("Document conversion service is not enabled."
                            + " Cannot convert node {} into a PDF. Skip generating image.",
                            fileNode.getPath());
                    return null;
                } else {
                    long timerPdf = System.currentTimeMillis();
                    File inFile = null;
                    try {
                        inFile = File.createTempFile("doc-thumbnail-source", null);
                        JCRContentUtils.downloadFileContent(fileNode, inFile);
                        pdfFile = documentConverter.convert(inFile, sourceContentType,
                                "application/pdf");
                        pdfInputStream = new FileInputStream(pdfFile);
                    } catch (IOException e) {
                        throw new DocumentOperationException(
                                "Error occurred trying to generate an image for the first page of the node {}"
                                        + fileNode.getPath(), e);
                    } finally {
                        FileUtils.deleteQuietly(inFile);

                        if (pdfInputStream != null && logger.isDebugEnabled()) {
                            logger.debug("Converted document {} into a PDF document in {} ms",
                                    fileNode.getPath(), System.currentTimeMillis() - timerPdf);
                        }
                    }
                }
            }

            if (pdfInputStream != null) {
                image = pdf2ImageConverter.getImageOfPage(pdfInputStream, 0);
            }
        } finally {
            IOUtils.closeQuietly(pdfInputStream);
            FileUtils.deleteQuietly(pdfFile);

            if (image != null && logger.isDebugEnabled()) {
                logger.debug(
                        "Generated an image for the first page of the document node {} in {} ms",
                        fileNode.getPath(), System.currentTimeMillis() - timer);
            }
        }

        return image;
    }

    public BufferedImage getImageOfPage(File pdfFile, int pageNumber)
            throws DocumentOperationException {
        return pdf2ImageConverter.getImageOfPage(pdfFile, pageNumber);
    }

    public BufferedImage getImageOfPage(InputStream pdfInputStream, int pageNumber)
            throws DocumentOperationException {
        return pdf2ImageConverter.getImageOfPage(pdfInputStream, pageNumber);
    }

    public boolean isEnabled() {
        return enabled && pdf2ImageConverter != null && pdf2ImageConverter.isEnabled();
    }

    public void setDocumentConverter(DocumentConverterService documentConverter) {
        this.documentConverter = documentConverter;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setImageService(JahiaImageService imageService) {
        this.imageService = imageService;
    }

    public void setPDF2ImageConverter(PDF2ImageConverter service) {
        this.pdf2ImageConverter = service;
    }

    public void setSupportedDocumentFormats(String[] supportedDocumentFormats) {
        this.supportedDocumentFormats = supportedDocumentFormats;
    }

    public void setUsePNGForThumbnailImage(boolean usePNGForThumbnailImage) {
        this.usePNGForThumbnailImage = usePNGForThumbnailImage;
    }

    protected JCRNodeWrapper storeThumbnailNode(JCRNodeWrapper fileNode, BufferedImage thumbnail,
            String thumbnailName) throws RepositoryException, IOException {
        JCRNodeWrapper node = null;

        if (thumbnail == null) {
            return null;
        }

        fileNode.getSession().checkout(fileNode);

        try {
            node = fileNode.getNode(thumbnailName);
        } catch (PathNotFoundException e) {
            node = fileNode.addNode(thumbnailName, Constants.JAHIANT_RESOURCE);
            node.addMixin("jmix:size");
        }

        if (node.hasProperty(Constants.JCR_DATA)) {
            node.getProperty(Constants.JCR_DATA).remove();
        }

        Binary b = null;
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(16 * 1024);
            ImageIO.write(thumbnail, usePNGForThumbnailImage ? "png" : "jpeg", os);
            b = new BinaryImpl(os.toByteArray());
            node.setProperty(Constants.JCR_DATA, b);
        } finally {
            os = null;
            if (b != null) {
                b.dispose();
            }
        }
        node.setProperty("j:width", thumbnail.getWidth());
        node.setProperty("j:height", thumbnail.getHeight());
        node.setProperty(Constants.JCR_MIMETYPE, usePNGForThumbnailImage ? "image/png"
                : "image/jpeg");
        Calendar lastModified = Calendar.getInstance();
        node.setProperty(Constants.JCR_LASTMODIFIED, lastModified);
        fileNode.setProperty(Constants.JCR_LASTMODIFIED, lastModified);

        return node;
    }
}
