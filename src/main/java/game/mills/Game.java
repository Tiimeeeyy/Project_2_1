package game.mills;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import MinMax.AiPlayer;
import gui.MillGameUI;

/**
 * The NewGame class manages the game logic for Mills.
 * It controls the game phases, player turns, moves, and interactions with the game board.
 */
public class Game {
    private static Logger logger = Logger.getLogger(Game.class.getName());
    private Player humanPlayer1;
    private Player humanPlayer2;
    private Player ai;
    private AiPlayer aiPlayer;
    private Player currentPlayer;
    private Board board;
    private MoveValidator moveValidator;
    @SuppressWarnings("unused")
    private int totalMoves;
    private int phase;
    private boolean millFormed = false;
    private MillGameUI ui;
    public boolean isGameOver = false;

    /**
     * Constructs a new game instance with two players.
     * Initializes the board and sets up the game for the placing phase.
     *
     * @param p1 the first player.
     * @param p2 the second player.
     */
    public Game(Player p1, Player p2) {
        this.humanPlayer1 = p1;
        //this.humanPlayer2 = p2;
        this.ai =p2;
        this.currentPlayer = p1;
        this.board = new Board();
        this.moveValidator = new MoveValidator(board);
        this.phase = 1; //Start the game in the placing phase

        this.totalMoves = 0;
    }

    /**
     * Switches the player when the turn changes.
     */
    public void switchPlayer() {
        if (currentPlayer == humanPlayer1) {
            currentPlayer = aiPlayer;
            aiTurn(); // Let the AI take its turn automatically
        } else {
            currentPlayer = humanPlayer1;
        }
    }


    /**
     * Places a piece on the board at the specified node ID.
     * Validates the move and checks if a mill has been formed.
     *
     * @param nodeID the node ID where the piece is to be placed.
     * @throws InvalidMove if the move is not valid.
     */
    public void placePiece(int nodeID) {
        if (moveValidator.isValidPlacement(currentPlayer, nodeID)) {
            board.placePiece(currentPlayer, nodeID);
            if (board.checkMill(board.getNode(nodeID), currentPlayer)) {
                logger.log(Level.ALL, "HumanPlayer {0} made a mill!", currentPlayer.getName());
                millFormed = true;
            } else {
                switchPlayer();
            }
            totalMoves++;
            checkPhase();
        } else {
            throw new InvalidMove("Placement is invalid!");
        }
    }

    /**
     * Updates the current game phase
     */
    public void updatePhase() {
        phase++;
    }

    /**
     * Executes a move from one node to another.
     * Validates the move and checks if a mill is formed after the move.
     *
     * @param fromID the node ID where the piece is moved from.
     * @param toID   the node ID where the piece is moved to.
     * @throws InvalidMove if the move is not valid or flying is not allowed.
     */
    public void makeMove(int fromID, int toID) {
        if (moveValidator.isValidMove(currentPlayer, fromID, toID)) {
            board.movePiece(currentPlayer, fromID, toID);
            if (board.checkMill(board.getNode(toID), currentPlayer)) {
                logger.log(Level.ALL, "HumanPlayer {0} made a mill!", currentPlayer.getName());
                millFormed = true;
            } else {
                switchPlayer();
            }
            checkGameOver();
        } else {
            throw new InvalidMove("Move is invalid, or flying is not allowed!");
        }
    }

    /**
     * Removes an opponent's stone from the board, if permitted.
     *
     * @param nodeID the node ID where the opponent's stone is located.
     * @throws InvalidMove if the removal is not allowed.
     */
    public void removeOpponentStone(int nodeID) {
        Node node = board.getNode(nodeID);
        Player opponent = node.getOccupant();

        if (node.isOccupied() && opponent != currentPlayer) {
            if (board.checkMill(node, opponent)) {
                boolean canRemoveMillStone = board.allOpponentStonesInMill(opponent);
                if (canRemoveMillStone) {
                    // If all stones are in mills, allow removal
                    removeStone(node, opponent);
                } else {
                    // If not all stones are in mills, disallow removal
                    throw new InvalidMove("Cannot remove a stone from a mill while other stones are available.");
                }
            } else {
                // If stone is not part of a mill, allow removal
                removeStone(node, opponent);
            }

            // Checking phase and game over after removing a stone
            if (phase >= 2) { // Only checking game over when phase 2 has started
                checkGameOver();
            }

            // Switching the turn after removing the stone
            switchPlayer();
            if (ui != null) {
                ui.updateGameStatus("Turn: " + currentPlayer.getName());
            }
        } else {
            throw new InvalidMove("Cannot remove this stone.");
        }
    }

    /**
     * Helper method to remove a stone from the board and update the game state.
     *
     * @param node     the node from which the stone is to be removed.
     * @param opponent the player whose stone is being removed.
     */
    private void removeStone(Node node, Player opponent) {
        node.setOccupant(null);
        opponent.decrementStonesOnBoard();
        millFormed = false; // Reset flag after removal
        logger.log(Level.ALL, "HumanPlayer {0}'s stone at node {1} has been removed.", new Object[]{opponent.getName(), node.getId()});
        checkGameOver();
    }

