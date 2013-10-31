package memory;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.wintrisstech.cards.Card;
import org.wintrisstech.cards.Deck;

/**
 * The Memory Game is a game where cards are laid out facing down in a grid. For
 * each card, there is a matching card with the same suit and number. The user
 * clicks on one card then on another card. After each click, the selected card
 * is flipped, thus showing the card's face. If the two cards match, they remain
 * facing up. If they do not match, then, upon the user's next click or after
 * one second, whichever occurs first, the two cards are flipped so they face
 * down again. The game continues until all cards are facing up.
 * 
 * @author Erik Colban &copy; 2013
 */
@SuppressWarnings("serial")
public class MemoryGame extends JPanel implements Runnable, ActionListener {

	/**
	 * Constants
	 */
	private static final int NUM_ROWS = 4;
	private static final int NUM_COLUMNS = 6;
	private static final int NUM_CARDS = NUM_ROWS * NUM_COLUMNS;
	private static final int AUTOFLIP_TIME = 1000;

	/**
	 * Variables associated with the state of the game
	 */
	private int state = 0; // 3 states total: 0, 1, and 2
	private CardButton first; // first card button in selecting a pair
	private CardButton second; // second card button in selecting a pair
	private int misses = 0; //number of non-matching selected pairs
	private int matches = 0; // number of matching selected pairs

	/**
	 * Resources / helpers
	 */
	private final Deck deck; // A deck of cards
	private final Random random = new Random(); //A random number generator
	private final CardButton[] buttons; // An array to hold all the card buttons
	private final Timer autoFlipTimer; // A timer used to automatically flip over two non-matching cards

	// private coroutine.MemoryGameController controller = new
	// coroutine.MemoryGameController(this);

	/**
	 * Creates an instance of the MemoryGame with numRows rows and numColumns
	 * columns.
	 * 
	 */
	private MemoryGame() {

		buttons = new CardButton[NUM_CARDS];
		deck = new Deck(Color.RED);
		autoFlipTimer = new Timer(AUTOFLIP_TIME, this);
		autoFlipTimer.setRepeats(false); //prevents the timer from automatically restarting after expiring
	}

	/**
	 * Starts the game.
	 * 
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new MemoryGame());
	}

	/**
	 * Initializes the MemoryGame. Generates a list of CardButtons and places
	 * them on a grid inside a JFrame.
	 */
	@Override
	public void run() {
		JFrame frame = new JFrame("Memory");
		setLayout(new GridLayout(NUM_ROWS, NUM_COLUMNS));
		Card[] selectedCards = selectCards();
		for (int i = 0; i < NUM_CARDS; i++) {
			CardButton cardButton = new CardButton(selectedCards[i]);
			cardButton.addActionListener(this);
			add(cardButton); // Adds the button to the grid
			buttons[i] = cardButton; // Maintain an array of all the buttons
		}
		frame.add(this);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Produces an array containing a random selection of cards where each card
	 * occurs exactly two times in the array. Cards occur in random order.
	 * <p>
	 * The "inside-out" version of the Fisher-Yates shuffle algorithm, which
	 * simultaneously initializes and shuffles the array, is used.
	 * 
	 * @return the Card array
	 */
	private Card[] selectCards() {
		if (deck.getCount() < NUM_CARDS / 2) {
			deck.shuffle();
		}
		Card[] selection = new Card[NUM_CARDS];
		Card card = deck.getCard();
		selection[1] = selection[0] = card;
		for (int index = 2; index < NUM_CARDS; index++) {
			if ((index & 1) == 0) {
				card = deck.getCard();
			}
			int randomIndex = random.nextInt(index + 1);
			selection[index] = selection[randomIndex];
			selection[randomIndex] = card;
		}
		return selection;
	}

	/**
	 * Handles action events. The state diagram is shown below.
	 * <pre>
	 *    +-+               +-+
	 *c.u.| |           c.u.| |
	 *    | v               | v
	 *  +-----+           +-----+
	 *  |  0  |   c.d.    |  1  |
	 *  |     |---------->|     |<---+
	 *  +-----+           +-----+    |
	 *    ^ ^                |       |
	 *    | |                |c.d.   |
	 *    | |           Y    |       |
	 *c.u.| +-------------[match?]   |c.d.
	 *t.e.|                  | N     |
	 *    |    +-----+       |       |           
	 *    +----|  2  |<------+       |
	 *         |     |---------------+
	 *         +-----+               
	 * </pre>
	 * 
	 * There are three states; 0, 1 and 2, the number representing how many cards 
	 * of the last pair is facing up. The event "c.d." is short for a button click
	 * on a card button facing down, and "c.u." is short for a button click on a
	 * card facing up. The event "t.e." is the event when the auto-flip timer 
	 * expires.
	 * <p>
	 * The handling of the c.u. and c.d. events is done by the method 
	 * {@link #handleClick(CardButton)},
	 * whereas the handling of the timer expiration is done by the method 
	 * {@link #autoFlip()}.
	 * 
	 * @param evt an event coming from the auto-flip timer or from a button click
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == autoFlipTimer) {
			autoFlip();
		} else {
			CardButton button = (CardButton) evt.getSource();
			handleClick(button);
		}
	}

	/**
	 * Handles a click on one of the card buttons.
	 * @param button the button that was clicked
	 */
	private void handleClick(CardButton button) {
		switch (state) {
		case 0:
			if (button.isFaceUp()) {
				//do nothing
			} else {
				first = button;
				first.flip(); // face up
				state = 1;
			}
			break;
		case 1:
			if (button.isFaceUp()) {
				// do nothing
			} else {
				second = button;
				second.flip(); //face up
				if (first.getCard().equals(second.getCard())) {
					matches++;
					endOfGameCheck();
					state = 0;
				} else {
					misses++;
					autoFlipTimer.start();
					state = 2;
				}
			}
			break;
		case 2:
			autoFlipTimer.stop();
			first.flip(); //face down
			second.flip(); //face down
			if (button.isFaceUp()) {
				state = 0;
			} else {
				first = button;
				first.flip(); //face up
				state = 1;
			}
			break;
		default:
		}
	}

	/**
	 * Checks if the end of the game has been reached and, if so, 
	 * asks the user if he/she wants to play again. If the user does
	 * not want to play again, the program shuts down.
	 * 
	 * @return true if end of game
	 */
	private void endOfGameCheck() {
		if (endOfGame()) {
			if (playAgain()) {
				resetGame();
			} else {
				System.exit(0);
			}
		}
	}

	/**
	 * Handles the expiration of the auto-flip timer.
	 */
	private void autoFlip() {
		if (state == 2) { //This test is needed in case of race condition
			first.flip();
			second.flip();
			state = 0;
		}
	}
	
	/**
	 * Checks if the end of the game has been reached.
	 * @return true if end of game
	 */
	private boolean endOfGame() {
		return matches * 2 == NUM_CARDS;
	}

	/**
	 * Asks the user if he/she wants to play again. 
	 * @return true if the user wants to play again.
	 */
	private boolean playAgain() {
		return JOptionPane.showConfirmDialog(this, "You had " + misses
				+ (misses == 1 ? " miss" : " misses") + ".\nPlay again?",
				"End of Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

	}

	/**
	 * Re-initializes the game
	 */
	private void resetGame() {
		final Card[] selectedCards = selectCards();
		for (int i = 0; i < selectedCards.length; i++) {
			buttons[i].setCard(selectedCards[i]);
		}
		misses = matches = 0;
	}
}