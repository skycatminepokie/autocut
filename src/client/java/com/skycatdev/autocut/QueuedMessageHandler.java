package com.skycatdev.autocut;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Utility for consistently sending messages to the player from another thread.
 */
public class QueuedMessageHandler implements ClientTickEvents.StartTick {
    protected ConcurrentLinkedQueue<Text> messageQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onStartTick(MinecraftClient client) {
        if (messageQueue.isEmpty()) return;
        do {
            client.inGameHud.getChatHud().addMessage(messageQueue.poll());
        } while (!messageQueue.isEmpty());
    }

    public void queueMessage(Text message) {
        messageQueue.add(message);
    }
}
