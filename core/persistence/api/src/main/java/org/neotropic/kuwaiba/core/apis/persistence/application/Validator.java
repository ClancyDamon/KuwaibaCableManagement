/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.Properties;

/**
 * Validators are flags indicating things about objects. Of course, every instance may have
 * something to expose or not. For instance, a port has an indicator to mark it as "connected physically",
 * but a Building (so far) has nothing to "indicate". This is done in order to avoid a second call to query
 * for a particular information that could affect the performance. I.e:
 * Call 1: getPort (retrieving a LocalObjectLight) <br>
 * Call 2: isThisPortConnected (retrieving a boolean according to a condition) <br>
 *
 * With this method there's only one call
 * getPort (a LocalObjectLight with a flag to indicate that the port is connected) <br>
 *
 * Why not use getPort retrieving a LocalObject? Well, because the condition might be complicated, and
 * it's easier to compute its value at server side. Besides, it can involve complex queries that would require
 * more calls to the web service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Validator {
    /**
     * One of the built-in properties indicating the color that should be used to render 
     * the display name (it is recommended to be the text color, but it depends on the renderer) 
     * of the object related to this validator. 
     */
    public static final String PROPERTY_COLOR = "color";
    /**
     * One of the built-in properties indicating the color that should be used to render 
     * the display name (it is recommended to be the background color, but it depends on the renderer) 
     * of the object related to this validator. 
     */
    public static final String PROPERTY_FILLCOLOR = "fill-color";
    /**
     * One of the built-in properties indicating the text that should be prepended to the 
     * display name of the object related to this validator. 
     */
    public static final String PROPERTY_PREFIX = "prefix";
    /**
     * One of the built-in properties indicating the text that should be appended to the 
     * display name of the object related to this validator. 
     */
    public static final String PROPERTY_SUFFIX = "suffix";
    
    /**
     * The name of this validator
     */
    private String name;
    /**
     * The properties of this validator. The idea behind this, is that a validator should contain a main value (such us "yes, the port is connected") 
     * but also extra support values relevant mostly for rendering purposes (such as "display the port name red or with a busy icon since is already connected, 
     * or display it orange if since it's not connected, but it's reserved")
     */
    private Properties properties;

    public Validator(String name, Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
