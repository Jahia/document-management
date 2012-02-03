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

package org.jahia.modules.dm.viewer.tags;

import javax.jcr.RepositoryException;

import org.jahia.dm.viewer.DocumentViewerService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Custom functions, which are exposed into the template scope for document viewer operations.
 * 
 * @author Sergiy Shyrkov
 */
public final class Functions {

    private static DocumentViewerService getViewerService() {
        return (DocumentViewerService) SpringContextSingleton.getBean("DocumentViewerService");
    }

    private static String getViewUrl(JCRNodeWrapper documentNode) throws RepositoryException {
        if (documentNode.isNodeType("jmix:swfDocumentView") && documentNode.hasNode("swfView")) {
            String docUrl = documentNode.getUrl();
            return docUrl + (docUrl.contains("?") ? "&amp;t=swfView" : "?t=swfView");
        }

        return null;
    }

    /**
     * Returns the URL of the SWF view for the document or <code>null</code> is the view is not available. If the
     * <code>createViewIfNotExists</code> is set to true also forces the creation of the SWF view
     * 
     * @param documentNode
     *            the document node to be viewed
     * @param createViewIfNotExists
     *            if set to true it forces the creation of the SWF view
     * @return the URL of the SWF view for the document or <code>null</code> is the view is not available
     * @throws RepositoryException
     *             in case of a JCR exception
     */
    public static String getViewUrl(JCRNodeWrapper documentNode, boolean createViewIfNotExists)
            throws RepositoryException {
        if (!isViewable(documentNode)) {
            return null;
        }
        String url = getViewUrl(documentNode);

        if (url == null && createViewIfNotExists) {
            DocumentViewerService documentViewService = getViewerService();
            if (documentViewService.isEnabled()) {
                documentViewService.createView(documentNode);
                documentNode.getSession().save();

                url = getViewUrl(documentNode);
            }
        }

        return url;
    }

    /**
     * Checks if the provided node is a valid viewable document, i.e. it either already has an SWF view stored or its MIME type is supported
     * for creating that SWF view.
     * 
     * @param documentNode
     *            the node to be checked
     * @return <code>true</code> if the specified node is a file and it either already has an SWF view stored or its MIME type is supported
     *         for creating that SWF view
     * @throws RepositoryException
     *             in case of a JCR exception
     */
    public static boolean isViewable(JCRNodeWrapper documentNode) throws RepositoryException {
        DocumentViewerService documentViewService = getViewerService();

        return documentViewService != null && documentViewService.canHandle(documentNode);
    }

    private Functions() {
        super();
    }
}