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

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.apiguardian.api.API;
import org.powernukkit.tests.api.MockEntity;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.mockito.Mockito.*;
import static org.powernukkit.tests.api.ReflectionUtil.supply;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class EntityMocker extends ChunkBoundMocker<Entity> {
    final MockEntity config;
    final Class<? extends Entity> fieldType;
    
    Entity entity;

    private static MockEntity defaultMockEntity() {
        return supply(()-> PowerNukkitExtension.class.getDeclaredField("defaults").getAnnotation(MockEntity.class));
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public EntityMocker(Function<String, Level> levelSupplier) {
        this(levelSupplier, defaultMockEntity());
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public EntityMocker(Function<String, Level> levelSupplier, MockEntity config) {
        this(levelSupplier, config.type(), config);
    }

    @API(status = EXPERIMENTAL, since = "0.1.1")
    public EntityMocker(Function<String, Level> levelSupplier, Class<? extends Entity> fieldType) {
        this(levelSupplier, fieldType, defaultMockEntity());
    }

    @API(status = EXPERIMENTAL, since = "0.1.1")
    public EntityMocker(Function<String, Level> levelSupplier, Class<? extends Entity> fieldType, MockEntity config) {
        super(levelSupplier);
        this.fieldType = fieldType;
        this.config = config;
    }

    @Override
    protected String getLevelName() {
        return config.level();
    }

    @Override
    protected Vector3 getSpawnPos() {
        return AnnotationParser.parseVector3(config.position());
    }

    @Override
    public Entity create() {
        CompoundTag nbt = Entity.getDefaultNBT(pos, new Vector3(), config.yaw(), config.pitch());
        entity = mock(fieldType, withSettings().defaultAnswer(CALLS_REAL_METHODS).useConstructor(chunk, nbt));
        entity.noDamageTicks = 0;
        return entity;
    }
}
