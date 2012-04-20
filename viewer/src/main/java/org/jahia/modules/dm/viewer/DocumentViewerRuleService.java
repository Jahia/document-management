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

package org.jahia.modules.dm.viewer;

import javax.jcr.RepositoryException;

import org.drools.spi.KnowledgeHelper;
import org.jahia.dm.viewer.DocumentViewerService;
import org.jahia.dm.viewer.DocumentViewerServiceAware;
import org.jahia.services.content.rules.AddedNodeFact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for converting documents from the right-hand-side (consequences) of rules into SWF files.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentViewerRuleService implements DocumentViewerServiceAware {

    private static Logger logger = LoggerFactory.getLogger(DocumentViewerRuleService.class);

    private DocumentViewerService viewerService;

    /**
     * Creates the SWF view for the specified file node.
     * 
     * @param nodeFact
     *            the node to create a view for
     * @param drools
     *            the rule engine helper class
     * @throws RepositoryException
     *             in case of an error
     */
    public void createView(AddedNodeFact nodeFact, KnowledgeHelper drools)
            throws RepositoryException {
        if (viewerService == null || !viewerService.isEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Documen SWF view generation service is not enabled. Skipping generation for node {}",
                        nodeFact.getPath());
            }
            return;
        }
        try {
            viewerService.createViewForNode(nodeFact.getNode());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setDocumentViewerService(DocumentViewerService service) {
        this.viewerService = service;
    }
}