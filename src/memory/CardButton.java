package memory;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.wintrisstech.cards.Card;

/**
 * A CardButton is a JButton that is associated with a Card. A CardButton
 * can be in one of two states; face up or face down. In the face up state it 
 * displays the face of the associated Card, and in the face down state it 
 * displays the back of the associated Card.
 * 
 * @see javax.swing.JButton
 *
 * @author Erik Colban
 */
public class CardButton extends JButton {

    private boolean faceUp = false;
    private Card card;
    private Icon faceIcon;
    private final Icon backIcon;

    /**
     * Constructor
     *
     * @param card the Card of this CardButton
     * @throws IllegalArgumentException if card is null
     */
    CardButton(Card card) throws IllegalArgumentException {
        if (card == null) {
            throw new IllegalArgumentException("Parameter card must be non-null.");
        }
        this.card = card;
        faceIcon = new ImageIcon(card.getFaceImage());
        backIcon = new ImageIcon(card.getBackImage());
        faceUp = false;
        setIcon(backIcon);
        setBorder(null);
    }

    /**
     * Toggles the state of the CardButton from face up to face down or vice
     * versa.
     */
    public void flip() {
        faceUp = !faceUp;
        setIcon(faceUp ? faceIcon : backIcon);
        repaint();
    }

    /**
     * Gets the state of the CardButton
     * @return true if the CardButton is in the face up state; false otherwise.
     */
    public boolean isFaceUp() {
        return faceUp;
    }

    /**
     * @return the CardButton's associated Card
     */
    public Card getCard() {
        return card;
    }

    /**
     * Sets the CardButton's associated Card and sets the state of the
     * CardButton to the face down state.
     * @param card a non-null instance of Card
     * @throws IllegalArgumentException if the parameter is null
     */
    public void setCard(Card card) throws IllegalArgumentException {
        if (card == null) {
            throw new IllegalArgumentException("Parameter card must be non-null.");
        }
        this.card = card;
        faceUp = false;
        faceIcon = new ImageIcon(card.getFaceImage());
        setIcon(backIcon);
        repaint();
    }
}