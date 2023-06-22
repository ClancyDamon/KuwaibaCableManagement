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

package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

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
 * more calls to the webservice
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteValidator implements Serializable {
    /**
     * The name of this validator
     */
    private String name;
    /**
     * The properties of this validator. The idea behind this, is that a validator should contain a main value (such us "yes, the port is connected") 
     * but also extra support values relevant mostly for rendering purposes (such as "display the port name red or with a busy icon since is already connected, 
     * or display it orange if since it's not connected, but it's reserved")
     */
    private List<StringPair> properties;

    //No-arg constructor required
    public RemoteValidator() { }

    public RemoteValidator(String name, Properties properties) {
        this.name = name;
        
        this.properties = new ArrayList<>();
        if (properties != null) {
            properties.forEach((key, value) -> {
                this.properties.add(new StringPair((String)key, (String)value));
            });
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StringPair> getProperties() {
        return properties;
    }

    public void setProperties(List<StringPair> properties) {
        this.properties = properties;
    }
    
    /**
     * Gets the value of a property in the current validator (if exists)
     * @param propertyName The name of the property
     * @return The value of the property or null if it doesn't exist
     */
    public String getProperty(String propertyName) {
        try {
            return properties.stream().filter((aProperty) -> {
                return aProperty.getKey().equals(propertyName);
            }).findFirst().get().getValue();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
}
