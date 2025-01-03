package agents.neural_network;

import game.mills.Player;
import game.mills.Node;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import game.mills.Game;
import game.mills.InvalidMove;

public class BaselineAgent implements Player {
    private String name;
    private Color color;
    private int stonesToPlace;
    private int stonesOnBoard;
    private Game game;

    /**
     * Constructor for the BaselineAgent class.
     * 
     * @param name The name of the player.
     * @param color The color of the player's pieces.
     */
    public BaselineAgent(String name, Color color) {
        this.name = name;
        this.color = color;
        this.stonesToPlace = 9;
        this.stonesOnBoard = 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public int getStonesToPlace() {
        return stonesToPlace;
    }

    @Override
    public int getStonesOnBoard() {
        return stonesOnBoard;
    }

    @Override
    public void decrementStonesToPlace() {
        if (stonesToPlace > 0) {
            stonesToPlace--;
            stonesOnBoard++;
        }
    }

    @Override
    public void incrementStonesOnBoard() {
        stonesOnBoard++;
    }

    @Override
    public void decrementStonesOnBoard() {
        stonesOnBoard--;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void makeMove() {
        if (stonesToPlace > 0) {
            placePiece();
        } else {
            movePiece();
        }
    }

    private void placePiece() {
        List<Integer> nodeIndices = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            nodeIndices.add(i);
        }
        Collections.shuffle(nodeIndices); // Shuffle the node indices to randomize the placement

        for (int i : nodeIndices) {
            Node node = game.getBoard().getNode(i);
            if (!node.isOccupied()) {
                try {
                    game.placePiece(i);
                    System.out.println("Bot placed piece at node " + i);
                    if (game.isMillFormed()) {
                        removeOpponentPiece();
                    }
                    break;
                } catch (InvalidMove e) {
                    // Move to the next node if the move is invalid
                    System.out.println("Invalid move at node " + i + ", trying next node.");
                    continue;
                }
            }
        }
    }

    private void movePiece() {
        int random = (int) (Math.random() * 24);
        for (int i = random; i < 24; i++) {
            Node fromNode = game.getBoard().getNode(i);
            if (fromNode.isOccupied() && fromNode.getOccupant() == this) {
                for (Node toNode : game.getBoard().getNeighbours(fromNode)) {
                    if (!toNode.isOccupied()) {
                        try {
                            game.makeMove(fromNode.getId(), toNode.getId());
                            System.out.println("Bot moved piece from node " + fromNode.getId() + " to node " + toNode.getId());
                            if (game.isMillFormed()) {
                                removeOpponentPiece();
                            }
                            return;
                        } catch (InvalidMove e) {
                            // Move to the next node if the move is invalid
                            System.out.println("Invalid move from node " + fromNode.getId() + " to node " + toNode.getId() + ", trying next node.");
                        }
                    }
                }
            }
        }
    }

    private void removeOpponentPiece() {
        for (int i = 0; i < 24; i++) {
            Node node = game.getBoard().getNode(i);
            if (node.isOccupied() && node.getOccupant().getColor() == Color.BLACK && !game.getBoard().isPartOfMill(node)) {
                try {
                    game.removePiece(i);
                    System.out.println("Bot removed opponent's piece at node " + i);
                    break;
                } catch (InvalidMove e) {
                    // Move to the next node if the removal is invalid
                    System.out.println("Invalid removal at node " + i + ", trying next node.");
                    continue;
                }
            }
        }
    }


}
