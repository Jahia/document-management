/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.dm.utils;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process related utilities.
 * 
 * @author Sergiy Shyrkov
 */
public final class ProcessUtils {

    private static Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static boolean commandPresent(String executablePath, File workingDir) {
        boolean present = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Checking if the {} is present in the current path", executablePath);
            Map<String, String> env = System.getenv();
            for (Map.Entry<String, String> envVar : env.entrySet()) {
                if ("path".equalsIgnoreCase(envVar.getKey())) {
                    logger.info("Current PATH is: ", envVar.getValue());
                    break;
                }
            }
        }

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(executablePath);
            if (workingDir != null) {
                if (workingDir.exists() || workingDir.mkdirs()) {
                    pb.directory(workingDir);
                }
            }
            process = pb.start();
            present = true;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to execute command " + executablePath
                        + " in the current path. Cause: " + e.getMessage(), e);
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return present;
    }
}
