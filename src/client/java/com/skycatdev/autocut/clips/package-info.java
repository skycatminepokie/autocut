/**
 * To create a new {@link com.skycatdev.autocut.clips.ClipType} (with no extra config):<br>
 * 1. Create a class that {@code extends ClipType}<br>
 * 2. Give it an {@link net.minecraft.util.Identifier} and {@link com.mojang.serialization.Codec}<br>
 * 3. Give it a constructor to match the {@code Codec}<br>
 * 4. Fill in required overrides<br>
 * 5. Register it at startup with {@link com.skycatdev.autocut.clips.ClipTypes#registerClipType(net.minecraft.util.Identifier, com.mojang.serialization.Codec, java.util.function.Supplier)}<br>
 * 6. Create a clip when the time is right! Make sure to check if it's enabled first.<br>
 * @see com.skycatdev.autocut.clips.ShootPlayerClipType ShootPlayerClipType for a simple example
 * @see com.skycatdev.autocut.clips.TakeDamageClipType TakeDamageClipType for a slightly more complex example (extra config)
 */
package com.skycatdev.autocut.clips;