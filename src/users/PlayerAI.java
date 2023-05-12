package users;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

import components.Card;
import components.GameStateNode;
import driver.SkipBoGameModel;

public class PlayerAI extends Player {
	
	private ArrayList<String> turnLog;
	private final int logLevel = 0; 
	// 0 ----- need to know. user-level stuff. big actions.
	// 1 ----- important but non-essential
	// 2 ----- "the junk messages" if debugging needs to happen
	
	private final String[] playFrom = {"h0", "h1", "h2", "h3", "h4", "d1", "d2", "d3", "d4"};
	private final String[] playTo = {"f1", "f2", "f3", "f4"};
	
	private Boolean lastStockAttemptWorked;
	private Boolean lastOutOfHandAttemptWorked;
	
	/**
	 * Constructor
	 * @param name of Player
	 */
	public PlayerAI(String name) {
		super(name);
		turnLog = new ArrayList<String>();
	}

	/**
	 * Constructor
	 * @param name of Player
	 * @param PlayerColor associated with Player
	 */
	public PlayerAI(String name, PlayerColor color) {
		super(name, color);
		turnLog = new ArrayList<String>();
	}
	
	
	/**
	 * Copy constructor
	 * @param player to copy
	 */
	public PlayerAI(Player player) {
		super(player);
		try {
			turnLog = player.getMostRecentLogs();
		} catch (Exception e) {
			// Tried to copy a non-AI player.
			e.printStackTrace();
		}
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
			// Zeroth, check if the stock is empty
			if(workingGame.getStockTop(true) == null) {
				return workingGame;
			}
			
			// First, try to draw cards.
			try {
				int c = workingGame.drawCards();	
				waitForConfirmation();
				log(c + " cards drawn.", 0);
				continue;
			} catch (RuntimeException e){
				waitForConfirmation();
				log("(Cannot draw cards now)", 1);
			}
			
			// Second, try playing out of the stock.
			SkipBoGameModel stockResult = breadthFirstSearch(workingGame, "stock");
			if(lastStockAttemptWorked) {
				workingGame = new SkipBoGameModel(stockResult);
				waitForConfirmation();
				log("A " + workingGame.getStockTop(true) + " was uncovered on the Stock Pile.", 0);
				continue;
			} else {
				waitForConfirmation();
				log("Was not able to play from the stock.", 1);
			}
			
//			// Then, try to completely use up the cards in the hand.
//			SkipBoGameModel outOfHandResult = playOutOfHandIfPossible(workingGame);
			if(lastOutOfHandAttemptWorked) {
//				workingGame = new SkipBoGameModel(outOfHandResult);
				waitForConfirmation();
				log("Was able to play from hand.", 0);
				continue;
			} else {
				waitForConfirmation();
				log("Was not able to play all cards from the hand.", 1);
			}
			
			haltConditionNotMet = false;
		}

