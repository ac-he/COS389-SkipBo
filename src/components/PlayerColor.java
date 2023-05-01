package components;


/**
 * PlayerColor, contains a list of the possible colors that players can pick from and their hex codes
 * @author Anna Heebsh
 */
public enum PlayerColor {
	RED ("#cf594e"),
	ORANGE ("#ffa713"),
	YELLOW ("#fff386"),
	GREEN ("#9bf582"),
	CYAN ("#00ffff"),
	BLUE ("#15468e"),
	PURPLE ("#9d62d1"),
	PINK ("#e14d8e"), 
	BLACK ("#373737");

	private final String styleString;
	
	
	/**
	 * Constructor
	 * @param styleString the corresponding hex value for this PlayerColor 
	 */
	PlayerColor(String styleString) {
		this.styleString = styleString;
	}

	
	/**
	 * Gets the hex code corresponding to this color
	 * @returns a hex code
	 */
	public String getStyleString() {
		return styleString;
	}
}
