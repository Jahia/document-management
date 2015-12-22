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