package net.rizen.submarines.api.item;

/**
 * Marker interface for items that can be used as torpedo ammunition in submarines.
 * Custom item classes implementing this interface become compatible with the submarine weapon system.
 *
 * <p>Items implementing this interface are:
 * <ul>
 *   <li>Recognized as torpedo ammunition by {@link net.rizen.submarines.api.submarine.SubmarineWeaponSystem}</li>
 *   <li>Skipped when the submarine searches for fuel in {@link net.rizen.submarines.api.submarine.SubmarinePower}</li>
 *   <li>Counted and consumed when firing torpedoes</li>
 * </ul>
 *
 * <h2>Example Implementation:</h2>
 * <pre>{@code
 * public class CustomTorpedoItem extends Item implements TorpedoItem {
 *     public CustomTorpedoItem(Settings settings) {
 *         super(settings);
 *     }
 * }
 * }</pre>
 */
public interface TorpedoItem {
}
