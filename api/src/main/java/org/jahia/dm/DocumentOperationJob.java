/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
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
