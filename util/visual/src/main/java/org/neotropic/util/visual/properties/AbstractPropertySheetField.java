/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.properties;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public abstract class AbstractPropertySheetField {

    protected abstract Object getValue();
    
    protected abstract void setValue(Object value);
}
