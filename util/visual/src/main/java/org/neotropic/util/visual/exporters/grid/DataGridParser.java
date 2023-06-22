/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.exporters.grid;

import java.util.List;

/**
 * Transforms the given list of Objects into a new matrix object
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public interface DataGridParser {
    /**
     * converts a data source to an array that can be exported 
     * @param dataSource data source
     * @return the new array with the matrix structure
     */
       public String[][] getData(Object dataSource);
}
