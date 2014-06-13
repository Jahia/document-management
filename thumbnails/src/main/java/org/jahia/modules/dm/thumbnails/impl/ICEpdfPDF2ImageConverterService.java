/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
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
 *     ======================================================================================
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
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.dm.thumbnails.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.jahia.dm.DocumentOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates images for PDF document pages using ICEpdf library.
 * 
 * @author Sergiy Shyrkov
 */
public class ICEpdfPDF2ImageConverterService extends AbstractPDF2ImageConverterService {

    private static final Logger logger = LoggerFactory
            .getLogger(ICEpdfPDF2ImageConverterService.class);

    public BufferedImage getImageOfPage(File pdfFile, int pageNumber)
            throws DocumentOperationException {
        BufferedImage image = null;

        long timer = System.currentTimeMillis();

        Document document = null;
        try {
            document = new Document();
            document.setFile(pdfFile.getPath());
            image = (BufferedImage) document.getPageImage(pageNumber, GraphicsRenderingHints.PRINT,
                    Page.BOUNDARY_CROPBOX, 0, 1);
            if (image != null && logger.isDebugEnabled()) {
                logger.debug("Generated an image for the page {} of the file {} in {} ms",
                        new Object[] { pageNumber, pdfFile, (System.currentTimeMillis() - timer) });
            }
        } catch (Exception e) {
            throw new DocumentOperationException(
                    "Error occurred trying to generate an image for the page " + pageNumber
                            + " of the file " + pdfFile, e);
        } finally {
            if (document != null) {
                try {
                    document.dispose();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return image;
    }

    public BufferedImage getImageOfPage(InputStream pdfInputStream, int pageNumber)
            throws DocumentOperationException {
        BufferedImage image = null;

        long timer = System.currentTimeMillis();

        Document document = null;
        try {
            document = new Document();
            document.setInputStream(pdfInputStream, null);
            image = (BufferedImage) document.getPageImage(pageNumber, GraphicsRenderingHints.PRINT,
                    Page.BOUNDARY_CROPBOX, 0, 1);

            if (image != null && logger.isDebugEnabled()) {
                logger.debug(
                        "Generated an image for the page {} of the supplied input stream in {} ms",
                        pageNumber, (System.currentTimeMillis() - timer));
            }
        } catch (Exception e) {
            throw new DocumentOperationException(
                    "Error occurred trying to generate an image for the page " + pageNumber
                            + " of the supplied input stream", e);
        } finally {
            if (document != null) {
                try {
                    document.dispose();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return image;
    }

}
