package driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EmptyStackException;
import java.util.concurrent.ThreadLocalRandom;

import components.Card;
import components.gameCollections.ClearedPile;
import components.gameCollections.DrawPile;
import components.gameCollections.FoundationPile;
import users.Player;
import users.PlayerAI;
import users.PlayerColor;
import users.PlayerType;

/**
 * SkipBo Game Model.
 * 
 * @author Anna Heebsh
 */
public class SkipBoGameModel {
	
	/*The "Deck" -- This is where cards are drawn from.*/
	private DrawPile drawPile;

	/*This is essentially the garbage pile. When the Foundations are cleared, cards go here.*/
	private ClearedPile clearedPile;
	
	/*The Foundations (Building Piles) are where cards are played.*/
	private FoundationPile[] foundationPiles;
	
	/*The two players of this game*/
	private Player[] players;
	
	/*To track whose turn it is*/
	private int turn;
	
	/*To track if it's currently in a state where cards can be drawn */
	private boolean initialDrawDone;
	
	/*To track if there is a winner, if there is one*/
	private boolean hasWinner;
	
	/** Helper object for observer patterns */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private int id;

	
	/**
	 * Default Constructor
	 * Creates a new game with generic values
	 */
	public SkipBoGameModel() {
		players = new Player[2];
		resetSkipBoGame();
	}
	
	
	/**
	 * Copy Constructor
	 * Copies all the data out of an old game into a new one
	 * @param oldGame the SkipBoGameModel to copy.
	 */
	public SkipBoGameModel(SkipBoGameModel oldGame) {	
		// Misc variables
		turn = oldGame.getTurn();
		hasWinner = oldGame.isHasWinner();
		initialDrawDone = oldGame.isInitialDrawDone();
		id = ThreadLocalRandom.current().nextInt(0,10000);
		
		// Draw Pile
		drawPile = new DrawPile(oldGame.getDrawPile());
		// Cleared Pile
		clearedPile = new ClearedPile(oldGame.getClearedPile());
		// Foundation Pile
		foundationPiles = new FoundationPile[4];
		for(int i = 0; i < 4; i++) {
			foundationPiles[i] = new FoundationPile(oldGame.getFoundationPiles()[i]);
		}
		
		// Players
		players = new Player[2];
		for(int i = 0; i < 2; i++) {
			if(oldGame.getPlayer(i).getPlayerType() == PlayerType.AI) {
				players[i] = new PlayerAI(oldGame.getPlayer(i));
			} else {
				players[i] = new Player(oldGame.getPlayer(i));
			}
		}
	}
	
	
	
