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
package org.jahia.modules.dm.thumbnails.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import org.jahia.dm.DocumentOperationException;
import org.jahia.services.Serviceable;

/**
 * Creates images for PDF document pages.
 * 
 * @author Sergiy Shyrkov
 */
public interface PDF2ImageConverter extends Serviceable {

    /**
     * Generates an image for the first page of the specified document.
     * 
     * @param pdfFile
     *            the PDF file to generate image for
     * @param pageNumber
     *            the page number to generate the image for
     * @return the generated image for the specified page
     * @throws DocumentOperationException
     *             if the document conversion exception occurs
     */
    BufferedImage getImageOfPage(File pdfFile, int pageNumber) throws DocumentOperationException;

    /**
     * Generates an image for the first page of the specified document.
     * 
     * @param pdfInputStream
     *            the input stream of the PDF document to generate image for
     * @param pageNumber
     *            the page number to generate the image for
     * @return the generated image for the specified page
     * @throws DocumentOperationException
     *             if the document conversion exception occurs
     */
    BufferedImage getImageOfPage(InputStream pdfInputStream, int pageNumber)
            throws DocumentOperationException;
}