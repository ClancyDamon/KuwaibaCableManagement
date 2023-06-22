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
import org.neotropic.kuwaiba.modules.optional.physcon.actions.WindowFreePortsMirroring;

/**
 * Component to manage single mirrors.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SingleMirrorManagerComponent extends AbstractMirrorManagerComponent<
    FreePortsComponentForSingleMirrors, AssignedPortsComponentForSingleMirrors, WindowFreePortsMirroring> {

    public SingleMirrorManagerComponent(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts) {
        super(businessObject, bem, ts);
    }

    @Override
    public FreePortsComponentForSingleMirrors getFreePortsComponent(AssignedPortsComponentForSingleMirrors assignedPortsComponent) {
        return new FreePortsComponentForSingleMirrors(businessObject, bem, ts, lytUnassignedSourcePorts, lytUnassignedTargetPorts, assignedPortsComponent, cmdUpdateFreePorts);
    }

    @Override
    public AssignedPortsComponentForSingleMirrors getAssignedPortsComponent() {
        return new AssignedPortsComponentForSingleMirrors(businessObject, cmdUpdateFreePorts, bem, ts);
    }

    @Override
    public WindowFreePortsMirroring getMirrorFreePortsAction() {
        return new WindowFreePortsMirroring(businessObject, bem, ts, cmdUpdateMirrorManager);
    }
}