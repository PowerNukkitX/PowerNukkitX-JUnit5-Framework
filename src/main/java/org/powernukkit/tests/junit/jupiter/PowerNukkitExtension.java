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

package org.powernukkit.tests.junit.jupiter;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.dispenser.DispenseBehaviorRegister;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.LevelProviderManager;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.generator.Flat;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.Nether;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.network.process.DataPacketManager;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powernukkit.tests.api.MockEntity;
import org.powernukkit.tests.api.MockLevel;
import org.powernukkit.tests.api.MockPlayer;
import org.powernukkit.tests.api.MockServer;
import org.powernukkit.tests.mocks.EntityMocker;
import org.powernukkit.tests.mocks.LevelMocker;
import org.powernukkit.tests.mocks.PlayerMocker;
import org.powernukkit.tests.mocks.ServerMocker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.powernukkit.tests.api.ReflectionUtil.execute;
import static org.powernukkit.tests.api.ReflectionUtil.setField;

/**
 * @author joserobjr
 */
@API(since = "0.1.0", status = EXPERIMENTAL)
@MockServer(name = "TinyTestServer", initPrivateFields = false, callsRealMethods = false, createTempDir = false)
public class PowerNukkitExtension extends MockitoExtension implements TestInstancePostProcessor, BeforeAllCallback, AfterAllCallback {
    private static final ExtensionContext.Namespace POWERNUKKIT = create("org.powernukkit");
    private static final String SESSION = "session";
    
    static class Session {
        ServerMocker serverMocker;
        Map<String, List<LevelMocker>> levels = new LinkedHashMap<>();
        Map<String, List<PlayerMocker>> players = new LinkedHashMap<>();
        List<EntityMocker> entities = new ArrayList<>();
        
        void releaseResources() {
            serverMocker.releaseResources();
        }
    }
    
    @MockPlayer
    @MockLevel
    @MockEntity
    private static final Void defaults = null;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        MockServer config = context.getRequiredTestClass().getAnnotation(MockServer.class);
        if (config == null) {
            config = PowerNukkitExtension.class.getAnnotation(MockServer.class);
        }
        
        Session session = new Session();
        
        ServerMocker serverMocker = new ServerMocker(config);
        Server server = serverMocker.create();
        
        session.serverMocker = serverMocker;
        context.getStore(POWERNUKKIT).put(SESSION, session);
        
        serverMocker.setActive();
        
        initStatics(server);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Session session = context.getStore(POWERNUKKIT).get(SESSION, Session.class);
        
        Map<String, List<LevelMocker>> levels = session.levels;
        for (Field field : AnnotationSupport.findAnnotatedFields(context.getRequiredTestClass(), MockLevel.class)) {
            if (field.isAnnotationPresent(MockLevel.class) && field.getType().isAssignableFrom(Level.class)) {
                LevelMocker levelMocker = new LevelMocker(field.getAnnotation(MockLevel.class));
                Level level = levelMocker.create();
                levels.computeIfAbsent(levelMocker.getLevelName(), it-> new ArrayList<>()).add(levelMocker);
                setField(testInstance, field, level);
            }
        }

        Function<String, Level> levelProvider = name-> {
            if (name.isEmpty()) {
                if (!levels.isEmpty()) {
                    return levels.values().iterator().next().get(0).getLevel();
                }
                LevelMocker levelMocker = new LevelMocker();
                Level level = levelMocker.create();
                List<LevelMocker> list = new ArrayList<>();
                list.add(levelMocker);
                levels.put(levelMocker.getLevelName(), list);
                return level;
            }
            
            List<LevelMocker> list = levels.computeIfAbsent(name, it-> new ArrayList<>());
            if (list.size() > 1) {
                throw new IllegalStateException("Multiple level options: "+list);
            } else if (list.size() == 1) {
                return list.get(0).getLevel();
            }
            
            LevelMocker levelMocker = new LevelMocker(name);
            Level level = levelMocker.create();
            list.add(levelMocker);
            return level;
        };
        
