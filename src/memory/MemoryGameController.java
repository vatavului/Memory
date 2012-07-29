package memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * This class handles ActionEvents that are fired when a user clicks on a
 * CardButton. The handling depends on the state of this ActionListener.
 *
 * <p> There are 3 states, determined by how far the user has come in selecting
 * a pair of cards.
 *
 * <ol>
 *
 * <li>State A: no cards selected.
 *
 * <li>State B: 1 card selected.
 *
 * <li>State C: 2 cards selected.
 *
 * </ol>
 *
 * There are three kinds of events:
 *
 * <ol>
 *
 * <li> clickUp: user clicks on a CardButton that is face up.
 *
 * <li> clickDown: user clicks on a CardButton that is face down.
 *
 * <li> autoFlip: the autoFlipTimer fires. This event can only occur in State C.
 *
 * </ol>
 *
 * The state transitions and the actions performed are as shown below:
 *
 * <ol>
 *
 * <li> State A -- clickUp --> State A / Do nothing
 *
 * <li> State A - clickDown -> State B / Clicked CardButton becomes 1st
 * selection and is flipped.
 *
 * <li> State B -- clickUp --> State B / Do nothing
 *
 * <li> State B - clickDown -> State C / Selected CardButton becomes 2nd
 * selection and is flipped. End-of-game check. Start autoFlipTimer.
 *
 * <li> State C -- clickUp --> State A / If 1st and 2nd selections do not match,
 * they are flipped back again. Selections are cleared. Stop autoFlipTimer
 *
 * <li> State C - clickDown -> State B / If 1st and 2nd selections do not match,
 * they are flipped back again. Stop autoFlipTimer. Clicked CardButton becomes
 * 1st selection and is flipped.
 *
 * <li> State C - autoFlip --> State A / If 1st and 2nd selections do not match,
 * they are flipped back again. Selections are cleared.
 *
 * </ol>
 *
 * @see CardButton
 * @see MemoryGame
 *
 * @author Erik Colban
 */
public class MemoryGameController implements ActionListener
{

    /**
     * the first CardButton that the user clicks on in search for a matching
     * pair
     */
    private CardButton selection1 = null;
    /*
     * the second CardButton that the user clicks on in search for a matching
     * pair
     */
    private CardButton selection2 = null;
    /**
     * The number of times during a game that the user fails to find a matching
     * pair
     */
    private int misses;

    private interface State
    {

        void clickUp(CardButton cb);

        void clickDown(CardButton cb);

        void autoFlip();
    }
    private State noCardSelectedState = new State()
    {
        @Override
        public void clickUp(CardButton cb)
        {
        }

        @Override
        public void clickDown(CardButton cb)
        {
            assert selection1 == null && selection2 == null;
            // transition to state B (one card selected)
            selection1 = cb;
            selection1.flip();
            currentState = oneCardSelectedState;
        }

        @Override
        public void autoFlip()
        {
            assert false;
        }
    };
    private State oneCardSelectedState = new State()
    {
        @Override
        public void clickUp(CardButton cb)
        {
            //do nothing
        }

        @Override
        public void clickDown(CardButton cb)
        {
            assert selection1 != null && selection2 == null;
            // transition to state C (2 cards selected)
            selection2 = cb;
            selection2.flip();
            if (game.isEndOfGame())
            {
                // End of game; continue playing or exit.
                if (continuePlaying())
                {
                    game.reset();
                    selection1 = selection2 = null;
                    misses = 0;
                    currentState = noCardSelectedState;
                } else
                {
                    System.exit(0);
                }
            } else
            {
                currentState = twoCardsSelectedState;
                autoFlipTimer.start();
            }
        }

        @Override
        public void autoFlip()
        {
            assert false;
        }
    };
    private State twoCardsSelectedState = new State()
    {
        @Override
        public void clickUp(CardButton cb)
        {

            autoFlipTimer.stop();
            // Check for a match:
            if (!selection1.getCard().equals(selection2.getCard()))
            {
                // no match
                misses++;
                selection1.flip(); // flip so card faces down again
                selection2.flip(); // flip so card faces down again
            }
            selection1 = null;
            selection2 = null;
            currentState = noCardSelectedState;
            if (!cb.isFaceUp())
            {
                selection1 = cb;
                selection1.flip();
                currentState = oneCardSelectedState;
            }
        }

        @Override
        public void clickDown(CardButton cb)
        {
            autoFlipTimer.stop();
            // Check for a match:
            if (!selection1.getCard().equals(selection2.getCard()))
            {
                // no match
                misses++;
                selection1.flip(); // flip so card faces down again
                selection2.flip(); // flip so card faces down again
            }
            selection1 = cb;
            selection1.flip(); // flip so card faces up.
            selection2 = null;
            currentState = oneCardSelectedState;
        }

        @Override
        public void autoFlip()
        {
//            autoFlipTimer.stop();
            if (!selection1.getCard().equals(selection2.getCard()))
            {
                misses++;
                selection1.flip(); // flip so card faces down again
                selection2.flip(); // flip so card faces down again
            }
            selection1 = null;
            selection2 = null;
            currentState = noCardSelectedState;
        }
    };
    private State currentState = noCardSelectedState;
    /**
     * Timer restarted upon entering state C (2 cards selected) and stopped upon
     * exiting State C. If this timer expires, the two cards selected in State C
     * are automatically flipped if they do not match, and a transition to State
     * A (no cards selected) occurs.
     */
    private Timer autoFlipTimer = new Timer(2000, new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            currentState.autoFlip();
        }
    });
    /**
     * The MemoryGame instance that contains the buttons for which this
     * CardButtonlistener handles the ActionEvents
     */
    private MemoryGame game;

    /**
     * Creates an instance of MemoryGameController.
     *
     * @param game the MemoryGame instance that contains the CardButtons for
     * which this CardButtonlistener handles the ActionEvents
     */
    MemoryGameController(MemoryGame game)
    {
        if (game == null)
        {
            throw new IllegalArgumentException("Argument must be non-null.");
        }
        this.game = game;
        autoFlipTimer.setRepeats(false);
    }

    /**
     * Handles a user click on a CardButton or a timer event.
     *
     * @param event the ActionEvent produced when a user clicks on a CardButton.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
        CardButton cb = (CardButton) event.getSource();
        if (cb.isFaceUp())
        {
            currentState.clickUp(cb);
        } else
        {
            currentState.clickDown(cb);
        }
    }

    /**
     * Asks if the user wants to play again.
     *
     * @return true is the user answers yes.
     */
    private boolean continuePlaying()
    {
        return JOptionPane.showConfirmDialog(
                null,
                "You had " + misses + (misses == 1 ? " miss" : " misses") + ".\nPlay again?",
                "End of Game", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}