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
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Set of units of length supported
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public enum UnitOfLength {
    M("m"), //NOI18N
    KM("Km"), //NOI18N
    IN("in"), //NOI18N
    FT("ft"), //NOI18N
    YD("yt"), //NOI18N
    MI("mi"); //NOI18N

    private final String unitOfLength;

    private UnitOfLength(String unitOfLength) {
        this.unitOfLength = unitOfLength;
    }

    public static UnitOfLength getUnitOfLength(String unitOfLength) {
        if (UnitOfLength.M.toString().equals(unitOfLength)) {
            return UnitOfLength.M;
        }

        if (UnitOfLength.KM.toString().equals(unitOfLength)) {
            return UnitOfLength.KM;
        }

        if (UnitOfLength.IN.toString().equals(unitOfLength)) {
            return UnitOfLength.IN;
        }

        if (UnitOfLength.FT.toString().equals(unitOfLength)) {
            return UnitOfLength.FT;
        }

        if (UnitOfLength.YD.toString().equals(unitOfLength)) {
            return UnitOfLength.YD;
        }

        if (UnitOfLength.MI.toString().equals(unitOfLength)) {
            return UnitOfLength.MI;
        }

        throw new UnsupportedOperationException();
    }

    private String getArithmeticOperator() {
        if (this == UnitOfLength.M) {
            return "*";
        }
        if (this == UnitOfLength.KM) {
            return "/";
        }
        if (this == UnitOfLength.IN) {
            return "*";
        }
        if (this == UnitOfLength.FT) {
            return "*";
        }
        if (this == UnitOfLength.YD) {
            return "*";
        }
        if (this == UnitOfLength.MI) {
            return "/";
        }
        throw new UnsupportedOperationException();
    }

    private double getOperand2() {
        if (this == UnitOfLength.M) {
            return 1;
        }
        if (this == UnitOfLength.KM) {
            return 1000;
        }
        if (this == UnitOfLength.IN) {
            return 39.37;
        }
        if (this == UnitOfLength.FT) {
            return 3.281;
        }
        if (this == UnitOfLength.YD) {
            return 1.094;
        }
        if (this == UnitOfLength.MI) {
            return 1609;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Provides internationalization support for each entry in the enumeration.
     *
     * @param unitOfLength The unit to be translated.
     * @param ts The translation service.
     * @return The translated entry.
     */
    public static String getTranslatedString(UnitOfLength unitOfLength, TranslationService ts) {
        Objects.requireNonNull(unitOfLength);
        Objects.requireNonNull(ts);

        if (unitOfLength == UnitOfLength.M) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.m");
        }

        if (unitOfLength == UnitOfLength.KM) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.km");
        }

        if (unitOfLength == UnitOfLength.IN) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.in");
        }

        if (unitOfLength == UnitOfLength.FT) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.ft");
        }

        if (unitOfLength == UnitOfLength.YD) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.yd");
        }

        if (unitOfLength == UnitOfLength.MI) {
            return ts.getTranslatedString("module.ospman.property.unit-of-length.mi");
        }

        throw new UnsupportedOperationException();
    }

    /**
     * Converts a given value of another unit to meters.
     *
     * @param length The length in a unit other that meters (though value in
     * meters is also supported).
     * @param unitOfLength The unit to be converted to.
     * @return The converted value.
     */
    public static double toMeters(Double length, UnitOfLength unitOfLength) {
        Objects.requireNonNull(length);
        Objects.requireNonNull(unitOfLength);

        if (unitOfLength == UnitOfLength.M) {
            return length;
        }
        if (unitOfLength == UnitOfLength.KM) {
            return length * 1000;
        }
        if (unitOfLength == UnitOfLength.IN) {
            return length / 39.37;
        }
        if (unitOfLength == UnitOfLength.FT) {
            return length / 3.281;
        }
        if (unitOfLength == UnitOfLength.YD) {
            return length / 1.094;
        }
        if (unitOfLength == UnitOfLength.MI) {
            return length * 1609;
        }

        throw new UnsupportedOperationException();
    }

    /**
     * Converts from meter to a unit of length.
     *
     * @param meters Length in meters.
     * @param unitOfLength The unit to convert the meters.
     * @return Converted meters
     */
    public static double convertMeters(Double meters, UnitOfLength unitOfLength) {
        Objects.requireNonNull(meters);
        Objects.requireNonNull(unitOfLength);

        if (unitOfLength == UnitOfLength.M) {
            return meters;
        }
        if (unitOfLength == UnitOfLength.KM) {
            return meters / 1000;
        }
        if (unitOfLength == UnitOfLength.IN) {
            return meters * 39.37;
        }
        if (unitOfLength == UnitOfLength.FT) {
            return meters * 3.281;
        }
        if (unitOfLength == UnitOfLength.YD) {
            return meters * 1.094;
        }
        if (unitOfLength == UnitOfLength.MI) {
            return meters / 1609;
        }

        throw new UnsupportedOperationException();
    }

    public static List<UnitOfLength> getUnits() {
        List<UnitOfLength> units = Arrays.asList(
                UnitOfLength.M,
                UnitOfLength.KM,
                UnitOfLength.IN,
                UnitOfLength.FT,
                UnitOfLength.YD,
                UnitOfLength.MI
        );
        Collections.sort(units);
        return units;
    }

    @Override
    public String toString() {
        return unitOfLength;
    }

    public JsonObject toJson(double numberOfDigits, TranslationService ts) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("arithmeticOperator", getArithmeticOperator());
        jsonObject.put("operand2", getOperand2());
        jsonObject.put("numberOfDigits", numberOfDigits);
        jsonObject.put("translated", UnitOfLength.getTranslatedString(this, ts));
        return jsonObject;
    }
}
