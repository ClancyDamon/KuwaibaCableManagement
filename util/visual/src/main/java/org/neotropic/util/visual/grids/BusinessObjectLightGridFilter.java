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
package org.neotropic.util.visual.grids;

import java.util.HashMap;
import java.util.List;

/**
 * Filter for Grids that contains BusinessObjectLights
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BusinessObjectLightGridFilter {
    /**
     * a cached list of last searched filters
     */
    private List<BusinessObjectLightGridFilter> searchHistory;
    /**
     * the current searched filters
     */
    private String current;
    /**
     * _uuid of the object
     */
    private String _uui;
    /**
     * businessObjectName of the objects to filter
     */
    private String businessObjectName;
    /**
     * class businessObjectName to filter
     */
    private String className;
    /**
     * attributes of to filter
     */
    private HashMap<String, String> attributes;

    public List<BusinessObjectLightGridFilter> getSearchHistory() {
        return searchHistory;
    }

    public void setSearchHistory(List<BusinessObjectLightGridFilter> searchHistory) {
        this.searchHistory = searchHistory;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getUui() {
        return _uui;
    }

    public void setUui(String _uui) {
        this._uui = _uui;
    }

    public String getBusinessObjectName() {
        return businessObjectName;
    }

    public void setBusinessObjectName(String businessObjectName) {
        this.businessObjectName = businessObjectName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
}
