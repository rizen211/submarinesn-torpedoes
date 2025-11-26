package net.rizen.submarines.api.submarine.sonar;

/**
 * Different types of contacts that sonar can detect underwater. The sonar system categorizes what it finds
 * so you can tell the difference between a threat like another submarine and harmless things like dropped items.
 */
public enum ContactType {
    /**
     * Small underwater creatures like fish, squid, and similar mobs.
     */
    SMALL_MOB,
    /**
     * Medium-sized entities that are not players or submarines.
     */
    MEDIUM_ENTITY,
    /**
     * Other players in the water.
     */
    PLAYER,
    /**
     * Dropped items floating in the water.
     */
    ITEM,
    /**
     * Other submarines. These are your primary threats.
     */
    SUBMARINE,
    /**
     * Solid terrain and block surfaces underwater.
     */
    TERRAIN
}
