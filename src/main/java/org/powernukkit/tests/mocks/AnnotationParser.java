/*
 * PowerNukkit JUnit 5 Testing Framework
 * Copyright (C) 2020  José Roberto de Araújo Júnior
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.powernukkit.tests.mocks;

import cn.nukkit.math.Vector3;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * @author joserobjr
 */
@API(status = INTERNAL, since = "0.1.0")
public class AnnotationParser {
    private AnnotationParser() { throw new UnsupportedOperationException(); }

    @API(status = INTERNAL, since = "0.1.0")
    public static Vector3 parseVector3(double[] pos) {
        int index = 0;
        return new Vector3(
                pos.length > index? pos[index++] : 0, 
                pos.length > index? pos[index++] : 0, 
                pos.length > index? pos[index] : 0
        );
    }
}
