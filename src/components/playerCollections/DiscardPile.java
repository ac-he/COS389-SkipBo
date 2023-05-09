package components.playerCollections;

import java.util.Stack;

import components.Card;

/**
 * DiscardPile object to store a Stack of playable Cards.
 * @author Anna Heebsh 
 */
public class DiscardPile {
	private Stack<Card> discardPile;
	private String id;

	
	/**
	 * Constructor
	 * @param id The ID string of this DiscardPile
	 */
	public DiscardPile(String id) {
		this.id = id;
		discardPile = new Stack<Card>();
	}
	
	
	/**
	 * Copy constructor
	 * @param oldDiscardPile the DiscardPile to copy
	 */
	@SuppressWarnings("unchecked")
	public DiscardPile(DiscardPile oldDiscardPile) {
		id = oldDiscardPile.getId();
		discardPile = (Stack<Card>) oldDiscardPile.getDiscardPile().clone();
	}


	/**
	 * Peeks at the top Card of the Discard Pile but does not remove it.
	 * @return Card from the top of this DiscardPile. 
	 */
	public Card peek() {
		return discardPile.peek();
	}
	
	
	/**
	 * Removes the top Card of the DiscardPile. 
	 * @return Card from the top of the DiscardPile. 
	 */
	public Card pop() {
		return discardPile.pop();
	}
	
	
	/**
	 * Pushes a Card onto the top of the DiscardPile.
	 * @param Card to add to the DiscardPile.
	 */
	public void push(Card card) {
		discardPile.push(card);
	}
	
	
	/**
	 * Checks if the DiscardPile is empty.
	 * @return True if empty, otherwise false.
	 */
	public boolean isEmpty() {
		return discardPile.isEmpty();
	}
	
	
	/**
	 * Checks if the top card is a SkipBo
	 * @return True if the top is an unplayed SkipBo, otherwise false.
	 */
	public boolean topIsSkipBo()  {
		if(discardPile.peek() == Card.SKIPBO_UNPLAYED) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Peeks at the DiscardPile and returns a string representing the top card.
	 * @return String representing the top card. "EMPTY" if the pile is empty
	 */
	public String peekString() {
		if(isEmpty()) {
			return "EMPTY";
		} 
		return discardPile.peek().name();
	}
	
	
	@Override
	public String toString() {
		return "DISCARD " + id + ": Top=" + peekString() + " Size=" + discardPile.size();
	}

	
	/**
	 * Gets the size of this DiscardPile
	 * @return the size of this DiscardPile
	 */
	public int size() {
		return discardPile.size();
	}


	/**
	 * Gets the whole discard pile.
	 * @return the discardPile
	 */
	public Stack<Card> getDiscardPile() {
		return discardPile;
	}


	/**
	 * Gets the ID of this discard pile.
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	

}
