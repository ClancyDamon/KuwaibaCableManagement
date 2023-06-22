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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * A modal notification intended to be used to show the details in the notification itself, 
 * instead of providing a link to another window.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DetailedNotification extends AbstractNotification {

    public DetailedNotification(String title, String text, NotificationType type, TranslationService ts) {
        super(title, text, type, ts);
    }
    
    @Override
    public void open() {
        Dialog wdwNotification = new Dialog(new H4(this.title), new Paragraph(this.text));
        wdwNotification.open();
    }

    @Override
    public void close() { }
}