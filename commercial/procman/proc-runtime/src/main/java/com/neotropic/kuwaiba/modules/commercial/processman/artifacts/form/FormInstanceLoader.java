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
package com.neotropic.kuwaiba.modules.commercial.processman.artifacts.form;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractFormInstanceLoader;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FileInformation;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FunctionRunnerException;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormInstanceLoader extends AbstractFormInstanceLoader {
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    public FormInstanceLoader(String formDefinitionsDirectory, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        super(formDefinitionsDirectory, consumerFuncRunnerEx, funcRunnerParams);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }
    
    @Override
    public Object getInventoryObjectPool(String poolId) {
        try {
            return bem.getPool(poolId);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }
    
    @Override
    public BusinessObjectLight getRemoteObjectLight(long classId, String objectId) {
        try {
            ClassMetadata classMetadata = mem.getClass(classId);
            return bem.getObjectLight(classMetadata.getName(), objectId);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }

    @Override
    public ClassMetadata getClassInfoLight(long classId) {
        try {
            return mem.getClass(classId);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }

    @Override
    public Object getAttachment(String name, String path) {
        return new FileInformation(name, path);
    }
    
}
