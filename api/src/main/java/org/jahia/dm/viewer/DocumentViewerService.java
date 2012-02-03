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

package org.jahia.dm.viewer;

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
    boolean createView(JCRNodeWrapper fileNode) throws RepositoryException,
            DocumentOperationException;
}
