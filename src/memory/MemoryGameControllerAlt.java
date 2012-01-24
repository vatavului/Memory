package memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Non-FSM based alternative to MemoryGameController. 
 * <p>
 * The {@link #actionPerformed(java.awt.event.ActionEvent) } method is called 
 * from the Event Dispatch Thread whenever the user clicks on a CardButton or
 * when the <tt>autoFlipTimer</tt> fires. All other methods are called on 
 * another thread, which consumes the events fired from the EDT.
 * 
 * @author Erik
 */
class MemoryGameControllerAlt extends Thread implements ActionListener {

    private BlockingQueue<ActionEvent> eventQueue = new ArrayBlockingQueue<ActionEvent>(1);
    private final MemoryGame game;
    private int misses = 0;

    /**
     * Constructor
     * 
     * @param game the MemoryGame instance that serves as model and view for 
     * this controller. 
     */
    MemoryGameControllerAlt(MemoryGame game) {
        this.game = game;
    }

    
    public void actionPerformed(ActionEvent e) {
        try {
            eventQueue.put(e);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Determines the time that the user may see two open non-matching cards
     * before they are flipped back to face-down position.
     */
    private Timer autoFlipTimer = new Timer(1000, this);

    @Override
    public void run() {
        try {
            playGame();
            while (continuePlaying()) {
                game.reset();
                playGame();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Plays on round of the game until the user has found all matching pairs.
     * @throws InterruptedException 
     */
    private void playGame() throws InterruptedException {
        CardButton cardButton1;
        CardButton cardButton2;
        misses = 0;
        while (!game.isEndOfGame()) {
            cardButton1 = openNextCard();
            cardButton2 = openNextCard();
            assert !cardButton1.equals(cardButton2);
            assert cardButton1.isFaceUp() && cardButton2.isFaceUp();
            if (!cardButton1.getCard().equals(cardButton2.getCard())) {
                misses++;
                waitASecond();
                cardButton1.flip();
                cardButton2.flip();
                game.repaint();
            }
        }
    }

    /**
     * Ignores all events until a button click on one of the CardButton's that
     * is facing down is clicked. 
     * @return the CardButton that was the source of the button click.
     * @throws InterruptedException 
     */
    private CardButton openNextCard() throws InterruptedException {
        CardButton cb;
        ActionEvent event = eventQueue.take();
        assert event != null;
        while (!(event.getSource() instanceof CardButton)
                || (cb = (CardButton) event.getSource()).isFaceUp()) {
            event = eventQueue.take();
        }
        cb.flip();
        game.repaint();
        assert event.getSource() instanceof CardButton;
        assert ((CardButton) event.getSource()).isFaceUp();
        return cb;
    }

    /**
     * Starts the autoFlipTimer and waits till it times out. Consumes all 
     * other intermediary ActionEvents. 
     * 
     * @throws InterruptedException 
     */
    private void waitASecond() throws InterruptedException {
        autoFlipTimer.start();
        ActionEvent event = eventQueue.take();
        while (event.getSource() != autoFlipTimer) {
            event = eventQueue.take();
        }
        autoFlipTimer.stop();
        assert event.getSource() == autoFlipTimer;
    }

    /**
     * Asks if the user wants to play again.
     * 
     * @return true is the user answers yes.
     */
    private boolean continuePlaying() {
        return JOptionPane.showConfirmDialog(
                null,
                "You had " + misses + (misses == 1 ? " miss" : " misses") + ".\nPlay again?",
                "End of Game", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}