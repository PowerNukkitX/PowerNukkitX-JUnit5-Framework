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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.apiguardian.api.API;
import org.powernukkit.tests.api.MockPlayer;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.powernukkit.tests.api.ReflectionUtil.*;

/**
 * @author joserobjr
 */
public class PlayerMocker extends Mocker<Player> {
    final Function<String, Level> levelSupplier;
    final MockPlayer config;

    Level level;
    Player player;
    String playerName;

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public PlayerMocker(Function<String, Level> levelSupplier) {
        this(levelSupplier, supply(()-> PowerNukkitExtension.class.getDeclaredField("defaults").getAnnotation(MockPlayer.class)));
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public PlayerMocker(Function<String, Level> levelSupplier, MockPlayer config) {
        this.config = config;
        this.levelSupplier = levelSupplier;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public void prepare() {
        level = levelSupplier.apply(config.level());
        String name = config.name();
        if (name.isEmpty()) {
            name = "TestPlayer"+ThreadLocalRandom.current().nextInt(0, 999999);
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }
        }
        playerName = name;
    }

    @Override
    public Player create() {
        Vector3 pos = AnnotationParser.parseVector3(config.position());
        int chunkX = pos.getChunkX();
        int chunkZ = pos.getChunkZ();
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = mock(BaseFullChunk.class);
            LevelProvider provider = mock(LevelProvider.class);
            lenient().when(provider.getChunk(eq(chunkX), eq(chunkZ))).thenReturn(chunk);
            lenient().when(provider.getLevel()).thenReturn(level);
            
            lenient().doCallRealMethod().when(chunk).setPosition(anyInt(), anyInt());
            chunk.setPosition(chunkX, chunkZ);
            lenient().when(chunk.getProvider()).thenReturn(provider);
        }

        /// Setup skin ///
        Skin skin = new Skin();
        skin.setSkinId("TestSkin"+ThreadLocalRandom.current().nextDouble());
        skin.setSkinData(new BufferedImage(64, 32, BufferedImage.TYPE_INT_BGR));
        assertTrue(skin.isValid());

        /// Make player login ///
        SourceInterface sourceInterface = mock(SourceInterface.class);
        long clientId = config.clientId();
        if (clientId == 0) {
            clientId = ThreadLocalRandom.current().nextLong();
        }

        String clientIp = config.clientIp();
        if (clientIp.isEmpty()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            clientIp = random.nextInt(1, 255)+"."
                    +random.nextInt(1, 255)+"."
                    +random.nextInt(1, 255)+"."
                    +random.nextInt(1, 255);
        }
        
        int clientPort = config.clientPort();
        if (clientPort == 0) {
            clientPort = ThreadLocalRandom.current().nextInt(1, 0xFFFF);
        }

        player = mock(Player.class, withSettings().defaultAnswer(CALLS_REAL_METHODS)
                .useConstructor(sourceInterface, clientId, clientIp, clientPort));
        
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.username = playerName;
        loginPacket.protocol = ProtocolInfo.CURRENT_PROTOCOL;
        loginPacket.clientId = clientId;
        loginPacket.clientUUID = config.clientUUID().length == 2? new UUID(config.clientUUID()[0], config.clientUUID()[1]) : UUID.randomUUID();
        loginPacket.skin = skin;
        loginPacket.putLInt(2);
        loginPacket.put("{}".getBytes());
        loginPacket.putLInt(0);
        execute(()-> setField(Server.getInstance(), Server.class.getDeclaredField("defaultLevel"), level));
        doCallRealMethod().when(Server.getInstance()).getDefaultLevel();
        doReturn(new Position(pos.x, pos.y, pos.z, level)).when(level).getSafeSpawn();
        player.handleDataPacket(loginPacket);
        assertNotNull(player.namedTag, ()-> "Failed to initialize the player mock for "+getPlayerName());
        execute(()-> {
            Method method = Player.class.getDeclaredMethod("completeLoginSequence");
            method.setAccessible(true);
            method.invoke(player);
        });

        assertTrue(player.isOnline(), "Failed to make the fake player login");

        execute(()-> {
            Method method = Player.class.getDeclaredMethod("doFirstSpawn");
            method.setAccessible(true);
            method.invoke(player);
        });
        
        player.yaw = config.yaw();
        player.pitch = config.pitch();
        player.setHealth(config.health());
        player.noDamageTicks = 0;
        
        return player;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public Player getPlayer() {
        return player;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public String getPlayerName() {
        return playerName;
    }
}
