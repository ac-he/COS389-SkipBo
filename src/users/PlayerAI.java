package users;

import java.util.ArrayList;
import java.util.PriorityQueue;

import components.Card;
import components.GameStateNode;
import driver.SkipBoGameModel;

public class PlayerAI extends Player {
	
	private ArrayList<String> turnLog;
	private final int logLevel = 1; 
	// 0 ----- need to know. user-level stuff. big actions.
	// 1 ----- important but non-essential
	// 2 ----- "the junk messages" if debugging needs to happen
	
	private final String[] playFrom = {"h0", "h1", "h2", "h3", "h4", "d1", "d2", "d3", "d4"};
	private final String[] playTo = {"f1", "f2", "f3", "f4"};
	
	private Boolean lastStockAttemptWorked;
	
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
	
	
	public PlayerAI(Player player) {
		super(player);
	}

	@Override
	public PlayerType getPlayerType() {
		 return PlayerType.AI;
	}
	
	
	@Override
	public SkipBoGameModel takeTurn(SkipBoGameModel game) {
		turnLog = new ArrayList<String>();
		
		SkipBoGameModel workingGame = new SkipBoGameModel(game);
		
		boolean haltConditionNotMet = true;
		while(haltConditionNotMet) {
			// First, try to draw cards.
			try {
				int c = workingGame.drawCards();	
				waitForConfirmation();
				log(c + " cards drawn", 0);
				continue;
			} catch (RuntimeException e){
				waitForConfirmation();
				log("(Cannot draw cards now)", 1);
			}
			SkipBoGameModel stockResult = playStockIfPossible(workingGame);
			if(lastStockAttemptWorked) {
				workingGame = new SkipBoGameModel(stockResult);
				waitForConfirmation();
				log("Was able to play from stock.", 0);
				continue;
			} else {
				waitForConfirmation();
				log("Was not able to play from the stock.", 1);
			}
			
			haltConditionNotMet = false;
		}

		log("before discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);
		
		String discardInfo = decideOnDiscard(workingGame);
		String hand = discardInfo.substring(0, 2);
		String pile = discardInfo.substring(2);
		
		log("Discarded " + workingGame.currentPlayer().hand.getAt(hand.charAt(1) - 48).name() 
				+ " from " + hand +" to " + pile, 0);
		workingGame.discard(hand, pile);
	
		log("after discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);
		
		return workingGame;
	}
	
	
	private String decideOnDiscard(SkipBoGameModel game) {
		ArrayList<Card> hand = game.currentPlayer().hand.getHand();
		
		char[] discardIndices = {'1', '2', '3', '4'};
		
		log(game.currentPlayer().hand.toString(), 1);
		
		// STRATEGY 1:
		// If any one of my cards equals any of the discard tops, put this on top of it.
		int i = 0;
		for (Card h : hand) {
			if(h != Card.SKIPBO_UNPLAYED) {
				for(char d : discardIndices) {
					if(!game.discardIsEmpty(true, d) && game.getDiscardTop(true, d) == h) {
						log("discarding with strategy 1", 1);
						return "h" + i + "d" + d;
					}
				}
			}
			i++;
		}
		
		// STRATEGY 2:
		// If this card is 1 less than any of the discard tops, put this on top of it.
		i = 0;
		for (Card h : hand) {
			if(h != Card.SKIPBO_UNPLAYED) {
				for(char d : discardIndices) {
					if(!game.discardIsEmpty(true, d) && 
							game.getDiscardTop(true, d).getValue() == (h.getValue() + 1)) {
						log("discarding with strategy 2", 1);
						return "h" + i + "d" + d;
					}
				}
			}
			i++;
		}
		
		// STRATEGY 3:
		// If there is an empty discard pile and I have a "mode" card, play the "mode" card on it
		char emptyPile = '0';
		for(char d : discardIndices) {
			if(game.discardIsEmpty(true, d)) {
				emptyPile = d;
				break;	
			}
		}
		if(emptyPile != '0') {
			int[] cardCounts = {0, 0, 0, 0,  0, 0, 0,  0, 0, 0,  0, 0, 0};
			for(Card h : hand) {
				if(h != Card.SKIPBO_UNPLAYED) {
					cardCounts[h.getValue()]++;
				}
			}
			int highestIndex = 0;
			for(int c = 1; c < 13; c++) {
				if(cardCounts[c] > cardCounts[highestIndex]) {
					highestIndex = c;
				}
			}
			if(cardCounts[highestIndex] > 1) {
				i = 0;
				for(Card h : hand) {
					if(h != Card.SKIPBO_UNPLAYED && h.getValue() == highestIndex) {
						log("discarding with strategy 3", 1);
						log("card counts: ", 1);
						for(int c : cardCounts) {
							log("    " + c, 1);
						}
						return "h" + i + "d" + emptyPile;
					}
					i++;
				}
			}
		}
		
		// STRATEGY 4:
		// If there is an empty discard pile, play my highest card on it.
		// (This one uses the same empty pile detection as Strategy 3
		if(emptyPile != '0') {
			i = 0;
			int highestIndex = 0;
			for (Card h : hand) {
				if(h != Card.SKIPBO_UNPLAYED) {
					if(hand.get(highestIndex).getValue() < h.getValue()) {
						highestIndex = i;
					}
				}
				i++;
			}
			log("discarding with strategy 4", 1);
			return "h" + highestIndex + "d" + emptyPile;
		}
		
		// STRATEGY 5:
		// If this card is 2 less than any of the discard tops, put this on top of it.
		i = 0;
		for (Card h : hand) {
			if(h != Card.SKIPBO_UNPLAYED) {
				for(char d : discardIndices) {
					if(!game.discardIsEmpty(true, d) && 
							game.getDiscardTop(true, d).getValue() == (h.getValue() + 2)) {
						log("discarding with strategy 5", 1);
						return "h" + i + "d" + d;
					}
				}
			}
			i++;
		}
		
		// If this card is 3 less than any of the discard tops, put this on top of it.
		i = 0;
		for (Card h : hand) {
			if(h != Card.SKIPBO_UNPLAYED) {
				for(char d : discardIndices) {
					if(!game.discardIsEmpty(true, d) && 
							game.getDiscardTop(true, d).getValue() == (h.getValue() + 3)) {
						log("discarding with strategy 5", 1);
						return "h" + i + "d" + d;
					}
				}
			}
			i++;
		}
		
		// Just discard at random if good moves weren't available :(
		log("discarding with strategy 0", 1);
		return "h0d4";
	}
	
	
	/**
	 * Plays from the stock if possible
	 * @param game state to work off of
	 * @return the resulting game state
	 */
	private SkipBoGameModel playStockIfPossible(SkipBoGameModel game) {
		SkipBoGameModel testGame = new SkipBoGameModel(game);
		char[] tryAt = {'1', '2', '3', '4'};
		
		ArrayList<SkipBoGameModel> seenBefore = new ArrayList<SkipBoGameModel>();
		
		PriorityQueue<GameStateNode> queue = new PriorityQueue<GameStateNode>();
		queue.add(new GameStateNode(evaluateScore(testGame), testGame));
		
		while(!queue.isEmpty()) {
			GameStateNode curNode = queue.remove();
			SkipBoGameModel curGame = curNode.getGame();
			log("--------------------------------------------------", 2);
			log("--------------------------------------------------", 2);
			log("Removing new node from queue. New Length: " + queue.size(), 1);
			log("--------------------------------------------------", 2);
			log("--------------------------------------------------", 2);
			log(curGame.toString(), 2);
			for(char cur : tryAt) {
				log("looking at nodes", 1);
				waitForConfirmation();
				try {
					log("TRYING TO PLAY STOCK: ss to f" + cur, 1);
					curGame.play("ss", "f" + cur);
					log("MOVE ACCPTED.", 1);
					waitForConfirmation();
					lastStockAttemptWorked = true;
					return curGame;
				} catch (Exception e){
					log("Could not play stock on " + cur + ": " + e, 2);
					log("MOVE REJECTED.", 1);
					waitForConfirmation();
				}
			}
			log("ABOUT TO START LOOPING THROUGH ALL POSSIBLE PLAYS", 1);
			waitForConfirmation();
			for(String from : playFrom) {
				for(String to : playTo) {
					waitForConfirmation();;
					log("Checking new node: Playing from " + from + " to " + to, 1);
					try {
						SkipBoGameModel newGame = new SkipBoGameModel(curGame);
						newGame.play(from, to);
						log("Heading into Seen Before loop. " + seenBefore.size(), 2);
						for(SkipBoGameModel model : seenBefore) {
							waitForConfirmation();
							log("checking if seen before", 1);
							log("result of check: " + model.equals(newGame), 2);
							if(model.equals(newGame)) {
								waitForConfirmation();
								log("not including previously seen node", 1);
								break;
							}
							log("including never-before-seen node. Queue is now " + seenBefore.size() + 
									" items long.", 1);
						}
						queue.add(new GameStateNode(evaluateScore(newGame), newGame));
						seenBefore.add(newGame);
						waitForConfirmation();
						log(newGame.toString(), 1);
						log("Adding Children to Stock Queue: Could play " + from + " to " + to, 2);
					} catch (Exception e) {
						waitForConfirmation();
						log("Adding Children to Stock Queue: Could not play " + from + " to " + to +
								": " + e.getMessage() , 2);
						if(e.getMessage() == null) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		lastStockAttemptWorked = false;
		return testGame;
	}
	
	
	/**
	 * Evaluates the worth of a particular game by comparing the stock height to the heights of 
	 * the foundations. The closer a stock is to the foundation, the more valuable it is
	 * @param testGame the game to score
	 * @return the score of this game
	 */
	private int evaluateScore(SkipBoGameModel testGame) {
		char[] tryAt = {'1', '2', '3', '4'};
		int currentLowest = 6;
		Integer stockTop = testGame.getStockTop(true).getValue();
		if(stockTop == null) {
			return 0;
		}
		for(char cur : tryAt) {
			int fndnTop;
			try {
				fndnTop = testGame.getFoundationTop(cur).getValue();
				
			} catch (Exception e){
				fndnTop = 12;
			}
			int difference = (stockTop - fndnTop + 11) % 12;
			if (difference < currentLowest) {
				currentLowest = difference;
			}
		}
		return currentLowest;
	}
	
	
	/**
	 * Logs a message for this game
	 * 0: need to know. user-level stuff. big actions.
	 * 1: important but non-essential
	 * 2: "the junk messages" if debugging needs to happen
	 * @param message the message to log
	 * @param level the level at which to log this.
	 */
	private void log(String message, int level) {
		if(level <= logLevel) {
			System.out.println(message);
			turnLog.add(message);
		}
	}

	
	/**
	 * Debugging utility. Uncomment the lines and this will allow for slow traversal of the logs.
	 */
	private void waitForConfirmation() {
//		Scanner scanner = new Scanner(System.in);
//		String latestInput = scanner.nextLine();
	}
}
