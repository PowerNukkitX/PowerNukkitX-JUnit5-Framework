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

import cn.nukkit.entity.Entity;
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
public @interface MockEntity {
    @API(status = EXPERIMENTAL, since = "0.1.0")
    Class<? extends Entity> type() default Entity.class;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    float health() default 20;
    
    @API(status = EXPERIMENTAL, since = "0.1.0")
    String level() default "";

    @API(status = EXPERIMENTAL, since = "0.1.0")
    double[] position() default {};

    @API(status = EXPERIMENTAL, since = "0.1.0")
    float yaw() default 0;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    float pitch() default 0;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    ItemStack[] inventory() default {};

    @API(status = EXPERIMENTAL, since = "0.1.0")
    ItemStack mainHand() default @ItemStack;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    ItemStack offhand() default @ItemStack;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    ItemStack[] equipments() default {};
}
