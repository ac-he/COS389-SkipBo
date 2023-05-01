package components.gameCollections;

import java.util.ArrayList;

import components.Card;

/**
 * Object managing the Cleared Pile array list. 
 * Essentially, this collects cards once they are out of play.
 * 
 * @author Anna Heebsh
 */
public class ClearedPile {
	
	private ArrayList<Card> clearedPile;

	/**
	 * Constructor
	 */
	public ClearedPile() {
		reset();
	}

	
	/**
	 * Resets the ClearedPile.
	 */
	public void reset() {
		clearedPile = new ArrayList<Card>();
	}
	
	
	/**
	 * Clears all the cards out of a specified FoundationPile.
	 * @param foundationPile to clear
	 */
	public void addCards(FoundationPile foundationPile) {
		while(!foundationPile.isEmpty()) {
			Card current = foundationPile.pop();
			if(current.getIsPlayedSkipBo()) {
				clearedPile.add(Card.SKIPBO_UNPLAYED);
			} else {
				clearedPile.add(current);
			}
		}

	}
	
	
	/**
	 * Gets the whole ClearedPile array, for use in re-shuffling
	 * @return ArrayList containing all the cleared Cards. 
	 */
	public ArrayList<Card> getAll() {
		return clearedPile;
	}

}
