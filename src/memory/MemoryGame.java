package memory;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
 * @author Erik Colban
 */
public class MemoryGame extends JPanel implements Runnable {

    /**
     * The Deck from which Cards used in the game are selected.
     */
    private final Deck deck;
    private final int numRows;
    private final int numColumns;
    private final int numCards;
    private final Random random = new Random();
    /**
     * An array of all the CardButtons
     */
    private CardButton[] buttonArray;
    /**
     * The JFrame which contains this game.
     */
    private JFrame frame;
    /**
     * The ActionListener that handles clicks on all the CardButtons
     */
    private MemoryGameController buttonListener = new MemoryGameController(this);

    /**
     * Creates an instance of the MemoryGame with default number of rows (= 3) and
     * columns (= 6).
     */
    private MemoryGame() {
        this(4, 11);
    }

    /**
     * Creates an instance of the MemoryGame with numRows rows and numColumns columns.
     *
     * @param numRows the number of rows
     * @param numColumns  the number of columns
     */
    private MemoryGame(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        numCards = numRows * numColumns;
        buttonArray = new CardButton[numCards];
//        deck = new Deck(Color.RED);
//        deck = new Deck(Color.BLUE);
        deck = new Deck(Color.BLUE);
//        deck = new Deck(MemoryGame.class.getResource("images/back-dwight-150-1.png"));
    }

    /**
     * Starts the game.
     *
     * @param args the command line arguments.
     * <p> Either 0 or 2 arguments are accepted.
     * If there are no arguments, then default number of rows and columns 
     * apply. 
     * <p> If there are two arguments, then
     * they must be two parse-able integers. First argument is the number of
     * rows, which must be between 1 and 5. The second argument is the number of
     * columns, which must be between 1 and 10. Either the number of rows or the
     * number of columns must be even.
     *
     * @throws NumberFormatException if the line arguments are not two integers
     * @throws IllegalArgumentException if the first argument is not between 1
     * and 5 or the second argument is not between 1 and 10.
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                int rows = Integer.parseInt(args[0]);
                int cols = Integer.parseInt(args[1]);
                if (rows < 1 || rows > 5) {
                    System.out.println("First argument (the number of rows) must be between 1 and 5.");
                } else if (cols < 1 || cols > 10) {
                    System.out.println("Second argument (the number of columns) must be between 1 and 10.");
                } else if (cols * rows % 2 != 0) {
                    System.out.println("Either the number of rows or the number of columns must be even.");
                } else {

                    SwingUtilities.invokeLater(new MemoryGame(rows, cols));
                }
            } catch (NumberFormatException ex) {
                System.out.println("Arguments must be integers.");
            }
        } else if (args.length == 0) {
            SwingUtilities.invokeLater(new MemoryGame());
        } else {
            System.out.println("0 or 2 arguments are accepted");
        }
    }

    /**
     * Initializes the MemoryGame. Generates a list of CardButtons and places them on
     * a grid inside a JFrame.
     */
    public void run() {
        frame = new JFrame("Memory");
        setLayout(new GridLayout(numRows, numColumns));
        Card[] selectedCards = selectCards();
        for (int i = 0; i < numCards; i++) {
            CardButton cardButton = new CardButton(selectedCards[i]);
            cardButton.addActionListener(buttonListener);
            add(cardButton); //Adds the button to the grid
            buttonArray[i] = cardButton; //Maintain an array of all the buttons
        }
        frame.add(this);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
//        buttonListener.start();
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
        assert numCards % 2 == 0;
        if (deck.getCount() < numCards / 2) {
            deck.shuffle();
        }
        Card[] selection = new Card[numCards];
        Card card = deck.getCard();
        selection[0] = card;
        for (int index = 1; index < numCards; index++) {
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
     * Check if game is over.
     *
     * @return true if game is over
     */
    boolean isEndOfGame() {
        for (CardButton button : buttonArray) {
            if (!button.isFaceUp()) {
                //game is not over
                return false;
            }
        }
        return true; // game is over
    }

    /**
     * Re-initializes the game
     */
    void reset() {
        final Card[] selectedCards = selectCards();
        for (int i = 0; i < selectedCards.length; i++) {
            buttonArray[i].setCard(selectedCards[i]);
        }
    }
}