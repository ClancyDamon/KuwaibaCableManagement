/*
 *  Copyright 2010-2022, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.reports.defaults;

import java.nio.charset.StandardCharsets;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;


/**
 * A report that receives raw text and returns its corresponding bytes. It will be used to wrap the old, hard-coded reports
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RawReport extends InventoryReport {
    private String text;

    public RawReport(String title, String author, String version, String text) {
        super(title, author, version);
        this.text = text;
    }

    @Override
    public byte[] asByteArray() {
        return text.getBytes(StandardCharsets.UTF_8);
    }
    
}
