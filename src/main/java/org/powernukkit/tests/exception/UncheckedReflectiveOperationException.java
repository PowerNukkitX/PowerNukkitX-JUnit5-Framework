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

package org.powernukkit.tests.exception;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.*;

/**
 * @author joserobjr
 */
@API(since = "0.1.0", status = EXPERIMENTAL)
public class UncheckedReflectiveOperationException extends RuntimeException {
    @API(since = "0.1.0", status = EXPERIMENTAL)
    public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause) {
        super(message, cause);
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public UncheckedReflectiveOperationException(ReflectiveOperationException cause) {
        super(cause);
    }

    @API(since = "0.1.0", status = EXPERIMENTAL)
    public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public synchronized ReflectiveOperationException getCause() {
        return (ReflectiveOperationException) super.getCause();
    }
}
