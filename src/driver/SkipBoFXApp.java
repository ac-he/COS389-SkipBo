package driver;

import styles.Styles;
import users.PlayerColor;
import users.PlayerType;

import resources.Instructions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import components.Card;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Runs the GUI Application for the Skip Bo Game. 
 * This class functions as both the controller and the view for all of the tabs visible within the application.
 * 
 * @author Anna Heebsh
 */
public class SkipBoFXApp extends Application implements PropertyChangeListener, EventHandler<ActionEvent>{
	
	// DISPLAY CONSTANTS
	// Essentially, I designed the view so that it is divided up into units of size ds (Display Scalar)
	// The position of every element is given in this unit.
	private final double ds = 25; // Display scalar
	private final double xTotal = 56; // Total X-dimension in ds
	private final double yTotal = 28; // Total Y-dimension in ds
	private final String imagePath = "/"; // In case Java lets me move the assets into a different folder.
	
	// DISPLAY ELEMENTS
	// The root element, a tab pane
	private TabPane root = new TabPane(); 
	// The three tabs contained within the tab pane
	private Tab settingsTab;
	private Tab gameTab;
	private Tab rulesTab;
	// The containers that live inside each of the tabs, respectively
	private VBox settingsPane = new VBox();
	private Pane gamePane = new Pane();
	private ScrollPane rulesPane = new ScrollPane();
	// The elements that live inside of the settings tab
	private TextField stfP1Name, stfP2Name;
	private Button submit;
	private ComboBox<PlayerColor> scbP1Color, scbP2Color;
	private ComboBox<PlayerType> scbP2Type;
	public Slider slider;
	// Stores the contents of the game tab
	private HashMap<String, Button> gameButtons;
	private HashMap<String, Label> gameLabels;
	private ScrollPane turnLogSidebar = new ScrollPane();
	private VBox innerTurnLogSidebar = new VBox();

