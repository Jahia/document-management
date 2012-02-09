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

import org.jahia.exceptions.JahiaRuntimeException;

/**
 * Service exception related to document management operations.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentOperationException extends JahiaRuntimeException {

    private static final long serialVersionUID = 1592725525818581452L;

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     */
    public DocumentOperationException(String message) {
        super(message);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     * @param cause
     */
    public DocumentOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param cause
     */
    public DocumentOperationException(Throwable cause) {
        super(cause);
    }

}
