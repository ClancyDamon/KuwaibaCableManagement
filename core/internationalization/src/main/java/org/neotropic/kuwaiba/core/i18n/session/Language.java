/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.i18n.session;

/**
 * A simple model class that represents a language that can be picked to be used in the 
 * presentation layer.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Language {
    /**
     * Language name.
     */
    private String name;
    /**
     * Internationalization code (ES, EN, PT, etc).
     */
    private String internationalizationCode;
    /**
     * Localization code (es, us, gb, br, etc).
     */
    private String localizationCode;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternationalizationCode() {
        return internationalizationCode;
    }

    public void setInternationalizationCode(String internationalizationCode) {
        this.internationalizationCode = internationalizationCode;
    }

    public String getLocalizationCode() {
        return localizationCode;
    }

    public void setLocalizationCode(String localizationCode) {
        this.localizationCode = localizationCode;
    }
}

