package org.powernukkit.tests.mocks;

import cn.nukkit.Player;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;

import java.net.InetSocketAddress;

public class DelegatePlayer extends Player {

    public DelegatePlayer(SourceInterface interfaz, Long clientID, String ip, int port) {
        super(interfaz, clientID, ip, port);
    }

    public DelegatePlayer(SourceInterface interfaz, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, clientID, socketAddress);
    }

    @Override
    public boolean dataPacket(DataPacket packet) {
        if (!this.connected) {
            return false;
        }

        try (Timing ignored = Timings.getSendDataPacketTiming(packet)) {
            DataPacketSendEvent ev = new DataPacketSendEvent(this, packet);
            this.server.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dataPacketImmediately(DataPacket packet) {
        if (!this.connected) {
            return false;
        }

        try (Timing ignored = Timings.getSendDataPacketTiming(packet)) {
            DataPacketSendEvent ev = new DataPacketSendEvent(this, packet);
            this.server.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void forceDataPacket(DataPacket packet, Runnable callback) {}
}
