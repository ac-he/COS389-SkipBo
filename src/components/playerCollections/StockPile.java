package components.playerCollections;

import java.util.Stack;

import components.Card;

/**
 * StockPile object to store Cards
 * @author Anna Heebsh
 */
public class StockPile {

	private Stack<Card> stockPile;
	
	
	/*
	 * Constructor
	 */
	public StockPile() {
		stockPile = new Stack<Card>();
	}
	
	
	/** 
	 * Checks if the Stock is empty.
	 * @return true if Stock is empty, otherwise false
	 */
	public Boolean isEmpty() {
		return stockPile.isEmpty();
	}
	
	
	/**
	 * Peeks at the StockPile and returns a string representing the top card.
	 * @return String representing the top card. "EMPTY" if the pile is empty
	 */
	public String peekString() {
		if(isEmpty()) {
			return "EMPTY";
		} 
		return stockPile.peek().name();
	}
	
	
	/**
	 * Pushes a Card onto the Stock
	 * @param Card to add
	 */
	public void push(Card card) {
		stockPile.add(card);
	}
	
	
	@Override
	public String toString() {
		return "Stock: Top=" + peekString() + " Size=" + stockPile.size();
	}

	
	/**
	 * Peeks at the top of the Stock
	 * @return Card on the top of the Stock
	 */
	public Card peek(){
		return stockPile.peek();
	}
	
	
	/**
	 * Removes the Card at the top of the Stock
	 * @return Card on the top of the Stock
	 */
	public Card pop() {
		return stockPile.pop();
	}


	/**
	 * Gets the size of this Stock Pile
	 * @returns the size of this Stock Pile
	 * */
	public int size() {
		return stockPile.size();
	}
}
