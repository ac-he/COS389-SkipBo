package users;

import java.util.ArrayList;

import driver.SkipBoGameModel;

public class PlayerAI extends Player {
	
	/**
	 * Constructor
	 * @param name of Player
	 */
	public PlayerAI(String name) {
		super(name);
	}

	/**
	 * Constructor
	 * @param name of Player
	 * @param PlayerColor associated with Player
	 */
	public PlayerAI(String name, PlayerColor color) {
		super(name, color);
	}
	
	
	@Override
	public PlayerType getPlayerType() {
		 return PlayerType.AI;
	}
	
	
	@Override
	public void takeAction(SkipBoGameModel game) {
		game.drawCards();
		game.discard("h0", "d4");
	}

}
