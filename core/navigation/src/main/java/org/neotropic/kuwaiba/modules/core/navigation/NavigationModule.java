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
package org.neotropic.kuwaiba.modules.core.navigation;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopySpecialBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveSpecialBusinessObjectActionVisual;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleSpecialBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewSpecialBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewSpecialBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ReleaseFromVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.AttachmentExplorer;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.AuditTrailExplorer;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.RelationshipExplorer;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.SpecialChildrenExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The definition of the Navigation module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NavigationModule extends AbstractModule {
    /**
     * The module id.
     */
    public static final String MODULE_ID = "navman"; 
    /**
     * translation service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualAction actNewObj;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewSpecialBusinessObjectVisualAction actNewSpecialObj;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DefaultDeleteBusinessObjectVisualAction actDeleteObj;
    /**
     * Reference to the action that creates a new Business Object from a template.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction actNewObjFromTemplate;
    /**
     * Reference to the action that creates a new special business object from a template.
     */
    @Autowired
    private NewSpecialBusinessObjectFromTemplateVisualAction actNewSpecialObjFromTemplate;
    /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction actNewMultipleObj;
    /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private NewMultipleSpecialBusinessObjectsVisualAction actNewMultipleSpecialObj;
    /**
     * Reference to the action that copy a business object to another business object.
     */
    @Autowired
    private CopyBusinessObjectVisualAction actCopyBusinessObject;
    /**
     * Reference to the action that copy special a business object to another business object.
     */
    @Autowired
    private CopySpecialBusinessObjectVisualAction actCopySpecialBusinessObject;
    /**
     * Reference to the action that move to business object to another business object.
     */
    @Autowired
    private MoveBusinessObjectVisualAction actMoveBusinessObject;
    /**
     * Reference to the action that move special to business object to another business object.
     */
    @Autowired
    private MoveSpecialBusinessObjectActionVisual actMoveSpecialBusinessObject;
    /**
     * Reference to the action that release a business object from other business object.
     */
    @Autowired
    private ReleaseFromVisualAction actReleaseFrom;
    /**
     * Reference to the action that manage Attachments for an object.
     */
    @Autowired
    private ManageAttachmentsVisualAction actManageAttachmentsObject;
    /**
     * The explorer that shows the list of file attachments related to an object.
     */
    @Autowired
    private AttachmentExplorer attachmentExplorer;
    /**
     * The explorer that shows the list of special children of an object.
     */
    @Autowired
    private SpecialChildrenExplorer specialChildrenExplorer;
    /**
     * The explorer that show the list of relationships of an object.
     */
    @Autowired
    private RelationshipExplorer relationshipsExplorer;
    /**
     * The explorer that show the audit trail of an object.
     */
    @Autowired 
    private AuditTrailExplorer auditTrailExplorer;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry actionRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the list that contains the object explorers registered by every module.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    
    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        this.actionRegistry.registerAction(MODULE_ID, actNewObj);
        this.actionRegistry.registerAction(MODULE_ID, actNewObjFromTemplate);
        this.actionRegistry.registerAction(MODULE_ID, actNewMultipleObj);
        this.actionRegistry.registerAction(MODULE_ID, actNewSpecialObj);
        this.actionRegistry.registerAction(MODULE_ID, actNewSpecialObjFromTemplate);
        this.actionRegistry.registerAction(MODULE_ID, actNewMultipleSpecialObj);
        this.actionRegistry.registerAction(MODULE_ID, actDeleteObj);
        this.actionRegistry.registerAction(MODULE_ID, actCopyBusinessObject);
        this.actionRegistry.registerAction(MODULE_ID, actMoveBusinessObject);
        this.actionRegistry.registerAction(MODULE_ID, actCopySpecialBusinessObject);
        this.actionRegistry.registerAction(MODULE_ID, actMoveSpecialBusinessObject);
        this.actionRegistry.registerAction(MODULE_ID, actManageAttachmentsObject);
        this.advancedActionsRegistry.registerAction(MODULE_ID, actReleaseFrom);
        // The explorers exposed by this module
        this.explorerWidgetRegistry.registerWidget(attachmentExplorer);
        this.explorerWidgetRegistry.registerWidget(specialChildrenExplorer);
        this.explorerWidgetRegistry.registerWidget(relationshipsExplorer);
        this.explorerWidgetRegistry.registerWidget(auditTrailExplorer);
        // Now the module itself
        this.moduleRegistry.registerModule(this);
    }
   
    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.navigation.description");
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }
    
     @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;        
    }

    @Override
    public int getCategory() {
        return CATEGORY_NAVIGATION;
    }
}