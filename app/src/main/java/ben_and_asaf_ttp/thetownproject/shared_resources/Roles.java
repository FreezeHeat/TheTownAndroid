/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

/**
 *  This {@code Enum} class holds all the constants used for roles in the game,
 * this is used by the server and client to identify and set roles in the game
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @see Game
 * @since 1.0
 */
public enum Roles implements Serializable{
    /**
     * Specifies that a user has the role of a CITIZEN
     */
    CITIZEN,

    /**
     * Specifies that a user has the role of a KILLER
     */
    KILLER,

    /**
     * Specifies that a user has the role of a HEALER
     */
    HEALER,

    /**
     * Specifies that a user has the role of a SNITCH
     */
    SNITCH,
    
    /**
     * Specifies that a user is DEAD
     */
    DEAD,
	
	/**
	 * Specifies that a user has no role
	 */
	NONE;
}
