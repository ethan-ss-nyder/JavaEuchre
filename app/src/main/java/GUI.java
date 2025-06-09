import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import Euchre.Card;
import Euchre.Deck;
import Euchre.EuchreEngine;

public class GUI {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String WINDOW_NAME = "JavaEuchre";

    private JFrame frame;
    private JMenuBar menuBar;
    private JLabel mainText;
    private String mainTextString;

    private JPanel playerZeroPanel;
    private JPanel playerOnePanel;
    private JPanel playerTwoPanel;
    private JPanel playerThreePanel;
    private JPanel[] playerPanels;
    public boolean allFaceUp;

    private JPanel centerPanel = new JPanel();
    private JPanel scorePanel = new JPanel();
    private JPanel trickPanel = new JPanel();

    private ScaledImagePanel cardBackSideImage;
    private ScaledImagePanel cardBackSideStackImage;

    // I can't put this on my GitHub, oh man.
    private Map<String, ScaledImagePanel> cardImageMap;
    private ScaledImagePanel nineOfHearts, tenOfHearts, jackOfHearts, queenOfHearts, kingOfHearts, aceOfHearts;
    private ScaledImagePanel nineOfDiamonds, tenOfDiamonds, jackOfDiamonds, queenOfDiamonds, kingOfDiamonds, aceOfDiamonds;
    private ScaledImagePanel nineOfClubs, tenOfClubs, jackOfClubs, queenOfClubs, kingOfClubs, aceOfClubs;
    private ScaledImagePanel nineOfSpades, tenOfSpades, jackOfSpades, queenOfSpades, kingOfSpades, aceOfSpades;

    private int windowWidth;
    private int windowHeight;
    private String windowName;

    private PlayPrompter prompter;
    private EuchreEngine engine;