	// GAMEPLAY VARIABLES
	// The Skip Bo model tied to this application
	private SkipBoGameModel game;
	// Keeps track of the id of the card that is currently selected
	private String selectedCard;
	
	
	/**
	 * Main method
	 * Launches the applications
	 */
	public static void main(String[] args) {
		launch(args);
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {	
		populateGamePane();
		
		if(evt.getPropertyName().equals("newTurn") && game.hasWinner() ||
				evt.getPropertyName().equals("gameOver")) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Winner!");
			alert.setContentText("Game over: " + game.currentPlayer().getName() + " has won!");
			alert.showAndWait();
			root.getSelectionModel().select(settingsTab);
		}
		
		if(evt.getPropertyName().equals("newAITurn")) {
			try {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("AI Turn");
				alert.setContentText("The AI Player is taking its turn now.");
				alert.showAndWait();
				game = game.takeTurn();
				game.addPropertyChangeListener(this);
				populateGamePane();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			// GAME SETUP
			// Create a new game and set up the PCL
			game = new SkipBoGameModel();
			game.addPropertyChangeListener(this);
			
			// Instantiate the selected card
			selectedCard = "none";
			
			// GRAPHICS SETUP
			// Set up the root
			Scene scene = new Scene(root, xTotal*ds, (yTotal + 1)*ds);
			
			// Draw both of the game panes for the first time
			drawGamePane();
			drawSettingsPane();
			drawRulesPane();
			gamePane.setStyle(Styles.TAB);
			settingsPane.setStyle(Styles.TAB);
			rulesPane.setStyle(Styles.TAB);
			
			// Put the panes inside permanent tabs
			settingsTab = new Tab("Settings", settingsPane);
			gameTab = new Tab("Game", gamePane);
			rulesTab = new Tab("Rules", rulesPane);
			settingsTab.setClosable(false);
			gameTab.setClosable(false);
			rulesTab.setClosable(false);
			root.getTabs().addAll(settingsTab, rulesTab, gameTab);
			
			// Create and show the primary stage
			primaryStage.getIcons().add(new Image(SkipBoFXApp.class.getResourceAsStream(imagePath + "Logo.jpg")));
			primaryStage.setTitle("Skip-Bo");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void handle(ActionEvent event) {
		try {
			// IF THE EVENT WAS THE SUBMIT BUTTON
			if(event.getSource().equals(submit)) {
				game.resetSkipBoGame(stfP1Name.getText(), scbP1Color.getValue(), stfP2Name.getText(), 
						scbP2Color.getValue(), scbP2Type.getValue(), slider.getValue());
				root.getSelectionModel().select(gameTab);
				return;
			}
			// OTHERWISE, IT'S ONE OF THE GAMEPLAY INPUTS
			// All the card buttons can be looped through to find the one that sent the event
			gameButtons.forEach((key, value) -> {
				// If this was the button that sent the event...
				if(event.getSource().equals(value)) {
					//If it's the draw pile
					if(key.matches("a-draw-x-b")) {
						// Draw cards from the deck
						game.drawCards();
						selectedCard = "none";
						
					// If it's anything belonging to the opponent
					} else if (key.matches("o.{9}")) {
						// Send an alert because that's not a legal move.
						throw new RuntimeException("You can't interact with your opponent's cards.");
						
					// If it's anything in our hand
					} else if (key.matches("c-hand-(0|1|2|3|4)-b")) {
						// Try selecting that card.
						selectCard(key);
					
					// If it's anything in our discard pile
					} else if (key.matches("c-disc-(1|2|3|4)-b")) {
						// If we have a card from our hand selected
						if(selectedCard.matches("c-hand-(0|1|2|3|4)-b")) {
							// Discard it there
							game.discard("h" + selectedCard.charAt(7), "d" + key.charAt(7));
							selectCard(selectedCard);
						} else {
							// Make sure it's not empty
							if(game.discardIsEmpty(true, key.charAt(7))) {
								throw new RuntimeException("You can't select this empty discard pile.");
							}
							
							// Try selecting that card.
							selectCard(key);
						}
						
					// If it's our stock
					} else if (key.matches("c-stoc-x-b")) {
						// Try selecting that card
						selectCard(key);
						
					// If it's anything in the foundations
					} else if (key.matches("a-fndn-(1|2|3|4)-b")) {
						// If we have a card from our hand selected
						if(selectedCard.matches("c-....-.-b")) {
							if(selectedCard.matches("c-stoc-x-b")) {
								game.play("ss", key.charAt(2) + "" + key.charAt(7));
							} else {
								game.play(selectedCard.charAt(2) + "" + selectedCard.charAt(7), 
									key.charAt(2) + "" + key.charAt(7));
							}
							selectCard(selectedCard);
						} else {
							throw new RuntimeException("You have to select a card before playing "
									+ "it on a foundation.");
						}
						
					// If it's anything else, something's gone horribly wrong
					} else {
						System.out.println("That input was not recognized. KEY=" + key );
					}
					
					// No need to search the other buttons.
					return;
				}
			});

		} catch (RuntimeException e) { 
			// Send the alert to the user. 
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("User Error!");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			selectCard(selectedCard);
			
			// Switch away from the game tab if the game has been won.
			if(game.hasWinner()) {
				root.getSelectionModel().select(settingsTab);
			}
		} catch (Exception e) {
			// Print the alert to the console
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates everything that goes inside of the Settings pane. 
	 * These controls allow the user to set their name and color and to determine the length of the game.
	 */
	private void drawSettingsPane() {
		// Clear out all the old stuff
		settingsPane.getChildren().clear();
		
		// Set up the Settings Text Field for Player 1 Name
		stfP1Name = new TextField("Player 1");
		stfP1Name.setOnAction(this);
		Label slP1 = new Label("Player 1");
		slP1.setStyle(Styles.WHITE_HEADING_TEXT);
		Label slP1Name = new Label("Name: ");
		slP1Name.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slP1, slP1Name, stfP1Name);
		
		// Set up the Settings Combo Box for Player 1 Color
		scbP1Color = new ComboBox<PlayerColor>();
		scbP1Color.getItems().addAll(PlayerColor.values());
		scbP1Color.setValue(PlayerColor.RED);
		Label slP1Color = new Label("Color: ");
		slP1Color.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slP1Color, scbP1Color);
		
		// Put some space in between the two settings options, in a really hacky way
		settingsPane.getChildren().add(new Label(""));
		
		// Set up the Settings Text Field for Player 2 Name
		stfP2Name = new TextField("Player 2");
		stfP2Name.setOnAction(this);
		Label slP2 = new Label("Player 2");
		slP2.setStyle(Styles.WHITE_HEADING_TEXT);
		Label slP2Name = new Label("Name: ");
		slP2Name.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slP2, slP2Name, stfP2Name);
		
		// Set up the Settings Combo Box for Player 2 Color
		scbP2Color = new ComboBox<PlayerColor>();
		scbP2Color.getItems().addAll(PlayerColor.values());
		scbP2Color.setValue(PlayerColor.BLUE);
		Label slP2Color = new Label("Color: ");
		slP2Color.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slP2Color, scbP2Color);
		
		// Set up the Settings Combo Box for Player 2 Agent Type
		scbP2Type = new ComboBox<PlayerType>();
		scbP2Type.getItems().addAll(PlayerType.values());
		scbP2Type.setValue(PlayerType.AI);
		Label slP2Type = new Label("Opponent Type: ");
		slP2Type.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slP2Type, scbP2Type);
		
		// Put some space in between the two settings options, in a really hacky way
		settingsPane.getChildren().add(new Label(""));
		
		// Create the Game Length Slider
		Label slSlider = new Label("Game Length");
		slSlider.setStyle(Styles.WHITE_HEADING_TEXT);
		Label slSliderText = new Label("This slider controls how many cards are in the Stocks. "
				+ "More cards makes for a longer game.");
		slSliderText.setStyle(Styles.WHITE_BODY_TEXT);
		settingsPane.getChildren().addAll(slSlider, slSliderText, generateGameLengthSlider());

		// Put some space in between the two settings options, in a really hacky way
		settingsPane.getChildren().add(new Label(""));
		
		// Set up the submit button!
		Label slStart = new Label ("Change Settings and Start Game");
		slStart.setStyle(Styles.WHITE_HEADING_TEXT);
		submit = new Button("Start Game");
		submit.setOnAction(this);
		settingsPane.getChildren().addAll(slStart, submit);
	}

	
	/**
	 * Creates everything that goes inside of the Game pane.
	 */
	private void drawGamePane() {
		gameLabels = new HashMap<String, Label>();
		gameButtons = new HashMap<String, Button>();
		
		gameLabels.put("o-rect-0-l", generateLabel(5.5, 0, 16, 5.5));
		gameLabels.put("o-rect-1-l", generateLabel(22.5, 0.5, 16, 7));
		gameLabels.put("o-rect-2-l", generateLabel(0.5, 0.5, 4, 7));
		gameLabels.put("a-rect-0-l", generateLabel(8.5, 10.5, 4, 6));
		gameLabels.put("a-rect-1-l", generateLabel(14.5, 10.5, 16, 6));
		gameLabels.put("c-rect-0-l", generateLabel(17.5, 22.5, 16, 5.5));
		gameLabels.put("c-rect-1-l", generateLabel(0.5, 20.5, 16, 7));
		gameLabels.put("c-rect-2-l", generateLabel(34.5, 20.5, 4, 7));
		
		gameLabels.put("o-name-x-l", generateLabel( 5,  6, 17, 1));
		gameLabels.put("o-hand-x-l", generateLabel( 6,  4, 15, 1));
		gameLabels.put("o-stna-x-l", generateLabel( 1,  5,  3, 1));
		gameLabels.put("o-stco-x-l", generateLabel( 1,  6,  3, 1));
		gameLabels.put("o-disc-1-l", generateLabel(35,  5,  3, 1));
		gameLabels.put("o-disc-2-l", generateLabel(31,  5,  3, 1));
		gameLabels.put("o-disc-3-l", generateLabel(27,  5,  3, 1));
		gameLabels.put("o-disc-4-l", generateLabel(23,  5,  3, 1));
		gameLabels.put("o-disc-x-l", generateLabel(23,  6, 15, 1));
		
		gameLabels.put("a-draw-x-l", generateLabel( 9, 15,  3, 1));
		gameLabels.put("a-fndn-x-l", generateLabel(15, 15, 15, 1));
		
		gameLabels.put("c-name-x-l", generateLabel(17, 20, 17, 1));
		gameLabels.put("c-hand-x-l", generateLabel(18, 23, 15, 1));
		gameLabels.put("c-stna-x-l", generateLabel(35, 21,  3, 1));
		gameLabels.put("c-stco-x-l", generateLabel(35, 22,  3, 1));
		gameLabels.put("c-disc-1-l", generateLabel( 1, 22,  3, 1));
		gameLabels.put("c-disc-2-l", generateLabel( 5, 22,  3, 1));
		gameLabels.put("c-disc-3-l", generateLabel( 9, 22,  3, 1));
		gameLabels.put("c-disc-4-l", generateLabel(13, 22,  3, 1));
		gameLabels.put("c-disc-x-l", generateLabel( 1, 21, 15, 1));
		
		
		gameLabels.forEach((key, value) -> {
			gamePane.getChildren().add(value);
		});
		
		gameButtons.put("o-stoc-x-b", generateCardButton( 1,  1));
		gameButtons.put("o-disc-1-b", generateCardButton(35,  1));
		gameButtons.put("o-disc-2-b", generateCardButton(31,  1));
		gameButtons.put("o-disc-3-b", generateCardButton(27,  1));
		gameButtons.put("o-disc-4-b", generateCardButton(23,  1));
		gameButtons.put("o-hand-0-b", generateCardButton( 6,  0));
		gameButtons.put("o-hand-1-b", generateCardButton( 9,  0));
		gameButtons.put("o-hand-2-b", generateCardButton(12,  0));
		gameButtons.put("o-hand-3-b", generateCardButton(15,  0));
		gameButtons.put("o-hand-4-b", generateCardButton(18,  0));
		
		gameButtons.put("a-draw-x-b", generateCardButton( 9, 11));
		gameButtons.put("a-fndn-1-b", generateCardButton(15, 11));
		gameButtons.put("a-fndn-2-b", generateCardButton(19, 11));
		gameButtons.put("a-fndn-3-b", generateCardButton(23, 11));
		gameButtons.put("a-fndn-4-b", generateCardButton(27, 11));
		
		gameButtons.put("c-stoc-x-b", generateCardButton(35,  23));
		gameButtons.put("c-disc-1-b", generateCardButton( 1,  23));
		gameButtons.put("c-disc-2-b", generateCardButton( 5,  23));
		gameButtons.put("c-disc-3-b", generateCardButton( 9,  23));
		gameButtons.put("c-disc-4-b", generateCardButton(13,  23));
		gameButtons.put("c-hand-0-b", generateCardButton(18,  24));
		gameButtons.put("c-hand-1-b", generateCardButton(21,  24));
		gameButtons.put("c-hand-2-b", generateCardButton(24,  24));
		gameButtons.put("c-hand-3-b", generateCardButton(27,  24));
		gameButtons.put("c-hand-4-b", generateCardButton(30,  24));
		
		gameButtons.forEach((key, value) -> {
			gamePane.getChildren().add(value);
		});
		
		turnLogSidebar = new ScrollPane();
		turnLogSidebar.setLayoutX(39 * ds);
		turnLogSidebar.setLayoutY(0 * ds);
		turnLogSidebar.setPrefHeight(28 * ds);
		turnLogSidebar.setPrefWidth(17 * ds);
		turnLogSidebar.setContent(innerTurnLogSidebar);
		gamePane.getChildren().add(turnLogSidebar);
		
		populateGamePane();
	}
	
