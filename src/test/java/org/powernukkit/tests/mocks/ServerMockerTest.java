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

import cn.nukkit.Server;
import org.junit.jupiter.api.Test;
import org.powernukkit.tests.api.ReflectionUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author joserobjr
 */
class ServerMockerTest {
    @Test
    void privateField() throws ReflectiveOperationException {
        Server server = new ServerMocker().create();
        assertNotNull(ReflectionUtil.getField(server, Server.class.getDeclaredField("pluginManager")));
        assertNotNull(ReflectionUtil.getField(server, Server.class.getDeclaredField("isRunning")));
    }
}
