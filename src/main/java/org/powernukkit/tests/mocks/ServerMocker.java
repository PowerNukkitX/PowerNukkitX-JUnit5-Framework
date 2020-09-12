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
import cn.nukkit.plugin.PluginManager;
import org.apiguardian.api.API;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.configuration.injection.scanner.MockScanner;
import org.mockito.internal.util.collections.Sets;

import java.util.Set;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class ServerMocker implements Mocker<Server> {
    @Mock
    public PluginManager pluginManager;
    
    @Mock
    Server server;
    
    @Override
    public Server create() throws ReflectiveOperationException {
        MockitoAnnotations.initMocks(this);
        
        Set<Object> mocks = Sets.newMockSafeHashSet();
        new MockScanner(this, ServerMocker.class).addPreparedMocks(mocks);
        mocks.remove(server);
        
        MockUtil.injectMocks(ServerMocker.class.getDeclaredField("server"), mocks, this);
        return server;
    }
}