	/**
	 * Creates the Rules Pane, which basically just lists the game rules.
	 */
	private void drawRulesPane() {
		VBox innerRulesPane = new VBox();
		innerRulesPane.setStyle(Styles.TAB);
		
		int numItems = Instructions.headings.length;
		for(int i = 0; i < numItems; i++) {
			Text heading = new Text(Instructions.headings[i]);
			heading.setWrappingWidth(ds * xTotal - 10);
			heading.setFont(new Font(32));
			heading.setFill(Color.WHITE);
			Text text = new Text(Instructions.paragraphs[i]);
			text.setWrappingWidth(ds * (xTotal - 1));
			text.setFont(new Font(16));
			text.setFill(Color.WHITE);
			innerRulesPane.getChildren().addAll(heading, text);
		}
		rulesPane.setFitToWidth(true);
		rulesPane.setFitToHeight(true);
		rulesPane.setContent(innerRulesPane);
	}
	
	
	/**
	 * Generates a card Button element according to specifications
	 * @param id a string that represents this button. Used to identify the button when it has been clicked.
	 * @param x the x-coordinate of this button, in ds units. 
	 * @param y the y-coordinate of this button, in ds units.
	 * @param value the value to display on this button. Currently set up to take in the path of the image.
	 * @return the Button generated by these specifications.
	 */
	private Button generateCardButton(double x, double y) {
		Button button = new Button();
		button.setLayoutX(x * ds);
		button.setLayoutY(y * ds);
		button.setPrefSize(3 * ds, 4 * ds);
		button.setPadding(new Insets(0));
		button.setStyle(Styles.CARD_BASE);
		button.setOnAction(this);
		return button;
	}
	
	
	/**
	 * Generates a Label element according to specifications
	 * @param id a string that represents this label. Used to identify the label.
	 * @param x the x-coordinate of this Label, in ds units. 
	 * @param y the y-coordinate of this Label, in ds units.
	 * @param w the width of this Label, in ds units.
	 * @param h the height of this Label, in ds units.
	 * @param value the value to display on this Label.
	 * @return the Label generated by these specifications.
	 */
	private Label generateLabel(double x, double y, double w, double h) {
		// Set up the initial Label 
		Label label = new Label();
		label.setLayoutX(x * ds);
		label.setLayoutY(y * ds);
		label.setPrefSize(w * ds, h * ds);
		label.setStyle(Styles.getBodyTextStyle(ds));
		return label;
	}


