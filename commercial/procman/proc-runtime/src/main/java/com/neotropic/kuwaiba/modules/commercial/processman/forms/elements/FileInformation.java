/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FileInformation {
    private static long counter = 0;
    private final long id;
    private String name;    
    private String path;
    
    public FileInformation() {
        counter += 1;
        
        id = counter;                
    }
    
    public FileInformation(String name, String path) {
        this();
        this.name = name;
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileInformation other = (FileInformation) obj;
        return this.id == other.id;
    }
    
    @Override
    public String toString() {
        return name;
    }    
}
