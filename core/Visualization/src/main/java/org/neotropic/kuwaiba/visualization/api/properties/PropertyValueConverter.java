/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.visualization.api.properties;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.properties.AbstractProperty;

/**
 * class used to transform the value of certain properties in the database format that are stored
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class PropertyValueConverter {
   public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.ENGLISH);
    
    public static String getMultipleListTypeAsStringToPersist(List<BusinessObjectLight> value) {
        if (value == null) 
            return ""; 
        
        List<BusinessObjectLight> tempList = new ArrayList<>(value);
        
        String idItems = "";
        for (int i = 0; i < tempList.size(); i++) {
            if (i > 0) 
                idItems += ";";
            
            idItems += tempList.get(i).getId();
        }
        return idItems;
    }

    public static String getListTypeAsStringToPersist(BusinessObjectLight value) {
        return value == null ? "" : value.getId() + "" ;
    }
    
    public static String getLocalDateAsStringToPersist(LocalDate value) {
        if (value != null) {
            Instant instant = value.atStartOfDay(ZoneId.systemDefault()).toInstant();	
            long timeInMillis = instant.toEpochMilli();
            return timeInMillis + "";
        } else 
            return "0";
    }
    
    public static String getLocalDateTimeAsStringToPersist(LocalDateTime value) {
        if (value != null) {          	
            long timeInMillis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return timeInMillis + "";
        } else 
            return "0";
    }
        
    public static String getAsStringToPersist(AbstractProperty property) { 
        if (property.getValue() == null) 
            return null;
        switch (property.getType()) {
            case Constants.DATA_TYPE_DATE:
                return getLocalDateAsStringToPersist((LocalDate) property.getValue());
            case Constants.DATA_TYPE_TIME_STAMP:
                return getLocalDateTimeAsStringToPersist((LocalDateTime) property.getValue());
            case Constants.DATA_TYPE_STRING:
                return (String) property.getValue();
            case Constants.DATA_TYPE_DOUBLE:
            case Constants.DATA_TYPE_FLOAT:
                return  String.valueOf((Double) property.getValue());
            case Constants.DATA_TYPE_INTEGER:
                return  String.valueOf((Integer) property.getValue());
            case Constants.DATA_TYPE_BOOLEAN: 
                 return String.valueOf((Boolean) property.getValue());
            case Constants.DATA_TYPE_LONG: 
                 return String.valueOf((Long) property.getValue());
            default:               
                  if (property.getValue() instanceof BusinessObjectLight)
                        return getListTypeAsStringToPersist((BusinessObjectLight) property.getValue());
                  else if (property.getValue() instanceof Collection)
                        return getMultipleListTypeAsStringToPersist( new ArrayList<>((Set) property.getValue()));
               
        }
        return null;
    }   
}