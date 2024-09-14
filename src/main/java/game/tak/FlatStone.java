package game.tak;

public class FlatStone implements Piece {
    private final Player owner;

    public FlatStone(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public boolean isCapStone() {
        return false;
    }

    @Override
    public PieceType getType() {
        return PieceType.FLAT;
    }
}