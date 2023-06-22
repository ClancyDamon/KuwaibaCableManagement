/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.views.util;

import java.awt.Color;

/**
 * A collection of commonly used methods to generate HTML-related content. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class UtilHtml {
    
    /**
     * Converts a Java Color instance into an hexadecimal string that can be used to format HTML components.
     * @param color The Java Color instance.
     * @return The hex representation of the color, including a heading hashtag (#).
     */
    public static String toHexString(Color color) {
        StringBuilder sb = new StringBuilder("#");

        if (color.getRed() < 16) sb.append('0');
        sb.append(Integer.toHexString(color.getRed()));

        if (color.getGreen() < 16) sb.append('0');
        sb.append(Integer.toHexString(color.getGreen()));

        if (color.getBlue() < 16) sb.append('0');
        sb.append(Integer.toHexString(color.getBlue()));

        return sb.toString();
    }
}