package PlayPrompter;

import Euchre.EuchreEngine;
import Logging.MasterLogger;

public class AutoPlayPrompter extends MasterPrompter{

    /**
     * Create a new PlayPrompter instance.
     * @param logger pass in a logger is logging is desired, an antilogger is not.
     * @param gui pass in a GUI instance.
     * @param playSpeed in milliseconds how long of a pause between each step of play.
     */
    public AutoPlayPrompter(MasterLogger logger, EuchreEngine engine) {
        this.firstInit = true;
        this.logger = logger;
        this.engine = engine;
        
        this.init();
    }

    @Override
    public void mainLoop() {
        
    }
    
}
