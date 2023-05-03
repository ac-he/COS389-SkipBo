package users;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

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
		//System.out.println("	wait started");
		//game.firePropertyChange();
//		for(int i = 0; i < 1000000000; i++) {
//			ThreadLocalRandom.current().nextInt(0, 1000);
//		}
//		System.out.println("	wait stopped");
		game.discard("h0", "d4");
//		System.out.println("	wait started");
//		for(int i = 0; i < 1000000000; i++) {
//			ThreadLocalRandom.current().nextInt(0, 1000);
//		}
		//game.firePropertyChange();
		//System.out.println("	wait stopped");
	}

}
