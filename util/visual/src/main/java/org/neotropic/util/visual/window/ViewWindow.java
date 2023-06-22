/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.window;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.button.Button;
import java.util.Objects;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Window with a title, content, and a button to close it
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ViewWindow extends ConfirmDialog {
    
    public ViewWindow(String title, TranslationService ts) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(ts);
        setResizable(true);
        setModal(false);
        setDraggable(true);
        addThemeVariants(EnhancedDialogVariant.SIZE_LARGE);
        setContentSizeFull();        
        setHeader(title);
        setFooter(new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close()));
    }
}
