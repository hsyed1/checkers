package referee;

/**
 * <p>Title: PlayerInterface</p>
 * <p>Description: Checkers playing agents must implement this interface.</p>
 * <p>Copyright: Matt Evett (c) 2004</p>
 * <p>Company: Eastern Michigan University</p>
 * @author Matthew Evett
 * @version 1.0
 */

public interface Player extends java.rmi.Remote {
  /**
   *
   * @author Matthew Evett
   * @param board --- The board is encoded as a 33 integer array.
   * board[0] is unused. For i from 1 to 32, board[i] indicates what piece, if any
   * is position i on the board, using the official checkers numbering
   * scheme.  board[i] = -2 for a black king, -1 for a black pawn, 1 for a white pawn,
   * 2 for a white king, and is 0 if the position is unoccupied
   * @param isWhite is true if the player should make a move for White.
   * @param movesRemaining is the number of moves remaining before a draw is called
   *
   * @return First element of the array is the position of the piece to be moved.
   * The remaining elements are subsequent positions to which that piece moves.
   * (There will usually be only one remaining element, unless result encodes a
   * multiple capture/jumping move.)  Thus a move from position 17 to 10, jumping over
   * an opposition piece at position 14 would be encoded via the array: {17, 10}.
   * <b>It is important that the length of the array be exactly 1+ the number of moves
   * made by the piece.</b>
   */
  public int[] getMove(int[] board, boolean isWhite, int movesRemaining) throws java.rmi.RemoteException;

  /**
   *
   * @author Matthew Evett
   * @return The name of the player (hopefully something fun!)
   */
   public String getName() throws java.rmi.RemoteException;
}