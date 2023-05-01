package users;

import java.util.ArrayList;

import components.Card;
import components.playerCollections.DiscardPile;
import components.playerCollections.Hand;
import components.playerCollections.StockPile;
import driver.SkipBoGameModel;

/**
 * Player Object. Holds onto a few collections of Cards and helps manage them. 
 * @author Anna Heebsh
 */
public class Player {
	
	/*Display Name*/
	private String name;
	
	/*Display Color*/
	private PlayerColor color;
	
	/*Contains all the cards this player is trying to get rid of*/
	private StockPile stockPile;
	
	/*Contains this player's reserve cards*/
	public DiscardPile[] discardPiles;
	
	/*Stores this player's hand*/
	public Hand hand;

	
	/**
	 * Constructor
	 * @param name of Player
	 */
	public Player(String name) {
		resetPlayer(name, null);
	}
	
	
	/**
	 * Constructor
	 * @param name of Player
	 * @param color the PlayerColor associated with this Player
	 */
	public Player(String name, PlayerColor color) {
		resetPlayer(name, color);
	}
	
	
	/**
	 * Get Name of Player
	 * @return name of Player
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Get Type of Player
	 * @return PlayerType of Player
	 */
	public PlayerType getPlayerType() {
		return PlayerType.HUMAN;
	}
	
	
	/**
	 * Get display color of Player
	 * @return display color of Player
	 */
	public PlayerColor getColor() {
		return color;
	}
	
	
	/**
	 * Resets the Player's Hand, StockPile, and DiscardPile
	 */
	public void resetPlayer(String name, PlayerColor color) throws RuntimeException {
		this.color = color;
		
		if (name.isBlank() || name.length() > 12) {
			throw new RuntimeException("Names must be between 0 and 12 characters.");
		}
		this.name = name.strip();
		
		stockPile = new StockPile();
		hand = new Hand();
		
		discardPiles = new DiscardPile[4];
		for(int i = 0; i < 4; i++) {
			discardPiles[i] = new DiscardPile((i+1) + "");
		}
	}
	
	
	/**
	 * Adds a Card to this Player's StockPile
	 * @param Card to add
	 */
	public void addToStock(Card card) {
		stockPile.push(card);
	}
	
	
	/**
	 * Checks if the Hand can be added to.
	 * @return True if Hand has room, otherwise False.
	 */
	public boolean canAddToHand() {
		return hand.size() < 5;
		
	}
	
	
	/**
	 * Add a Card to the Player's Hand
	 * @param Card to add to the Hand
	 */
	public void addToHand(Card card) {
		hand.add(card);
	}
	
	
	/**
	 * Checks if the top of the Stock is a SkipBo.
	 * @return True if the top is a SkipBo, otherwise false
	 */
	public boolean topIsSkipBo() {
		if(stockPile.peek() == Card.SKIPBO_UNPLAYED) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Peeks at the top of the Stock.
	 * @return Card on top of the Stock
	 */
	public Card peekStock(){
		return stockPile.peek();
	}
	
	
	/**
	 * Removes the top card of the Stock.
	 * @return Card on top of the Stock
	 */
	public Card popStock()  {
		return stockPile.pop();
	}
	
	
	/**Checks if the Stock is empty.
	 * @return True if the Stock is empty, otherwise false
	 */
	public boolean stockEmpty() {
		return stockPile.isEmpty();
	}
	
	
	@Override
	public String toString() {
		String discardPilesRetString = "";
		for (int i = 0; i < 4; i++) {
			discardPilesRetString += "\n    ";
			discardPilesRetString += discardPiles[i].toString();
		}

		return "\nPlayer: " + name + "\n    " + hand.toString() + "\n    " + 
				stockPile.toString() + discardPilesRetString;
	}
	
	
	/**
	 * Returns a "redacted" version of the Player String that hides the actual values of the cards.
	 * @return String about the Opponent Player
	 */
	public String toStringOpponent() {
		String discardPilesRetString = "";
		for (int i = 0; i < 4; i++) {
			discardPilesRetString += "\n    ";
			discardPilesRetString += discardPiles[i].toString();
		}

		return "\nPlayer: " + name + "\n    " + hand.toStringHidden() + "\n    " + 
				stockPile.toString() + discardPilesRetString;
	}

	
	/**
	 * Moves a Card from the Player's Hand to their Discard Pile
	 * @param handI index within Hand
	 * @param discardI index of Discard Pile	
	 */
	public void discard(int handI, int discardI) {
		if(hand.hasElementAt(handI)) {
			discardPiles[discardI].push(hand.removeAt(handI));
		}
	}

	
	/**
	 * Gets a string describing the size of this Player's stock
	 * @return the size of the stock as a String
	 */
	public String getStockSize() {
		return stockPile.size() + " cards";
	}

	
	/**
	 * Takes an action for this player.
	 * @return 
	 * @throws Exception if the method is used on a Human Player
	 */
	public void takeAction(SkipBoGameModel skipBoGameModel) throws Exception {
		throw new Exception("This method doesn't work with Human Players."); 
	}
}

