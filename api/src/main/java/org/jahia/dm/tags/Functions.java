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
package org.jahia.dm.tags;

import java.util.Date;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;

import org.jahia.dm.DocumentManagement;
import org.jahia.dm.viewer.DocumentViewerService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

/**
 * Custom functions, which are exposed into the template scope for document management operations.
 * 
 * @author Sergiy Shyrkov
 */
public final class Functions {

    private static String getPdfViewUrl(JCRNodeWrapper documentNode) throws RepositoryException {
        if (documentNode.isNodeType("jmix:pdfDocumentView") && documentNode.hasNode("pdfView")) {
            String docUrl = documentNode.getUrl();
            return docUrl + (docUrl.contains("?") ? "&amp;t=pdfView" : "?t=pdfView");
        }

        return null;
    }

    /**
     * Returns the URL of the PDF view for the document or <code>null</code> is the view is not available. If the
     * <code>createViewIfNotExists</code> is set to true also forces the creation of the PDF view
     * 
     * @param documentNode
     *            the document node to be viewed
     * @param createViewIfNotExists
     *            if set to true it forces the creation of the SWF view
     * @return the URL of the PDF view for the document or <code>null</code> is the view is not available
     * @throws RepositoryException
     *             in case of a JCR exception
     */
    public static String getPdfViewUrl(final JCRNodeWrapper documentNode,
            boolean createViewIfNotExists) throws RepositoryException {
        String url = getPdfViewUrl(documentNode);

        if (createViewIfNotExists && isViewerEnabled()
                && (url == null || isPdfViewObsolete(documentNode))) {
            final DocumentViewerService viewerService = getViewerService();
            if (!documentNode.hasPermission(Privilege.JCR_MODIFY_PROPERTIES)) {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                        documentNode.getSession().getWorkspace().getName(),
                        documentNode.getSession().getLocale(), new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session)
                                    throws RepositoryException {
                                JCRNodeWrapper systemDocumentNode = session
                                        .getNodeByIdentifier(documentNode.getIdentifier());
                                viewerService.createPdfViewForNode(systemDocumentNode);
                                session.save();
                                return null;
                            }
                        });
            } else {
                viewerService.createPdfViewForNode(documentNode);
            }

            url = getPdfViewUrl(documentNode);
        }

        return url;
    }

    private static DocumentViewerService getViewerService() {
        return DocumentManagement.getInstance().getDocumentViewerService();
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
    public static String getViewUrl(final JCRNodeWrapper documentNode, boolean createViewIfNotExists)
            throws RepositoryException {
        String url = getViewUrl(documentNode);

        if (createViewIfNotExists && isViewable(documentNode)
                && (url == null || isViewObsolete(documentNode))) {
            final DocumentViewerService documentViewService = getViewerService();
            if (!documentNode.hasPermission(Privilege.JCR_MODIFY_PROPERTIES)) {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                        documentNode.getSession().getWorkspace().getName(),
                        documentNode.getSession().getLocale(), new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session)
                                    throws RepositoryException {
                                JCRNodeWrapper systemDocumentNode = session
                                        .getNodeByIdentifier(documentNode.getIdentifier());
                                documentViewService.createViewForNode(systemDocumentNode);
                                session.save();
                                return null;
                            }
                        });
            } else {
                documentViewService.createViewForNode(documentNode);
            }

            url = getViewUrl(documentNode);
        }

        return url;
    }

    /**
     * Checks if the document converter service is running.
     * 
     * @return <code>true</code> if the document converter service is enabled
     */
    public static boolean isConverterEnabled() {
        return DocumentManagement.getInstance().isDocumentConverterServiceEnabled();
    }

    /**
     * Checks if the document is of type PDF.
     * 
     * @return <code>true</code> if the node is a PDF document
     */
    public static boolean isPdf(JCRNodeWrapper node) {
        return node.isFile()
                && JCRContentUtils.isMimeTypeGroup(node.getFileContent().getContentType(), "pdf");
    }

    private static boolean isPdfViewObsolete(JCRNodeWrapper documentNode)
            throws PathNotFoundException, RepositoryException {
        Date docDate = documentNode.getNode("jcr:content").getLastModifiedAsDate();
        Date swfDate = null;
        if (docDate != null && documentNode.hasNode("pdfView")) {
            swfDate = documentNode.getNode("pdfView").getLastModifiedAsDate();
        }

        return docDate != null && swfDate != null && docDate.after(swfDate);
    }

    /**
     * Checks if the document thumbnail service is available and enabled.
     * 
     * @return <code>true</code> if the document thumbnail service is available and enabled
     */
    public static boolean isThumbnailEnabled() {
        return DocumentManagement.getInstance().isDocumentThumbnailServiceEnabled();
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

    /**
     * Checks if the document viewer service is available and enabled.
     * 
     * @return <code>true</code> if the document viewer service is available and enabled
     */
    public static boolean isViewerEnabled() {
        return DocumentManagement.getInstance().isDocumentViewerServiceEnabled();
    }

    private static boolean isViewObsolete(JCRNodeWrapper documentNode)
            throws PathNotFoundException, RepositoryException {
        Date docDate = documentNode.getNode("jcr:content").getLastModifiedAsDate();
        Date swfDate = null;
        if (docDate != null && documentNode.hasNode("swfView")) {
            swfDate = documentNode.getNode("swfView").getLastModifiedAsDate();
        }

        return docDate != null && swfDate != null && docDate.after(swfDate);
    }

    private Functions() {
        super();
    }
}