package org.powernukkit.tests.mocks;

import cn.nukkit.level.Level;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import org.apiguardian.api.API;

import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@API(status = EXPERIMENTAL, since = "0.1.1")
public abstract class ChunkBoundMocker<T> extends Mocker<T> {
    final Function<String, Level> levelSupplier;
    Level level;
    BaseFullChunk chunk;
    Vector3 pos;

    @API(status = EXPERIMENTAL, since = "0.1.1")
    protected ChunkBoundMocker(Function<String, Level> levelSupplier) {
        this.levelSupplier = levelSupplier;
    }

    @API(status = EXPERIMENTAL, since = "0.1.1")
    protected abstract String getLevelName();

    @API(status = EXPERIMENTAL, since = "0.1.1")
    protected abstract Vector3 getSpawnPos();

    @API(status = EXPERIMENTAL, since = "0.1.1")
    public void prepare() {
        level = levelSupplier.apply(getLevelName());
        pos = getSpawnPos();
        int chunkX = pos.getChunkX();
        int chunkZ = pos.getChunkZ();
        chunk = level.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = mock(BaseFullChunk.class);
            LevelProvider provider = mock(LevelProvider.class);
            lenient().when(provider.getChunk(eq(chunkX), eq(chunkZ))).thenReturn(chunk);
            lenient().when(provider.getLevel()).thenReturn(level);

            lenient().doCallRealMethod().when(chunk).setPosition(anyInt(), anyInt());
            chunk.setPosition(chunkX, chunkZ);
            lenient().when(chunk.getProvider()).thenReturn(provider);
        }
    }
}
