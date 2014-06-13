/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.dm.viewer.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.api.Constants;
import org.jahia.dm.DocumentOperationException;
import org.jahia.dm.viewer.DocumentViewerService;
import org.jahia.dm.viewer.DocumentViewerServiceAware;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader.ContextInitializedEvent;
import org.jahia.services.transform.DocumentConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationListener;

/**
 * The document SWF view generation service that uses SWFTools.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentViewerServiceImpl implements DocumentViewerService,
        ApplicationListener<ContextInitializedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentViewerServiceImpl.class);

    private DocumentConverterService documentConverter;

    private PDF2SWFConverterService pdf2SWFConverter;

    private String[] supportedDocumentFormats;

    public boolean canHandle(JCRNodeWrapper fileNode) throws RepositoryException {
        if (!isEnabled() || supportedDocumentFormats == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Document viewer service is disabled"
                                + (supportedDocumentFormats == null ? " as no supported document formats are configured"
                                        : "") + ". Skip converting node {}", fileNode.getPath());
            }
            return false;
        }

        return fileNode.isNodeType("nt:file")
                && JCRContentUtils.isMimeTypeGroup(fileNode.getFileContent().getContentType(),
                        supportedDocumentFormats);
    }

    public File convert(File inputPdfFile) throws DocumentOperationException {
        return pdf2SWFConverter.convert(inputPdfFile);
    }

    public boolean convert(File inputPdfFile, File outputSwfFile) throws DocumentOperationException {
        return pdf2SWFConverter.convert(inputPdfFile, outputSwfFile);
    }

    public OutputStream convert(InputStream inputPdfStream) throws DocumentOperationException {
        return pdf2SWFConverter.convert(inputPdfStream);
    }

    public boolean convert(InputStream inputPdfStream, OutputStream outputSwfStream)
            throws DocumentOperationException {
        return pdf2SWFConverter.convert(inputPdfStream, outputSwfStream);
    }

    public boolean createPdfViewForNode(JCRNodeWrapper fileNode) throws RepositoryException,
            DocumentOperationException {
        if (!isEnabled() || !documentConverter.isEnabled()) {
            logger.info("Viewer service is disabled. Skip converting node {}", fileNode.getPath());
            return false;
        }

        long timer = System.currentTimeMillis();

        if (fileNode.isNodeType("nt:file")) {
            File outFile = null;
            try {
                outFile = getAsPDF(fileNode);
                if (outFile != null) {
                    fileNode.getSession().checkout(fileNode);
                    JCRNodeWrapper pdfNode = null;
                    try {
                        pdfNode = fileNode.getNode("pdfView");
                    } catch (PathNotFoundException e) {
                        if (!fileNode.isNodeType("jmix:pdfDocumentView")) {
                            fileNode.addMixin("jmix:pdfDocumentView");
                        }
                        pdfNode = fileNode.addNode("pdfView", "nt:resource");
                    }

                    BufferedInputStream convertedStream = new BufferedInputStream(
                            new FileInputStream(outFile));
                    try {
                        if (pdfNode.hasProperty(Constants.JCR_DATA)) {
                            pdfNode.getProperty(Constants.JCR_DATA).remove();
                        }
                        pdfNode.setProperty(Constants.JCR_DATA, new BinaryImpl(convertedStream));
                        pdfNode.setProperty(Constants.JCR_MIMETYPE, "application/pdf");
                        Calendar lastModified = Calendar.getInstance();
                        pdfNode.setProperty(Constants.JCR_LASTMODIFIED, lastModified);
                        fileNode.getSession().save();
                    } finally {
                        IOUtils.closeQuietly(convertedStream);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created PDF view for node {} in {} ms", fileNode.getPath(),
                                System.currentTimeMillis() - timer);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                FileUtils.deleteQuietly(outFile);
            }
        } else {
            logger.warn("Path should correspond to a file node. Skipping node {}",
                    fileNode.getPath());
        }

        return true;
    }

    public boolean createViewForNode(JCRNodeWrapper fileNode) throws RepositoryException,
            DocumentOperationException {
        if (!isEnabled() || supportedDocumentFormats == null) {
            logger.info(
                    "Viewer service is disabled"
                            + (supportedDocumentFormats == null ? " as no supported document formats are configured"
                                    : "") + ". Skip converting node {}", fileNode.getPath());
            return false;
        }

        long timer = System.currentTimeMillis();

        if (fileNode.isNodeType("nt:file")
                && JCRContentUtils.isMimeTypeGroup(fileNode.getFileContent().getContentType(),
                        supportedDocumentFormats)) {
            String sourceContentType = fileNode.getFileContent().getContentType();
            File inFile = null;
            boolean sourceAvailable = true;
            try {
                if (JCRContentUtils.isMimeTypeGroup(sourceContentType, "pdf")) {
                    inFile = File.createTempFile("doc-viewer-source", null);
                    JCRContentUtils.downloadFileContent(fileNode, inFile);
                } else {
                    inFile = getAsPDF(fileNode);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                sourceAvailable = false;
            } finally {
                if (!sourceAvailable) {
                    FileUtils.deleteQuietly(inFile);
                }
            }

            if (inFile == null) {
                return false;
            }

            File outFile = null;
            try {
                outFile = pdf2SWFConverter.convert(inFile);
                if (outFile != null) {
                    fileNode.getSession().checkout(fileNode);
                    JCRNodeWrapper swfNode = null;
                    try {
                        swfNode = fileNode.getNode("swfView");
                    } catch (PathNotFoundException e) {
                        if (!fileNode.isNodeType("jmix:swfDocumentView")) {
                    		fileNode.addMixin("jmix:swfDocumentView");
                        }
                        
                        swfNode = fileNode.addNode("swfView", "nt:resource");
                    }

                    BufferedInputStream convertedStream = new BufferedInputStream(
                            new FileInputStream(outFile));
                    try {
                        if (swfNode.hasProperty(Constants.JCR_DATA)) {
                            swfNode.getProperty(Constants.JCR_DATA).remove();
                        }
                        swfNode.setProperty(Constants.JCR_DATA, new BinaryImpl(convertedStream));
                        swfNode.setProperty(Constants.JCR_MIMETYPE, "application/x-shockwave-flash");
                        Calendar lastModified = Calendar.getInstance();
                        swfNode.setProperty(Constants.JCR_LASTMODIFIED, lastModified);
                        fileNode.getSession().save();
                        // Handle version after thumbnail creation
                        VersionManager vm = fileNode.getSession().getWorkspace().getVersionManager();
                        VersionIterator vi = vm.getVersionHistory(fileNode.getPath()).getAllLinearVersions();
                        Version current = null;
                        while (vi.hasNext()) {
                            current = (Version) vi.next();
                        }
                        if (current != null ) {
                            String label = null;
                            for (String l :vm.getVersionHistory(fileNode.getPath()).getVersionLabels(current)) {
                                if (l.startsWith(fileNode.getSession().getWorkspace().getName() + "_uploaded_at_")) {
                                    label = l;
                                    break;
                                }
                            }
                            if (label != null) {
                                Version v = vm.checkpoint(fileNode.getPath());
                                vm.getVersionHistory(fileNode.getPath()).addVersionLabel(v.getName(),label,true);
                            }
                        }

                    } finally {
                        IOUtils.closeQuietly(convertedStream);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created SWF view for node {} in {} ms", fileNode.getPath(),
                                System.currentTimeMillis() - timer);
                    }
                }
            } catch (LockException e)  {
            	logger.warn("Document preview cannot be generated because this node is locked (or archived): " + fileNode.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                FileUtils.deleteQuietly(inFile);
                FileUtils.deleteQuietly(outFile);
            }
        } else {
            logger.warn("Path should correspond to a file node with one"
                    + " of the supported formats {}. Skipping node {}", supportedDocumentFormats,
                    fileNode.getPath());
        }

        return true;
    }

    protected File getAsPDF(JCRNodeWrapper fileNode) {
        if (documentConverter == null || !documentConverter.isEnabled()) {
            logger.info(
                    "Document converter service is not enabled. Cannot convert document {} into a PDF. Skip creating SWF view.",
                    fileNode.getPath());
            return null;
        }

        File pdf = null;
        long timer = System.currentTimeMillis();
        File inFile = null;
        try {
            inFile = File.createTempFile("doc-viewer-source", null);
            JCRContentUtils.downloadFileContent(fileNode, inFile);
            pdf = documentConverter.convert(inFile, fileNode.getFileContent().getContentType(),
                    "application/pdf");
        } catch (IOException e) {
            throw new DocumentOperationException("Error occurred downloading the source document "
                    + fileNode.getPath() + " into a temporary file " + inFile
                    + " for generating an SWF view", e);
        } finally {
            FileUtils.deleteQuietly(inFile);

            if (pdf != null && logger.isDebugEnabled()) {
                logger.debug("Converted document {} into a PDF file in {} ms", fileNode.getPath(),
                        System.currentTimeMillis() - timer);
            }
        }

        return pdf;
    }

    public boolean isEnabled() {
        return pdf2SWFConverter != null && pdf2SWFConverter.isEnabled();
    }

    public void onApplicationEvent(ContextInitializedEvent event) {
        for (DocumentViewerServiceAware bean : BeanFactoryUtils.beansOfTypeIncludingAncestors(
                event.getContext(), DocumentViewerServiceAware.class).values()) {
            bean.setDocumentViewerService(this);
        }
    }

    public void setDocumentConverter(DocumentConverterService documentConverter) {
        this.documentConverter = documentConverter;
    }

    public void setPDF2SWFConverter(PDF2SWFConverterService service) {
        pdf2SWFConverter = service;
    }

    public void setSupportedDocumentFormats(String[] supportedDocumentFormats) {
        this.supportedDocumentFormats = supportedDocumentFormats;
    }

}
