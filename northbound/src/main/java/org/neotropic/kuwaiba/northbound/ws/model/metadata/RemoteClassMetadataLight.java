/**
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
package org.neotropic.kuwaiba.northbound.ws.model.metadata;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;

/**
 * Same as ClassInfo, but lighter, since it's intended to provide the information to
 * render a node in a view (usually a tree) at client side.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteClassMetadataLight implements Serializable, Comparable<RemoteClassMetadataLight> {
    protected long id;
    protected Boolean _abstract;
    protected Boolean viewable;
    protected Boolean custom;
    protected Boolean inDesign;
    protected Boolean listType;
    protected String className;
    protected String displayName;
    protected String parentClassName;
    protected int color;
    /**
     * 16x16 icon
     */
    protected byte[] smallIcon;

    /**
     * Mandatory parameter-less constructor
     */
    public RemoteClassMetadataLight() {}

    public RemoteClassMetadataLight(ClassMetadataLight myClassLight) {
        this.id = myClassLight.getId();
        this.className = myClassLight.getName();
        this.parentClassName = myClassLight.getParentClassName();
        this.smallIcon = myClassLight.getSmallIcon();
        this._abstract = myClassLight.isAbstract();
        this.displayName = myClassLight.getDisplayName();
        this.viewable = myClassLight.isViewable();
        this.listType = myClassLight.isListType();
        this.custom = myClassLight.isCustom();
        this.inDesign = myClassLight.isInDesign();
        this.color = myClassLight.getColor();
    }

    public RemoteClassMetadataLight (long id, String className, String displayName, boolean viewable, boolean _abstract, boolean custom, 
            boolean inDesign, String parentClassName, boolean listType, byte[] smallIcon, int color){
        this.id = id;
        this.className = className;
        this.displayName = displayName;
        this.viewable = viewable;
        this._abstract = _abstract;
        this.custom = custom;
        this.inDesign = inDesign;
        this.parentClassName = parentClassName;
        this.smallIcon = smallIcon;
        this.listType = listType;
        this.color = color;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public Boolean isAbstract() {
        return _abstract;
    }

    public void setAbstract(Boolean _abstract) {
        this._abstract = _abstract;
    }

    public Boolean isViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }

    public Boolean isCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    public Boolean isInDesign() {
        return inDesign;
    }

    public void setInDesign(Boolean inDesign) {
        this.inDesign = inDesign;
    }

    public Boolean isListType() {
        return listType;
    }

    public void setListType(Boolean listType) {
        this.listType = listType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof RemoteClassMetadataLight))
            return false;
        if (((RemoteClassMetadataLight)obj).getId() == getId())
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 47 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return displayName == null || displayName.trim().isEmpty() ? className : displayName;
    }
    
    @Override
    public int compareTo(RemoteClassMetadataLight o) {
        return this.className.compareTo(o.getClassName());
    }
}
