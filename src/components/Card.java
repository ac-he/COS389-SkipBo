package components;

/**
 * Card object, with properties value and isPlayedSkipBo
 * @author Anna Heebsh
 */
public enum Card {
	ONE 	(1, false),			SKIPBO_ONE 		(1, true),
	TWO 	(2, false),			SKIPBO_TWO 		(2, true),
	THREE 	(3, false),			SKIPBO_THREE 	(3, true),
	FOUR 	(4, false),			SKIPBO_FOUR 	(4, true),
	FIVE 	(5, false),			SKIPBO_FIVE 	(5, true),
	SIX 	(6, false),			SKIPBO_SIX 		(6, true),
	SEVEN 	(7, false),			SKIPBO_SEVEN 	(7, true),
	EIGHT 	(8, false),			SKIPBO_EIGHT 	(8, true),
	NINE 	(9, false),			SKIPBO_NINE 	(9, true),
	TEN 	(10, false), 		SKIPBO_TEN 		(10, true),
	ELEVEN 	(11, false),		SKIPBO_ELEVEN 	(11, true),
	TWELVE 	(12, false),		SKIPBO_TWELVE 	(12, true),
	SKIPBO_UNPLAYED (null, false);
	
	private final Integer value;
	private final boolean playedSkipBo;
	
	
	/**
	 * Constructor
	 * @param value 	numerical value of the card
	 * @param boolean	true if the card is a played Skip-Bo Card taking on the value of a normal card 
	 */
	Card(Integer value, boolean playedSkipBo){
		this.value = value;
		this.playedSkipBo = playedSkipBo;
	}
	
	
	/**
	 * Gets the value of this card
	 * @return Integer value of card. Null if card is an unplayed SkipBo.
	 */
	public Integer getValue(){
		return this.value;	
	}
	
	
	/**
	 * Checks if this card is a played Skip Bo card
	 * @return true if played SkipBo that has taken on the value of a normal card, otherwise false.
	 */
	public boolean getIsPlayedSkipBo() {
		return this.playedSkipBo;
	}
	
	
	/**
	 * Generates a file name for this card
	 * @returns the file name
	 */
	public String getImagePath() {
		String sb = "";
		if(this == SKIPBO_UNPLAYED) {
			return "UnplayedSB.jpg";
		} else if (this.playedSkipBo) {
			sb = "SB";
		}
		
		if(this.value < 10) {
			return "0" + this.value + sb + ".jpg";
		} else {
			return this.value + sb + ".jpg";
		}
	}
	
	
	/**
	 * Uses an integer input to get the played SkipBo card with that value.
	 * @returns Played SkipBo Card with the specified value.
	 */
	public static Card getSkipBoized(int value) {
		switch(value) {
			case 1: 
				return SKIPBO_ONE;
			case 2:
				return SKIPBO_TWO;
			case 3: 
				return SKIPBO_THREE;
			case 4:
				return SKIPBO_FOUR;
			case 5: 
				return SKIPBO_FIVE;
			case 6:
				return SKIPBO_SIX;
			case 7: 
				return SKIPBO_SEVEN;
			case 8:
				return SKIPBO_EIGHT;
			case 9: 
				return SKIPBO_NINE;
			case 10:
				return SKIPBO_TEN;
			case 11: 
				return SKIPBO_ELEVEN;
			case 12:
			default:
				return SKIPBO_TWELVE;
		}
	}
}
