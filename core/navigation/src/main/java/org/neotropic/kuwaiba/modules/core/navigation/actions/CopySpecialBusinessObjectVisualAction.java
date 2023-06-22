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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.dialog.Dialog;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI of {@link CopySpecialBusinessObjectAction copy special business object action}.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class CopySpecialBusinessObjectVisualAction extends AbstractVisualInventoryAction {
    /**
     * Business object parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject";
    /**
     * Reference to module action.
     */
    @Autowired
    private CopySpecialBusinessObjectAction copySpecialBusinessObjectAction;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    private BusinessObjectLight targetObject;
    
    public CopySpecialBusinessObjectVisualAction() {
        super(NavigationModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        if (businessObject != null) {
            ConfirmDialog wdw = new ConfirmDialog(ts,
                    String.format(ts.getTranslatedString("module.navigation.actions.copy-special-business-object.header"), businessObject),
                    copySpecialBusinessObjectAction.getDisplayName());
            wdw.setModal(false);
            wdw.setDraggable(true);
            wdw.setResizable(true);
            wdw.setWidth("60%");

            BusinessObjectSelector lytContent = new BusinessObjectSelector(ts.getTranslatedString("module.navigation.actions.copy-special-business-object.placeholder"), aem, bem, mem, ts);
            lytContent.addSelectedObjectChangeListener(event -> targetObject = event.getSelectedObject());

            wdw.getBtnConfirm().addClickListener(clickEvent -> {
                if (targetObject != null) {
                    try {
                        List<ClassMetadataLight> possibleChildren = mem.getPossibleSpecialChildren(targetObject.getClassName());
                        for (ClassMetadataLight possibleChild : possibleChildren) {
                            if (businessObject.getClassName().equals(possibleChild.getName())) {
                                ModuleActionParameterSet actionParameters = new ModuleActionParameterSet(
                                        new ModuleActionParameter(CopySpecialBusinessObjectAction.PARAM_TARGET_CLASS, targetObject.getClassName()),
                                        new ModuleActionParameter(CopySpecialBusinessObjectAction.PARAM_TARGET_ID, targetObject.getId()),
                                        new ModuleActionParameter(CopySpecialBusinessObjectAction.PARAM_OBJECT_CLASS, businessObject.getClassName()),
                                        new ModuleActionParameter(CopySpecialBusinessObjectAction.PARAM_OBJECT_ID, businessObject.getId())
                                );
                                try {
                                    ActionResponse actionResponse = copySpecialBusinessObjectAction.getCallback().execute(actionParameters);
                                    actionResponse.put(ActionResponse.ActionType.COPY, targetObject);
                                    actionResponse.put(PARAM_BUSINESS_OBJECT, businessObject);
                                    actionResponse.put(Constants.PROPERTY_PARENT_ID, targetObject.getId());
                                    actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, targetObject.getClassName());

                                    fireActionCompletedEvent(new ActionCompletedEvent(ActionCompletedEvent.STATUS_SUCCESS,
                                            ts.getTranslatedString("module.navigation.actions.copy-special-business-object.visual.success"),
                                            CopySpecialBusinessObjectAction.class, actionResponse));
                                } catch (ModuleActionException ex) {
                                    fireActionCompletedEvent(new ActionCompletedEvent(
                                            ActionCompletedEvent.STATUS_ERROR,
                                            ex.getLocalizedMessage(),
                                            ModuleActionException.class
                                    ));
                                }
                                wdw.close();
                                return;
                            }
                        }
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.navigation.actions.copy-special-business-object.is-not-possible-special-children"), businessObject.toString()),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    } catch (MetadataObjectNotFoundException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR,
                                ex.getLocalizedMessage(),
                                ModuleActionException.class
                        ));
                    }
                }
            });

            wdw.setContent(lytContent);
            return wdw;
        }
        return null;
    }

    @Override
    public AbstractAction getModuleAction() {
        return copySpecialBusinessObjectAction;
    }   
}