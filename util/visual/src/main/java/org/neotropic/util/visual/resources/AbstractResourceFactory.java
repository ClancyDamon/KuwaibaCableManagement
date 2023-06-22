/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.resources;

import com.vaadin.flow.server.StreamResource;

/**
 * Abstract class that defines the main methods to obtain different resources such as images, icons etc.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public abstract class AbstractResourceFactory {
    
    /**
     * Builds and caches an icon of a given class. 
     * @param className the class name of the icon will be built for
     * @return The cached resource
     */
    public abstract StreamResource getClassIcon(String className);
    
    /**
     * Gets or builds (but doesn't caches) the small icon of the given class name
     * @param className The class name
     * @return The cached resource if it has been previously cached, or a generic black icon otherwise
     */
    public abstract StreamResource getClassSmallIcon(String className);
}
