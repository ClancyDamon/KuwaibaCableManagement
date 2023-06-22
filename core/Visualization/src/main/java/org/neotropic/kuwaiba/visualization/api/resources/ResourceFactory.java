/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.visualization.api.resources;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.resources.AbstractResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A factory class that builds and/or caches resources (mostly icons and backgrounds).
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Orlando Paz {@literal <Orlando.paz@kuwaiba.org>}
 */
@Service
public class ResourceFactory extends AbstractResourceFactory {
    /**
     * Default icon width (used in views)
     */
    public static final int DEFAULT_ICON_WIDTH = 24;
    /**
     * Default icon height (used in views)
     */
    public static final int DEFAULT_ICON_HEIGHT = 24;
    /**
     * Default icon width (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_ICON_WIDTH = 12;
    /**
     * Default icon height (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_ICON_HEIGHT = 12;
    /**
     * Default no icon width (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_NO_ICON_WIDTH = 10;
    /**
     * Default no icon height (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_NO_ICON_HEIGHT = 10;
    /**
     * Large icons cache
     */
    private final HashMap<String, StreamResource> icons;
    /**
     * Small icons cache
     */
    private final HashMap<String, StreamResource> smallIcons;
    /**
     * Default large icons cache
     */
    private final HashMap<Integer, StreamResource> defaultIcons;
    /**
     * Default small icons cache
     */
    private final HashMap<Integer, StreamResource> defaultSmallIcons;
    
    @Autowired
    private  MetadataEntityManager mem;
    
    public ResourceFactory() {
        icons = new HashMap();
        smallIcons = new HashMap();
        defaultIcons = new HashMap();
        defaultSmallIcons = new HashMap();
    }
           
    @Override
    public StreamResource getClassIcon(String className) {
        if (className == null || className.isEmpty() || mem == null) {
            StreamResource icon = buildIcon("default.png", getIcon(Color.black, DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
            VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
            return icon;
        }
//        if (icons.containsKey(className))
//            return icons.get(className);
//        else {
        try {

            ClassMetadata remoteClass = mem.getClass(className);
            byte[] classIcon = remoteClass.getIcon();
            if (classIcon != null && classIcon.length > 0) {
                StreamResource icon = buildIcon(className + ".png", remoteClass.getIcon());
                VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                icons.put(className, icon);
                return icon;
            } else {
                int color = remoteClass.getColor();
                /* if (defaultIcons.containsKey(color)) {  // Caching its temporarily commented for bug with multiple windows accecing to resources
                    return defaultIcons.get(color);
                } else {*/
                StreamResource icon = buildIcon("default" + color + ".png", getIcon(new Color(color), DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
                VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                defaultIcons.put(color, icon);
                return icon;
                //}
            }

        } catch (MetadataObjectNotFoundException ex) {
            if (!Constants.DUMMY_ROOT.equals(className)) {
                Notification.show(ex.getMessage());
            }

            int color = Color.BLACK.getRGB();

            StreamResource icon = buildIcon("default" + color + ".png", getIcon(new Color(color), DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
            VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
            defaultIcons.put(color, icon);
            return icon;
        }
    }
       
    @Override
     public StreamResource getClassSmallIcon(String className) {
        if (className == null || className.isEmpty() || mem == null) {
           StreamResource icon = buildIcon("default.png", getIcon(Color.darkGray, DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
           VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
           return icon;
        }

        try {
            ClassMetadata remoteClass = mem.getClass(className);
            byte[] classIcon = remoteClass.getSmallIcon();
            if (classIcon != null && classIcon.length > 0) {
                StreamResource icon = buildIcon("small" + className + ".png", remoteClass.getSmallIcon());
                VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                smallIcons.put(className, icon);
                return icon;
            } else {
                int color = remoteClass.getColor();
//                if (defaultSmallIcons.containsKey(color))  // Caching its temporarily commented for bug with multiple windows accecing to resources
//                    return defaultSmallIcons.get(color);
//                else {
                StreamResource icon = buildIcon("default" + color + ".png", getIcon(new Color(color), DEFAULT_SMALL_NO_ICON_WIDTH, DEFAULT_SMALL_NO_ICON_HEIGHT));
                VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                defaultSmallIcons.put(color, icon);
                return icon;
//                }
            }

        } catch (MetadataObjectNotFoundException ex) {
            if (!Constants.DUMMY_ROOT.equals(className)) 
                Notification.show(ex.getMessage());

            int color = Color.darkGray.getRGB();

            StreamResource icon = buildIcon("default" + color + ".png", getIcon(new Color(color), DEFAULT_SMALL_ICON_WIDTH, DEFAULT_SMALL_ICON_HEIGHT));
            VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
            defaultSmallIcons.put(color, icon);
            return icon;
        }
     }
     
     /**
      * Builds an icon resource
      * @param color The color of the icon
      * @param width The width of the icon
      * @param height The height of the icon
      * @return The icon
      */
    public StreamResource getRelationshipIcon(Color color, int width, int height) {
            StreamResource icon = buildIcon("defaul" + color.getRGB() + ".png", getIcon(new Color(color.getRGB()), width, height));
            VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
            defaultSmallIcons.put(color.getRGB(), icon);
            return icon;
     }    
     
    /**
     * Builds an icon resource
     * @param name the name of the resource
     * @param icon the icon as byte array
     * @return An icon resource which is not registry
     */
    private StreamResource buildIcon(String name, byte[] icon) {
        return new StreamResource(name, new InputStreamFactory() {
            @Override
            public InputStream createInputStream() {
                return new ByteArrayInputStream(icon);
            }
        });                                
    }
    /**
     * Creates (or retrieves a cached version) of a squared colored icon
     * @param color The color of the icon
     * @param width The width of the icon
     * @param height The height of the icon
     * @return The icon as a byte array
     */
    private byte[] getIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}