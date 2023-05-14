package users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

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
		log("starting to Take Turn!",  2);
		
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
				log(workingGame.currentPlayer().hand.toString(), 1);
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
				if(workingGame.getStockTop(true) == null) {
					log("The stock pile is now empty.", 0);
					log(getName() + " has won!", 0);
				} else {
					log("A " + workingGame.getStockTop(true) + " was uncovered on the Stock Pile.", 0);
				}
				continue;
			} else {
				waitForConfirmation();
				log("Was not able to play from the stock.", 1);
			}
			
			// Then, try to completely use up the cards in the hand.
			log("continuing on to try to play hand out", 2);
			SkipBoGameModel outOfHandResult = breadthFirstSearch(workingGame, "hand0");
			if(lastOutOfHandAttemptWorked) {
				workingGame = new SkipBoGameModel(outOfHandResult);
				waitForConfirmation();
				log("Was able to play every card from hand.", 0);
				continue;
			} else {
				waitForConfirmation();
				log("Was not able to play all cards from the hand.", 1);
			}
			
			haltConditionNotMet = false;
		}
		
		// If no more of those actions can be taken, block the other player from playing their stock card. 
		workingGame = breadthFirstSearch(workingGame, "block");
		
		// Then, play anything that can be played from the hand 
		// (will automatically not allow states that let the other player play their stock card.) 
		workingGame = breadthFirstSearch(workingGame, "extra");

		log("before discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);
		
		// Prepare discard information
		String discardInfo = decideOnDiscard(workingGame);
		String hand = discardInfo.substring(0, 2);
		String pile = discardInfo.substring(2);
		
		log("Discarded " + workingGame.currentPlayer().hand.getAt(hand.charAt(1) - 48).name() 
				+ " from Hand to Discard " + pile.charAt(1) + ".", 0);
		
		workingGame.discard(hand, pile);
	
		log("after discarding, the game I am giving back expects it to be " + 
				workingGame.currentPlayer().getName() + "'s turn. ", 2);

		return workingGame;
	}

	
	/**
	 * Makes an informed decision on how to discard based on the cards in the hand and discard piles.
	 * @param game the Game to make a decision with
	 * @return string matching h[0-5]d[1-4] describing the determined-upon move.
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
					try {
						if(hand.get(highestIndex).getValue() < h.getValue()) {
							highestIndex = i;
						}
					} catch (Exception e) {
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
		// Validate input
		if(!type.matches("^(stock)|(hand0)|(block)|(extra)$")) {
			log("invalid input type for BFS method: " + type, 1);
			return game;
		}
		log("BFSing on " + type, 1);
		
		// Make a copy of the game to work of 
		SkipBoGameModel testGame = new SkipBoGameModel(game);
	
		// List of foundations to try at.
		char[] tryAt = {'1', '2', '3', '4'};
		
		// To store previously seen game states.
		ArrayList<SkipBoGameModel> seenBefore = new ArrayList<SkipBoGameModel>();
		PriorityQueue<GameStateNode> queue = new PriorityQueue<GameStateNode>();
		
		// Add the initial state to the queue, along with an empty list of steps
		ArrayList<String> steps = new ArrayList<String>();
		queue.add(new GameStateNode(evaluateScoreOnStock(testGame), testGame, steps));
		
		// Here's the actual queue
		while(!queue.isEmpty()) {
			// Logging
			waitForConfirmation();
			log("\n\n", 1);
			log("What's in the queue?", 2);
			for(GameStateNode node : queue) {
				log("    " + node.getGame().getId() + ": " + node.getValue(), 2);
			}
			waitForConfirmation();
			
			// Get the new node
			GameStateNode curNode = queue.remove();
			SkipBoGameModel curGame = curNode.getGame();
			
			// Logging
			log("\n\n", 1);
			log("Removing new node from " + type.toUpperCase() + " queue. New Length: " + queue.size(), 1);
			log(curGame.toString(), 2);
			for(String step : curNode.getStepsTaken()) {
				log(step, 2);
			}
			
			// Here are the base cases for each of the different use cases of this method. 
			// Base Case: Stock
			if(type.matches("stock")) {
				// Find the stock top, in case we need to log it later
				Card stockTop = curGame.getStockTop(true);
				
				// Store which pile we end up playing to
				char pileIndex = 'x'; // junk value. won't do anything if the program doesn't overwrite it.
				
				if(stockTop == Card.SKIPBO_UNPLAYED) {
					log("stock top is a skip bo. Getting a random pileIndex", 2);
					// If the stock is a skip bo, pick a stack at random to place it at.
					// This does not necessarily consider 4 different places. 
					//It counts how many unique foundation tops there are.
					HashMap<Integer, Character> uniqueTops = new HashMap<Integer, Character>();
					for(char cur : tryAt) {
						int cardValue;
						try {
							cardValue = game.getFoundationTop(cur).getValue();
						} catch (NullPointerException e) {
							cardValue = 0;
						}
						if(!uniqueTops.containsKey(cardValue)) {
							uniqueTops.put(cardValue, cur);
						}
					}
					
					log("found " + uniqueTops.size() + " unique tops", 2);
			
					int rand = ThreadLocalRandom.current().nextInt(uniqueTops.size());
					pileIndex = (char) uniqueTops.values().toArray()[rand];
					
					log("pile index selected for SkipBo: " + pileIndex, 2);
					
					curGame.play("ss", "f" + pileIndex);

					log("played there: " + pileIndex, 2);
					
				} else {
					// For each of the four foundations, try to play the top Stock Card there.
					for(char cur : tryAt) {
						// Logging
						log("TRYING TO PLAY STOCK: ss to f" + cur, 1);
						
						try {
							// The moment of truth: try to play this card
							curGame.play("ss", "f" + cur);

							// If we passed that last line, then this attempt did work. 
							log("Stock move accepted.", 1);
							pileIndex = cur;
							break;
							
						} catch (Exception e){
							// It will err of the move was not valid. 
							log("Could not play stock on " + cur + ": " + e, 2);
							log("Stock move rejected.", 2);
							waitForConfirmation();
						}
					}
				}
				
				if((pileIndex + "").matches("[1-4]")) {
					log("Picked pile to play stock on: f" + pileIndex, 1);
					
					lastStockAttemptWorked = true;
					
					// Important logging actions!
					// Add the steps to reach this node to the official list of actions that happened on this turn. 
					// Reverse the order of the steps nodes as they are added
					while(!curNode.getStepsTaken().isEmpty()) {
						String step = curNode.getStepsTaken().remove(curNode.getStepsTaken().size() - 1);
						if(!step.isBlank()) {
							log(step, 0);
						}
					}
					
					// Log information about the stock top played as well.
					String skipBo = "";
					if(stockTop == Card.SKIPBO_UNPLAYED) {
						skipBo = "The Skip-Bo became a " + curGame.getFoundationTop(pileIndex) + ".";
					}
					log("Played " + stockTop + " from Stock to Foundation " + pileIndex + ". " + skipBo, 0);
					waitForConfirmation();
					
					// Return this game state so that it can become the new official one!
					return curGame;
				}
				
			// Base Case: Hand0 (Play out all cards in hand)	
			} else if(type.matches("hand0")) {
				if(curGame.getHandCountAsInt(true) == 0) {
					// If we passed that last line, then this attempt did work. Set flag.
					lastOutOfHandAttemptWorked = true;
					
					// Important logging actions!
					// Add the steps to reach this node to the official list of actions that happened on this turn. 
					// Reverse the order of the steps nodes as they are added
					while(!curNode.getStepsTaken().isEmpty()) {
						String step = curNode.getStepsTaken().remove(curNode.getStepsTaken().size() - 1);
						if(!step.isBlank()) {
							log(step, 0);
						}
					}
					
					log("Played all five cards." , 0);
					log("MOVE ACCPTED.", 1);
					waitForConfirmation();
					
					// Return this game state so that it can become the new official one!
					return curGame;
				}
				
			// Base Case: Block
			} else if(type.matches("block")) {
				// If the other person's stock card is a skip-bo, we won't even bother
				if(curGame.getStockTop(false) != Card.SKIPBO_UNPLAYED) {
					// Get the other stock top
					int otherStock = curGame.getStockTop(false).getValue();
					
					// We'll start looking for piles that are too close.
					boolean anyClose = false;
					for (char c : tryAt) {
						try {
							// For piles with cards in them
							if(curGame.getFoundationTop(c).getValue() + 1 == otherStock) {

								log("Blocking: Close on f" + c, 1);
								anyClose = true;
							}
						} catch (NullPointerException e) {
							// For piles without cards in them
							if(otherStock == 1) {
								anyClose = true;
							}
						}
					}
					
					// If we found no close ones and everything has been successfully blocked, 
					if(!anyClose) {
						log("Successfully blocked opponent, or there was nothing worth blocking.", 1);
						// Important logging actions!
						// Add the steps to reach this node to the official list of actions that happened on this turn. 
						// Reverse the order of the steps nodes as they are added
						while(!curNode.getStepsTaken().isEmpty()) {
							String step = curNode.getStepsTaken().remove(curNode.getStepsTaken().size() - 1);
							if(!step.isBlank()) {
								log(step, 0);
							}
						}
						
						// Return this game state
						return curGame;
					}
				} else {
					log("Not blocking because opponent has a skip-bo at the top of the stock.", 2);
					return game; 
				}
				
			// Base Case: Extra
			} else if(type.matches("extra")) {
				// THe base case for this happens after
			}
			
			// More logging
			log("ABOUT TO START LOOPING THROUGH ALL POSSIBLE PLAYS", 1);
			waitForConfirmation();
			
			// Loop through all possible ways to play cards from Discard Piles and Hand to foundations
			ArrayList<String> playsThatWorked = new ArrayList<String>(); // Used to keep track of plays for some BFS use cases.
			for(String from : playFrom) {
				for(String to : playTo) {
					// Logging
					waitForConfirmation();
					log("Checking new node: from " + from + " to " + to, 1);	
					log("Move sequence for parent node (" + curGame.getId() + "): ", 2);
					for(String move : curNode.getStepsTaken()) {
						log(move, 2);
					}
					
					// Try to execute this combination of to and from.
					try { 
						// Copy the game so that we can create a new hypothetical game based on it.
						SkipBoGameModel newGame = new SkipBoGameModel(curGame);
						
						// Get information about the "From" Card in case we need it for logging later.
						Card fromCard = newGame.getCardAt(true, from);
						
						// We won't include any nodes that are using skip-bos if the goal is
						// to use up the cards in the hand.
						if(type.matches("extra") && fromCard == Card.SKIPBO_UNPLAYED) {
							continue;
						}
						
						// Actually try to play this.
						newGame.play(from, to);
						
						// If we passed that line, the move was valid. 
						playsThatWorked.add(from + to);
						log("That worked! Heading into Seen Before loop. " + seenBefore.size(), 3);
						
						// Check if this game state has been seen before, and if so, skip it. 
						boolean skip = false;
						for(SkipBoGameModel model : seenBefore) {
							log("checking if seen before", 3);
							log("result of check: " + model.equals(newGame), 3);
							if(model.equals(newGame)) {
								log("not including previously seen node", 2);
								skip = true;
								break;
							}
						}
						if(skip) {
							continue;
						}
						
						// If we passed that, this is a unique game state and we will keep it around.
						seenBefore.add(newGame);
						log("including never-before-seen node. Queue is now " + seenBefore.size() + 
								" items long.", 1);
						
						// Transfer the list of turns to the next node, for later logging. 
						ArrayList<String> addMoveSequence = new ArrayList<String>();
						addMoveSequence.addAll(curNode.getStepsTaken());
						log("The log has been copied.", 2);
						
						// Construct a string describing this latest move to add to the log
						String fromString = from;
						if(from.matches("h[0-4]")) {
							fromString = "Hand";
						} else if(from.matches("d[1-4]")) {
							fromString = "Discard " + from.charAt(1);
						}
						String toString = to;
						if(to.matches("f[1-4]")) {
							toString = "Foundation " + to.charAt(1);
						}
						
						String skipBo = "";
						if(fromCard == Card.SKIPBO_UNPLAYED) {
							if(newGame.getFoundationTop(to.charAt(1)) == null) {
								skipBo = " That Stock is now empty.";
							} else {
								skipBo = " The Skip-Bo became a " + newGame.getFoundationTop(to.charAt(1)) + ".";
							}
						}
						
						// Actually add the description of this turn to the turn list, for future logging
						addMoveSequence.add(0, "Played " + fromCard + " from " + fromString + " to " + toString 
								+ ". " + skipBo);
						
						// Figure out how to score this node, for the priority queue. 
						int score = 0;
						switch(type) {
							case "stock":
								score = evaluateScoreOnStock(newGame);
								break;
							case "hand0":
							case "extra":
								score = evaluateScoreOnHand(newGame);
								break;
							case "block":
								score = evaluateScoreOnBlock(newGame);
								break;
						};
						
						// Penalize any solution that used a skip bo to get there. 
						// This will discourage using skip-bos up early in the turn.
						if(fromCard == Card.SKIPBO_UNPLAYED) {
							score++;
						}
						log("Score of this node: " + score, 2);
						
						// Add this node to the queue.
						queue.add(new GameStateNode(score, newGame, addMoveSequence));
						
						// Logging
						//waitForConfirmation();
						log("Adding Children to Queue: Could play " + from + " to " + to, 2);
						log("Move sequence for this node ("+ newGame.getId() +"): ", 2);
						for(String move : addMoveSequence) {
							log(move, 2);
						}
						
						
					} catch (Exception e) {
						// Logging in the case that this went awry
						waitForConfirmation();
						log("Adding Children to Queue: Could not play " + from + " to " + to +
								": " + e.getMessage() , 3);
						
						// This shouldn't be happening any more, but just in case it is, in a debugging context
						if(e.getMessage() == null) {
							e.printStackTrace();
						}
					}
				} // Part of to/from looping
			} // Part of to/from looping
			
			// Base case: Extra
			if(type.matches("extra")) {
				// If we have reached a leaf node
				if(playsThatWorked.size() == 0) {
					// We'll start looking for piles that are too close.
					boolean anyClose = false;
					if(curGame.getStockTop(false) != Card.SKIPBO_UNPLAYED) {
						int otherStock = curGame.getStockTop(false).getValue();
						for (char c : tryAt) {
							try {
								// For piles with cards in them
								if(curGame.getFoundationTop(c).getValue() + 1 == otherStock) {
									anyClose = true;
								}
							} catch (NullPointerException e) {
								// For piles without cards in them
								if(otherStock == 1) {
									anyClose = true;
								}
							}
						}
					} else {
						anyClose = false;
					}
						
					// If we found no close ones and everything has been successfully blocked, 
					if(!anyClose) {
						// Important logging actions!
						// Add the steps to reach this node to the official list of actions that happened on this turn. 
						// Reverse the order of the steps nodes as they are added
						while(!curNode.getStepsTaken().isEmpty()) {
							String step = curNode.getStepsTaken().remove(curNode.getStepsTaken().size() - 1);
							if(!step.isBlank()) {
								log(step, 0);
							}
						}
							
						// Return this game state
						return curGame;
					}
				}
			} // End of base case extra
		}// End of while loop
		
		// If we didn't return at a goal state and have exited the priority queue, set the relevant flag
		if(type.matches("stock")) {
			lastStockAttemptWorked = false;
		} else if (type.matches("hand0")) {
			lastOutOfHandAttemptWorked = false;
		}
		
		// Return the unchanged game state.
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
	 * Evaluates the worth of a particular game by comparing the stock height of the opponent to the heights of 
	 * the foundations. The farther a stock is to the foundation, the more valuable it is
	 * @param testGame the game to score
	 * @return the score of this game
	 */
	private int evaluateScoreOnBlock(SkipBoGameModel testGame) {
		char[] tryAt = {'1', '2', '3', '4'};
		int currentLowest = 6;
		Integer stockTop = testGame.getStockTop(false).getValue();
		if(stockTop == null) {
			return 12;
		}
		for(char cur : tryAt) {
			int fndnTop;
			try {
				fndnTop = testGame.getFoundationTop(cur).getValue();
				
			} catch (Exception e){
				fndnTop = 12;
			}
			int difference = 12 - (stockTop - fndnTop + 11) % 12;
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
	@SuppressWarnings("unused")
	private void log(String message, int level) {
		if(level <= logLevel) {
			if(1 <= logLevel) {
				System.out.println(message);
			}
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
