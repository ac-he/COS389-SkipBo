package driver;

import java.util.Scanner;

/**
 * @author Anna Heebsh
 * Console App View of the SkipBo Game Model
 */
public class ConsoleApp {

	public static SkipBoGameModel game;

	/**
	 * Main method to control the actual logic of the game.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		game = new SkipBoGameModel();

		// Scanner setup
		Scanner scanner;
		try {
			scanner = new Scanner(System.in);

			String latestInput;

			while (true) {
				try {
					// Print out the game state
					System.out.println(game);

					// Get user input
					latestInput = scanner.nextLine();

					// Parse user input using regular expressions
					if (latestInput.matches("comms")) {
						printCommandList();
					} else if (latestInput.matches("draw")) {
						game.drawCards();
					} else if (latestInput.matches("new") && game.hasWinner()) {
						game.resetSkipBoGame();
					} else if (latestInput.matches("quit") && game.hasWinner()) {
						break;
					} else if (latestInput.matches("(play )(h(0|1|2|3|4)|d(1|2|3|4)|ss) on f(1|2|3|4)")) {
						game.play(latestInput.substring(5, 7), latestInput.substring(11, 13));
					} else if (latestInput.matches("discard h(0|1|2|3|4) on d(1|2|3|4)")) {
						game.discard(latestInput.substring(8, 10), latestInput.substring(14, 16));
					} else {
						System.out.println("That input is not recognized. Type \"comms\" to see a " + 
								"list of valid commands.");
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Looks like that command didn't work. Type \"comms\" to see a "
							+ "list of valid commands.");
				}
			}
		} catch (Exception e) {
			System.out.println("Something went wrong with the Scanner.");
		}
	}

	/**
	 * Prints info about all the commands and how to use them.
	 */
	private static void printCommandList() {
		System.out.println("All input must be lowercase.");
		System.out.println("To specify the location of a card, use this system:");
		System.out.println("         Stock  ss");
		System.out.println("       Discard  d# (where # represents name pile)");
		System.out.println("    Foundation  f# (where # represents name of pile)");
		System.out.println("          Hand  h# (where # represents index of card in hand, 0-based)");
		System.out.println("COMMANDS:");
		System.out.println("draw                Draw cards from the deck to fill your hand.");
		System.out.println("discard h# on d#    Discard a card from your hand to a discard pile.");
		System.out.println("play h# on f#	    Play a card from your hand onto a foundation.");
		System.out.println("play d# on f#	    Play a card from your discard piles onto a foundation.");
		System.out.println("play ss on f#       Play your stock card onto a foundation.");
		System.out.println("comms				See the list of commands (You just did this.)");
	}

}
