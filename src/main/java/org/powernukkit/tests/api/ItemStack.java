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

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public @interface ItemStack {
    @API(status = EXPERIMENTAL, since = "0.1.0")
    int item() default 0;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    int block() default 0;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    int meta() default 0;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    int amount() default 1;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    String displayName() default "~~~";

    @API(status = EXPERIMENTAL, since = "0.1.0")
    String[] lore() default {};

    @API(status = EXPERIMENTAL, since = "0.1.0")
    boolean unbreakable() default false;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    ItemEnchantment[] enchantments() default {};

    @API(status = EXPERIMENTAL, since = "0.1.0")
    String namedTags() default "";
}
