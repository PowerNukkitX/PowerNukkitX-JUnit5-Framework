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

import cn.nukkit.level.Level;
import org.apiguardian.api.API;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powernukkit.tests.api.MockLevel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class LevelMocker implements Mocker<Level> {
    @Mock
    Level level;

    public LevelMocker() {
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public LevelMocker(MockLevel config) {
    }

    @Override
    public Level create() {
        MockitoAnnotations.initMocks(this);
        return level;
    }
}
