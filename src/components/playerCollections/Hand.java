package components.playerCollections;

import java.util.ArrayList;

import components.Card;

/**
 * Hand object to store a handful of cards
 * @author Anna Heebsh
 */

public class Hand {
	
	private ArrayList<Card> hand;

	/**
	 * Constructor
	 */
	public Hand() {
		hand = new ArrayList<Card>();
	}
	
	
	/**
	 * Adds a card to the hand
	 * @param Card to add
	 */
	public void add(Card card) {
		hand.add(card);
	}
	
	
	/**
	 * Returns the size of the hand
	 * @return int representing the hand size
	 */
	public int size() {
		return hand.size();
	}
	
	
	/**
	 * Checks if the Hand has an element at the specified index
	 * @param i Index to check
	 * @returns true if element at that index, otherwise false
	 */
	public boolean hasElementAt(int i) {
		if(i > (hand.size() - 1) || i < 0) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks if the Card at the specified index is an unplayed SkipBo
	 * @param i index to check at
	 * @return True if element at that index is a SkipBo, false if that element does not exist or is not a SkipBo
	 */
	public boolean isSkipBo(int i) {
		if (hasElementAt(i) && hand.get(i).equals(Card.SKIPBO_UNPLAYED)) {
			return true;
		} 
		return false;
	}
	

	/**
	 * Returns the Card at a specified index within the Hand
	 * @param i index to get from
	 * @return the Card at that element
	 */
	public Card getAt(int i) {
		if (hasElementAt(i)) {
			return hand.get(i);
		}
		return null;
	}
	
	
	/**
	 * Removes the Card from the Hand at the specified index
	 * @param i index to remove at
	 * @return Removed Card
	 */
	public Card removeAt(int i) {
		if (hasElementAt(i)) {
			return hand.remove(i);
		}
		return null;
	}
	
	
	/**
	 * Checks if the Hand is empty.
	 * @return True if empty, otherwise False.
	 */
	public boolean isEmpty() {
		return hand.size() == 0;
	}
	
	
	@Override
	public String toString() {
		return toStringUtil(true);
	}
	
	
	/** 
	 * Returns a "censored" version of ToString.
	 * It reveals how many cards there are, but not their values.
	 * @return String representing the Hand.
	 */
	public String toStringHidden() {
		return toStringUtil(false);
	}
	
	
	/**
	 * Utility function that will print out the right version of the Hand string.
	 * @param showValues true if Card values should be shown, false if Card values should be redacted.
	 * @return String representing the Hand.
	 */
	private String toStringUtil(boolean showValues) {
		String handRetString = "Hand: ";
		int handSize = hand.size();
		for (int i = 0; i < 5; i++) {
			if(i < handSize) {
				if(showValues) {
					handRetString += hand.get(i);
				} else {
					handRetString += "?";
				}
			} else {
				handRetString += "_";
			}
			if (i != 4) {
				handRetString += ", ";
			}
		}
		
		return handRetString;
	}
}
