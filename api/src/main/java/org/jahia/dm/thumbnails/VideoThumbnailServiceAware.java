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
package org.jahia.dm.thumbnails;

/**
 * Should be implemented by services (Spring-managed beans), wishing to get an instance of {@link VideoThumbnailService} to be automatically
 * injected as a dependency.
 * 
 * @author Cédric Mailleux
 * @author Sergiy Shyrkov
 */
public interface VideoThumbnailServiceAware {

    /**
     * Injects an instance of the {@link VideoThumbnailService} into the Spring bean.
     * 
     * @param service
     *            an instance of the video thumbnails generation service
     */
    void setVideoThumbnailService(VideoThumbnailService service);

}
