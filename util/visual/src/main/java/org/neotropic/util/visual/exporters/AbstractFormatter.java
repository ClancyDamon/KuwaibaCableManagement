/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.exporters;

import java.io.IOException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Abstract class to implement different format classes
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public abstract class AbstractFormatter {
    
    TranslationService ts;

    public AbstractFormatter(TranslationService ts) {
        this.ts = ts;
    }

    /**
     * To be used as label in the formats combo box
     * @return the name to be displayed
     */
    public abstract String getDisplayName();
    /**
     * Gets the file extension
     * @return the file extension (including the dot)
     */
    public abstract String getExtension();
     /**
     * The export action.
     * @param result
     * @return the byte array with the formated data
     * @throws IOException    
     */
    public abstract byte[] format(String[][] result) throws IOException;
    
}
