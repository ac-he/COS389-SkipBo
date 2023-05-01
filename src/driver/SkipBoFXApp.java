package driver;

import styles.Styles;
import users.PlayerColor;
import users.PlayerType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import resources.Instructions;


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
	private final double xTotal = 39; // Total X-dimension in ds
	private final double yTotal = 28; // Total Y-dimension in ds
	private final String imagePath = ""; // In case Java lets me move the assets into a different folder.
	
	// DISPLAY ELEMENTS
	// The root element, a tab pane
	private TabPane root = new TabPane(); 
	// The three tabs contained within the tab pane
	private Tab settingsTab;
	private Tab gameTab;
	private Tab rulesTab;
	// The containers that live inside each of the tabs, respectively
	private VBox settingsPane = new VBox();
	private AnchorPane gamePane = new AnchorPane();
	private ScrollPane rulesPane = new ScrollPane();
	// The elements that live inside of the settings tab
	private TextField stfP1Name, stfP2Name;
	private Button submit;
	private ComboBox<PlayerColor> scbP1Color, scbP2Color;
	private ComboBox<PlayerType> scbP2Type;
	public Slider slider;
	// Stores the ever-regenerating contents of the game tab
	private ArrayList<Button> buttons = new ArrayList<Button>();
	
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
		if(evt.getPropertyName().matches("wait")) {
//			System.out.println("	wait started");
//			for(int i = 0; i < 10000000; i++) {
//				ThreadLocalRandom.current().nextInt(0, 1000);
//			}
//			System.out.println("	wait stopped");
		}
		// Any property changes that happen will impact the state of the game, so we re-draw it.
		drawGamePane();
		
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
			for(Button button : buttons) {
				// If this was the button that sent the event...
				if(event.getSource().equals(button)) {
					// Get the id of the button. This will be used to figure out what action to take.
					String id = button.getId();
					
					//If it's the draw pile
					if(id.matches("draw")) {
						// Draw cards from the deck
						game.drawCards();
						selectedCard = "none";
						
					// If it's anything belonging to the opponent
					} else if (id.matches("o.{3}")) {
						// Send an alert because that's not a legal move.
						throw new RuntimeException("You can't interact with your opponent's cards.");
						
					// If it's anything in our hand
					} else if (id.matches("ch[0-4]c")) {
						// Try selecting that card.
						selectCard(id);
					
					// If it's anything in our discard pile
					} else if (id.matches("cd[1-4]c")) {
						// If we have a card from our hand selected
						if(selectedCard.matches("ch[0-4]c")) {
							// Discard it there
							game.discard(selectedCard.substring(1, 3), id.substring(1, 3));
							selectCard(selectedCard);
						} else {
							// Make sure it's not empty
							if(game.discardIsEmpty(true, id.charAt(2))) {
								throw new RuntimeException("You can't select this empty discard pile.");
							}
							
							// Try selecting that card.
							selectCard(id);
						}
						
					// If it's our stock
					} else if (id.matches("cssc")) {
						// Try selecting that card
						selectCard(id);
						
					// If it's anything in the foundations
					} else if (id.matches("df[1-4]c")) {
						// If we have a card from our hand selected
						if(selectedCard.matches("c..c")) {
							game.play(selectedCard.substring(1, 3), id.substring(1, 3));
							selectCard(selectedCard);
						} else {
							throw new RuntimeException("You have to select a card before playing "
									+ "it on a foundation.");
						}
						
					// If it's anything else, something's gone horribly wrong
					} else {
						throw new Exception("That input was not recognized.");
					}
					
					// No need to search the other buttons.
					return;
				}
			}
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
	 * This includes populating the pane with values drawn from the model.
	 */
	private void drawGamePane() {
		// Clear the old stuff out
		buttons.clear();
		gamePane.getChildren().clear();
		
		// "LOOSE RECTANGLES"
		// Essentially, these are just used to create zones of related cards for organization.
		gamePane.getChildren().addAll(generateLabel("rec2", 0.5, 0.5, 4, 7, ""),
				generateLabel("rec2", 5.5, 0, 16, 5.5, ""),
				generateLabel("rec2", 22.5, 0.5, 16, 7, ""),
				generateLabel("rect", 8.5, 10.5, 4, 6, ""),
				generateLabel("rect", 14.5, 10.5, 16, 6, ""),
				generateLabel("rec1", 0.5, 20.5, 16, 7, ""),
				generateLabel("rec1", 17.5, 22.5, 16, 5.5, ""),
				generateLabel("rec1", 34.5, 20.5, 4, 7, ""));
		
		// OPPONENET CARDS
		// Labels
		gamePane.getChildren().add(generateLabel("ogal", 5, 6, 17, 1, 
				game.getPlayerName(false)));
		gamePane.getChildren().add(generateLabel("ohal", 6, 4, 15, 1, 
				game.getHandCount(false)));
		
		// Stock Pile
		gamePane.getChildren().addAll(
				generateLabel("ossl", 1, 5, 3, 1, 
						game.getStockCount(false)),
				generateLabel("osll", 1, 6, 3, 1, "Stock"),
				generateCardButton("ossc", 1, 1,
						game.getStockTop(false)));
		
		// Discard Piles
		for(int i = 1; i < 5; i++) {
			gamePane.getChildren().addAll(
					generateLabel("od" + i + "l", xTotal - (4 * i), 5, 3, 1, 
							game.getDiscardCount(false, i)),
					generateCardButton("od" + i + "c", xTotal - (4 * i), 1, 
							game.getDiscardTop(false, i)));
		}
		gamePane.getChildren().add(generateLabel("odll", 23, 6, 15, 1, "Discard"));
		
		// Hand
		int numCardsO = game.getHandCountAsInt(false);
		for(int i = 0; i < numCardsO; i++) {
			gamePane.getChildren().add(generateCardButton("oh" + i + "c", 6 + (3 * i), 0,
			game.getHandAtIndex(false, i)));
		}
		
		// SHARED CARDS
		// Draw Pile
		gamePane.getChildren().addAll(generateCardButton("draw", 9, 11, game.getDraw()),
				generateLabel("drll", 9, 15, 3, 1, "Draw"),
				generateLabel("dfll", 15, 15, 15, 1, "Foundations"));
		
		// Foundation piles
		for(int i = 1; i < 5; i++) {
			gamePane.getChildren().addAll(
					generateCardButton("df" + i + "c", 15 + (4 * (i - 1)), 11, 
							game.getFoundationTop(i)));
		}
		
		// CURRENT PLAYER CARDS
		// Labels
		gamePane.getChildren().add(generateLabel("cgal", 17, 20, 17, 1, 
				game.getPlayerName(true)));
		gamePane.getChildren().add(generateLabel("chal", 18, 23, 15, 1, 
				game.getHandCount(true)));
		
		// Stock
		gamePane.getChildren().addAll(
				generateCardButton("cssc", 35, 23, 
						game.getStockTop(true)),
				generateLabel("csll", 35, 21, 3, 1, "Stock"),
				generateLabel("cssl", 35, 22, 2, 1, 
						game.getStockCount(true)));
		
		// Discard Piles
		for(int i = 1; i < 5; i++) {
			gamePane.getChildren().addAll(
					generateCardButton("cd" + i + "c", 1 + (4 * (i - 1)), 23, 
							game.getDiscardTop(true, i)),
					generateLabel("cd" + i + "l", 1 + (4 * (i - 1)), 22, 3, 1, 
							game.getDiscardCount(true, i)));
		}
		gamePane.getChildren().add(generateLabel("odll", 1, 21, 15, 1, "Discard"));
		
		// Hand
		int numCardsC = game.getHandCountAsInt(true);
		for(int i = 0; i < numCardsC; i++) {
			gamePane.getChildren().add(generateCardButton("ch" + i + "c", 18 + (3 * i), 24,
					game.getHandAtIndex(true, i)));
		}
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
	private Button generateCardButton(String id, double x, double y, String value) {
		// Create the button
		Button button = new Button();
		button.setId(id);
		
		// Get the image associated with this card and add it to the Button
		ImageView view = new ImageView(new Image(imagePath + value, 3 * ds, 4 * ds, true, true));
		button.setGraphic(view);
		button.setPadding(new Insets(0));
		button.setStyle(Styles.CARD_BASE);
		button.setPrefSize(3 * ds, 4 * ds);
		
		// Style the card style specially if it has been selected
		if(id.equals(selectedCard)) {
			button.setStyle(Styles.SELECTED);
		}
		
		// Add the card to where it lives within the anchor pane.
		AnchorPane.setLeftAnchor(button, x * ds);
		AnchorPane.setRightAnchor(button, (xTotal - x - 3) * ds);
		AnchorPane.setTopAnchor(button, y * ds);
		AnchorPane.setBottomAnchor(button, (yTotal - y - 4) * ds);
		
		// Set the button up to be interacted with by the Controller
		button.setOnAction(this);
		buttons.add(button);
		
		// Return the fully-formed button!
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
	private Label generateLabel(String id, double x, double y, double w, double h, String value) {
		// Set up the initial Label 
		Label label = new Label(value);
		label.setId(id);
		
		// Style the Label according to its id
		if(id.matches("ogal")) {
			label.setStyle(Styles.getHeadingTextStyle(ds, game.getPlayerColor(false)));
		} else if(id.matches("cgal")) {
			label.setStyle(Styles.getHeadingTextStyle(ds, game.getPlayerColor(true)));
		} else if (id.matches("rect")) {
			label.setStyle(Styles.DECORATIVE_RECTANGLE);
		} else if (id.matches("rec2")) {
			label.setStyle(Styles.getRectangleStyle(game.getPlayerColor(false)));
		} else if (id.matches("rec1")) {
			label.setStyle(Styles.getRectangleStyle(game.getPlayerColor(true)));
		} else {
			label.setStyle(Styles.getBodyTextStyle(ds));
		}
		
		// Position the Label within the anchor pane.
		AnchorPane.setLeftAnchor(label, x * ds);
		AnchorPane.setRightAnchor(label, (xTotal - x - w) * ds);
		AnchorPane.setTopAnchor(label,  y * ds);
		AnchorPane.setBottomAnchor(label, (yTotal - y - h) * ds);
		
		// Return the completed Label
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
	 * Handles the selection (and deselection) of a card, by id
	 * @param the id of the card to select or deselect
	 */
	private void selectCard(String id) throws RuntimeException {
		// Deselect if already selected the same card
		if(selectedCard.equals(id)) {
			selectedCard = "none";
		} else if (selectedCard.equals("none")) {
			selectedCard = id;
		} else {
			throw new RuntimeException("You already have a card selected.");
		}
		
		// Restyle whichever card this may be, accordingly
		for (Button button : buttons) {
			if(button.getId().equals(selectedCard)) {
				button.setStyle(Styles.SELECTED);
			} else {
				button.setStyle(Styles.CARD_BASE);
			}
		}
	}
}
