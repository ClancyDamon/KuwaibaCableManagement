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

package org.neotropic.util.visual.notifications;

import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * A feedback message displayed after the execution of an action. Subclasses can customize
 * the way they are displayed 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractNotification {
    /**
     * Notification title.
     */
    protected String title;
    /**
     * Short descriptive text.
     */
    protected String text;
    /**
     * What kind of information is intended to be displayed.
     */
    protected NotificationType type;
    /**
     * Reference to the translation service.
     */
    protected TranslationService ts;
    /**
     * An optional string with a more detailed information that normally wouldn't fit inside 
     * the notification window. If this value is different from null, a "details" link should 
     * be provided by the subclass implementation.
     */
    protected String details;

    public AbstractNotification(String title, String text, NotificationType type, TranslationService ts) {
        this.title = title;
        this.text = text;
        this.type = type;
        this.ts = ts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
    
    /**
     * Displays the notification,
     */
    public abstract void open();
    /**
     * Programmatically closes the notification window (might not be supported in all implementations).
     */
    public abstract void close();
    
    public enum NotificationType {
        /**
         * An information message.
         */
        INFO, 
        /**
         * A warning message.
         */
        WARNING,
        /**
         * Error message.
         */
        ERROR
    }
}
