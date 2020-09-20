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

package org.powernukkit.tests.api;

import cn.nukkit.level.Level;
import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "0.1.0")
public @interface MockLevel {
    @API(status = EXPERIMENTAL, since = "0.1.0")
    String name() default "";

    @API(status = EXPERIMENTAL, since = "0.1.0")
    int dimension() default Level.DIMENSION_OVERWORLD;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    String generator() default "flat";

    @API(status = EXPERIMENTAL, since = "0.1.0")
    int[] spawn() default {0, 64, 0};

    @API(status = EXPERIMENTAL, since = "0.1.0")
    boolean isDefault() default false;
}
