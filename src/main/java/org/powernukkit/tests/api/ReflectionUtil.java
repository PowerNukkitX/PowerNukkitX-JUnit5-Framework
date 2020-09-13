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

import javax.annotation.Nullable;
import java.lang.reflect.Field;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@API(since = "0.1.0", status = EXPERIMENTAL)
public class ReflectionUtil {
    private ReflectionUtil() { throw new UnsupportedOperationException(); }
    
    @SuppressWarnings("unchecked")
    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static <T> T getField(@Nullable Object instance, Field field) {
        return supply(()-> {
            field.setAccessible(true);
            return (T) field.get(instance);
        });
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static <T> T getField(Object instance, String name) {
        return supply(()-> getField(instance, instance.getClass().getField(name)));
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static void setField(@Nullable Object instance, Field field, @Nullable Object value) {
        execute(()-> {
            field.setAccessible(true);
            field.set(instance, value);
        });
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static void setField(Object instance, String name, @Nullable Object value) {
        execute(()-> setField(instance, instance.getClass().getField(name), value));
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static <T> T supply(ReflectiveSupplier<T> supplier) {
        return supplier.get();
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public static void execute(ReflectiveRunnable runnable) {
        runnable.run();
    }
}
