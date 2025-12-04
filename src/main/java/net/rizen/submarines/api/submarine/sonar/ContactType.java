package net.rizen.submarines.api.submarine.sonar;

/**
 * Different types of contacts that sonar can detect underwater. The sonar system categorizes detected entities
 * to distinguish between threats like other submarines and harmless objects like dropped items.
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
     * Other submarines. Primary threats in underwater combat.
     */
    SUBMARINE,
    /**
     * Solid terrain and block surfaces underwater.
     */
    TERRAIN
}
