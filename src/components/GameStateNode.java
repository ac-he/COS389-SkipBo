package components;

import java.util.ArrayList;

import driver.SkipBoGameModel;

/**
 * Stores a GameStateModel, how it was reached, and an associated value, for use in priority queues
 * @author Anna Heebsh
 */
public class GameStateNode implements Comparable<GameStateNode> {

	private int value;
	private SkipBoGameModel game;
	private ArrayList<String> stepsTaken;
	
	/**
	 * Constructor
	 * @param value integer value for this node.
	 * @param game associated with this node.
	 * @param stepsTaken to reach this game state node.
	 */
	public GameStateNode(int value, SkipBoGameModel game, ArrayList<String> stepsTaken) {
		this.value = value;
		this.game = game;
		this.stepsTaken = stepsTaken;
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
	
	
	/**
	 * Get the steps taken to reach this game state
	 * @return the steps taken to get to this game state
	 */
	public ArrayList<String> getStepsTaken() {
		return stepsTaken;
	}

}
