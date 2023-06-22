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
package org.neotropic.kuwaiba.modules.core.favorites.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.favorites.FavoritesModule;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to manage favorites.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class FavoritesVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to Application Entity Manager.
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
     * Reference to the module action to manage favorites.
     */
    @Autowired
    private FavoritesAction favoritesAction;
    /**
     * The visual action to create a new favorite folder.
     */
    @Autowired
    private NewFavoriteVisualAction newFavoriteVisualAction;
    /**
     * The visual action to delete a favorite folder.
     */
    @Autowired
    private DeleteFavoriteVisualAction deleteFavoriteVisualAction;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry coreActionsRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered explorers.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the action that creates a new Business Object from a template.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DefaultDeleteBusinessObjectVisualAction deleteBusinessObjectVisualAction;
    /**
     * Reference to the action that copies a business object to another business object.
     */
    @Autowired
    private CopyBusinessObjectVisualAction copyBusinessObjectVisualAction;
    /**
     * Reference to the action that moves a business object to another business object.
     */
    @Autowired
    private MoveBusinessObjectVisualAction moveBusinessObjectVisualAction;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    
    public FavoritesVisualAction() {
        super(FavoritesModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public WindowFavoritesManager getVisualComponent(ModuleActionParameterSet parameters) {
        return new WindowFavoritesManager(ts, aem, bem, mem,
                coreActionsRegistry,
                advancedActionsRegistry,
                viewWidgetRegistry,
                explorerWidgetRegistry,
                newBusinessObjectVisualAction,
                newBusinessObjectFromTemplateVisualAction,
                newMultipleBusinessObjectsVisualAction,
                deleteBusinessObjectVisualAction,
                copyBusinessObjectVisualAction,
                moveBusinessObjectVisualAction,
                windowMoreInformation,
                newFavoriteVisualAction,
                deleteFavoriteVisualAction,
                resourceFactory);
    }

    @Override
    public AbstractAction getModuleAction() {
        return favoritesAction;
    }
}