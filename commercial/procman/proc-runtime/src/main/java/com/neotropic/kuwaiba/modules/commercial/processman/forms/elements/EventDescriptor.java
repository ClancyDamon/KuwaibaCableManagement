/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

/**
 * @param <T> Type old/new value
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EventDescriptor<T> {
    private String eventName;
    private String propertyName;
    private T oldValue;
    private T newValue;
    
    public EventDescriptor() {
    }
    
    public EventDescriptor(String eventName) {
        this.eventName = eventName;
    }
    
    public EventDescriptor(String eventName, String propertyName) {
        this(eventName);
        this.propertyName = propertyName;
    }
    
    public EventDescriptor(String eventName, String propertyName, T newValue, T oldValue) {
        this(eventName, propertyName);
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String name) {
        this.eventName = name;
    }
    
    public T getNewValue() {
        return newValue;
    }
    
    public void setNewValue(T newValue) {
        this.newValue = newValue;        
    }
    
    public T getOldValue() {
        return oldValue;        
    }
    
    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }
}