        for (Field field: AnnotationSupport.findAnnotatedFields(context.getRequiredTestClass(), MockEntity.class)) {
            Class<?> type = field.getType();
            Class<? extends Entity> entityType;
            if (!Entity.class.isAssignableFrom(type)) {
                throw new AssertionError("@MockEntity must be used on fields of type Entity or higher.");
            }
            MockEntity annotation = field.getAnnotation(MockEntity.class);
            if (!annotation.type().equals(Entity.class)) {
                entityType = annotation.type();
            } else {
                //noinspection unchecked
                entityType = (Class<? extends Entity>) field.getType();
            }
            EntityMocker entityMocker = new EntityMocker(levelProvider, entityType, annotation);
            entityMocker.prepare();
            entityMocker.createAndSet(testInstance, field);
            session.entities.add(entityMocker);
        }
        
        for (Field field: AnnotationSupport.findAnnotatedFields(context.getRequiredTestClass(), MockPlayer.class)) {
            PlayerMocker playerMocker = new PlayerMocker(levelProvider, field.getAnnotation(MockPlayer.class));
            playerMocker.prepare();
            playerMocker.setInstance(testInstance);
            playerMocker.setInstanceField(field);
            session.players.computeIfAbsent(playerMocker.getPlayerName(), it-> new ArrayList<>()).add(playerMocker);
        }

        Map<Integer, Level> loadedLevels;
        if (session.serverMocker.getConfig().callsRealMethods()) {
            loadedLevels = session.serverMocker.getServer().getLevels();
        } else {
            loadedLevels = new LinkedHashMap<>();
        }

        levels.values().stream().flatMap(Collection::stream)
                .map(LevelMocker::getLevel)
                .forEachOrdered(mock-> loadedLevels.put(mock.getId(), mock));
        
        Level defaultLevel = levels.values().stream().flatMap(Collection::stream)
                .filter(mocker-> mocker.getConfig().isDefault())
                .map(LevelMocker::getLevel)
                .findFirst()
                .orElseGet(()-> levels.values().stream().flatMap(Collection::stream).findFirst().map(LevelMocker::getLevel).orElse(null));
        
        if (!session.serverMocker.getConfig().callsRealMethods()) {
            Server server = session.serverMocker.getServer();
            lenient().when(server.getLevel(anyInt())).thenAnswer(call-> loadedLevels.get(call.getArgument(0, Integer.class)));
            lenient().when(server.getLevelByName(anyString())).thenAnswer(call-> loadedLevels.values().stream()
                    .filter(level-> level.getName().equals(call.getArgument(0)))
                    .findFirst().orElse(null));
            
            lenient().when(server.isLevelLoaded(anyString())).thenAnswer(call-> loadedLevels.values().stream()
                    .anyMatch(level-> level.getName().equals(call.getArgument(0))));
            lenient().when(server.getLevels()).thenReturn(loadedLevels);
        }
        
        session.serverMocker.getServer().setDefaultLevel(defaultLevel);
        
        session.players.values().stream().flatMap(Collection::stream)
                .forEachOrdered(playerMocker -> execute(playerMocker::recreate));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            context.getStore(POWERNUKKIT).get(SESSION, Session.class).releaseResources();
        } finally {
            super.afterEach(context);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        context.getStore(POWERNUKKIT).remove(SESSION, Session.class).releaseResources();
        ServerMocker.setServerInstance(null);
    }

    private void initStatics(Server server) {
        execute(()-> {
            Method method = Server.class.getDeclaredMethod("registerEntities");
            method.setAccessible(true);
            method.invoke(server);
            
            method = Server.class.getDeclaredMethod("registerBlockEntities");
            method.setAccessible(true);
            method.invoke(server);
        });
        DataPacketManager.registerDefaultProcessors();
        Block.init();
        Enchantment.init();
        Potion.init();
        Effect.init();
        Attribute.init();
        Item.init();
        EnumBiome.values(); //load class, this also registers biomes
        DispenseBehaviorRegister.init();
        GlobalBlockPalette.getOrCreateRuntimeId(0, 0); //Force it to load

        LevelProviderManager.addProvider(null, Anvil.class);

        Generator.addGenerator(Flat.class, "flat", Generator.TYPE_FLAT);
        Generator.addGenerator(Normal.class, "normal", Generator.TYPE_INFINITE);
        Generator.addGenerator(Normal.class, "default", Generator.TYPE_INFINITE);
        Generator.addGenerator(Nether.class, "nether", Generator.TYPE_NETHER);
    }
}
