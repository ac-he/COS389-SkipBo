package styles;

import users.PlayerColor;

/**
 * This sort of works as the style sheet for this project, albeit in a sort of hacky way.
 * I am sure there are better ways to style elements in JavaFx, but I have never been taught them.
 * 
 * @author Anna Heebsh
 */
public final class Styles {

	public static final String SELECTED = "-fx-background-color: Yellow; -fx-border-color: Yellow;"
			+ "-fx-border-width: 3px;";
	public static final String TAB = "-fx-background-color: #35775b; -fx-text-fill: White; ";
	public static final String CARD_BASE = "-fx-background-color: #35775b";
	public static final String DECORATIVE_RECTANGLE = "-fx-border-color: White; -fx-border-width: 3px;";
	public static final String WHITE_HEADING_TEXT = "-fx-text-fill: White; -fx-font-size: 32px;";
	public static final String WHITE_BODY_TEXT = "-fx-text-fill: White; -fx-font-size: 16px;";
	public static final String SLIDER = "-fx-background-color: White;";
	
	
	/**
	 * Generates body text style, according to the size of the display. 
	 * @param ds The display scalar, to resize the font with.
	 * @return string to use in styling a text element.
	 */
	public static String getBodyTextStyle(double ds) {
		return "-fx-text-fill: White;" + 
				"-fx-font: " + Math.floor(ds * 3/4) + "px Sans-serif; " +
				"-fx-alignment: Center;";
	}

	
	/**
	 * Generates decorative rectangle style for a specified PlayerColor. 
	 * @playerColor The PlayerColor to generate the style with.
	 * @return string to use in styling a text element.
	 */
	public static String getRectangleStyle(PlayerColor color) {
		return "-fx-border-color: " + color.getStyleString() + "; -fx-border-width: 3px;";
	}
	
	
	/**
	 * Generates heading text style, according to the size of the display and the specified PlayerColor. 
	 * @param ds The display scalar, to resize the font with.
	 * @playerColor The PlayerColor to generate the text with.
	 * @return string to use in styling a text element.
	 */
	public static String getHeadingTextStyle(double ds, PlayerColor color) {
		return "-fx-text-fill: " + color.getStyleString() + "; " + 
				"-fx-font: " + Math.floor(ds * 3/2) + "px Sans-serif; " +
				"-fx-alignment: Center;";
	}
}
