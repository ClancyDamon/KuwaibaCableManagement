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

package org.neotropic.kuwaiba.core.apis.integration.modules.actions;

/**
 * Some classes require special procedures to delete their instances (for example, physical connections 
 * might need to release the endpoint ports before deleting the connection itself). If the developer 
 * will provide a custom delete action instead of the default one, he/she should subclass this class.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractDeleteAction extends AbstractVisualInventoryAction {

    public AbstractDeleteAction(String moduleId) {
        super(moduleId);
    }
}
