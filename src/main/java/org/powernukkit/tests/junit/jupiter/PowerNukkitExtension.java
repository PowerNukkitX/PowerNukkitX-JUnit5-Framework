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

import cn.nukkit.Player;
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
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.powernukkit.tests.api.ReflectionUtil.execute;
import static org.powernukkit.tests.api.ReflectionUtil.setField;

/**
 * @author joserobjr
 */
@API(since = "0.1.0", status = EXPERIMENTAL)
@MockServer(name = "TinyTestServer", initPrivateFields = false, callsRealMethods = false, createTempDir = false)
public class PowerNukkitExtension extends MockitoExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        MockServer config = testInstance.getClass().getAnnotation(MockServer.class);
        ServerMocker serverMocker = config != null? new ServerMocker(config) : new ServerMocker();
        Server server = serverMocker.create();
        serverMocker.setActive();
        
        initStatics(server);

        for (Field field : testInstance.getClass().getFields()) {
            if (field.isAnnotationPresent(MockPlayer.class) && field.getType().isAssignableFrom(Player.class)) {
                setField(testInstance, field, new PlayerMocker(field.getAnnotation(MockPlayer.class)).create());
            } else if (field.isAnnotationPresent(MockEntity.class) && field.getType().isAssignableFrom(Entity.class)) {
                setField(testInstance, field, new EntityMocker(field.getAnnotation(MockEntity.class)).create());
            } else if (field.isAnnotationPresent(MockLevel.class) && field.getType().isAssignableFrom(Level.class)) {
                setField(testInstance, field, new LevelMocker(field.getAnnotation(MockLevel.class)).create());
            }
        }
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

        Block.init();
        Enchantment.init();
        Item.init();
        EnumBiome.values(); //load class, this also registers biomes
        Effect.init();
        Potion.init();
        Attribute.init();
        DispenseBehaviorRegister.init();
        GlobalBlockPalette.getOrCreateRuntimeId(0, 0); //Force it to load

        LevelProviderManager.addProvider(null, Anvil.class);

        Generator.addGenerator(Flat.class, "flat", Generator.TYPE_FLAT);
        Generator.addGenerator(Normal.class, "normal", Generator.TYPE_INFINITE);
        Generator.addGenerator(Normal.class, "default", Generator.TYPE_INFINITE);
        Generator.addGenerator(Nether.class, "nether", Generator.TYPE_NETHER);
    }
}
