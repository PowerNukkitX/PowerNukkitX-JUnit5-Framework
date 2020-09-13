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
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Flat;
import org.apiguardian.api.API;
import org.powernukkit.tests.api.MockLevel;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;
import org.powernukkit.tests.memory.MemoryLevelProvider;

import java.util.concurrent.ThreadLocalRandom;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.mockito.Mockito.*;
import static org.powernukkit.tests.api.ReflectionUtil.supply;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class LevelMocker extends Mocker<Level> {
    final String levelName;
    
    MockLevel config;
    
    Level level;

    public LevelMocker() {
        this(supply(()-> PowerNukkitExtension.class.getDeclaredField("defaults").getAnnotation(MockLevel.class)));
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public LevelMocker(MockLevel config) {
        this(config, config.name());
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public LevelMocker(MockLevel config, String levelName) {
        this.config = config;
        
        if (levelName.isEmpty()) {
            levelName = "TestLevel" + ThreadLocalRandom.current().nextInt(0, 1000000);
        }
        
        this.levelName = levelName;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public LevelMocker(String levelName) {
        this(supply(()-> PowerNukkitExtension.class.getDeclaredField("defaults").getAnnotation(MockLevel.class)), levelName);
    }

    @Override
    public Level create() {
        String path = "memory/" + levelName;
        MemoryLevelProvider.generate(path, levelName, ThreadLocalRandom.current().nextLong(), Flat.class);
        level = mock(Level.class, withSettings().defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(Server.getInstance(), levelName, path, MemoryLevelProvider.class));
        level.initLevel();
        return level;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public String getLevelName() {
        return levelName;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public Level getLevel() {
        return level;
    }
}