    /**
     * Checks if the game is currently in the placing phase.
     *
     * @return true if the game is in the placing phase, false otherwise.
     */
    public boolean isPlacingPhase() {
        return phase == 1;
    }

    /**
     * Gets the current player whose turn it is.
     *
     * @return the current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the game board.
     *
     * @return the board object representing the game board.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Checks if the current player is allowed to "fly" (move a piece to any open space on the board).
     * A player can fly if they have exactly 3 stones on the board.
     *
     * @param currentPlayer the player whose ability to fly is being checked.
     * @return true if the player can fly, false otherwise.
     */
    public boolean canFly(Player currentPlayer) {
        logger.log(Level.INFO, "HumanPlayer {0} has {1} stones on board.", new Object[]{currentPlayer.getName(), currentPlayer.getStonesOnBoard()});
        boolean canFly = moveValidator.canFly(currentPlayer);  // HumanPlayer can fly if they have exactly 3 stones
        logger.log(Level.INFO, "HumanPlayer {0} canFly: {1}", new Object[]{currentPlayer.getName(), canFly});
        return canFly;
    }

    /**
     * Checks if the game should move to the next phase (from placing to moving).
     */
    private void checkPhase() {
        if (humanPlayer1.getStonesToPlace() == 0 && ai.getStonesToPlace() == 0) {
            phase = 2;
        }
    }

    /**
     * Gets the current game phase.
     *
     * @return the current phase of the game.
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Checks if a mill has been formed by the current player.
     *
     * @return true if a mill has been formed, false otherwise.
     */
    public boolean isMillFormed() {
        return millFormed;
    }

    /**
     * Sets the mill formed status.
     *
     * @param millFormed true if a mill has been formed, false otherwise.
     */
    public void setMillFormed(boolean millFormed) {
        this.millFormed = millFormed;
    }

    /**
     * Checks if the game is over and determines the winner if applicable.
     */
    private void checkGameOver() {
        if (isGameOver) {
            return; // Do not proceed if the game is already over
        }
        if (phase >= 2) {
            // Check if any player has 2 or fewer stones
            if (humanPlayer1.getStonesOnBoard() <= 2) {
                gameOver(ai);
                return;
            } else if (ai.getStonesOnBoard() <= 2) {
                gameOver(humanPlayer1);
                return;
            }
        }
        // Check if any player has no valid moves left
        boolean p1HasMoves = board.hasValidMoves(humanPlayer1);
        boolean p2HasMoves = board.hasValidMoves(ai);

        if (!p1HasMoves && !p2HasMoves) {
            // If neither player has valid moves, it's a draw
            gameOver(null);
        } else if (!p1HasMoves) {
            gameOver(ai);
        } else if (!p2HasMoves) {
            gameOver(humanPlayer1);
        }
    }

    /**
     * Ends the game and declares the winner.
     *
     * @param winner the player who won the game.
     */
    private void gameOver(Player winner) {
        if (!isGameOver) {
            isGameOver = true;
            logger.log(Level.INFO, winner != null ? "Game Over! {0} wins!" : "Game Over! It's a draw!", new Object[]{winner != null ? winner.getName() : ""});
            if (ui != null) {
                ui.displayGameOverMessage(winner);  // Display the game-over message
            }
        }
    }

    /**
     * Sets the UI reference for the game to interact with.
     *
     * @param ui the MillGameUI object to be used for updating the game status and UI.
     */
    public void setUI(MillGameUI ui) {
        this.ui = ui;
    }

    public void undoMove(int fromID, int toID) {
        // 1. Reverse the move: Move the piece back to the previous position
        board.movePiece(currentPlayer, toID, fromID);

        // 2. Revert any changes in the player's stone count if necessary
        if (currentPlayer == ai) {
            ai.incrementStonesOnBoard();
            ai.decrementStonesToPlace();
        } else {
            humanPlayer1.incrementStonesOnBoard();
            humanPlayer1.decrementStonesToPlace();
        }

        // 3. Revert the player turn
        switchPlayer();

        // 4. Reset the mill status if a mill was formed and it’s being undone
        millFormed = false;

        // 5. Recheck the game state (e.g., for mills, phase, game over)
        checkGameOver();
    }

    public void aiTurn() {
        // Only let the AI play if it is its turn and the game is not over
        if (currentPlayer == ai && !isGameOver) {
            // Get the best move from the AI using the minimax algorithm
            int[] bestMove = getBestMove(ai, aiPlayer);

            // Execute the best move
            makeMove(bestMove[0], bestMove[1]);
        }
    }

    private int[] getBestMove(Player ai, AiPlayer aiPlayer) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        List<int[]> legalMoves = new ArrayList<>(board.getLegalMoves(ai));

        for (int[] move : legalMoves) {
            int fromID = move[0];
            int toID = move[1];

            makeMove(fromID, toID);
            int score = aiPlayer.minimax(this, 3, true);
            undoMove(fromID, toID);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }


}