	/**
	 * Constructor
	 * @param playerOneName the Name of Player One
	 * @param playerOneColor the Color associated with Player One
	 * @param playerTwoName the Name of Player Two
	 * @param playerTwoColor the Color associated with Player Two
	 * @param playerTwoType the Player Type of Player Two
	 * @param gameLength the depth of the Stock, in Cards, at the start of the game. 
	 * More Cards makes for a longer game.
	 */
	public SkipBoGameModel(String playerOneName, PlayerColor playerOneColor, String playerTwoName, 
			PlayerColor playerTwoColor, PlayerType playerTwoType, int gameLength) {
		players = new Player[2];
		resetSkipBoGame(playerOneName, playerOneColor, playerTwoName, playerTwoColor, 
				playerTwoType, gameLength);
	}
	
	
	/**
	 * Resets the game, including information about players, turn mechanics, 
	 * 	cards, and all the involved data structures. 
	 * This empty method sets up the game with default settings.
	 */
	public void resetSkipBoGame() {
		resetSkipBoGame("Anna", PlayerColor.CYAN, "Vivienne", PlayerColor.BLACK, PlayerType.HUMAN, 7);
	}
	
	
	/**
	 * Resets the game, including information about players, turn mechanics, 
	 * 	cards, and all the involved data structures.
	 * @param playerOneName the Name of Player One
	 * @param playerOneColor the Color associated with Player One
	 * @param playerTwoName the Name of Player Two
	 * @param playerTwoColor the Color associated with Player Two
	 * @param playerTwoType the Player Type of Player Two
	 * @param gameLength the depth of the Stock, in Cards, at the start of the game. 
	 * More Cards makes for a longer game.
	 */
	public void resetSkipBoGame(String playerOneName, PlayerColor playerOneColor, String playerTwoName, 
			PlayerColor playerTwoColor, PlayerType playerTwoType, double gameLength) throws RuntimeException {
		turn = 0;
		hasWinner = false;
		initialDrawDone = false;
		id = ThreadLocalRandom.current().nextInt(0,10000);
		
		// Set up Players
		players[0] = new Player("-");
		if(playerTwoType.equals(PlayerType.HUMAN)) {
			players[1] = new Player("-");
		} else {
			players[1] = new PlayerAI("-");
		}
		
		players[0].resetPlayer(playerOneName, playerOneColor);
		players[1].resetPlayer(playerTwoName, playerTwoColor);
		if(players[0].getName().equals(players[1].getName())) {
			throw new RuntimeException("Players cannot have the same name.");
		}
		if(playerOneColor == playerTwoColor) {
			throw new RuntimeException("Players cannot have the same color.");
		}
		
		// Set up the new DrawPile and ClearedPile
		drawPile = new DrawPile();
		clearedPile = new ClearedPile();
		
		// Make sure the game length is valid
		if(gameLength < 5) {
			throw new RuntimeException("Stock must be at least 5 Cards high.");
		} else if (gameLength >= 50) {
			throw new RuntimeException("Stock must be no more than 50 Cards high.");
		}
		
		// Set up the four foundation piles
		foundationPiles = new FoundationPile[4];
		for(int i = 0; i < 4; i++) {
			foundationPiles[i] = new FoundationPile((i+1) + "");
		}
		
		// Deal each player 20 Cards.
		for(int i = 0; i < gameLength; i++) {
			for (Player player : players) {
				player.addToStock(drawPile.draw());
			}
		}
		
		// Fire property change
		pcs.firePropertyChange("start", null, null);
	}
	
	
	/**
	 * Plays a Card.
	 * @param from Source of Card
	 * @param to Destination of Card
	 * @throws Exception if the move is invalid
	 */
	public void play(String from, String to) throws RuntimeException {
		if(hasWinner()) {
			pcs.firePropertyChange("gameOver", null, null);
			return;
		}
		
		// Checks if the "to" destination is valid
		int foundationIndex;
		if(to.matches("f(1|2|3|4)")) {
			foundationIndex = indexConvertUtil(to.charAt(1), true);
		} else {
			throw new RuntimeException("Must play to a foundation, f1-4.");
		}
		
		
		// Checks if the "from" destination is valid
		if(from.matches("(ss)|(h(0|1|2|3|4))|(d(1|2|3|4))")) {
			// Call the right method based on which destination type it is
			if(from.charAt(0) == 'h') { // Hand
				int handIndex = indexConvertUtil(from.charAt(1), false);
				playFromHand(handIndex, foundationIndex);
			} else if (from.charAt(0) == 'd') { // Discard Pile
				int discardIndex = indexConvertUtil(from.charAt(1), true);
				playFromDiscard(discardIndex, foundationIndex);
			} else { // Foundation Pile
				playFromStock(foundationIndex);
			}
		} else {
			throw new RuntimeException(
					"Must play from your hand, stock, or discard piles; h0-4, ss, or d1-4.");
		}
		pcs.firePropertyChange("play" + from + to, null, null);
	}

	
	/**
	 * Plays a Card from the Hand to a FoundationPile
	 * @param handIndex Place the Card is in the Hand
	 * @param foundationIndex Which FoundationPile to play on.
	 * @throws Exception if the move is invalid.
	 */
	private void playFromHand(int handIndex, int foundationIndex) throws RuntimeException {
		int handValue;
		int foundationValue;
		boolean handIsSkipBo = false;
		
		// Enforces Draw Condition
		if (!initialDrawDone) {
			throw new RuntimeException (
					"Must draw cards before the first time you play out of your hands.");
		}

		// Finds the top value of the foundation pile
		if (foundationPiles[foundationIndex].isEmpty()) {
			foundationValue = 0;
		} else {
			foundationValue = foundationPiles[foundationIndex].peek().getValue();
		}
		
		// Finds the value of the specified Card in the Hand
		if (!currentPlayer().hand.hasElementAt(handIndex)) {
			throw new RuntimeException ("Cannot pick up a card from an empty discard pile.");
		} else if (currentPlayer().hand.isSkipBo(handIndex)) {
			handValue = foundationValue + 1;
			handIsSkipBo = true;
		} else {
			handValue = currentPlayer().hand.getAt(handIndex).getValue();
		}
		
		// Play that Card, if possible
		if ((foundationValue + 1) == handValue) {
			if (handIsSkipBo) {
				foundationPiles[foundationIndex].push(Card.getSkipBoized(handValue));
				currentPlayer().hand.removeAt(handIndex);
			} else {
				foundationPiles[foundationIndex].push(currentPlayer().hand.removeAt(handIndex));
			}
			afterAddToFoundation(foundationIndex);
		} else {
			throw new RuntimeException ("That card cannot be played on this foundation.");
		}
	}
	
	
	/**
	 * Plays a Card from the DiscardPile to a FoundationPile
	 * @param discardIndex Which DiscardPile to play from
	 * @param foundationIndex Which FoundationPile to play on.
	 * @param throws RuntimeException if the move is invalid
	 */
	private void playFromDiscard(int discardIndex, int foundationIndex) throws RuntimeException {
		int discardValue;
		int foundationValue;
		boolean discardIsSkipBo = false;

		// Finds the top value of the foundation pile
		if (foundationPiles[foundationIndex].isEmpty()) {
			foundationValue = 0;
		} else {
			foundationValue = foundationPiles[foundationIndex].peek().getValue();
		}
		
		// Finds the value of the specified Card in the DiscardIndex
		if (currentPlayer().discardPiles[discardIndex].isEmpty()) {
			throw new RuntimeException ("That card doesn't exist.");
		} else if (currentPlayer().discardPiles[discardIndex].topIsSkipBo()) {
			discardValue = foundationValue + 1;
			discardIsSkipBo = true;
		} else {
			discardValue = currentPlayer().discardPiles[discardIndex].peek().getValue();
		}
		
		// Play that Card, if possible
		if ((foundationValue + 1) == discardValue) {
			if (discardIsSkipBo) {
				foundationPiles[foundationIndex].push(Card.getSkipBoized(discardValue));
				currentPlayer().popStock();
			} else {
				foundationPiles[foundationIndex].push(currentPlayer().discardPiles[discardIndex].pop());
			}
			afterAddToFoundation(foundationIndex);
		} else {
			throw new RuntimeException ("That card cannot be played on this foundation.");
		}
	}
	
	
	/**
	 * Plays a Card from the StockPile to a FoundationPile
	 * @param foundationIndex Which FoundationPile to play on.
	 * @throws Exception if the move is invalid.
	 */
	private void playFromStock(int foundationIndex) throws RuntimeException {
		int stockValue;
		int foundationValue;
		boolean stockIsSkipBo = false;
		
		// Finds the top value of the foundation pile
		if (foundationPiles[foundationIndex].isEmpty()) {
			foundationValue = 0;
		} else {
			foundationValue = foundationPiles[foundationIndex].peek().getValue();
		}
		
		// Finds the value of the StockCard
		if (currentPlayer().peekStock() == Card.SKIPBO_UNPLAYED) {
			stockValue = foundationValue + 1;
			stockIsSkipBo = true;
		} else {
			stockValue = currentPlayer().peekStock().getValue();
		}
		
		// Play that Card, if possible
		if ((foundationValue + 1) == stockValue) {
			if (stockIsSkipBo) {
				foundationPiles[foundationIndex].push(Card.getSkipBoized(stockValue));
				currentPlayer().popStock();
			} else {
				foundationPiles[foundationIndex].push(currentPlayer().popStock());
			}
			afterAddToFoundation(foundationIndex);
		} else {
			throw new RuntimeException ("That card cannot be played on this foundation.");
		}
		
		// If the Stock is empty, the game has been won!
		if (currentPlayer().stockEmpty()) {
			hasWinner = true;
			pcs.firePropertyChange("gameOver", null, null);
			return;
		}
	}
	
	
	/**
	 * Draws up to a full Hand of Cards for the current player from the DrawPile
	 * @throws Exception if this is not a valid time for the player to draw Cards
	 */
	public int drawCards() throws RuntimeException {
		if(hasWinner()) {
			pcs.firePropertyChange("gameOver", null, null);
			return 0;
		}
		
		if (initialDrawDone &&  !currentPlayer().hand.isEmpty()) {
			throw new RuntimeException ("Cannot draw after playing cards, unless you play all of the cards.");
		}
		Player current = players[turn%2];
		
		int cardsDrawn = 0;
		while(current.canAddToHand()) {
			current.addToHand(drawPile.draw());
			cardsDrawn++;
		}
		if (!initialDrawDone) {
			initialDrawDone = true;
		}
		if (drawPile.gettingLow()) {
			drawPile.shuffleIn(clearedPile.getAll());
			clearedPile.reset();
		}
		
		pcs.firePropertyChange("draw", null, null);
		return cardsDrawn;
	}
	
	
	/**
	 * Utility function that runs after a FoundationPile is added to.
	 * It clears the foundation if it is empty.
	 * @param foundation index that was added to
	 */
	private void afterAddToFoundation(int foundation) {
		if(foundationPiles[foundation].peek().getValue() == 12) {
				clearedPile.addCards(foundationPiles[foundation]);
		}
	}
	
	
	/**
	 * Moves a Card from the current player's Hand to one of their Discard piles.
	 * @param hand "h#" where # is the index within the Hand (0 to 4)
	 * @param discard "d#" where # is the index/id of the discard pile (1 to 4)
	 * @throws Exception if the Hand index is invalid
	 */
	public void discard(String hand, String discard) throws RuntimeException {
		if(hasWinner()) {
			pcs.firePropertyChange("gameOver", null, null);
			return;
		}
		
		if(!initialDrawDone) {
			throw new RuntimeException("You must draw cards before you can end your turn.");
		}
		
		int handI = indexConvertUtil(hand.charAt(1), false);
		int discardI = indexConvertUtil(discard.charAt(1), true);
		
		currentPlayer().discard(handI, discardI);
		
		pcs.firePropertyChange("discard", null, null);
		doneWithTurn();
		
	}
	
	
	/**
	 * Utility function that manages end-of-turn actions. 
	 * It increments the turn counter and resets the draw permissions.
	 */
	private void doneWithTurn() {
		turn++;
		initialDrawDone = false;
		if(currentPlayer().getPlayerType() == PlayerType.AI) {
			pcs.firePropertyChange("newAITurn", null, null);
		}
		pcs.firePropertyChange("newTurn", null, null);
	}
	
