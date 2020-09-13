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
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.apiguardian.api.API;
import org.powernukkit.tests.api.MockEntity;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.powernukkit.tests.api.ReflectionUtil.supply;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class EntityMocker extends Mocker<Entity> {
    final Function<String, Level> levelSupplier;
    final MockEntity config;
    
    Entity entity;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public EntityMocker(Function<String, Level> levelSupplier) {
        this(levelSupplier, supply(()-> PowerNukkitExtension.class.getDeclaredField("defaults").getAnnotation(MockEntity.class)));
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public EntityMocker(Function<String, Level> levelSupplier, MockEntity config) {
        this.levelSupplier = levelSupplier;
        this.config = config;
    }

    @Override
    public Entity create() {
        Level level = levelSupplier.apply(config.level());
        Vector3 pos = AnnotationParser.parseVector3(config.position());
        CompoundTag nbt = Entity.getDefaultNBT(pos, new Vector3(), config.yaw(), config.pitch());
        int chunkX = pos.getChunkX();
        int chunkZ = pos.getChunkZ();
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = mock(BaseFullChunk.class);
            LevelProvider provider = mock(LevelProvider.class);
            lenient().when(provider.getChunk(eq(chunkX), eq(chunkZ))).thenReturn(chunk);
            lenient().when(provider.getLevel()).thenReturn(level);

            lenient().when(chunk.getX()).thenReturn(chunkX);
            lenient().when(chunk.getZ()).thenReturn(chunkZ);
            lenient().when(chunk.getProvider()).thenReturn(provider);
        }

        entity = mock(config.type(), withSettings().defaultAnswer(CALLS_REAL_METHODS).useConstructor(chunk, nbt));
        entity.noDamageTicks = 0;
        return entity;
    }
}
