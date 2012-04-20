package coroutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import memory.CardButton;
import memory.MemoryGame;

/**
 *
 * @author Erik
 */
public class MemoryGameController extends Coroutine implements ActionListener {

    private volatile ActionEvent event;
    private final MemoryGame game;
    /**
     * the first CardButton that the user clicks on in search for a matching
     * pair
     */
    private CardButton selection1 = null;
    /**
     * the second CardButton that the user clicks on in search for a matching
     * pair
     */
    private CardButton selection2 = null;
    /**
     * The number of times during a game that the user fails to find a matching
     * pair
     */
    private int misses = 0;
    /**
     * Timer restarted upon entering state C (2 cards selected) and stopped upon
     * exiting State C. If this timer expires, the two cards selected in State C
     * are automatically flipped if they do not match, and a transition to State
     * A (no cards selected) occurs.
     */
    private Timer autoFlipTimer = new Timer(1000, this);

    public MemoryGameController(MemoryGame game) {
        if (game == null) {
            throw new IllegalArgumentException("Argument must be non-null.");
        }
        this.game = game;
        autoFlipTimer.setRepeats(false);
    }

    /**
     * Called from the coroutine / event handler thread. This method calls
     * {@link #nextEvent()} repeatedly until a null event is encountered and
     * handles each returned event.
     *
     * @throws InterruptedException if the consumer thread is interrupted
     */
    @Override
    public void execute() throws InterruptedException {
        while (true) {
            playGame();
            if (!continuePlaying()) {
                System.exit(0);
            }
            resetGame();
        }
    }

    private void playGame() throws InterruptedException {
        boolean done = false;
        nextEvent();
        while (!done) {
            selectFirstCard();
            nextEvent();
            selectSecondCard();
            if (!selection1.getCard().equals(selection2.getCard())) {
                misses++;
                autoFlipTimer.start();
                nextEvent();
                autoFlipTimer.stop();//does nothing if autoFlipTimer has expired
                selection1.flip();
                selection2.flip();
                if (event.getSource() == autoFlipTimer) {
                    nextEvent();
                }
            } else if (game.isEndOfGame()) {
                done = true;
            } else {
                nextEvent();
            }
            selection1 = selection2 = null;
        }
    }

    private void selectFirstCard() throws InterruptedException {
        assert event.getSource() instanceof CardButton;
        CardButton cb = (CardButton) event.getSource();
        while (cb.isFaceUp()) {
            nextEvent();
            cb = (CardButton) event.getSource();
        }
        cb.flip();
        selection1 = cb;
    }

    private void selectSecondCard() throws InterruptedException {
        assert event.getSource() instanceof CardButton;
        CardButton cb = (CardButton) event.getSource();
        while (cb.isFaceUp()) {
            nextEvent();
            cb = (CardButton) event.getSource();
        }
        cb.flip();
        selection2 = cb;
    }

    /**
     * Asks the user if he/she wishes to continue playing. This is a little
     * tricky, because the dialog needs to be run on the EDT, whereas this
     * method is called from the event handler thread.
     *
     * @return
     * @throws InterruptedException
     */
    private boolean continuePlaying() throws InterruptedException {
        final boolean[] res = new boolean[1];
        //Place a Runnable on the EDQ
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    res[0] = JOptionPane.showConfirmDialog(
                            null,
                            "You had " + misses + " misses" + ".\nPlay again?",
                            "End of Game", JOptionPane.YES_NO_OPTION)
                            == JOptionPane.YES_OPTION;

                    reattach(); // to pass control back to the event handler 
                } catch (InterruptedException ex) {
                }
            }
        });
        detach(); // to let the Runnable run
        return res[0];
    }

    /**
     * This method waits for he EDT to make an ActionEvent available through a
     * call to {@link #actionPerformed(java.awt.event.ActionEvent) } (or {@link #cancel()},
     * in which case a null event is produced), then returns that event. The
     * method is called on the event handler thread.
     *
     * This method returns null if the last event has been encountered (i.e.,
     * <code>null</code> serves as an EOF indicator.
     *
     * @return the most recent MouseEvent.
     * @throws InterruptedException if the event handler thread is interrupted.
     */
    public final void nextEvent() throws InterruptedException {
        detach(); // wait for the EDT to produce an event and call actionPerformed()
        if (isCancelled()) {
            event = null; // serves as EOF event
        }
    }

    /**
     * Starts the MemoryGameController thread. This method is called from the EDT.
     */
    public final void enter() {
        attach();
    }

    /**
     * This method is invoked from the EDT.
     *
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            this.event = event;
            reattach();
            //
        } catch (InterruptedException ex) {
        }
    }

    private void resetGame() {
        game.reset();
        selection1 = selection2 = null;
        misses = 0;
    }
}
