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

package org.powernukkit.tests.memory;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.anvil.Chunk;
import cn.nukkit.level.format.anvil.ChunkSection;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.format.generic.BaseLevelProvider;
import cn.nukkit.level.generator.Flat;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.ChunkException;
import cn.nukkit.utils.ThreadCache;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apiguardian.api.API;
import org.iq80.leveldb.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @author joserobjr
 * @since 2020-09-13
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class MemoryLevelProvider extends BaseLevelProvider {
    private static final Map<Path, Prepared> preparedMap = new ConcurrentHashMap<>();
    
    private final Long2ObjectMap<BaseFullChunk> savedChunks = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectMap<BaseFullChunk> loadedChunks = new Long2ObjectLinkedOpenHashMap<>();

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static ChunkSection createChunkSection(int y) {
        return Anvil.createChunkSection(y);
    }
    
    @API(status = EXPERIMENTAL, since = "0.1.0")
    public MemoryLevelProvider(Level level, String pathString) throws IOException {
        // TODO Use the new constructor in 1.4.0.0-PN instead of this hack
        super(level, Files.isRegularFile(Paths.get(pathString, "level.dat"))? pathString : trickConstructor());
        Path path = Paths.get(pathString);
        if (!super.getPath().equals(pathString)) {
            FileUtils.deleteRecursively(new File(super.getPath()));
            try {
                Field field = BaseLevelProvider.class.getDeclaredField("path");
                field.setAccessible(true);
                field.set(this, pathString);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
            
            if (!preparedMap.containsKey(path)) {
                return;
            }
        }

        Prepared prepared = preparedMap.get(path);
        if (prepared == null) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            prepared = new Prepared("TestLevel"+ random.nextDouble(), random.nextLong(), Flat.class, new LinkedHashMap<>());
        }
        
        levelData.putString("generatorName", prepared.getGenerator().getSimpleName().toLowerCase());
        levelData.putString("generatorOptions", "");
        levelData.putLong("RandomSeed", prepared.getSeed());
    }
    
    private static String trickConstructor() throws IOException {
        Path tempDir = Files.createTempDirectory("trick_for_BaseLevelProvider");

        CompoundTag levelData = new CompoundTag("Data")
                .putString("generatorName", "memory")
                .putString("generatorOptions", "memory")
                .putInt("SpawnX", 128)
                .putInt("SpawnY", 70)
                .putInt("SpawnZ", 128);
        
        try (FileOutputStream fos = new FileOutputStream(tempDir.resolve("level.dat").toFile())) {
            NBTIO.writeGZIPCompressed(new CompoundTag().putCompound("Data", levelData), fos, ByteOrder.BIG_ENDIAN);
        }
        
        return tempDir.toString() + "/";
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static void reset() {
        preparedMap.clear();
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static void reset(Path path) {
        preparedMap.remove(path);
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static String getProviderName() {
        return "memory";
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static byte getProviderOrder() {
        return ORDER_YZX;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static boolean usesChunkSection() {
        return true;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static boolean isValid(String path) {
        return true;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static void generate(String path, String name, long seed, Class<? extends Generator> generator) {
        generate(path, name, seed, generator, new HashMap<>());
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static void generate(String path, String name, long seed, Class<? extends Generator> generator, Map<String, String> options) {
        preparedMap.put(Paths.get(path), new Prepared(name, seed, generator, new LinkedHashMap<>(options)));
    }
    
    @Override
    public BaseFullChunk loadChunk(long index, int chunkX, int chunkZ, boolean create) {
        BaseFullChunk chunk;
        if (!create) {
            chunk = savedChunks.get(index);
            if (chunk == null) {
                return null;
            }
        } else {
            chunk = savedChunks.computeIfAbsent(index, i-> getEmptyChunk(chunkX, chunkZ));
        }
        
        loadedChunks.put(index, chunk.clone());
        return chunk;
    }

    @Override
    public AsyncTask requestChunkTask(int x, int z) {
        Chunk chunk = (Chunk) this.getChunk(x, z, false);
        if (chunk == null) {
            throw new ChunkException("Invalid Chunk Set");
        }

        long timestamp = chunk.getChanges();

        byte[] blockEntities = new byte[0];

        if (!chunk.getBlockEntities().isEmpty()) {
            List<CompoundTag> tagList = new ArrayList<>();

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof BlockEntitySpawnable) {
                    tagList.add(((BlockEntitySpawnable) blockEntity).getSpawnCompound());
                }
            }

            try {
                blockEntities = NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN, true);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        Map<Integer, Integer> extra = chunk.getBlockExtraDataArray();
        BinaryStream extraData;
        if (!extra.isEmpty()) {
            extraData = new BinaryStream();
            extraData.putVarInt(extra.size());
            for (Map.Entry<Integer, Integer> entry : extra.entrySet()) {
                extraData.putVarInt(entry.getKey());
                extraData.putLShort(entry.getValue());
            }
        } else {
            extraData = null;
        }

        BinaryStream stream = ThreadCache.binaryStream.get().reset();
        int count = 0;
        cn.nukkit.level.format.ChunkSection[] sections = chunk.getSections();
        for (int i = sections.length - 1; i >= 0; i--) {
            if (!sections[i].isEmpty()) {
                count = i + 1;
                break;
            }
        }
        for (int i = 0; i < count; i++) {
            BinaryStream tmp = new BinaryStream();
            sections[i].writeTo(tmp);
            stream.put(tmp.getBuffer());
        }
        stream.put(chunk.getBiomeIdArray());
        stream.putByte((byte) 0);
        if (extraData != null) {
            stream.put(extraData.getBuffer());
        } else {
            stream.putVarInt(0);
        }
        stream.put(blockEntities);

        this.getLevel().chunkRequestCallback(timestamp, x, z, count, stream.getBuffer());

        return null;
    }

    @Override
    public BaseFullChunk getEmptyChunk(int x, int z) {
        return Chunk.getEmptyChunk(x, z, this);
    }

    @Override
    public void saveChunk(int x, int z) {
        long index = Level.chunkHash(x, z);
        BaseFullChunk chunk = loadedChunks.get(index);
        if (chunk != null) {
            savedChunks.put(index, chunk.clone());
        }
    }

    @Override
    public void saveChunk(int x, int z, FullChunk chunk) {
        if (!(chunk instanceof BaseFullChunk)) {
            throw new UnsupportedOperationException("The chunk is not BaseFullChunk!");
        }
        
        savedChunks.put(Level.chunkHash(x, z), ((BaseFullChunk) chunk).clone());
    }
    
    @Data
    @AllArgsConstructor
    private static class Prepared {
        private String name;
        private long seed;
        private Class<? extends Generator> generator;
        private Map<String, String> options; 
    }
}
