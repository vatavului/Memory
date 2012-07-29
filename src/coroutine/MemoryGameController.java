package coroutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import memory.CardButton;
import memory.MemoryGame;

/**
 * A coroutine based implementation of the controller.
 *
 * @author Erik
 */
public class MemoryGameController extends Coroutine implements ActionListener
{

    private volatile ActionEvent event;
    private final MemoryGame game;
    private boolean gameOver;
    /**
     * the pair of CardButtons that the user clicks on in search for a matching
     * pair
     */
    private CardButton[] selected = new CardButton[2];
    /**
     * The number of times during a game that the user fails to find a matching
     * pair
     */
    private int misses = 0;
    /**
     * Timer started upon selecting two mismatched cards. When this timer
     * expires, the two cards are flipped back again.
     */
    private Timer autoFlipTimer = new Timer(1000, this);

    /**
     * Constructs an instance of the MemoryGameController. A MemoryGame instance
     * is passed as argument, so that this controller can tell when the game to
     * reset itself when a game is over.
     *
     * @param game the MemoryGame instance
     */
    public MemoryGameController(MemoryGame game)
    {
        if (game == null)
        {
            throw new IllegalArgumentException("Argument must be non-null.");
        }
        this.game = game;
        autoFlipTimer.setRepeats(false);
    }

    /**
     * Called from the coroutine thread. This method calls {@link #nextEvent()}
     * repeatedly until a null event is encountered and handles each returned
     * event.
     *
     * @throws InterruptedException if the consumer thread is interrupted
     */
    @Override
    protected void execute() throws InterruptedException
    {
        nextEvent(); //1
        while (event != null)
        {
            playGame();
            nextEvent(); //1
        }
    }

    /**
     * Plays a single game.
     *
     * @throws InterruptedException
     */
    private void playGame() throws InterruptedException
    {
        while (!gameOver)
        {
            selectCard(0); // select the first card and flip it
            nextEvent(); //2
            selectCard(1); // select the second card and flip it
            if (!selected[0].getCard().equals(selected[1].getCard()))
            {
                misses++;
                autoFlipTimer.start();
                nextEvent(); //3 the source of this event is either the timer or a CardButton the user clicked on
                autoFlipTimer.stop();//does nothing if autoFlipTimer has expired
                selected[0].flip(); // flip the card so it is facing down
                selected[1].flip(); // flip the card so it is facing down
            } else if (game.isEndOfGame())
            {
                gameOver = true;
            } else
            {
                nextEvent(); //4
            }
        }
    }

    private void selectCard(int i) throws InterruptedException
    {
        CardButton cb;
        while (event.getSource() == autoFlipTimer
                || (cb = (CardButton) event.getSource()).isFaceUp())
        {
            nextEvent();//5 
        }
        cb.flip();
        selected[i] = cb;
    }

    /**
     * Asks the user if he/she wishes to continue playing. Must be called from
     * the invoker / EDT.
     *
     * @return
     */
    private boolean continuePlaying()
    {
        return JOptionPane.showConfirmDialog(
                null,
                "You had " + misses + " misses" + ".\nPlay again?",
                "End of Game", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * This method waits for he EDT to make an ActionEvent available through a
     * call to {@link #actionPerformed(java.awt.event.ActionEvent) } (or
     * {@link #cancel()}, in which case a null event is produced), then returns
     * that event. The method is called on the event handler thread.
     *
     * This method returns null if the last event has been encountered (i.e.,
     * <code>null</code> serves as an EOF indicator.
     *
     * @throws InterruptedException if the event handler thread is interrupted.
     */
    private void nextEvent() throws InterruptedException
    {
        detach(); // wait for the EDT to produce an event and call 
        // actionPerformed() or to cancel
        if (isCancelled())
        {
            event = null; // serves as EOF event
        }
    }

    /**
     * This method is invoked from the EDT.
     *
     * @param event
     * @see ActionListener
     *
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        try
        {
            this.event = event;
            reattach();
            if (gameOver)
            {
                if (continuePlaying())
                {
                    resetGame();
                } else
                {
                    cancel();
                    System.exit(0);
                }
            }
        } catch (InterruptedException ex)
        {
        }
    }

    private void resetGame()
    {
        game.reset();
        gameOver = false;
        misses = 0;
    }
}