	/**
	 * Generates a slider that can be used to get input for the Game Length
	 * @return the generated Slider
	 */
	public Slider generateGameLengthSlider() {
		slider = new Slider(5, 40, 20);
		slider.setMajorTickUnit(5);
		slider.setMinorTickCount(0);
		slider.snapToTicksProperty().set(true);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setStyle(Styles.SLIDER);
		return slider;
	}
	
	
	/**
	 * Populates the game pane with information. 
	 * This updates every single label and button, including the ones that don't necessarily need it.
	 */
	public void populateGamePane() {
		ArrayList<String> log = game.getLogContents();
		innerTurnLogSidebar.getChildren().clear();
		
		Text text = new Text("Turn Log");
		text.setWrappingWidth(ds * 16);
		text.setFont(new Font(ds * 1.2));
		text.setFill(Color.web("#073763"));
		innerTurnLogSidebar.getChildren().add(text);
		
		for(String s : log) {
			text = new Text("-- " + s);
			text.setWrappingWidth(ds * 16);
			text.setFont(new Font(ds * 0.75));
			text.setFill(Color.web("#073763"));
			innerTurnLogSidebar.getChildren().add(text);
		}
		
		gameLabels.forEach((key, value) -> {
			if(key.matches("(o|c)-name-x-l")) {
				if(key.charAt(0) == 'o') {
					value.setText(game.getPlayerName(false));
					value.setStyle(Styles.getHeadingTextStyle(ds, game.getPlayerColor(false)));
				} else {
					value.setText(game.getPlayerName(true));
					value.setStyle(Styles.getHeadingTextStyle(ds, game.getPlayerColor(true)));
				}
			} else if(key.matches("(o|c)-hand-x-l")) {
				if(key.charAt(0) == 'o') {
					value.setText(game.getHandCount(false));
				} else {
					value.setText(game.getHandCount(true));
				}
			} else if(key.matches("(o|c)-stna-x-l")) {
				value.setText("Stock");
			}  else if(key.matches("(o|c)-stco-x-l")) {
				if(key.charAt(0) == 'o') {
					value.setText(game.getStockCount(false));
				} else {
					value.setText(game.getStockCount(true));
				}
			} else if(key.matches("(o|c)-disc-(1|2|3|4)-l")) {
				if(key.charAt(0) == 'o') {
					value.setText(game.getDiscardCount(false, key.charAt(7)));
				} else {
					value.setText(game.getDiscardCount(true, key.charAt(7)));
				}
			} else if(key.matches("(o|c)-disc-x-l")) {
				value.setText("Discard");
			} else if(key.matches("a-draw-x-l")) {
				value.setText("Draw");
			} else if(key.matches("a-fndn-x-l")) {
				value.setText("Foundation");
			} else if(key.matches("(a|c|o)-rect-(0|1|2)-l")) {
				switch(key.charAt(0)) {
				case 'o':
					value.setStyle(Styles.getRectangleStyle(game.getPlayerColor(false)));
					break;
				case 'c':
					value.setStyle(Styles.getRectangleStyle(game.getPlayerColor(true)));
					break;
				default:
					value.setStyle(Styles.DECORATIVE_RECTANGLE);
			}
			
		}
		});
		
		gameButtons.forEach((key, value) -> {
			value.setVisible(true);
			
			if(key.matches("(o|c)-stoc-x-b")) {
				if(key.charAt(0) == 'o') {
					value.setGraphic(generateCardImage(game.getStockTop(false), key));
				} else {
					value.setGraphic(generateCardImage(game.getStockTop(true), key));
				}
			} else if(key.matches("(o|c)-disc-(1|2|3|4)-b")) {
				if(key.charAt(0) == 'o') {
					value.setGraphic(generateCardImage(game.getDiscardTop(false, key.charAt(7)), key));
				} else {
					value.setGraphic(generateCardImage(game.getDiscardTop(true, key.charAt(7)), key));
				}
			} else if(key.matches("(o|c)-hand-(0|1|2|3|4)-b")) {
				Card handAtIndex;
				if(key.charAt(0) == 'o') {
					handAtIndex = game.getHandAtIndex(false, key.charAt(7));
				} else {
					handAtIndex = game.getHandAtIndex(true, key.charAt(7));
				}
				if(handAtIndex == null){
					value.setVisible(false);
				} else {
					value.setGraphic(generateCardImage(handAtIndex, key));
				}
			} else if(key.matches("a-draw-x-b")) {
				value.setGraphic(generateCardImage(null, key));
			} else if(key.matches("a-fndn-(1|2|3|4)-b")) {
				value.setGraphic(generateCardImage(game.getFoundationTop(key.charAt(7)), key));
			}
			
			if(key.matches(selectedCard)) {
				value.setStyle(Styles.SELECTED);
			} else {
				value.setStyle(Styles.CARD_BASE);
			}
		});
	}
	
	
	/**
	 * This creates a picture of a Skip-Bo Card.
	 * @param ImageName the name of the image as a string. The file path will be set up by this method
	 * @return ImageView the Image of the Skip-Bo Card
	 */
	private ImageView generateCardImage(Card card, String key) {
		String imgString = "Empty.jpg";
		if(card == null) {
			if(key.matches("a-draw-x-b")) {
				imgString = "CardBack.jpg";
			}
		} else {
			if (key.matches(".-hand-.-b")) {
				if(key.charAt(0) == 'c' && game.currentPlayer().getPlayerType() == PlayerType.HUMAN) {
					imgString = card.getImagePath();
				} else {
					imgString = "CardBack.jpg";
				}
			} else {
				imgString = card.getImagePath();
			}
		}
		
		return new ImageView(new Image(SkipBoFXApp.class.getResourceAsStream(imagePath + imgString), 
				3 * ds, 4 * ds, true, true));
	}
	
	
	/**
	 * Handles the selection (and deselection) of a card, by id
	 * @param the id of the card to select or deselect
	 */
	private void selectCard(String key) throws RuntimeException {
	// Deselect if already selected the same card
		if (selectedCard.matches("none")) {
			selectedCard = key;
			// Restyle whichever card this may be, accordingly
			if(gameButtons.containsKey(key)) {
				gameButtons.get(key).setStyle(Styles.SELECTED);
			} else {
				//System.out.println("    Could not select " + key);
			}
		} else if(selectedCard.matches(key)) {
			selectedCard = "none";
			if(gameButtons.containsKey(key)) {
				gameButtons.get(key).setStyle(Styles.CARD_BASE);
			} else {
				//System.out.println("    Could not select " + key);
			}
		} else {
			throw new RuntimeException("You already have a card selected.");
		}
	}
}
