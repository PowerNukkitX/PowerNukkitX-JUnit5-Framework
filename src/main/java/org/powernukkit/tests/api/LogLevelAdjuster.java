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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apiguardian.api.API;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class LogLevelAdjuster {
    private final Map<Class<?>, Level> adjustedClasses = new LinkedHashMap<>();

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public synchronized void setLevel(Class<?> c, Level level) {
        adjustedClasses.computeIfAbsent(c, this::getLevel);
        applyLevel(c, level);
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public void onlyNow(Class<?> c, Level level, Runnable runnable) {
        Level original = getLevel(c);
        setLevel(c, level);
        try {
            runnable.run();
        } finally {
            setLevel(c, original);
        }
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public <V> V onlyNow(Class<?> c, Level level, Callable<V> runnable) throws Exception {
        Level original = getLevel(c);
        setLevel(c, level);
        try {
            return runnable.call();
        } finally {
            setLevel(c, original);
        }
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public Level getLevel(Class<?> c) {
        return LogManager.getLogger(c).getLevel();
    }

    private void applyLevel(Class<?> c, Level level) {
        Configurator.setLevel(LogManager.getLogger(c).getName(), level);
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public synchronized void restoreLevel(Class<?> c) {
        Level level = adjustedClasses.remove(c);
        if (level != null) {
            applyLevel(c, level);
        }
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public synchronized void restoreLevels() {
        adjustedClasses.forEach(this::applyLevel);
        adjustedClasses.clear();
    }
}
