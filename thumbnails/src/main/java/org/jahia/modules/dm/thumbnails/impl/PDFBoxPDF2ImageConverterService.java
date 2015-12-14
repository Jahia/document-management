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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jahia.dm.DocumentOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates images for PDF document pages using PDFBox library.
 * 
 * @author Sergiy Shyrkov
 */
public class PDFBoxPDF2ImageConverterService extends AbstractPDF2ImageConverterService {

    private static final Logger logger = LoggerFactory
            .getLogger(PDFBoxPDF2ImageConverterService.class);

    private int imageType = BufferedImage.TYPE_INT_RGB;

    private int resolution = 96;

    public BufferedImage getImageOfPage(File pdfFile, int pageNumber)
            throws DocumentOperationException {
        BufferedImage image = null;

        long timer = System.currentTimeMillis();

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(pdfFile));
            image = getImageOfPage(is, pageNumber);

            if (image != null && logger.isDebugEnabled()) {
                logger.debug("Generated an image for the page {} of the file {} in {} ms",
                        new Object[] { pageNumber, pdfFile, (System.currentTimeMillis() - timer) });

            }
        } catch (FileNotFoundException e) {
            throw new DocumentOperationException(
                    "Error occurred trying to generate an image for the page " + pageNumber
                            + " of the file " + pdfFile, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return image;
    }

    public BufferedImage getImageOfPage(InputStream pdfInputStream, int pageNumber)
            throws DocumentOperationException {
        BufferedImage image = null;

        long timer = System.currentTimeMillis();

        PDDocument pdfDoc = null;
        try {
            pdfDoc = PDDocument.load(pdfInputStream);
            PDPage page = (PDPage) pdfDoc.getDocumentCatalog().getAllPages().get(pageNumber);
            image = page.convertToImage(imageType, resolution);

            if (image != null && logger.isDebugEnabled()) {
                logger.debug(
                        "Generated an image for the page {} of the supplied input stream in {} ms",
                        pageNumber, (System.currentTimeMillis() - timer));
            }
        } catch (IndexOutOfBoundsException e) {
            logger.warn("No page with the number {} found in the PDF document", pageNumber);
        } catch (IOException e) {
            throw new DocumentOperationException(
                    "Error occurred trying to generate an image for the page " + pageNumber
                            + " of the supplied input stream", e);
        } finally {
            try {
                if (pdfDoc != null) {
                    pdfDoc.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return image;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

}
