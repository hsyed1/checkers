package referee;

import java.rmi.*;

import javax.swing.*;

/**
 * <p>Title: Driver</p>
 * <p>Description: Runs the checkers game.  Each opponent provides an RMI interface
 *  that allows this driver to request moves from it. The opponents must be named
* "first" and "second" and defined in the "referee" package.   rmiregistry should be
* running in the directory containing the referee directory.</p>
 * <p>Copyright: Matt Evett (c) 2004</p>
 * <p>Company: Dept. Computer Science, Eastern Michigan University</p>
 * @author Matthew Evett
 * @version 1.0
 */

public class Driver {
  private Player white;
  private Player black;
  private Player player1;
  private Player player2;
  private String nameWhite;
  private String nameBlack;

  private Board currentBoard;
  private CheckersFrame myFrame;
  private static int MAX_NUM_MOVES = 100;
  private static final String FIRST_PLAYER_REGISTRATION_NAME = "first";
  private static final String SECOND_PLAYER_REGISTRATION_NAME = "second";


  public Driver() {
    currentBoard = new Board();  // Get initial board
    
    myFrame = new CheckersFrame();
    myFrame.setTitle("Referee");
    myFrame.setSize(800,500);
  }

  /**
   * USAGE: There are up to two optional command-line parameters.  If any are provided, the first should
   * be an integer equal to the number of moves per game.  (The default is 100.)  If there is a
   * second argument of any type, then the Driver will not charge a player with a loss upon
   * receiving a faulty move.  Instead, the Driver will merely query the player for a different move.
   * A player providing 10 faulty moves will receive a loss.
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    boolean allowFaultyInput = false;

    if (args.length >= 1) {
      try {
        int i = Integer.parseInt(args[0]);
        if (i>0) MAX_NUM_MOVES = i;
        // Else, keep the default value
      }
      catch (NumberFormatException ex) {
        System.err.println("Usage: you may optionally provide an integer argument equal to the number of moves in a game.");
        System.exit(-1);
      }

      if (args.length >=2) {
        System.out.println("Because you provided two or more arguments, the referee will allow players to provide faulty\n"+
                           "moves.  In that case, the referee will ask the player to enter another move.");
        allowFaultyInput = true;
      }
    }
    Driver driver = new Driver();

    // DEBUGGING: COMMENT OUT EXACTLY ONE OF THE NEXT TWO LINES
    driver.setUpForRMI(); // Set up to use RMI players
    //driver.setUpForDirect();// Set up to use non-RMI based players.

    driver.myFrame.show();

    if (!allowFaultyInput)
      driver.playAMatch();
    else
      driver.playAllowingErrors();

  }

  /**
   * Play a match, which consists of two games.  Each player gets to play each side.
   */
  private void playAMatch() {
    white = player1;
    black = player2;
    int pointsForWhite = 0;
    try {
      nameWhite = white.getName();
	  System.out.println(nameWhite);
      nameBlack = black.getName();
	  System.out.println(nameBlack);
      pointsForWhite = playOneGame() + 1; // Number of half points for white

      black = player1;
      white = player2;
      nameWhite = white.getName();
      nameBlack = black.getName();
      pointsForWhite += -playOneGame() + 1;
    }
    catch (RemoteException ex) {
      System.err.println("Problem getting names from the players!");
      ex.printStackTrace();
      return;
    }

    System.out.println("\n\nRESULT OF MATCH: "+nameBlack+" wins "+pointsForWhite/2.0+" points, "+
                       nameWhite+" wins "+(2.0 - pointsForWhite/2.0)+" points.");
  }

