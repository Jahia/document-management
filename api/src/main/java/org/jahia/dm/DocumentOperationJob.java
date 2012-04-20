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
package org.jahia.dm;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract background task for document operations.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class DocumentOperationJob extends BackgroundJob {

    public static final String JOB_UUID = "uuid";

    public static final String JOB_WORKSPACE = "workspace";

    private static final Logger logger = LoggerFactory.getLogger(DocumentOperationJob.class);

    /**
     * Does the execution of the operation on the provided document node
     * 
     * @param documentNode
     *            the document node to execute the operation on
     * @param jobExecutionContext
     *            the background job data
     * @throws Exception
     *             in case of a rule error
     */
    protected abstract void doOperation(JCRNodeWrapper documentNode,
            JobExecutionContext jobExecutionContext) throws Exception;

    @Override
    public void executeJahiaJob(final JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        final String uuid = (String) data.get(JOB_UUID);
        final String workspace = StringUtils.defaultIfEmpty((String) data.get(JOB_WORKSPACE),
                Constants.EDIT_WORKSPACE);
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace,
                new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                        JCRNodeWrapper node = null;
                        try {
                            node = session.getNodeByIdentifier(uuid);
                            doOperation(node, jobExecutionContext);
                        } catch (ItemNotFoundException e) {
                            logger.warn(
                                    "Unable to find node with ID {} in {} workspace. Skipp executing a document operation.",
                                    uuid, workspace);
                        } catch (Exception e) {
                            logger.error(
                                    "Error executing operation on the document node "
                                            + node.getPath(), e);
                        }

                        return Boolean.TRUE;
                    }
                });
    }

}
