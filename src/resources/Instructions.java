package resources;

/**
 * Instructions are stored here so they don't have to be stored in the actual view....
 * 
 * @author Anna Heebsh
 */
public final class Instructions {

	public static String[] headings = {"Setup", "Goal", "Turns"};
	
	public static String[] paragraphs = {
			"The Skip-Bo deck is comprised of 12 sets of cards numbered 1-12, and 18 Skip-Bo cards, "
			+ "which are wild. By convention, the numbers 1-4, 5-8, and 9-12 are different colors, "
			+ "but this is purely decorative and has no bearing on gameplay.\r\n"
			+ "These are shuffled and added to the DRAW pile at the center of the play space.\r\n\r\n"
			+ "A small stack of these is dealt to each player at the start of the game to form their "
			+ "STOCK piles. Usually, there are 20 cards in each STOCK, but the number can be "
			+ "adjusted to make the game longer or shorter as desired.\r\n\r\n"
			+ "There is space reserved in the play space for four FOUNDATION piles, used by both "
			+ "players, and for DISCARD piles, of which there are four for each player.\r\n"
			+ "", 
			"The goal of the game is to play all of the cards from your STOCK pile before your "
			+ "opponent does. The game ends when one of the players' STOCK piles reaches 0 cards.\r\n"
			+ "",
			"There are three actions you can take on your turn: drawing cards into your HAND, playing "
			+ "cards on FOUNDATIONS, and playing cards on one of your DISCARD piles.\r\n\r\n"
			+ "DRAWING CARDS. You must take cards from the DRAW pile at least once per turn. Whenever "
			+ "you draw cards, you draw until your HAND has exactly five cards. You cannot play cards "
			+ "out of your HAND until you have taken cards out of the DRAW pile for the first time. "
			+ "You can also draw cards later in your turn if you have 0 cards left in your HAND.\r\n\r\n"
			+ "PLAYING CARDS. You can play cards on the FOUNDATIONS at any time before you discard, "
			+ "though you must take cards from the DRAW pile before playing out of your HAND. Cards "
			+ "from your HAND, STOCK, or DISCARD pile can be played on the FOUNDATIONS. FOUNDATIONS "
			+ "count up, starting from 1, and are cleared again when a 12 is played. It does not matter "
			+ "which FOUNDATION you play to, so long as the card you play is one greater than that "
			+ "FOUNDATION'S top card. Skip-Bo cards can take on any value and be played on any "
			+ "foundation regardless of what the top card is.\r\n\r\n"
			+ "DISCARDING CARDS. Your turn ends when you place a card on one of your DISCARD piles. Any "
			+ "card from your HAND can be played on any of the DISCARD piles, regardless of what other "
			+ "cards are already in that pile. However, only the topmost card of each DISCARD pile is "
			+ "accessible, and cards that are \"hidden\" can't be played until they are uncovered. It "
			+ "is also worthwhile to note that your opponent can see your DISCARD piles just as you can, "
			+ "and that you can see theirs."
	};

}
