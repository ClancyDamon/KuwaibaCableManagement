/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.components;

import com.neotropic.flow.component.mxgraph.bpmn.BPMNNode;
import com.neotropic.kuwaiba.modules.commercial.processman.ProcessEditorModule;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinition;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ArtifactDefinitionType;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Add or update an artifact.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ArtifactDefinitionDialog extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the Translation Service
     */            
    @Autowired
    private TranslationService ts; 
    /**
     * Reference to the Artifact Editor Dialog
     */
    @Autowired
    private ArtifactEditorDialog artifactEditorDialog;
    private List<ArtifactDefinition> listArtifacts;
    private ArtifactDefinition artifactDefinition;
    private Command commandFormArtifact;
    private Command commandAddFormArtifact;
    /**
     * Object to save the selected task
     */
    private BPMNNode task;
    /**
     * Process Id
     */
    private String processId;
    
    public ArtifactDefinitionDialog() {
        super(ProcessEditorModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("artifacts") && parameters.containsKey("task") && parameters.containsKey("processId")) {
            listArtifacts = (List<ArtifactDefinition>) parameters.get("artifacts");
            task = (BPMNNode) parameters.get("task");
            processId = (String) parameters.get("processId");
            commandFormArtifact = (Command) parameters.get("commandFormArtifact");
            
            ConfirmDialog wdwArtifact = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.processeditor.new-task"),
                    ts.getTranslatedString("module.general.labels.create"));
            wdwArtifact.setDraggable(true);
            wdwArtifact.setResizable(true);
            wdwArtifact.setContentSizeFull();
            wdwArtifact.setWidth("50");

            VerticalLayout lytContent = new VerticalLayout();
            lytContent.setWidthFull();

            ComboBox<ArtifactDefinitionType> cmbArtifactType = new ComboBox(ts.getTranslatedString("module.processeditor-artifact.label.type-artifact"));
            cmbArtifactType.setItems(
                    new ArtifactDefinitionType(ts.getTranslatedString("module.processeditor-artifact.form-type-artifact"), 1)
                    /*new ArtifactDefinitionType(ts.getTranslatedString("module.processeditor-artifact.conditional-type-artifact"), 2),
                    new ArtifactDefinitionType(ts.getTranslatedString("module.processeditor-artifact.attachment-type-artifact"), 3)*/
            );
            cmbArtifactType.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-type-placeholder"));
            cmbArtifactType.setRequiredIndicatorVisible(true);
            cmbArtifactType.setAllowCustomValue(false);
            cmbArtifactType.setVisible(false);
            cmbArtifactType.setWidthFull();
            
            BoldLabel lblName = new BoldLabel(String.format(ts.getTranslatedString("module.processeditor.label-parent-task")
                    , task.getLabel() ==  null ? "" : task.getLabel()));
            lblName.setWidthFull();
            
            TextField txtVersion = new TextField(ts.getTranslatedString("module.general.labels.version"));
            txtVersion.setPlaceholder(ts.getTranslatedString("module.general.messages.field-value-not-empty"));
            txtVersion.setRequiredIndicatorVisible(true);
            txtVersion.setWidthFull();
            txtVersion.setValue("1.0");
                        
            cmbArtifactType.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    if (listArtifacts.size() > 0) {
                        for (ArtifactDefinition artifact : listArtifacts) {
                            if (artifact.getType() == event.getValue().getType()) {
                                artifactDefinition = (ArtifactDefinition) artifact;
                                wdwArtifact.getBtnConfirm().setText(ts.getTranslatedString("module.general.label.action-update"));
                                if (artifactDefinition.getParameters() != null) {
                                    artifactDefinition.getParameters().forEach((k, v) -> {
                                        if (k.equals("version")) //I18N
                                            txtVersion.setValue(v);
                                    });
                                }
                            }
                        }
                    } else {
                        artifactDefinition = new ArtifactDefinition();
                        artifactDefinition.setBpmnNode(task);
                        artifactDefinition.setId(UUID.randomUUID().toString());
                    }
                }
            });
            cmbArtifactType.setValue(new ArtifactDefinitionType(ts.getTranslatedString("module.processeditor-artifact.form-type-artifact"), 1));
            
            wdwArtifact.getBtnConfirm().addClickListener(event -> {
                if (cmbArtifactType.getValue() == null)
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            ts.getTranslatedString("module.processeditor.editor-form-property-editor-type-placeholder"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else if (txtVersion.getValue() == null || txtVersion.getValue().trim().isEmpty())
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                    ts.getTranslatedString("module.general.labels.version")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else {
                    launchArtefactEditorDialog(cmbArtifactType.getValue().getType(), txtVersion.getValue());
                    wdwArtifact.close();
                }
            });

            lytContent.add(lblName, txtVersion);
            wdwArtifact.setContent(lytContent);
            return wdwArtifact;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "artifacts")));
    }

    private void launchArtefactEditorDialog(int artifactType, String version) {
        commandAddFormArtifact = () -> {
            listArtifacts.add(artifactDefinition);
            commandFormArtifact.execute();
        };
        
        HashMap parameters = new HashMap();
        parameters.put("version", version);
        parameters.put("definition", artifactDefinition.getId() + ".xml");
        artifactDefinition.setType(artifactType);
        artifactDefinition.setParameters(parameters);
        
        this.artifactEditorDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("artifact", artifactDefinition),
                new ModuleActionParameter("processId", processId),
                new ModuleActionParameter("commandFormArtifact", commandAddFormArtifact)
        )).open();
    }
    
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}