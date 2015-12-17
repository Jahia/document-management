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
 *     This program is free software; you can redistribute it and/or
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
package org.jahia.modules.dm.thumbnails;

import org.apache.commons.lang.StringUtils;
import org.jahia.dm.DocumentManagement;
import org.jahia.dm.DocumentOperationJob;
import org.jahia.dm.thumbnails.DocumentThumbnailService;
import org.jahia.services.content.JCRNodeWrapper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background task for create a thumbnail for the document.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentThumbnailJob extends DocumentOperationJob {

    public static final String JOB_WORKSPACE = "workspace";

    private static final Logger logger = LoggerFactory.getLogger(DocumentThumbnailJob.class);

    public static final String THUMBNAIL_NAME = "thumbnailName";

    public static final String THUMBNAIL_SIZE = "thumbnailSize";

    protected void doOperation(JCRNodeWrapper documentNode, JobExecutionContext jobExecutionContext)
            throws Exception {
        DocumentThumbnailService service = DocumentManagement.getInstance().getDocumentThumbnailService();
        if (service == null || !service.isEnabled()) {
            logger.info(
                    "Thumbnail generation service is not enabled. Skipping generation of a thumbnail for node {}",
                    documentNode.getPath());
            return;
        }

        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        int intValue = jobDataMap.getIntValue(THUMBNAIL_SIZE);

        service.createThumbnailForNode(documentNode,
                StringUtils.defaultIfBlank(jobDataMap.getString(THUMBNAIL_NAME), "thumbnail"),
                intValue > 0 ? intValue : 150);

        documentNode.getSession().save();
    }
}
