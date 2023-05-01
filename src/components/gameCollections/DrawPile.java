package components.gameCollections;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import components.Card;

/**
 * DrawPile object, holding the main "deck" that cards are drawn from. 
 * @author Anna Heebsh
 */
public class DrawPile {

	private ArrayList<Card> drawPile;

	
	/**
	 * Constructor
	 */
	public DrawPile() {
		reset();
	}

	
	/**
	 * Resets the draw pile by clearing it and re-adding the right number of cards.
	 * It also handles shuffling. 
	 */
	public void reset() {
		drawPile = new ArrayList<Card>();

		// Set up the deck. 12 of each "standard card" plus 18 Skip-Bo cards
		ArrayList<Card> toAdd = new ArrayList<Card>();
		for (Card card : Card.values()) {
			if (!card.getIsPlayedSkipBo()) {
				if (card.equals(Card.SKIPBO_UNPLAYED)) {
					for (int i = 0; i < 18; i++) {
						toAdd.add(card);
					}
				} else {
					for (int i = 0; i < 12; i++) {
						toAdd.add(card);
					}
				}
			}
		}

		// Shuffle the new deck.
		shuffleIn(toAdd);
	}

	
	/**
	 * Removes the top card from the DrawPile.
	 * @return Card drawn
	 */
	public Card draw() {
		return drawPile.remove(0);
	}

	
	/**
	 * Shuffles cards into the DrawPile.
	 * @param toAdd an ArrayList of Cards to shuffle into the deck. 
	 */
	public void shuffleIn(ArrayList<Card> toAdd) {
		drawPile.addAll(toAdd);
		int length = drawPile.size();
		for (int i = 0; i < length - 2; i++) {
			int j = ThreadLocalRandom.current().nextInt(i, length);
			Card temp = drawPile.get(i);
			drawPile.set(i, drawPile.get(j));
			drawPile.set(j, temp);
		}
	}
	
	
	@Override
	public String toString() {
		return "\nDeck Size: " + drawPile.size();
	}

	
	/**
	 * Signals that the DrawPile is running low.
	 * @return True when running out of cards.
	 */
	public boolean gettingLow() {
		return drawPile.size() <= 5;
	}
}
