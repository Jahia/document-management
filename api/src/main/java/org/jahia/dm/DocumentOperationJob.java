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
                                    "Unable to find node with ID {} in {} workspace. Skip executing a document operation.",
                                    uuid, workspace);
                        } catch (Exception e) {
                            logger.error(
                                    "Error executing operation on the document node "
                                            + (node != null ? node.getPath() : uuid), e);
                        }

                        return Boolean.TRUE;
                    }
                });
    }

}