		log("before discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);
		
		String discardInfo = decideOnDiscard(workingGame);
		String hand = discardInfo.substring(0, 2);
		String pile = discardInfo.substring(2);
		
		log("Discarded " + workingGame.currentPlayer().hand.getAt(hand.charAt(1) - 48).name() 
				+ " from " + hand +" to " + pile + ".", 0);
		workingGame.discard(hand, pile);
	
		log("after discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);

		return workingGame;
	}

	
	/**
	 */
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
					if(hand.get(highestIndex) != null && hand.get(highestIndex).getValue() < h.getValue()) {
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
	 * Uses a BFQ to accomplish a goal
	 * @param game state to work off of
	 * @param the goal or type of this search ["stock", "hand0", "block", "extra"]
	 * @return the resulting game state
	 */
	private SkipBoGameModel breadthFirstSearch(SkipBoGameModel game, String type) {
		if(!type.matches("^(stock)|(hand0)|(block)|(extra)$")) {
			return game;
		}
		
		SkipBoGameModel testGame = new SkipBoGameModel(game);
		char[] tryAt = {'1', '2', '3', '4'};
		
		ArrayList<SkipBoGameModel> seenBefore = new ArrayList<SkipBoGameModel>();
		ArrayList<String> steps = new ArrayList<String>();
		//steps.add("This node originated from the first node.");
		
		PriorityQueue<GameStateNode> queue = new PriorityQueue<GameStateNode>();
		queue.add(new GameStateNode(evaluateScoreOnStock(testGame), testGame, steps));
		
		while(!queue.isEmpty()) {
			GameStateNode curNode = queue.remove();
			SkipBoGameModel curGame = curNode.getGame();
			log("\n\n", 1);
			log("Removing new node from " + type + " queue. New Length: " + queue.size(), 1);
			log(curGame.toString(), 2);
			for(String step : curNode.getStepsTaken()) {
				log(step, 2);
			}
			
			if(type.matches("stock")) {
				for(char cur : tryAt) {
					log("looking at nodes", 1);
					waitForConfirmation();
					
					try {
						log("TRYING TO PLAY STOCK: ss to f" + cur, 1);
						
						Card stockTop = curGame.getStockTop(true);
						curGame.play("ss", "f" + cur);
						lastStockAttemptWorked = true;
						
						log("\nabout to start popping nodes", 1);
						
						while(!curNode.getStepsTaken().isEmpty()) {
							String step = curNode.getStepsTaken().remove(curNode.getStepsTaken().size() - 1);
							if(!step.isBlank()) {
								log(step, 0);
							}
						}
						
						String skipBo = "";
						if(stockTop == Card.SKIPBO_UNPLAYED) {
							skipBo = "The Skip-Bo became a " + curGame.getFoundationTop(cur) + ".";
						}
	
						log("Played " + stockTop + " from stock to f" + cur + ". " + skipBo, 0);
						log("MOVE ACCPTED.", 1);
						waitForConfirmation();
						
						return curGame;
						
					} catch (Exception e){
						log("Could not play stock on " + cur + ": " + e, 2);
						log("MOVE REJECTED.", 1);
						waitForConfirmation();
					}
				}
			} else if(type.matches("hand0")) {
				
			} else if(type.matches("block")) {
				
			} else if(type.matches("extra")) {
				
			}
			
			log("ABOUT TO START LOOPING THROUGH ALL POSSIBLE PLAYS", 1);
			waitForConfirmation();
			
			for(String from : playFrom) {
				for(String to : playTo) {
					
					waitForConfirmation();
					log("Checking new node: from " + from + " to " + to, 1);	
					log("Move sequence for parent node (" + curGame.getId() + "): ", 2);
					for(String move : curNode.getStepsTaken()) {
						log(move, 2);
					}
					
					
					try { 
						SkipBoGameModel newGame = new SkipBoGameModel(curGame);
						
						Card fromCard = newGame.getCardAt(true, from);
						newGame.play(from, to);
						
						log("Heading into Seen Before loop. " + seenBefore.size(), 3);
						
						boolean skip = false;
						for(SkipBoGameModel model : seenBefore) {
							//waitForConfirmation();
							log("checking if seen before", 3);
							log("result of check: " + model.equals(newGame), 3);
							
							if(model.equals(newGame)) {
								//waitForConfirmation();
								log("not including previously seen node", 3);
								
								skip = true;
								break;
							}
						}
						
						if(skip) {
							continue;
						}
						
						seenBefore.add(newGame);
						log("including never-before-seen node. Queue is now " + seenBefore.size() + 
								" items long.", 1);
						
						ArrayList<String> addMoveSequence = new ArrayList<String>();
						ArrayList<String> stepsToCopy = new ArrayList<String>();
						stepsToCopy.addAll(curNode.getStepsTaken());
						
						while(!curNode.getStepsTaken().isEmpty()) {
							String step = stepsToCopy.remove(stepsToCopy.size() - 1);
							if(!step.isBlank()) {
								addMoveSequence.add(step);
							}
						}
						
						String fromString = from;
						if(from.matches("h[0-4]")) {
							fromString = "hand";
						}
						String skipBo = "";
						if(fromCard == Card.SKIPBO_UNPLAYED) {
							if(newGame.getFoundationTop(to.charAt(1)) == null) {
								skipBo = " That stack is now empty.";
							} else {
								skipBo = " The Skip-Bo became a " + newGame.getFoundationTop(to.charAt(1)) + ".";
							}
						}
						addMoveSequence.add("Played " + fromCard + " from " + fromString + " to " + to 
								+ ". " + skipBo);
						
						int score = 0;
						switch(type) {
							case "stock":
								score = evaluateScoreOnStock(newGame);
								break;
							case "hand0":
								score = evaluateScoreOnHand(newGame);
								break;
							case "block":
							case "extra":
								score = 0;
								break;
						};
						
						queue.add(new GameStateNode(score, newGame, addMoveSequence));
						
						waitForConfirmation();
						log("Adding Children to Queue: Could play " + from + " to " + to, 2);
						log("Move sequence for this node ("+ newGame.getId() +"): ", 2);
						for(String move : addMoveSequence) {
							log(move, 2);
						}
					} catch (Exception e) {
						waitForConfirmation();
						log("Adding Children to Queue: Could not play " + from + " to " + to +
								": " + e.getMessage() , 3);
						if(e.getMessage() == null) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		if(type.matches("stock")) {
			lastStockAttemptWorked = false;
		} else if (type.matches("hand0")) {
			lastOutOfHandAttemptWorked = false;
		} else if(type.matches("block")) {
			
		} else if (type.matches("extra")) {
			
		}
		
		return testGame;
	}
	
	
	/**
	 * Evaluates the worth of a particular game by comparing the stock height to the heights of 
	 * the foundations. The closer a stock is to the foundation, the more valuable it is
	 * @param testGame the game to score
	 * @return the score of this game
	 */
	private int evaluateScoreOnStock(SkipBoGameModel testGame) {
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
	 * Evaluates the worth of a particular game by comparing the number of cards that have been played.
	 * Fewer cards is better.
	 * @param testGame the game to score
	 * @return the score of this game
	 */
	private int evaluateScoreOnHand(SkipBoGameModel testGame) {
		return testGame.getHandCountAsInt(true);
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
			// System.out.println(message);
			turnLog.add(message);
		}
	}
	
	
	/**
	 * Gets the most recent turn log. It clears it and returns a copy.
	 * @return ArrayList<String> describing all the turn actions for this AI player
	 */
	public ArrayList<String> getMostRecentLogs() {
		return turnLog;
	}

	
	/**
	 * Debugging utility. Uncomment the lines and this will allow for slow traversal of the logs.
	 */
	@SuppressWarnings("unused") // This won't be called unless I am actively trying to debug and have set the log level to 2
	private void waitForConfirmation() {
		if(2 <= logLevel) {
			Scanner scanner = new Scanner(System.in);
			String latestInput = scanner.nextLine();
		}
	}
}
