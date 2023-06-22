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

package org.neotropic.kuwaiba.core.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

/**
 * This service provides I18N support for the application
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class TranslationService {
    /**
     * The current application language.
     */
    private Locale currentLanguage;
    /**
     * Translation bundle with the internationalization keys for the current language.
     */
    private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();;
    /**
     * The list of languages currently supported.
     */
    private final List<Locale> languages = new ArrayList<>();;
    
    public Locale getCurrentLanguage() {
        return currentLanguage;
    }
    
    public Locale getSupportedLanguage(String locale) {
        for (Locale aLocale : getLanguages()) {
            if (aLocale.toString().equalsIgnoreCase(locale) || aLocale.getLanguage().equalsIgnoreCase(locale))
                return aLocale;
        }
        return null;
    }
    
    public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
    }
    /**
     * Sets the current language through a locale.
     * @param locale The locale string. This might be in the form <code>language_VARIANT</code> or simple <code>language</code>. Comparison is case-insensitive. See the contents of 
     * <code>i18n</code> resource folder in this project for currently supported languages. If not found, it will fallback to English.
     * @throws IllegalArgumentException If the locale is not supported.
     */
    public void setCurrentlanguage(String locale) throws IllegalArgumentException {
        if (locale == null)
            throw new IllegalArgumentException(String. format(getTranslatedString("module.general.messages.unsupported-locale"), locale));
        
        Locale supportedLanguage = getSupportedLanguage(locale);
        if (supportedLanguage != null)
            this.currentLanguage = supportedLanguage;
        else
            throw new IllegalArgumentException(String. format(getTranslatedString("module.general.messages.unsupported-locale"), locale));
    }
    
    public List<Locale> getLanguages() {
        return languages;
    }
    
    public String getTranslatedString(String key) {
        try {
            return messageSource.getMessage(key, null, currentLanguage);
        } catch (NoSuchMessageException ex) {
            return key;
        }
    }
    
    public String getTranslatedString(String locale, String key) {
        try {
            Locale supportedLanguage = getSupportedLanguage(locale);
            if (supportedLanguage != null)
                return messageSource.getMessage(key, null, supportedLanguage);
            else
                return key;
        } catch(NoSuchMessageException ex) {
            return key;
        }
    }
    
    public TranslationService() {
        messageSource.addBasenames("i18n/messages");
        // Supported languages 
        Locale enLanguage = new Locale("en", "US");
        languages.add(enLanguage);
        Locale esLanguage = new Locale("es", "CO");
        languages.add(esLanguage);
        Locale ptLanguage = new Locale("pt", "BR");
        languages.add(ptLanguage);
        Locale ruLanguage = new Locale("ru", "RU");
        languages.add(ruLanguage);
        
        this.currentLanguage = enLanguage;
    }
}
