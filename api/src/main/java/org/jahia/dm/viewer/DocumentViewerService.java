/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.dm.viewer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.RepositoryException;

import org.jahia.dm.DocumentOperationException;
import org.jahia.services.Serviceable;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Document to SWF converter API.
 * 
 * @author Sergiy Shyrkov
 */
public interface DocumentViewerService extends Serviceable {

    /**
     * Returns <code>true</code> if an SWF can be generated for the supplied document, i.e. the document viewer service is enabled and the
     * document format satisfies the requirements.
     * 
     * @param fileNode
     *            the document node to generate preview for
     * @return <code>true</code> if an SWF can be generated for the supplied document, i.e. the document viewer service is enabled and the
     *         document format satisfies the requirements
     * @throws RepositoryException
     */
    boolean canHandle(JCRNodeWrapper fileNode) throws RepositoryException;

    /**
     * Converts the provided PDF input file into an SWF file.
     * 
     * @param inputPdfFile
     *            the source file
     * @return the SFW file with the converted content
     * @throws DocumentOperationException
     *             in case of a conversion error
     */
    File convert(File inputPdfFile) throws DocumentOperationException;

    /**
     * Converts the provided PDF input file into an SWF file.
     * 
     * @param inputPdfFile
     *            the source file
     * @param outputSwfFile
     *            the output file to store converted SWF content into
     * @return <code>true</code> if the conversion succeeded
     * @throws DocumentOperationException
     *             in case of a conversion error
     */
    boolean convert(File inputPdfFile, File outputSwfFile) throws DocumentOperationException;

    /**
     * Converts the provided PDF input stream into an SWF output stream.
     * 
     * @param inputPdfStream
     *            the source PDF input stream
     * @return the output stream with the converted SWF content
     * @throws DocumentOperationException
     *             in case of a conversion error
     */
    OutputStream convert(InputStream inputPdfStream) throws DocumentOperationException;

    /**
     * Converts the provided PDF input stream into an SWF output stream.
     * 
     * @param inputPdfStream
     *            the source PDF input stream
     * @param outputSwfStream
     *            the output stream to store the converted SWF content into
     * @return <code>true</code> if the conversion succeeded
     * @throws DocumentOperationException
     *             in case of a conversion error
     */
    boolean convert(InputStream inputPdfStream, OutputStream outputSwfStream)
            throws DocumentOperationException;

    /**
     * Creates the PDF view for the specified file node.
     * 
     * @param fileNode
     *            the node to create a view for
     * @return <code>true</code> if the view was successfully created; returns <code>false</code> if e.g. the service is disabled or the
     *         document cannot be converted to the required format
     * @throws RepositoryException
     *             in case of an error
     * @throws DocumentOperationException
     *             in case of a document conversion error
     * 
     */
    boolean createPdfViewForNode(JCRNodeWrapper fileNode) throws RepositoryException,
            DocumentOperationException;

    /**
     * Creates the SWF view for the specified file node.
     * 
     * @param fileNode
     *            the node to create a view for
     * @return <code>true</code> if the view was successfully created; returns <code>false</code> if e.g. the service is disabled or the
     *         document cannot be converted to the required format
     * @throws RepositoryException
     *             in case of an error
     * @throws DocumentOperationException
     *             in case of a document conversion error
     * 
     */
    boolean createViewForNode(JCRNodeWrapper fileNode) throws RepositoryException,
            DocumentOperationException;
}
