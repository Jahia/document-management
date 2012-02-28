/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
 */
package org.jahia.dm.thumbnails;

import java.io.File;

import javax.jcr.RepositoryException;

import org.jahia.dm.DocumentOperationException;
import org.jahia.services.Serviceable;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Video thumbnails generator API.
 * 
 * @author Cédric Mailleux
 * @author Sergiy Shyrkov
 */
public interface VideoThumbnailService extends Serviceable {

    /**
     * Returns <code>true</code> if a thumbnail can be generated for the supplied document, i.e. the thumbnail service is enabled and the
     * document format satisfies the requirements.
     * 
     * @param fileNode
     *            the video node to generate thumbnail for
     * @return <code>true</code> if a thumbnail can be generated for the supplied document, i.e. the thumbnail service is enabled and the
     *         document format satisfies the requirements
     * @throws RepositoryException
     */
    boolean canHandle(JCRNodeWrapper fileNode) throws RepositoryException;

    /**
     * Generates thumbnails for the specified video file node.
     * 
     * @param fileNode
     *            the node to generate thumbnails for
     * @param thumbnailName
     *            the name of the thumbnail node
     * @param offsetSeconds
     *            the input time offset in seconds. Specifying a positive offset means that the corresponding streams are delayed by offset
     *            seconds.
     * @param thumbnailSize
     *            the size of the generated thumbnail (e.g. 640x480)
     * @return <code>true</code> if the thumbnail was successfully created; returns <code>false</code> if e.g. the service is disabled or
     *         the document cannot be converted to the required format
     * @throws RepositoryException
     *             in case of repository operation error
     * @throws DocumentOperationException
     *             in case of a video transformation error
     */
    boolean createThumbnailForNode(JCRNodeWrapper fileNode, String thumbnailName,
            int offsetSeconds, String thumbnailSize) throws RepositoryException,
            DocumentOperationException;

    /**
     * Generates a JPG thumbnail image for the specified video file.
     * 
     * @param videoFile
     *            the video file to generate thumbnail for
     * @param outputFile
     *            the target thumbnail file descriptor
     * @param offsetSeconds
     *            the input time offset in seconds. Specifying a positive offset means that the corresponding streams are delayed by offset
     *            seconds.
     * @param size
     *            the target thumbnail size (e.g. 640x480)
     * @return the file descriptor for the generated thumbnail image
     * @throws DocumentOperationException
     *             if the document conversion exception occurs
     */
    boolean generateThumbnail(File videoFile, File outputFile, int offsetSeconds, String size)
            throws DocumentOperationException;

    /**
     * Generates a JPG thumbnail image for the specified video file.
     * 
     * @param videoFile
     *            the video file to generate thumbnail for
     * @param offsetSeconds
     *            the input time offset in seconds. Specifying a positive offset means that the corresponding streams are delayed by offset
     *            seconds.
     * @param size
     *            the target thumbnail size (e.g. 640x480)
     * @return the file descriptor for the generated thumbnail image
     * @throws DocumentOperationException
     *             if the document conversion exception occurs
     */
    File generateThumbnail(File videoFile, int offsetSeconds, String size)
            throws DocumentOperationException;
}