  /**
   * This method plays a game of checkers
   *
   * @author Matthew Evett
   * @return +1 if white wins, -1 if black wins, 0 if
   * there is a tie.
   */
  private int playOneGame () {

    currentBoard = new Board();
/*  Test referee mpe.  Debugging: if you want to test from a particular initial position define it here.
    int[] testBoard = {0,0,0,0,0,
    		0,0,2,1,
    		0,1,0,0,
    		-1,0,0,0,
    		0,0,0,0,
    		1,0,-1,0,
    		0,0,0,0,
    		0,0,0,1};
  
    currentBoard = new Board(testBoard,true); */
    myFrame.recordMove(new int[0], currentBoard, "Referee sets initial board");
    myFrame.repaint(); // Draw first board
    Board nextBoard;
    int[] nextMove=null;
    Player currentPlayer = black;
    /* Debugging mpe
    currentPlayer = white;
    System.err.println("currentBoard="+currentBoard);*/
    int moveCount = 0;
    while (moveCount < MAX_NUM_MOVES &&
           currentBoard.numWhites() != 0 && currentBoard.numBlacks() != 0) {
    	
        if (currentBoard.isNoLegalMove()) {
            return printVictoryMessage(currentPlayer, " has no legal move");
        }

      try {
        nextMove = currentPlayer.getMove(currentBoard.toArray(), currentPlayer == white, MAX_NUM_MOVES-moveCount);
      }
      catch (RemoteException ex) {
        return printVictoryMessage(currentPlayer, " has failed to respond");
      }
      printMove(currentPlayer, nextMove);
      if (nextMove.length < 2) {
          return printVictoryMessage(currentPlayer, " has provided an illegal move: "+
                                     moveToString(nextMove));
      }
      else { // the provided move is long enough...
        Object result;
        result = currentBoard.updateBoard(nextMove);  // Check to see if move is legal
        if (result instanceof Board) {  // Legal move
          nextBoard = (Board)result;
          myFrame.recordMove(nextMove, nextBoard, (currentPlayer==black? nameBlack +
                                  " (Black)" : nameWhite+" (White)"));
          myFrame.repaint();
          currentBoard = nextBoard;
          currentPlayer = (currentPlayer == white ? black : white); // Change sides after each move
        }
        else {
          return printVictoryMessage(currentPlayer, " has provided an illegal move ("+
                                     moveToString(nextMove)+"):\n"+((String)result));
        }
      }
      moveCount++;
    } // while the game goes on

    /** The game has ended.  Determine who has won */
    if (currentBoard.numWhites() == 0)
      return printVictoryMessage(white, "has no more pieces");
    if (currentBoard.numBlacks() == 0)
      return printVictoryMessage(black, "has no more pieces");
    // else, moveCount has exceeded the maximum
    if (currentBoard.numWhites() == currentBoard.numBlacks()) {
      System.out.println("The move limit has been exceeded, and the players are tied!");
      return 0;
    }
    return printVictoryMessage((currentBoard.numWhites() > currentBoard.numBlacks() ? black : white),
                               "has the fewest pieces when the move limit was exceeded");
  }

  /**
   * Print the given move with a prefix indicating who is doing the move
   *
   * @param currentPlayer Player
   * @param nextMove int[]
   */
  private void printMove(Player currentPlayer, int[] nextMove) {
    // First, convert the array of integers to a string
    String move=moveToString(nextMove);
    System.out.println((currentPlayer==black? nameBlack +
                                  " (Black)" : nameWhite+" (White)")+": "+move);
  }

