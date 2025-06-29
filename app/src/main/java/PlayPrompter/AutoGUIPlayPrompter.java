package PlayPrompter;

import Euchre.*;
import GUI.GUI;
import Logging.*;

public class AutoGUIPlayPrompter extends MasterPrompter {
    
    /**
     * Create a new PlayPrompter instance.
     * @param logger pass in a logger is logging is desired, an antilogger is not.
     * @param gui pass in a GUI instance.
     * @param playSpeed in milliseconds how long of a pause between each step of play.
     */
    public AutoGUIPlayPrompter(MasterLogger logger, GUI gui, EuchreEngine engine, int playSpeed) {
        this.firstInit = true;
        this.playSpeed = playSpeed;
        this.logger = logger;
        this.gui = gui;
        this.engine = engine;
        
        this.gui.allFaceUp = true;
        this.init();
    }

    public void mainLoop() {

        while(this.engine.teamOneScore < 10 && this.engine.teamTwoScore < 10) {

            this.deal();
            this.bid();
            this.playTricks();
            
        }

        if (this.engine.teamOneScore >= 10) {
            System.out.println("Team one won! (Your team!)");
            gui.updateMainText("Team one wins the game!");
            this.sleep(5000);
        } else if (this.engine.teamTwoScore >= 10) {
            gui.updateMainText("Team two wins the game!");
            this.sleep(5000);
        }
    }

    @Override
    protected void deal() {

    }

    @Override
    protected void bid() {
        
    }

    @Override
    protected void playTricks() {

    }
}