    GUI(int windowWidth, int windowHeight, String windowName) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.windowName = windowName;
    }

    GUI() {
        this.windowWidth = WINDOW_WIDTH;
        this.windowHeight = WINDOW_HEIGHT;
        this.windowName = WINDOW_NAME;
    }

    public void init() {
        this.frame = new JFrame(this.windowName);

        this.frame.setSize(this.windowWidth, this.windowHeight);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLayout(new GridBagLayout());

        this.loadImages();
        this.initCardImageMap();
        this.buildMenuBar();
        this.buildHands();
        this.buildCenter();
        this.frame.setJMenuBar(menuBar);

        this.frame.setVisible(true);
    }

    /**
     * MUST BE SET. This is just a delayed constructor thing to solve circular dependencies.
     * 
     * @param prompter the PlayPrompter running the Euchre game.
     */
    public void setPrompter(PlayPrompter prompter) {
        this.prompter = prompter;
    }

    /**
     * MUST BE SET. This is just a delayed constructor thing to solve circular dependencies.
     * 
     * @param engine the EuchreEngine running the Euchre game.
     */
    public void setEngine(EuchreEngine engine) {
        this.engine = engine;
        this.displayScore(); // This depends on the engine being visible, so we'll put it here
        this.displayTrickInfo();
    }

    /**
     * Sets the text for the big bold text in the center of the play area.
     * 
     * @param newText new text to display as a String.
     */
    public void updateMainText(String newText) {
        this.mainTextString = newText;
        this.mainText.setText(this.mainTextString);
    }

    /**
     * Populates a players hand with blank cards.
     * 
     * @param player Numerical name of the player.
     * @param cardNumber How many cards to put in the players hand.
     */
    public void displayHandDown(int player, int cardNumber) {
        playerPanels[player].removeAll();
        for (int i = 0; i <= cardNumber; i++) {
            playerPanels[player].add(new ScaledImagePanel(this.cardBackSideImage.getImage()));
            playerPanels[player].add(Box.createHorizontalStrut(0));
            playerPanels[player].revalidate();
            playerPanels[player].repaint();
        }
    }

    public void displayHand(int player, Deck deck) {
        playerPanels[player].removeAll();
        for (Euchre.Card card : deck.getDeck()) {
            playerPanels[player].add(cardImageMap.get(card.name.toLowerCase().replaceAll("\\s+", "")));
            playerPanels[player].revalidate();
            playerPanels[player].repaint();
        }
    }

    /**
     * Updates the top right of the display with the trick/game score
     */
    public void displayScore() {
        this.frame.remove(this.scorePanel);

        this.scorePanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel teamOneScore = new JLabel("Team 1: " + this.engine.teamOneScore);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        this.scorePanel.add(teamOneScore, constraints);

        JLabel teamTwoScore = new JLabel("Team 2: " + this.engine.teamTwoScore);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        this.scorePanel.add(teamTwoScore, constraints);

        JLabel trickScore = new JLabel("Tricks: " + this.engine.trickScore[0] + "/" + this.engine.trickScore[1]);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.SOUTH;
        this.scorePanel.add(trickScore, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        this.frame.add(this.scorePanel, constraints);
    }

    /**
     * Updates the top left of the display with the current trump suit.
     */
    public void displayTrickInfo() {
        this.frame.remove(trickPanel);

        this.trickPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel trumpLabel;
        if (this.engine.trump == null) {
            trumpLabel = new JLabel("Trump Suit: N/A");
        } else {
            trumpLabel = new JLabel(this.engine.trump.toString());
        }
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        this.trickPanel.add(trumpLabel, constraints);

        JLabel dealerLabel;
        if (this.engine.dealer == -1) {
            dealerLabel = new JLabel("Dealer: N/A");
        } else {
            dealerLabel = new JLabel("Dealer: Player " + this.engine.dealer);
        }
        constraints.gridy = 1;
        this.trickPanel.add(dealerLabel, constraints);

        JLabel offenseLabel;
        if (this.engine.offense == -1) {
            offenseLabel = new JLabel("Called by: N/A");
        } else {
            offenseLabel = new JLabel("Called by: Team " + this.engine.offense);
        }
        constraints.gridy = 2;
        this.trickPanel.add(offenseLabel, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.frame.add(trickPanel, constraints);
    }

    private void buildMenuBar() {
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(_ -> {
            System.out.println("New game!");
            this.prompter.init();
        });
        fileMenu.add(newGame);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem seeHands = new JCheckBoxMenuItem("Show Other Hands");
        seeHands.addActionListener(_ -> {
            boolean selected = seeHands.isSelected();
            if (selected) {
                System.out.println("Cheater!");
                this.displayHand(1, engine.playerHands[1]);
                this.displayHand(2, engine.playerHands[2]);
                this.displayHand(3, engine.playerHands[3]);
                this.allFaceUp = true;
            } else {
                System.out.println("Hiding other players' hands.");
                this.displayHandDown(1, engine.playerHands[1].getDeck().length - 1);
                this.displayHandDown(2, engine.playerHands[2].getDeck().length - 1);
                this.displayHandDown(3, engine.playerHands[3].getDeck().length - 1);
                this.allFaceUp = false;
            }
        });
        viewMenu.add(seeHands);

        // Add menus to menu bar
        this.menuBar = new JMenuBar();
        this.menuBar.add(fileMenu);
        this.menuBar.add(viewMenu);
    }

    /**
     * Draws to this.frame the top, left, right, and bottom hand areas (one per player).
     */
    private void buildHands() {
        this.playerZeroPanel = new JPanel();
        this.playerOnePanel = new JPanel();
        this.playerTwoPanel = new JPanel();
        this.playerThreePanel = new JPanel();

        this.playerPanels = new JPanel[] {
            this.playerZeroPanel,
            this.playerOnePanel,
            this.playerTwoPanel,
            this.playerThreePanel
        };

        this.playerZeroPanel.setLayout(new BoxLayout(playerZeroPanel, BoxLayout.X_AXIS));
        this.playerOnePanel.setLayout(new BoxLayout(playerOnePanel, BoxLayout.Y_AXIS));
        this.playerTwoPanel.setLayout(new BoxLayout(playerTwoPanel, BoxLayout.X_AXIS));
        this.playerThreePanel.setLayout(new BoxLayout(playerThreePanel, BoxLayout.Y_AXIS));

        this.playerZeroPanel.setBorder(new LineBorder(Color.BLACK));
        this.playerOnePanel.setBorder(new LineBorder(Color.BLACK));
        this.playerTwoPanel.setBorder(new LineBorder(Color.BLACK));
        this.playerThreePanel.setBorder(new LineBorder(Color.BLACK));

        JLabel p0label = new JLabel("Me");
        p0label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p0label.setAlignmentY(Component.TOP_ALIGNMENT);
        this.playerZeroPanel.add(p0label);

        JLabel p1label = new JLabel("Player One");
        p1label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p1label.setAlignmentY(Component.TOP_ALIGNMENT);
        this.playerOnePanel.add(p1label);

        JLabel p2label = new JLabel("Player Two");
        p2label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p2label.setAlignmentY(Component.TOP_ALIGNMENT);
        this.playerTwoPanel.add(p2label);

        JLabel p3label = new JLabel("Player Three");
        p3label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p3label.setAlignmentY(Component.TOP_ALIGNMENT);
        this.playerThreePanel.add(p3label);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.weighty = 0.2;
        this.frame.add(playerZeroPanel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.weightx = 0.2;
        constraints.weighty = 1;
        this.frame.add(playerOnePanel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0.2;
        this.frame.add(playerTwoPanel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.2;
        constraints.weighty = 1;
        this.frame.add(playerThreePanel, constraints);
    }

    /**
     * Draws to this.frame the center area of the screen.
     */
    private void buildCenter() {
        // Create center panel JLabel and its GridBagConstraints
        this.centerPanel = new JPanel(new GridBagLayout());
        this.centerPanel.setBorder(new LineBorder(Color.BLACK));

        // Center text label
        GridBagConstraints mainTextConstraints = new GridBagConstraints();
        mainTextConstraints.gridx = 1;
        mainTextConstraints.gridy = 1;
        mainTextConstraints.weightx = 1;
        mainTextConstraints.weighty = 1;
        mainTextConstraints.insets = new Insets(10, 10, 10, 10);
        this.mainText = new JLabel("Select `New Game` to start a new game.");
        this.mainText.setFont(new Font("Serif", Font.BOLD, 20));
        this.centerPanel.add(this.mainText, mainTextConstraints);

        // Center card graphic
        GridBagConstraints centerCardGraphicConstraints = new GridBagConstraints();
        centerCardGraphicConstraints.gridx = 1;
        centerCardGraphicConstraints.gridy = 2;
        centerCardGraphicConstraints.fill = GridBagConstraints.BOTH;
        centerCardGraphicConstraints.insets = new Insets(-60, 0, 0, 0);
        centerCardGraphicConstraints.weightx = 0.5;
        centerCardGraphicConstraints.weighty = 0.5;
        this.centerPanel.add(this.cardBackSideStackImage, centerCardGraphicConstraints);

        // Placeholder bottom panel
        GridBagConstraints centerBottomPanelConstraints = new GridBagConstraints();
        JPanel centerBottomPanel = new JPanel();
        centerBottomPanelConstraints.gridx = 1;
        centerBottomPanelConstraints.gridy = 3;
        centerBottomPanelConstraints.weightx = 0.5;
        centerBottomPanelConstraints.weighty = 0.5;
        centerBottomPanelConstraints.fill = GridBagConstraints.BOTH;
        this.centerPanel.add(centerBottomPanel, centerBottomPanelConstraints);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        this.frame.add(centerPanel, constraints);
    }

    /**
     * Draws to the center of the screen. Used to redraw center throughout game.
     * 
     * @param text text to be displayed in the center.
     * @param card card to be displayed face-up in the center.
     */
    public void buildCenter(String text, Card card, boolean faceDown) {
        this.frame.remove(this.centerPanel);

        // Create center panel JLabel and its GridBagConstraints
        this.centerPanel = new JPanel(new GridBagLayout());
        this.centerPanel.setBorder(new LineBorder(Color.BLACK));

        // Center text label
        GridBagConstraints mainTextConstraints = new GridBagConstraints();
        mainTextConstraints.gridx = 1;
        mainTextConstraints.gridy = 1;
        mainTextConstraints.weightx = 1;
        mainTextConstraints.weighty = 1;
        mainTextConstraints.insets = new Insets(10, 10, 10, 10);
        this.mainText = new JLabel(text);
        this.mainText.setFont(new Font("Serif", Font.BOLD, 20));
        this.centerPanel.add(this.mainText, mainTextConstraints);

        // Center card graphic
        GridBagConstraints centerCardGraphicConstraints = new GridBagConstraints();
        centerCardGraphicConstraints.gridx = 1;
        centerCardGraphicConstraints.gridy = 2;
        centerCardGraphicConstraints.fill = GridBagConstraints.BOTH;
        centerCardGraphicConstraints.insets = new Insets(-80, 0, 0, 0);
        centerCardGraphicConstraints.weightx = 0.5;
        centerCardGraphicConstraints.weighty = 0.5;
        if (faceDown) {
            this.centerPanel.add(this.cardBackSideStackImage, centerCardGraphicConstraints);
        } else {
            Image tempImage = cardImageMap.get(card.name.toLowerCase().replaceAll("\\s+", "")).getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
            JLabel tempLabel = new JLabel(new ImageIcon(tempImage));
            this.centerPanel.add(tempLabel, centerCardGraphicConstraints);
        }

        // Placeholder bottom panel
        GridBagConstraints centerBottomPanelConstraints = new GridBagConstraints();
        JPanel centerBottomPanel = new JPanel();
        centerBottomPanelConstraints.gridx = 1;
        centerBottomPanelConstraints.gridy = 3;
        centerBottomPanelConstraints.weightx = 0.5;
        centerBottomPanelConstraints.weighty = 0.5;
        centerBottomPanelConstraints.fill = GridBagConstraints.BOTH;
        this.centerPanel.add(centerBottomPanel, centerBottomPanelConstraints);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        this.frame.add(centerPanel, constraints);
        this.frame.validate();
        this.frame.repaint();
    }

    public void displayLeader() {
        
    }

    public void displayPlayedCards() {
        this.frame.remove(this.centerPanel);

        // Create center panel JLabel and its GridBagConstraints
        this.centerPanel = new JPanel(new GridBagLayout());
        this.centerPanel.setBorder(new LineBorder(Color.BLACK));

        // Center text label
        GridBagConstraints mainTextConstraints = new GridBagConstraints();
        mainTextConstraints.gridx = 1;
        mainTextConstraints.gridy = 1;
        mainTextConstraints.weightx = 1;
        mainTextConstraints.weighty = 1;
        mainTextConstraints.insets = new Insets(10, 10, 10, 10);
        this.mainText = new JLabel(this.mainTextString);
        this.mainText.setFont(new Font("Serif", Font.BOLD, 20));
        this.centerPanel.add(this.mainText, mainTextConstraints);

        // Center face-down card graphic
        GridBagConstraints centerCardGraphicConstraints = new GridBagConstraints();
        centerCardGraphicConstraints.gridx = 1;
        centerCardGraphicConstraints.gridy = 2;
        centerCardGraphicConstraints.fill = GridBagConstraints.BOTH;
        centerCardGraphicConstraints.insets = new Insets(-20, 0, 0, 0);
        centerCardGraphicConstraints.weightx = 0.5;
        centerCardGraphicConstraints.weighty = 0.5;
        this.centerPanel.add(this.cardBackSideStackImage, centerCardGraphicConstraints);

        int[][] gridPositions = {
            {1, 3}, // Player 0, bottom
            {3, 2}, // Player 1, right
            {1, 0}, // Player 2, top
            {0, 2}, // Player 3, left
        };

        int leader = this.engine.leader;
        int cardsPlayed = this.engine.trickPlayedCards.getDeck().length;

        // Populate center with played cards
        for (int i = 0; i < cardsPlayed; i++) {
            int player = (leader + i)%4;
            int[] pos = gridPositions[player];

            GridBagConstraints tempConstraints = new GridBagConstraints();
            tempConstraints.gridx = pos[0];
            tempConstraints.gridy = pos[1];
            tempConstraints.weightx = 0.5;
            tempConstraints.weighty = 0.5;
            tempConstraints.insets = new Insets(10, 10, 10, 10);
            tempConstraints.fill = GridBagConstraints.BOTH;

            this.centerPanel.add(new JLabel(new ImageIcon(cardImageMap.get(this.engine.trickPlayedCards.getDeck()[i].name.toLowerCase().replaceAll("\\s+", "")).getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH))), tempConstraints);
        }

        // Fill all empty spots with placeholder panels
        for (int i = cardsPlayed; i < 4; i++) {
            int[] pos = gridPositions[i];

            GridBagConstraints tempConstraints = new GridBagConstraints();
            tempConstraints.gridx = pos[0];
            tempConstraints.gridy = pos[1];
            tempConstraints.weightx = 0.5;
            tempConstraints.weighty = 0.5;
            tempConstraints.insets = new Insets(10, 10, 10, 10);
            tempConstraints.fill = GridBagConstraints.BOTH;

            this.centerPanel.add(new JPanel(), tempConstraints);
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        this.frame.add(centerPanel, constraints);
        this.frame.validate();
        this.frame.repaint();
    }

    public void buildShuffleButtons() {

        AtomicBoolean shuffled = new AtomicBoolean(false);

        // Create a modal dialog that blocks until "Done" is clicked
        JDialog dialog = new JDialog(this.frame, "Shuffle Deck", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JPanel shufflePanel = new JPanel();
        JButton shuffleButton = new JButton("Shuffle");
        JButton doneButton = new JButton("Done");

        shuffleButton.addActionListener(_ -> {
            this.engine.deck.shuffle();
            shuffled.set(true);
            System.out.println("Shuffle.");
        });

        doneButton.addActionListener(_ -> {
            if (!shuffled.get()) {
                System.out.println("Shuffling deck anyways. Cheater.");
                this.engine.deck.shuffle();
            }
            dialog.dispose(); // This unblocks dialog.setVisible(true)
        });

        shufflePanel.add(shuffleButton);
        shufflePanel.add(doneButton);

        // Add your button panel to the dialog
        dialog.add(shufflePanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame); // Center on screen or parent
        dialog.setVisible(true);

        // After dialog is closed, resume here
        this.prompter.mainLoop();
    }

    public void buildShuffleButtonsNoLoop() {

        AtomicBoolean shuffled = new AtomicBoolean(false);

        // Create a modal dialog that blocks until "Done" is clicked
        JDialog dialog = new JDialog(this.frame, "Shuffle Deck", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JPanel shufflePanel = new JPanel();
        JButton shuffleButton = new JButton("Shuffle");
        JButton doneButton = new JButton("Done");

        shuffleButton.addActionListener(_ -> {
            this.engine.deck.shuffle();
            shuffled.set(true);
            System.out.println("Shuffle.");
        });

        doneButton.addActionListener(_ -> {
            if (!shuffled.get()) {
                System.out.println("Shuffling deck anyways. Cheater.");
                this.engine.deck.shuffle();
            }
            dialog.dispose(); // This unblocks dialog.setVisible(true)
        });

        shufflePanel.add(shuffleButton);
        shufflePanel.add(doneButton);

        // Add your button panel to the dialog
        dialog.add(shufflePanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame); // Center on screen or parent
        dialog.setVisible(true);
    }

    /**
     * Prompts the user to pass or pick up a card.
     * @param updateText Unfortunately, it is necessary to update the main central text with this function.
     */
    public void buildBiddingButtons(String updateText) {

        JDialog dialog = new JDialog(this.frame, updateText, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new FlowLayout());

        JPanel biddingPanel = new JPanel();
        JButton passButton = new JButton("Pass");
        JButton pickItUpButton = new JButton("Pick it up");

        passButton.addActionListener(_ -> {
            this.prompter.trumpCalled = false;
            dialog.dispose();
        });

        pickItUpButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            dialog.dispose();
        });

        biddingPanel.add(passButton);
        biddingPanel.add(pickItUpButton);

        dialog.add(biddingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }

    /**
     * Prompts the user to pass or pick up a card.
     * @param updateText Unfortunately, it is necessary to update the main central text with this function.
     */
    public void buildFreeBidButtons(String updateText) {

        JDialog dialog = new JDialog(this.frame, updateText, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new FlowLayout());

        JPanel biddingPanel = new JPanel();
        JButton passButton = new JButton("Pass");
        JButton spadeButton = new JButton("Spades");
        JButton clubButton = new JButton("Clubs");
        JButton diamondButton = new JButton("Diamonds");
        JButton heartButton = new JButton("Hearts");

        passButton.addActionListener(_ -> {
            this.prompter.trumpCalled = false;
            dialog.dispose();
        });

        spadeButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.SPADES);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        clubButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.CLUBS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        diamondButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.DIAMONDS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        heartButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.HEARTS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        biddingPanel.add(passButton);
        biddingPanel.add(spadeButton);
        biddingPanel.add(clubButton);
        biddingPanel.add(diamondButton);
        biddingPanel.add(heartButton);

        dialog.add(biddingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }

    /**
     * Prompts the user to pass or pick up a card.
     * @param updateText Unfortunately, it is necessary to update the main central text with this function.
     */
    public void buildFreeBidButtonsNoPass(String updateText) {

        JDialog dialog = new JDialog(this.frame, updateText, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new FlowLayout());

        JPanel biddingPanel = new JPanel();
        JButton spadeButton = new JButton("Spades");
        JButton clubButton = new JButton("Clubs");
        JButton diamondButton = new JButton("Diamonds");
        JButton heartButton = new JButton("Hearts");

        spadeButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.SPADES);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        clubButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.CLUBS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        diamondButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.DIAMONDS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        heartButton.addActionListener(_ -> {
            this.prompter.trumpCalled = true;
            this.engine.setTrump(Card.Suit.HEARTS);
            this.engine.setOffense(0);
            dialog.dispose();
        });

        biddingPanel.add(spadeButton);
        biddingPanel.add(clubButton);
        biddingPanel.add(diamondButton);
        biddingPanel.add(heartButton);

        dialog.add(biddingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }

    public void buildBidCardSwapButtons(String text) {

        JDialog dialog = new JDialog(this.frame, text, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new FlowLayout());

        JPanel biddingPanel = new JPanel();
        for (Card card : this.engine.playerHands[0].getDeck()) {
            JButton temp = new JButton(card.name);
            temp.addActionListener(_ -> {
                this.engine.bidCardSwap = card;
                dialog.dispose();
            });
            biddingPanel.add(temp);
        }

        dialog.add(biddingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }

    public void buildPlayCardButtons(String updateText) {

        AtomicInteger integer = new AtomicInteger();

        JDialog dialog = new JDialog(this.frame, updateText, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new FlowLayout());

        JPanel biddingPanel = new JPanel();
        for (Card card : this.engine.playerHands[0].getDeck()) {
            JButton temp = new JButton(card.name);
            int index = integer.getAndIncrement();
            temp.addActionListener(_ -> {
                this.engine.playCard(0, index);
                dialog.dispose();
            });
            biddingPanel.add(temp);
        }

        dialog.add(biddingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }

    /**
     * Lord forgive me for my sins. Allow me into your kingdom when it is my time.
     */
    private void loadImages() {
        this.cardBackSideImage = new ScaledImagePanel(new ImageIcon(getClass().getResource("cardbackside.png")).getImage());
        this.cardBackSideStackImage = new ScaledImagePanel(new ImageIcon(getClass().getResource("cardbacksidestack.png")).getImage());

        this.nineOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/nineofhearts.png")).getImage());
        this.tenOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/tenofhearts.png")).getImage());
        this.jackOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/jackofhearts.png")).getImage());
        this.queenOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/queenofhearts.png")).getImage());
        this.kingOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/kingofhearts.png")).getImage());
        this.aceOfHearts = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/aceofhearts.png")).getImage());

        this.nineOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/nineofdiamonds.png")).getImage());
        this.tenOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/tenofdiamonds.png")).getImage());
        this.jackOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/jackofdiamonds.png")).getImage());
        this.queenOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/queenofdiamonds.png")).getImage());
        this.kingOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/kingofdiamonds.png")).getImage());
        this.aceOfDiamonds = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/aceofdiamonds.png")).getImage());

        this.nineOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/nineofclubs.png")).getImage());
        this.tenOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/tenofclubs.png")).getImage());
        this.jackOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/jackofclubs.png")).getImage());
        this.queenOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/queenofclubs.png")).getImage());
        this.kingOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/kingofclubs.png")).getImage());
        this.aceOfClubs = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/aceofclubs.png")).getImage());

        this.nineOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/nineofspades.png")).getImage());
        this.tenOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/tenofspades.png")).getImage());
        this.jackOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/jackofspades.png")).getImage());
        this.queenOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/queenofspades.png")).getImage());
        this.kingOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/kingofspades.png")).getImage());
        this.aceOfSpades = new ScaledImagePanel(new ImageIcon(getClass().getResource("LightCards/aceofspades.png")).getImage());
    }

    private void initCardImageMap() {
        cardImageMap = new HashMap<>();

        cardImageMap.put("nineofhearts", nineOfHearts);
        cardImageMap.put("tenofhearts", tenOfHearts);
        cardImageMap.put("jackofhearts", jackOfHearts);
        cardImageMap.put("queenofhearts", queenOfHearts);
        cardImageMap.put("kingofhearts", kingOfHearts);
        cardImageMap.put("aceofhearts", aceOfHearts);

        cardImageMap.put("nineofclubs", nineOfClubs);
        cardImageMap.put("tenofclubs", tenOfClubs);
        cardImageMap.put("jackofclubs", jackOfClubs);
        cardImageMap.put("queenofclubs", queenOfClubs);
        cardImageMap.put("kingofclubs", kingOfClubs);
        cardImageMap.put("aceofclubs", aceOfClubs);

        cardImageMap.put("nineofdiamonds", nineOfDiamonds);
        cardImageMap.put("tenofdiamonds", tenOfDiamonds);
        cardImageMap.put("jackofdiamonds", jackOfDiamonds);
        cardImageMap.put("queenofdiamonds", queenOfDiamonds);
        cardImageMap.put("kingofdiamonds", kingOfDiamonds);
        cardImageMap.put("aceofdiamonds", aceOfDiamonds);

        cardImageMap.put("nineofspades", nineOfSpades);
        cardImageMap.put("tenofspades", tenOfSpades);
        cardImageMap.put("jackofspades", jackOfSpades);
        cardImageMap.put("queenofspades", queenOfSpades);
        cardImageMap.put("kingofspades", kingOfSpades);
        cardImageMap.put("aceofspades", aceOfSpades);
    }
}

class ScaledImagePanel extends JPanel {
    private Image image;

    public ScaledImagePanel(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int imgWidth = image.getWidth(this);
        int imgHeight = image.getHeight(this);

        double imgAspect = (double) imgWidth / imgHeight;
        double panelAspect = (double) getWidth() / getHeight();

        int drawWidth, drawHeight;

        if (panelAspect > imgAspect) {
            drawHeight = getHeight();
            drawWidth = (int) (drawHeight * imgAspect);
        } else {
            drawWidth = getWidth();
            drawHeight = (int) (drawWidth / imgAspect);
        }

        int x = (getWidth() - drawWidth) / 2;
        int y = (getHeight() - drawHeight) / 2;

        g.drawImage(image, x, y, drawWidth, drawHeight, this);
    }
}