  /**
   * This method
   *
   * @author Matthew Evett
   * @return +1 if white wins, -1 if black wins
   */
  private int printVictoryMessage (Player losingPlayer, String message) {
    String fullMessage = "Player "+(losingPlayer==black? nameBlack +
                                  " (Black)" : nameWhite+" (White)") + message + " and loses!!";
    System.out.println(fullMessage);
    myFrame.recordError(fullMessage);
    return (losingPlayer == black ? 1 : -1);  // If Black failed, then White wins
  }
  /**
   * This method plays a game of checkers.  Modified to allow a player to
   * try again.  For use as a debugging tool with HumanPlayer.
   * PRE: setUpForRMI must have been called.
   *
   * @author Matthew Evett
   * @return +1 if white wins, -1 if black wins, 0 if
   * there is a tie.
   * @see HumanPlayer
   */
  private void playAllowingErrors () {
    final int MAX_ERRORS = 10;  // Maximum number of errors tolerated by each opponent

    int numErrorsWhite = 0,
        numErrorsBlack = 0;
    int[] nextMove=null;
    Player currentPlayer = black;
    boolean continuePlaying = true;
    int moveCount = 0;
    while (moveCount < MAX_NUM_MOVES && continuePlaying &&
           currentBoard.numWhites() != 0 && currentBoard.numBlacks() != 0) {
      if (currentBoard.isNoLegalMove()) {
          System.out.println("Player "+(currentPlayer==black? nameBlack +
                  " (Black)" : nameWhite+" (White)") + " has no legal move and so loses!");
   	  
      }
      moveCount++;
      try {
		  System.out.println("Here");
		  System.out.println(currentBoard.toArray());
		  System.out.println(currentPlayer);
		  System.out.println(MAX_NUM_MOVES-moveCount);
        nextMove = currentPlayer.getMove(currentBoard.toArray(), 
        		currentPlayer == white, MAX_NUM_MOVES-moveCount);
      }
      catch (RemoteException ex) {
        System.err.println("Player "+(currentPlayer==black? nameBlack +
                                      " (Black)" : nameWhite+" (White)") + " has failed to respond!");
        System.exit(-2);
      }
      if (nextMove.length < 2) continuePlaying = false;
      else {
        Object result = currentBoard.updateBoard(nextMove);

        if (result instanceof Board) {
          currentBoard = (Board)result;
          currentPlayer = (currentPlayer == white ? black : white); // Change sides after each move
        }
        else {
          System.out.println("Player "+(currentPlayer==black? nameBlack +
                                      " (Black)" : nameWhite+" (White)") + " has made an illegal move: "+
                           (String)result);
          if ((currentPlayer == black && ++numErrorsBlack > MAX_ERRORS)
              || (++numErrorsWhite > MAX_ERRORS)) {
            printVictoryMessage(currentPlayer,"has made too many illegal moves");
            return;
          }
          System.out.println("Asking for another LEGAL move...");
        }

      }
    } // while
    System.out.println("Thanks for playing");
  }

  /**
   * Debugging set-up.   Allows execution of Driver without need for RMI.
   * This is especially useful if you want to run your program inside a debugger.
   * Contrast with setUpForRMI.
   * @see setUpForRMI
   */
  private void setUpForDirect() {
	  //System.setSecurityManager(new RMISecurityManager());
	  try {
		  player1 = new HumanPlayer("Obelix");
		  player2 = new HumanPlayer("Asterix");
	  } catch (RemoteException e) {
		  // This should never happen
		  System.err.println("Yikes!  rmiSecurity error when not using RMI!");
		  e.printStackTrace();
	  }

  }


  /**
   * Acquire the remote players
   *
   * @author Matthew Evett
   */
  private void setUpForRMI() {
    System.setSecurityManager(new RMISecurityManager());
    player1 = getAPlayer(FIRST_PLAYER_REGISTRATION_NAME);
    player2 = getAPlayer(SECOND_PLAYER_REGISTRATION_NAME);
  }

  
  
  /**
   * Access a remote player
   *
   * @author Matthew Evett
   * @param whichPlayer should be either "first" or "second", indicating which agent
   * we are trying to access.
   * @return reference to a remote player agent
   */
  private Player getAPlayer(String whichPlayer) {
    Player player = null;
    do {
      String playerIP = JOptionPane.showInputDialog(null,
          "Enter the IP address of the Player registered as " + whichPlayer +
            " \n(use \"localhost\" if everything is running locally)",
          "Enter IP", JOptionPane.QUESTION_MESSAGE);
      playerIP = playerIP.trim();

      try {
        player = (Player) Naming.lookup("rmi://" + playerIP + "/" +
                                        whichPlayer);
      }
      catch (Exception e) {
        System.err.println(e);
        JOptionPane.showMessageDialog(null,
                                      "Make sure the IP address is correct & the Player is rmi-registered there",
                                      "Bad input", JOptionPane.WARNING_MESSAGE);
        player = null;
        break;
      }
    }
    while (player == null);
  return player;
  }

  /**
   *
   * @param nextMove Sequence of positions (1-32) on the board
   * @return The move sequence as a string
   */
  public static String moveToString(int[] nextMove) {
    String result = "";
    for (int i = 0; i < nextMove.length; i++) {
      result += nextMove[i] + " ";
    }
    return result;
  }


}