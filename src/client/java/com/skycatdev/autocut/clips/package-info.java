/**
 * To create a new {@link com.skycatdev.autocut.clips.ClipType}:<br>
 * 1. Create a class that {@code extends ClipType}<br>
 * 2. Give it an {@link net.minecraft.util.Identifier} and {@link com.mojang.serialization.Codec}<br>
 * 3. Give it a constructor to match the {@code Codec}<br>
 * 4. Fill in required overrides<br>
 * 5. Register it at startup with {@link com.skycatdev.autocut.clips.ClipTypes .registerClipType()} in {@link com.skycatdev.autocut.clips.ClipTypes}<br>
 * 6. Create a clip when the time is right!<br>
 * @see com.skycatdev.autocut.clips.ShootPlayerClipType for a simple example
 * @see com.skycatdev.autocut.clips.TakeDamageClipType for a slightly more complex example (extra config)
 */
package com.skycatdev.autocut.clips;