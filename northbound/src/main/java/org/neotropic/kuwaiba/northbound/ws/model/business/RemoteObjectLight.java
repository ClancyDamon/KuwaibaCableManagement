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
package org.neotropic.kuwaiba.northbound.ws.model.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteValidator;

/**
 * This class is a simple representation of an object. It's used for trees and view. This is jus an entity wrapper
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLight implements Serializable, Comparable<RemoteObjectLight> {
    /**
     * MIME Type used mainly while dragging and dropping nodes representing RemoteObjectLights
     */
    public static final String DATA_TYPE = "object/remote-object-light";
    /**
     * Object's oid
     */
    private String id;
    /**
     * Object's name
     */
    private String name;
    /**
     * Object's class name
     */
    private String className;

    
    /**
     * Misc flags used to give more information about the object (i.e. is it already connected?)
     */
    protected List<RemoteValidator> validators;

    /**
     * Default constructor. Not used
     */
    protected RemoteObjectLight(){}

    public RemoteObjectLight(String className, String oid, String name) {
        this.id = oid;
        this.name = name;
        this.className = className;
        this.validators = new ArrayList<>();
    }

    public RemoteObjectLight(BusinessObjectLight obj){
        this.className = obj.getClassName();
        this.name = obj.getName();
        this.id = obj.getId();
        this.validators = new ArrayList<>();
        obj.getValidators().forEach((aValidator) -> {
            this.validators.add(new RemoteValidator(aValidator.getName(), aValidator.getProperties()));
        });
    }

    public String getClassName() {
        return className;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Validators are flags indicating things about objects. Of course, every instance may have
     * something to expose or not. For instance, a port has an indicator to mark it as "connected physically",
     * but a Building (so far) has nothing to "indicate". This is done in order to avoid a second call to query
     * for a particular information that could affect the performance. I.e:
     * Call 1: getPort (retrieving a LocalObjectLight)
     * Call 2: isThisPortConnected (retrieving a boolean according to a condition)
     *
     * With this method there's only one call
     * getPort (a LocalObjectLight with a flag to indicate that the port is connected)
     *
     * Why not use getPort retrieving a LocalObject? Well, because the condition might be complicated, and
     * it's easier to compute its value at server side. Besides, it can involve complex queries that would require
     * more calls to the webservice
     * @return a list with the validators
     */
    public List<RemoteValidator> getValidators() {
        return this.validators;
    }

    public void addValidator(RemoteValidator newValidator){
        if (this.validators == null)
            this.validators = new ArrayList<>();
        this.validators.add(newValidator);
    }

    public static List<RemoteObjectLight> toRemoteObjectLightArray(List<BusinessObjectLight> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        List<RemoteObjectLight> res = new ArrayList<>();
        for (BusinessObjectLight aRemoteBusinesObjectLight: toBeWrapped)
            res.add(new RemoteObjectLight(aRemoteBusinesObjectLight));

        return res;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        if (obj instanceof RemoteObjectLight) 
            return className.equals(((RemoteObjectLight)obj).getClassName()) && 
                    id.equals(((RemoteObjectLight)obj).getId());
        else
            return false;
    }
            
    @Override
    public String toString() {
        String suffix =  "", prefix = "";
        for (RemoteValidator validator : getValidators()) {
            String suffixProperty = validator.getProperty("suffix");
            if (suffixProperty != null)
                suffix += " " + suffixProperty;
            
            String prefixProperty = validator.getProperty("prefix");
            if (prefixProperty != null)
                prefix += prefixProperty + " ";
        }
        return String.format("%s%s%s [%s]", prefix, name, suffix, className);
    }

    @Override
    public int compareTo(RemoteObjectLight o) {
        return this.name.compareTo(o.getName());
    }
}
