JAVAEUCHRE CODE FLOW:

Main calls an instance of MasterPrompter.

MasterPrompter is in charge of game flow and timing. MasterPrompter instances call functions and trade information with EuchreEngines and GUIs in order to offload Euchre game logic and display information.

EuchreEngine tracks the deck, player hands, and scores. It can "play" cards by removing a card from a player's hand, it can swap a certain player card with the bidding card during bidding, and can give Euchre logistical game info, such as whether or not a player can follow suit, which card wins a trick, and can rank cards for an easy way to score tricks.

GUI ALSO interacts with EuchreEngine in order to get player hand information in order to display those cards, and it also gets all the top-row game/trick information from EuchreEngine. MasterPrompters and GUIs are very good friends, because MasterPrompter updates GUI with every step of the game, and GUI gives MasterPrompters a lot of information about player input made through GUI elements.

CoinTosser is the dumbest possible "AI" to play against. It plays random legal moves.

Learner is a work in progress which will be capable of machine learning to get better at Euchre.

----------------------------
Main show:
MasterPrompter <> GUI
    ^EuchreEngine^

Honorable mentions:
CoinTosser, Leaners, Loggers

Fundamental logic:
Card, Deck
----------------------------


ABSTRACTION:

Loggers are abstracted into MasterLogger. This is really just a toggle for logging. You can use Logger or AntiLogger depending on whether you want to log a game or not. AntiLogger doesn't "antilog", it just doesn't log games. The idea is that instead of having manual/auto logged/unlogged PlayPrompters written, all PlayPrompters are written with logging capabilities, but AntiLogger is just empty, so the calls don't break the code, they just don't do anything.

PlayPrompters are abstracted into MasterPrompter. This has more built-in functionality, but the difference between PlayPrompter and AutoPlayPrompter is human interaction. AutoPlayPrompters are for training bots, either through brute-force logging CoinTosser games or by more carefully logging machine learning games.

