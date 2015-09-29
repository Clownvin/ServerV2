package com.v2.packets;

import java.util.ArrayList;

import com.v2.packets.Packet;

public abstract class AbstractPacketHandler {
	protected static final ArrayList<PacketListener> PACKET_LISTENERS = new ArrayList<PacketListener>();

	public static PacketListener addPacketListener(PacketListener listener) {
		synchronized (PACKET_LISTENERS) {
			PACKET_LISTENERS.add(listener);
			return listener;
		}
	}

	public abstract boolean sendPacket(Packet p);

	public Packet getResponse(Packet p, PacketListener listener) throws ListenerTimeoutException {
		PacketListener responseListener = listener;
		if (listener.isSingleUse()) {
			responseListener = new PacketListener(listener.getProtocall(), listener.getRequest(), true);
		}
		sendPacket(p);
		return addPacketListener(responseListener).getPacket();
	}

	public static boolean checkPacketListeners(Packet p) {
		boolean returnValue = false;
		synchronized (PACKET_LISTENERS) {
			for (int i = 0; i < PACKET_LISTENERS.size(); i++) {
				if (PACKET_LISTENERS.get(i).checkConditional(p)) {
					PACKET_LISTENERS.get(i).setPacket(p);
					if (PACKET_LISTENERS.get(i).isSingleUse()) {
						PACKET_LISTENERS.remove(i);
						i--;
					}
					returnValue = true;
				}
			}
		}
		return returnValue;
	}
}
