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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Action to create a mirror given the type (single or multiple)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewMirrorAction extends AbstractConnectivityVisualAction {
    private enum MirrorTypes {
        SINGLE("mirror"), //NOI18N
        MILTIPLE("mirrorMultiple"); //NOI18N
        
        private final String type;
        
        private MirrorTypes(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private MirrorTypes mirrorType;
    
    public NewMirrorAction(Connection connection, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super(connection);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }

    @Override
    public Component getComponent() {
        ConfirmDialog wdw = new ConfirmDialog() {
            
            @Override
            public void open() {
                setContentSizeFull();
                
                Label lblHelper = new Label(ts.getTranslatedString("module.connectivity-manager.action.new-mirror.helper-text"));
                
                RadioButtonGroup<MirrorTypes> mirrorTypes = new RadioButtonGroup();
                mirrorTypes.setItems(MirrorTypes.SINGLE, MirrorTypes.MILTIPLE);
                mirrorTypes.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
                mirrorTypes.setRenderer(new TextRenderer<>(item -> {
                    if (item == MirrorTypes.SINGLE)
                        return ts.getTranslatedString("module.connectivity-manager.action.new-mirror.type.single");
                    else if (item == MirrorTypes.MILTIPLE)
                        return ts.getTranslatedString("module.connectivity-manager.action.new-mirror.type.multiple");
                    else
                        return "";
                }));
                mirrorTypes.addValueChangeListener(valueChangeEvent -> 
                    mirrorType = valueChangeEvent.getValue()
                );
                mirrorTypes.setValue(MirrorTypes.SINGLE);
                
                VerticalLayout lytContent = new VerticalLayout(lblHelper, mirrorTypes);
                
                Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> close());
                Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> close());
                HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
                lytFooter.setPadding(false);
                lytFooter.setMargin(false);
                lytFooter.setFlexGrow(1, btnCancel, btnOk);
                
                this.setHeader(ts.getTranslatedString("module.connectivity-manager.action.new-mirror"));
                this.setContent(lytContent);
                this.setFooter(lytFooter);
                
                super.open();
            }
        };
        return wdw;
    }

    @Override
    public boolean execute() {
        try {
            Connection connection = getConnection();
            if (connection != null && connection.getSource() != null && connection.getTarget() != null) {
                BusinessObjectLight source = connection.getSource().getSelectedObject();
                BusinessObjectLight target = connection.getTarget().getSelectedObject();
                
                if (source != null && target != null &&
                    mem.isSubclassOf(Constants.CLASS_GENERICPORT, source.getClassName()) &&
                    mem.isSubclassOf(Constants.CLASS_GENERICPORT, target.getClassName())) {
                    
                    boolean create = false;
                    
                    if (mirrorType == MirrorTypes.SINGLE) {
                        create = bem.getSpecialAttributes(source.getClassName(), source.getId(), MirrorTypes.SINGLE.getType(), MirrorTypes.MILTIPLE.getType()).isEmpty() && 
                            bem.getSpecialAttributes(target.getClassName(), target.getId(), MirrorTypes.SINGLE.getType(), MirrorTypes.MILTIPLE.getType()).isEmpty();
                    } else if (mirrorType == MirrorTypes.MILTIPLE) {
                        create = bem.getSpecialAttribute(source.getClassName(), source.getId(), MirrorTypes.SINGLE.getType()).isEmpty() && 
                            bem.getSpecialAttribute(target.getClassName(), target.getId(), MirrorTypes.SINGLE.getType()).isEmpty();
                    }
                    if (create) {
                        bem.createSpecialRelationship(source.getClassName(), source.getId(), target.getClassName(), target.getId(), mirrorType.getType(), true);
                        return true;
                    }
                }
            }
            return true;
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
            return false;
        }
    }
    
}
