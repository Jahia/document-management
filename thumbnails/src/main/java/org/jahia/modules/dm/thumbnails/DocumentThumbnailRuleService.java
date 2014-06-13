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
package org.jahia.modules.dm.thumbnails;

import org.drools.core.spi.KnowledgeHelper;
import org.jahia.dm.DocumentOperationJob;
import org.jahia.dm.thumbnails.DocumentThumbnailService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.AddedNodeFact;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Service class for generating document thumbnails from the right-hand-side (consequences) of rules.
 *
 * @author Sergiy Shyrkov
 */
public class DocumentThumbnailRuleService {

    private static Logger logger = LoggerFactory.getLogger(DocumentThumbnailRuleService.class);

    private boolean asBackgroundJob = true;

    private SchedulerService schedulerService;

    private DocumentThumbnailService thumbnailService;

    /**
     * Generates thumbnail of the specified size for the provided document node.
     *
     * @param nodeFact      the node to create a view for
     * @param thumbnailName the name of the thumbnail node
     * @param thumbnailSize the size of the generated thumbnail
     * @param drools        the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void createThumbnail(AddedNodeFact nodeFact, String thumbnailName, int thumbnailSize,
                                KnowledgeHelper drools) throws RepositoryException {
        if (thumbnailService == null || !thumbnailService.isEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Thumbnail generation service is not enabled. Skipping generation for node {}",
                        nodeFact.getPath());
            }
            return;
        } else if (!thumbnailService.canHandle(nodeFact.getNode())) {
            return;
        }

        try {
            if (asBackgroundJob) {
                scheduleAsJob(nodeFact.getNode(), thumbnailName, thumbnailSize);
            } else {
                thumbnailService.createThumbnailForNode(nodeFact.getNode(), thumbnailName, thumbnailSize);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Returns <code>true</code> if the document thumbnails service is enabled.
     *
     * @return <code>true</code> if the document thumbnails service is enabled
     * @throws RepositoryException in case of an error
     */
    public boolean isEnabled() throws RepositoryException {
        return thumbnailService != null && thumbnailService.isEnabled();
    }

    protected void scheduleAsJob(JCRNodeWrapper doc, String thumbnailName, int thumbnailSize)
            throws SchedulerException, RepositoryException {
        // execute as a background job
        JobDetail jobDetail = BackgroundJob.createJahiaJob(
                "Document thumbnail for " + doc.getName(), DocumentThumbnailJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(DocumentOperationJob.JOB_UUID, doc.getIdentifier());
        jobDataMap.put(DocumentOperationJob.JOB_WORKSPACE, doc.getSession().getWorkspace()
                .getName());
        jobDataMap.put(DocumentThumbnailJob.THUMBNAIL_NAME, thumbnailName);
        jobDataMap.put(DocumentThumbnailJob.THUMBNAIL_SIZE, thumbnailSize);

        schedulerService.scheduleJobNow(jobDetail);
    }

    public void setAsBackgroundJob(boolean asBackgorundJob) {
        this.asBackgroundJob = asBackgorundJob;
    }

    public void setDocumentThumbnailService(DocumentThumbnailService service) {
        this.thumbnailService = service;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

}