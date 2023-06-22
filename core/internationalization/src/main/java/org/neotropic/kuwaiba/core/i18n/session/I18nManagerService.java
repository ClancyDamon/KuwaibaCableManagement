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

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Manages the changes in the web interface language requested by the user.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class I18nManagerService {
    /**
     * Current interface language.
     */
    private Language currentLanguage;
    /**
     * Listeners to language changes (most likely pages within the same session).
     */
    private final List<I18nListener> i18nListeners= new ArrayList<>();
    
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(Language currentLanguage) {
        this.currentLanguage = currentLanguage;
        this.fireSessionStateChangeEvent(currentLanguage);
    }
    
    public void addI18nListener(I18nListener listener) {
        i18nListeners.add(listener);
    }
    
    public void removeI18nListener(I18nListener listener) {
        i18nListeners.remove(listener);
    }

    private void fireSessionStateChangeEvent(Language currentLanguage) {
        i18nListeners.forEach(obs -> obs.onLanguageChanged(currentLanguage));
    }
}
