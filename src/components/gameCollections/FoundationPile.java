package components.gameCollections;

import java.util.Stack;

import components.Card;

/**
 * Stores a FoundationPile object that Cards can be played on.
 * @author Anna Heebsh
 */
public class FoundationPile {
	
	private Stack<Card> foundationPile;
	private String id;

	
	/**
	 * Constructor
	 * @param id The ID string of this FoundationPile
	 */
	public FoundationPile(String id) {
		this.id = id;
		foundationPile = new Stack<Card>();
	}

	
	/**
	 * Checks if the FoundationPile is empty.
	 * @return True if empty
	 */
	public Boolean isEmpty() {
		return foundationPile.isEmpty();
	}
	
	
	/**
	 * Peeks at the FoundationPile and returns a string representing the top card.
	 * @return String representing the top card. "EMPTY" if the pile is empty
	 */
	public String peekString() {
		if(isEmpty()) {
			return "EMPTY";
		} 
		return foundationPile.peek().name();
	}
	
	
	/**
	 * Peeks at the FoundationPile and returns the top Card object.
	 * @return the top Card. 
	 */
	public Card peek() {
		return foundationPile.peek();
	}
	
	/**
	 * Pushes a Card onto the FoundationPile. 
	 * @param card The Card to Add
	 */
	public void push(Card card) {
		foundationPile.push(card);
	}
	
	@Override
	public String toString() {
		return "\nFoundation " + id + ": " + peekString();
	}

	/**
	 * Removes the top card off of the FoundationPile.
	 * @return The top card of the FoundationPile. 
	 */
	public Card pop() {
		return foundationPile.pop();
	}
}