	public SkipBoGameModel takeTurn() throws Exception {
		return currentPlayer().takeTurn(this);
	}
	
	
	/**
	 * Check if there is a winner/if the game is over
	 * @return True if the game is over, otherwise false.
	 */
	public boolean hasWinner() {
		return hasWinner;
	}
	
	
	/**
	 * Gets the current Player, whose turn it is
	 * @return the current Player
	 */
	public Player currentPlayer() {
		return players[turn%2];
	}
	
	
	/**
	 * Gets the name of a particular Player
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @return the name of the Player
	 */
	public String getPlayerName(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		return player.getName();
	}
	
	
	/**
	 * Gets the PlayerColor of a particular Player
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @return the PlayerColor of the Player
	 */
	public PlayerColor getPlayerColor(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		return player.getColor();
	}
	
	
	/**
	 * Gets a string describing the number of Cards a particular Player has
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @return the string describing the number of cards
	 */
	public String getHandCount(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		if(player.hand.isEmpty()) {
			return "Hand is empty.";
		}
		
		return "Hand has " + player.hand.size() + " cards.";
	}
	
	
	/**
	 * Gets the number of Cards a particular Player has
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @return the number of Cards
	 */
	public int getHandCountAsInt(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		return player.hand.size();
	}
	
	
	/**
	 * Gets the Card at a particular index within a particular Player's Hand
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @param index within Hand (0-4)
	 * @returns string describing the Card there
	 */
	public Card getHandAtIndex(boolean forCurrent, char index) {
		int i = indexConvertUtil(index, false);		
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		try {
			return player.hand.getAt(i);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Gets the top Card of a particular Player's Stock, as a string
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @returns top Card of the Stock
	 */
	public Card getStockTop(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		try {
			return player.peekStock();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Gets the amount of Cards in a particular Player's Stock, as a string
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @returns string describing the number of Cards in the Stock
	 */
	public String getStockCount(boolean forCurrent) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		return player.getStockSize();
		
	}
	
	
	/**
	 * Gets the top Card of a particular Foundation Pile as a string
	 * @param index of Foundation pile (1-4)
	 * @returns string describing the Foundation Pile
	 */
	public Card getFoundationTop(char index) {
		try {
			int i = indexConvertUtil(index, true);
			return foundationPiles[i].peek();
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	
	/**
	 * Gets the top Card of a particular Discard Pile as a string
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @param index of Discard pile (1-4)
	 * @returns string describing the Discard Pile
	 */
	public Card getDiscardTop(boolean forCurrent, char index) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		int i = indexConvertUtil(index, true);
		try {
			return player.discardPiles[i].peek();
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	
	/**
	 * Gets the number of Cards in a particular Discard Pile as a string
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @param index of Discard pile (1-4)
	 * @returns string describing the number of Cards in the Discard Pile
	 */
	public String getDiscardCount(boolean forCurrent, char index) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];

		int i = indexConvertUtil(index, true);
		return player.discardPiles[i].size() + " cards";
	}
	
	
	/**
	 * Checks if this Discard Pile is empty
	 * @param forCurrent true if for the current Player, false if for the opponent Player
	 * @param index of Discard pile (1-4)
	 * @returns true if empty, otherwise false
	 */
	public boolean discardIsEmpty(boolean forCurrent, char index) {
		int fc = 0;
		if(!forCurrent) {
			fc++;
		}
		Player player = players[(turn + fc)%2];
		
		int realIndex = indexConvertUtil(index, true);
		
		return player.discardPiles[realIndex].isEmpty();
	}
	
	
	/**
	 * Get the DrawPile
	 * @return the drawPile
	 */
	public DrawPile getDrawPile() {
		return drawPile;
	}


	/**
	 * Get the ClearedPile
	 * @return the clearedPile
	 */
	public ClearedPile getClearedPile() {
		return clearedPile;
	}


	/**
	 * Get the FoundationPiles
	 * @return the foundationPiles
	 */
	public FoundationPile[] getFoundationPiles() {
		return foundationPiles;
	}


	/**
	 * @return the players
	 */
	public Player getPlayer(int index) {
		return players[index];
	}


	/**
	 * Get the turn as an integer
	 * @return the turn
	 */
	public int getTurn() {
		return turn;
	}


	/**
	 * Get the value of initialDrawDone
	 * @return the initialDrawDone
	 */
	public boolean isInitialDrawDone() {
		return initialDrawDone;
	}


	/**
	 * Get the value of hasWinner
	 * @return the hasWinner
	 */
	public boolean isHasWinner() {
		return hasWinner;
	}
	
	
	/**
	 * Utility function to convert indices specified as characters to integers
	 * @param c Char to convert to integer
	 * @param shiftByOne True if the index system starts at 1 (for the Foundation and Discard Piles)
	 * @return int corresponding to the specified character
	 */
	private int indexConvertUtil(char c, boolean shiftByOne) {
		if (shiftByOne) {
			return c - 49;
		}
		return c - 48;
	}
	
	
	/**
	 * Checks if two SkipBoGameModels represent equivalent game states.
	 * @param other The other SkipBoGameModel to compare with this one.
	 * @return if the games are equivalent
	 */
	public boolean equals(SkipBoGameModel other) {
		if(drawPile.size() != other.getDrawPile().size()) {
			return false;
		}
		if (clearedPile.size() != other.getClearedPile().size()) {
			return false;
		}
		for(int f = 0; f < 4; f++) {
			if(!foundationPiles[f].isEmpty() && !other.getFoundationPiles()[f].isEmpty()) {
				if(foundationPiles[f].peek() != other.getFoundationPiles()[f].peek()) {
					return false;
			
				}
			} else if (!(foundationPiles[f].isEmpty() && other.getFoundationPiles()[f].isEmpty())) {
				return false;
			}
		}
		if(!players[0].equals(other.getPlayer(0))) {
			return false;
		}
		if(!players[1].equals(other.getPlayer(1))) {
			return false;
		}
		return true;
	}


	@Override
	public String toString() {
		if (hasWinner) {
			return "----------------------------------------------------------------" + 
					"\nGame is over. " + currentPlayer().getName() + " won!" + 
					"\nType \"new\" to play again or \"quit\" to exit the program.";
		}
		return "----------------------------------------------------------------" +
				"\nID: " + id +  
				drawPile.toString() + foundationPiles[0].toString() + foundationPiles[1].toString() +
				foundationPiles[2].toString() + foundationPiles[3].toString() + 
				"\nCurrent Player: " + players[turn%2].getName() + players[turn%2].toString() + 
				players[(turn + 1)%2].toStringOpponent() +
				"\n----------------------------------------------------------------" + 
				"\n" + players[turn%2].getName() + ", enter a command. (Type \"comms\" to see "
				+ "a list of commands.)";
	}


	/**
	 * A way for observers to subscribe
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	
	/**
	 * A way for observers to unsubscribe
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}
}