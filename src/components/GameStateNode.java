package components;

import driver.SkipBoGameModel;

/**
 * Stores a GameStateModel and an associated value, for use in priority queues
 * @author Anna Heebsh
 */
public class GameStateNode implements Comparable<GameStateNode> {

	private int value;
	private SkipBoGameModel game;
	
	/**
	 * Constructor
	 * @param value integer value for this node.
	 * @param SkipBoGameModel game associated with this node.
	 */
	public GameStateNode(int value, SkipBoGameModel game) {
		this.value = value;
		this.game = game;
	}

	
	@Override
	public int compareTo(GameStateNode other) {
		return Math.abs(other.value - value);
	}
	
	
	/**
	 * Get the value associated with this node
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	
	/**
	 * Get the game model associated with this node
	 * @return the game
	 */
	public SkipBoGameModel getGame() {
		return game;
	}

}
