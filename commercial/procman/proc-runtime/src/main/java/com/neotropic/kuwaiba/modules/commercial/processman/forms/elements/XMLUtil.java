/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Scanner;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class XMLUtil {
    private static XMLUtil instance;
    
    private XMLUtil() {
    }
    
    public static XMLUtil getInstance() {
        return instance == null ? instance = new XMLUtil() : instance;
    }
    
    public boolean createAttribute(XMLEventWriter xmlew, XMLEventFactory xmlef, String attrName, String attrValue) throws XMLStreamException {
        if (xmlew == null || xmlef == null || attrName == null || attrValue == null)
            return false;
        
        xmlew.add(xmlef.createAttribute(new QName(attrName), attrValue));
                
        return true;
    }
    
    public static byte[] getFileAsByteArray(File file) {
        try {
            Scanner in = new Scanner(file);

            String line = "";

            while (in.hasNext())
                line += in.nextLine() + "\n";

            byte [] structure = line.getBytes();

            in.close();

            return structure;

        } catch (FileNotFoundException ex) {

            return null;
        }
    }
    
    /**
     * Saves a file, receiving the file name and the contents as parameters. If the directory structure doesn't exist, it's created
     * @param directory path to the directory
     * @param fileName the file name
     * @param content the file content
     * @throws FileNotFoundException 
     * @throws IOException
     */
    public static void saveFile(String directory, String fileName, byte[] content) throws FileNotFoundException, IOException {
        java.nio.file.Path directoryPath = FileSystems.getDefault().getPath(directory);
        if (!Files.exists(directoryPath) || !Files.isWritable(directoryPath))
            throw new FileNotFoundException(String.format("Path %s does not exist or is not writeable", directoryPath.toAbsolutePath()));

        try (FileOutputStream fos = new FileOutputStream(directory + "/" + fileName)) { //NOI18N
            fos.write(content);
        }
    }
            
}
