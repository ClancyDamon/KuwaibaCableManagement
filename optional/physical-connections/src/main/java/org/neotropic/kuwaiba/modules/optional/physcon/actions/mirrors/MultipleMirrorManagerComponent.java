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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Component to manage multiple mirrors.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MultipleMirrorManagerComponent extends AbstractMirrorManagerComponent<
    FreePortsComponentForMultipleMirrors, AssignedPortsComponentForMultipleMirrors, WindowFreePortsMultipleMirroring> {

    public MultipleMirrorManagerComponent(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts) {
        super(businessObject, bem, ts);
    }

    @Override
    public FreePortsComponentForMultipleMirrors getFreePortsComponent(AssignedPortsComponentForMultipleMirrors assignedPortsComponent) {
        return new FreePortsComponentForMultipleMirrors(businessObject, bem, ts, lytUnassignedSourcePorts, lytUnassignedTargetPorts, assignedPortsComponent, cmdUpdateFreePorts);
    }

    @Override
    public AssignedPortsComponentForMultipleMirrors getAssignedPortsComponent() {
        return new AssignedPortsComponentForMultipleMirrors(businessObject, cmdUpdateFreePorts, bem, ts);
    }

    @Override
    public WindowFreePortsMultipleMirroring getMirrorFreePortsAction() {
        return new WindowFreePortsMultipleMirroring(businessObject, bem, ts, cmdUpdateMirrorManager);
    }
    
}
