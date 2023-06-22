/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.neotropic.util.visual.exporters;

import java.nio.charset.Charset;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Implements the logic necessary to export to CSV
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class CSVFormatter extends AbstractFormatter {

    //Possible separators
    public static Character CHARACTER_COMMA = ','; //NOI18N
    public static Character CHARACTER_TAB = '\t'; //NOI18N
    public static Character CHARACTER_SPACE = ' '; //NOI18N
    public static Character CHARACTER_PIPE = '|'; //NOI18N
    public static Character CHARACTER_TILDE = '~'; //NOI18N

    public CSVFormatter(TranslationService ts) {
        super(ts);
    }

    @Override
    public byte[] format(String[][] data) {
        String csv = ""; 
        for (Object[] record : data){
            String currentRecord = "";
            for (Object column : record)
                currentRecord += (column == null ? "" : column) + CHARACTER_COMMA.toString();
            currentRecord = currentRecord.substring(0,currentRecord.length() - 1) + "\n";
            csv += currentRecord;
        }
        return csv.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public String getDisplayName() {
        return "CSV";
    }

    @Override
    public String getExtension() {
        return ".csv"; //NOI18N
    }
}
