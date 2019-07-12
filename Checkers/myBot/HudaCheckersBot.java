/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myBot;
import java.util.*;
import java.rmi.*;
import java.net.*;
/**
 *
 * @author Huda
 */
public class HudaCheckersBot extends java.rmi.server.UnicastRemoteObject implements referee.Player {

    private String name;
    public static final int NO_PIECE = 0;
    public static final int WHITE_PAWN = 1;
    public static final int WHITE_KING = 2;
    public static final int BLACK_PAWN = -1;
    public static final int BLACK_KING = -2;
    public static final int MAX_PLY=9; 
    public static final int MIN=0;
    public static final int MAX=1;
    
    public HudaCheckersBot(String name) throws java.rmi.RemoteException 
    {
        this.name = name;
    }
    
    public String getName() throws java.rmi.RemoteException 
    {
        return name;
    }
    
    public int[] getMove(int[] board, boolean isWhite, int movesRemaining) throws java.rmi.RemoteException 
    {
        System.out.println(boardToString(board));
        System.out.print("There are "+movesRemaining+" moves remaining. Please enter move for " + name + " ("+
                     (isWhite ? "White" : "Black") + ") : ");
        
        String player;
        int ply=1;
        int alpha=Integer.MIN_VALUE;
        int beta=Integer.MAX_VALUE;
        if(isWhite)
            player="second";
        else
            player="first";
        MyBoard b=new MyBoard(board);
        //System.out.println("------- Parent Board------");
        //System.out.println(boardToString(b.boardVal));
        //System.out.println("--------------------------");
        LinkedList<MyBoard> boardsReached=getBoards(b, player);
        
        int maxIndex = 0;
        //boolean MAX=true;
        int maxValue = boardValue(boardsReached.get(0), player, MIN, ply, alpha, beta);
        
        for(int i = 1; i < boardsReached.size(); i++)    //find child with largest minmax value
        {
            int currentValue = boardValue(boardsReached.get(i), player, MIN, ply, alpha, beta);
            
            if(currentValue > maxValue)
            {
                maxIndex = i;
                maxValue = currentValue;
            }
        }
        
        //System.out.println(maxValue);
        MyBoard result = boardsReached.get(maxIndex);      //choose child as next move
        //String resultString=boardToString(result.boardVal);
        //System.out.println(resultString);
        System.out.println(Arrays.toString(result.getSeq().toArray()));
        //int maxIndex = 0;
        //int maxValue = boardValue(boardsReached.get(0), player, MAX, ply, alpha, beta);
        
        
        int[] resultArr = new int[result.getSeq().size()];
        ArrayList<Integer> resultArrList=result.getSeq();
        for (int i = 0; i<resultArrList.size(); i++) 
        {
            resultArr[i]=((Integer)resultArrList.get(i));
        }
        
        return resultArr;
        
    }
    
    public static int jumpFromTo(int fromPos, int midPos)
    {
        int val=fromPos-midPos;
        int newPos=0;
        if(val<0 && (midPos%8==1 || midPos%8==2 || midPos%8==3 || midPos%8==4) && !isCorner(midPos))//player 1 
        { 
            //(4-5 group)
            if(val==-4)
                newPos=midPos+5;
            else
                newPos=midPos+4;
        }
        if(val<0 && (midPos%8==0 || midPos%8==5 || midPos%8==6 || midPos%8==7) && !isCorner(midPos))//player 1
        {
            //(3-4 group)
            if(val==-4)
                newPos=midPos+3;
            else
                newPos=midPos+4;
        }
        if(val>0 && (midPos%8==1 || midPos%8==2 || midPos%8==3 || midPos%8==4) && !isCorner(midPos))//player 2 
        {
            //(3-4 group)
            if(val==4)
                newPos=midPos-3;
            else
                newPos=midPos-4;
        }
        if(val>0 && (midPos%8==0 || midPos%8==5 || midPos%8==6 || midPos%8==7) && !isCorner(midPos))//player 2
        {
            //(4-5 group)
            if(val==4)
                newPos=midPos-5;
            else
                newPos=midPos-4;
        }
        return newPos;
    }
    
    public static HashMap<Integer, Integer> getKingJump(MyBoard b, int pos, int king)
    {
        HashMap<Integer,Integer> jumpPos=new HashMap<>();
        HashMap<Integer, Integer> jumpPos1;
        int otherKing;
        int otherPawn;
        if(king==WHITE_KING)
        {
            otherKing=BLACK_KING;
            otherPawn=BLACK_PAWN;
        }
        else
        {
            otherKing=WHITE_KING;
            otherPawn=WHITE_PAWN;
        }
        if(pos%8==1 || pos%8==2 || pos%8==3 || pos%8==4)
        {
            if(pos+4<=32 && (b.boardVal[pos+4]==otherKing || b.boardVal[pos+4]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos+4);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos+4);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos+4]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            if(pos-4>0 && (b.boardVal[pos-4]==otherKing || b.boardVal[pos-4]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos-4);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos-4);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos-4]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            int midPos=pos-3;
            if(pos-3>0 && (midPos%8==0 || midPos%8==5 || midPos%8==6 || midPos%8==7) && (b.boardVal[pos-3]==otherKing || b.boardVal[pos-3]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos-3);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos-3);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos-3]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            midPos=pos+5;
            if(pos+5<=32 && (midPos%8==0 || midPos%8==5 || midPos%8==6 || midPos%8==7) && (b.boardVal[pos+5]==otherKing || b.boardVal[pos+5]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos+5);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos+5);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos+5]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
        }
        if(pos%8==0 || pos%8==5 || pos%8==6 || pos%8==7)
        {
            if(pos+4<=32 && (b.boardVal[pos+4]==otherKing || b.boardVal[pos+4]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos+4);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos+4);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos+4]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            if(pos-4>0 && (b.boardVal[pos-4]==otherKing || b.boardVal[pos-4]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos-4);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos-4);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos-4]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            int midPos=pos+3;
            if(pos+3<=32 && (midPos%8==1 || midPos%8==2 || midPos%8==3 || midPos%8==4) && (b.boardVal[pos+3]==otherKing || b.boardVal[pos+3]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos+3);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos+3);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos+3]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
            midPos=pos-5;
            if(pos-5>0 && (midPos%8==1 || midPos%8==2 || midPos%8==3 || midPos%8==4) && (b.boardVal[pos-5]==otherKing || b.boardVal[pos-5]==otherPawn))
            {
                int jump=jumpFromTo(pos, pos-5);
                if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                {
                    jumpPos.put(jump,pos-5);
                    MyBoard b1=new MyBoard(b.boardVal);
                    b1.boardVal[pos]=NO_PIECE;
                    b1.boardVal[pos-5]=NO_PIECE;
                    b1.boardVal[jump]=BLACK_PAWN;
                    jumpPos1=getKingJump(b1,jump,king);
                    if(!jumpPos1.isEmpty())
                        jumpPos.putAll(jumpPos1);
                }
            }
        }
        return jumpPos;
    }
    public static HashMap<Integer, Integer> getPawnJump(MyBoard b, int pos, String player)
    {
        HashMap<Integer, Integer> jumpPos=new HashMap<>();
        HashMap<Integer, Integer> jumpPos1;
        
        if("first".equals(player) && b.boardVal[pos]==BLACK_PAWN)
        {
            if(pos+4<=32)
            {
                if(b.boardVal[pos+4]==WHITE_PAWN || b.boardVal[pos+4]==WHITE_KING)
                {
                    int jump=jumpFromTo(pos, pos+4);
                    if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                    {
                        jumpPos.put(jump,pos+4);
                        MyBoard b1=new MyBoard(b.boardVal);
                        b1.boardVal[pos]=NO_PIECE;
                        b1.boardVal[pos+4]=NO_PIECE;
                        b1.boardVal[jump]=BLACK_PAWN;
                        jumpPos1=getPawnJump(b1,jump,player);
                        if(!jumpPos1.isEmpty())
                            jumpPos.putAll(jumpPos1);
                    }
                }
                if(pos+5<=32 && (pos%8==1 || pos%8==2 || pos%8==3))
                {
                    if(b.boardVal[pos+5]==WHITE_PAWN || b.boardVal[pos+5]==WHITE_KING)
                    {
                        int jump=jumpFromTo(pos, pos+5);
                        if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                        {
                            jumpPos.put(jump,pos+5);
                            MyBoard b1=new MyBoard(b.boardVal);
                            b1.boardVal[pos]=NO_PIECE;
                            b1.boardVal[pos+5]=NO_PIECE;
                            b1.boardVal[jump]=BLACK_PAWN;
                            jumpPos1=getPawnJump(b1,jump,player);
                            if(!jumpPos1.isEmpty())
                                jumpPos.putAll(jumpPos1);
                        }
                    }
                }
                if(pos+3<=32 && (pos%8==0 || pos%8==6 || pos%8==7))
                {
                    if(b.boardVal[pos+3]==WHITE_PAWN || b.boardVal[pos+3]==WHITE_KING)
                    {
                        int jump=jumpFromTo(pos, pos+3);
                        if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                        {
                            jumpPos.put(jump,pos+3);
                            MyBoard b1=new MyBoard(b.boardVal);
                            b1.boardVal[pos]=NO_PIECE;
                            b1.boardVal[pos+3]=NO_PIECE;
                            b1.boardVal[jump]=BLACK_PAWN;
                            jumpPos1=getPawnJump(b1,jump,player);
                            if(!jumpPos1.isEmpty())
                                jumpPos.putAll(jumpPos1);
                        }
                    }
                }
            }
        }
        if("second".equals(player) && b.boardVal[pos]==WHITE_PAWN)
        {
            if(pos-4>0)
            {
                if(b.boardVal[pos-4]==BLACK_PAWN || b.boardVal[pos-4]==BLACK_KING)
                {
                    //check for jump
                    int jump=jumpFromTo(pos, pos-4);
                    if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                    {
                        jumpPos.put(jump,pos-4);
                        MyBoard b1=new MyBoard(b.boardVal);
                        b1.boardVal[pos]=NO_PIECE;
                        b1.boardVal[pos-5]=NO_PIECE;
                        b1.boardVal[jump]=WHITE_PAWN;
                        jumpPos1=getPawnJump(b1,jump,player);
                        if(!jumpPos1.isEmpty())
                            jumpPos.putAll(jumpPos1);
                    }
                }
                if(pos-5>0 && (pos%8==0 || pos%8==6 || pos%8==7))
                {
                    if(b.boardVal[pos-5]==BLACK_PAWN || b.boardVal[pos-5]==BLACK_KING)
                    {
                        //check for jump
                        int jump=jumpFromTo(pos, pos-5);
                        if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                        {
                            jumpPos.put(jump,pos-5);
                            MyBoard b1=new MyBoard(b.boardVal);
                            b1.boardVal[pos]=NO_PIECE;
                            b1.boardVal[pos-5]=NO_PIECE;
                            b1.boardVal[jump]=WHITE_PAWN;
                            jumpPos1=getPawnJump(b1,jump,player);
                            if(!jumpPos1.isEmpty())
                                jumpPos.putAll(jumpPos1);
                        }
                    }
                }
                if(pos-3>0 && (pos%8==1 || pos%8==2 || pos%8==3))
                {
                    if(b.boardVal[pos-3]==BLACK_PAWN || b.boardVal[pos-3]==BLACK_KING)
                    {
                        //check for jump
                        int jump=jumpFromTo(pos, pos-3);
                        if((jump>0 && jump<=32) && b.boardVal[jump]==NO_PIECE)
                        {
                            jumpPos.put(jump,pos-3);
                            MyBoard b1=new MyBoard(b.boardVal);
                            b1.boardVal[pos]=NO_PIECE;
                            b1.boardVal[pos-3]=NO_PIECE;
                            b1.boardVal[jump]=BLACK_PAWN;
                            jumpPos1=getPawnJump(b1,jump,player);
                            if(!jumpPos1.isEmpty())
                                jumpPos.putAll(jumpPos1);
                        }
                    }
                }
            }      
        }
        return jumpPos;
    }
    public static ArrayList<Integer> getPawnMove(MyBoard b, int pos, String player)
    {
        ArrayList<Integer> newPos=new ArrayList<>();
        //for player 1 black pawn
        if("first".equals(player) && b.boardVal[pos]==BLACK_PAWN)
        {
            if(pos+4<=32)
            {
                if(b.boardVal[pos+4]==NO_PIECE)
                    newPos.add(pos+4);
                
                if(pos+5<=32 && (pos%8==1 || pos%8==2 || pos%8==3))
                {
                    if(b.boardVal[pos+5]==NO_PIECE)
                        newPos.add(pos+5);
                }
                if(pos+3<=32 && (pos%8==0 || pos%8==6 || pos%8==7))
                {
                    if(b.boardVal[pos+3]==NO_PIECE)
                        newPos.add(pos+3);
                }
            }
        }
        
        //for player 2 White pwan
        if("second".equals(player) && b.boardVal[pos]==WHITE_PAWN)
        {
            if(pos-4>0)
            {
                if(b.boardVal[pos-4]==NO_PIECE)
                    newPos.add(pos-4);
                
                if(pos-5>0 && (pos%8==0 || pos%8==6 || pos%8==7))
                {
                    if(b.boardVal[pos-5]==NO_PIECE)
                        newPos.add(pos-5);
                }
                if(pos-3>0 && (pos%8==1 || pos%8==2 || pos%8==3))
                {
                    if(b.boardVal[pos-3]==NO_PIECE)
                        newPos.add(pos-3);
                }
            }
        }
        return newPos;
    }
    public static ArrayList<Integer> getKingMove(MyBoard b, int pos)
    {
        ArrayList<Integer> newPos=new ArrayList<>();
        if(pos%8==1 || pos%8==2 || pos%8==3 || pos%8==4)
        {
            if(pos+4<=32 && b.boardVal[pos+4]==NO_PIECE && ((pos+4)%8==0 || (pos+4)%8==5 || (pos+4)%8==6 || (pos+4)%8==7))
                newPos.add(pos+4);
            if(pos-4>0 && b.boardVal[pos-4]==NO_PIECE && ((pos-4)%8==0 || (pos-4)%8==5 || (pos-4)%8==6 || (pos-4)%8==7))
                newPos.add(pos-4);
            if(pos-3>0 && b.boardVal[pos-3]==NO_PIECE && ((pos-3)%8==0 || (pos-3)%8==5 || (pos-3)%8==6 || (pos-3)%8==7))
                newPos.add(pos-3);
            if(pos+5<=32 && b.boardVal[pos+5]==NO_PIECE && ((pos+5)%8==0 || (pos+5)%8==5 || (pos+5)%8==6 || (pos+5)%8==7))
                newPos.add(pos+5);
        }
        if(pos%8==0 || pos%8==5 || pos%8==6 || pos%8==7)
        {
            if(pos+4<=32 && b.boardVal[pos+4]==NO_PIECE && ((pos+4)%8==1 || (pos+4)%8==2 || (pos+4)%8==3 || (pos+4)%8==4))
                newPos.add(pos+4);
            if(pos-4>0 && b.boardVal[pos-4]==NO_PIECE && ((pos-4)%8==1 || (pos-4)%8==2 || (pos-4)%8==3 || (pos-4)%8==4))
                newPos.add(pos-4);
            if(pos+3<=32 && b.boardVal[pos+3]==NO_PIECE && ((pos+3)%8==1 || (pos+3)%8==2 || (pos+3)%8==3 || (pos+3)%8==4))
                newPos.add(pos+3);
            if(pos-5>0 && b.boardVal[pos-5]==NO_PIECE && ((pos-5)%8==1 || (pos-5)%8==2 || (pos-5)%8==3 || (pos-5)%8==4))
                newPos.add(pos-5);
        }
        return newPos;
    }
    
    public static LinkedList<MyBoard> getBoards(MyBoard b, String player)
    {
        LinkedList<MyBoard> boardsReached=new LinkedList<>();
        ArrayList<Integer> newPos;
        ArrayList<Integer> newPosKing;
        HashMap<Integer, Integer> jumpPos;
        HashMap<Integer, Integer> jumpPos1;
        HashMap<Integer, Integer> jumpPosKing;
        MyBoard newBoard;
        boolean jumpAvail=false;
        
        for(int pos=1;pos<=32;pos++)
        {
            if("first".equals(player) && b.boardVal[pos]!=NO_PIECE  && b.boardVal[pos]==BLACK_PAWN)
            {
                jumpPos=getPawnJump(b, pos, player);
                HashMap<Integer,Integer> tempJump=new HashMap<>(jumpPos);
                newBoard=new MyBoard(b.boardVal);
                ArrayList<Integer> seq=new ArrayList<>();
                boolean set=false;
                int posCalc=pos;
                
                if(!jumpPos.isEmpty())
                    seq.add(posCalc);
                while(!tempJump.isEmpty())
                {
                    if(tempJump.containsKey(posCalc+7))
                    {
                        int jump=posCalc+7;
                        int midPos=tempJump.get(posCalc+7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        if(posCalc+7==29 || posCalc+7==30 || posCalc+7==31 || posCalc+7==32)
                            newBoard.boardVal[posCalc+7]=BLACK_KING;
                        else
                            newBoard.boardVal[posCalc+7]=BLACK_PAWN;

                        tempJump.remove(posCalc+7);
                        posCalc=posCalc+7;
                    }
                    else if(tempJump.containsKey(posCalc+9))
                    {
                        //seq.add(pos);
                        int jump=posCalc+9;
                        int midPos=tempJump.get(posCalc+9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        if(posCalc+9==29 || posCalc+9==30 || posCalc+9==31 || posCalc+9==32)
                            newBoard.boardVal[posCalc+9]=BLACK_KING;
                        else
                            newBoard.boardVal[posCalc+9]=BLACK_PAWN;

                        tempJump.remove(posCalc+9);
                        posCalc=posCalc+9;
                    }
                    else
                    {
                        set=true;
                        newBoard.setSeq(seq); 
                        boardsReached.add(newBoard);
                        break;
                    }
                }
                if(!set && !jumpPos.isEmpty())
                {
                  newBoard.setSeq(seq);
                  boardsReached.add(newBoard);
                  jumpAvail=true;
                }
            }
	}
	for(int pos=1;pos<=32;pos++)
        {
            if("first".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==BLACK_KING)
            {
                jumpPosKing=getKingJump(b, pos, BLACK_KING);
                HashMap<Integer,Integer> tempJump=new HashMap<>(jumpPosKing);
                newBoard=new MyBoard(b.boardVal);
                ArrayList<Integer> seq=new ArrayList<>();
                boolean set=false;
                int posCalc=pos;
                
                if(!jumpPosKing.isEmpty())
                    seq.add(posCalc);
                while(!tempJump.isEmpty())
                {
                    if(tempJump.containsKey(posCalc+7))
                    {
                        int jump=posCalc+7;
                        int midPos=tempJump.get(posCalc+7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc+7]=BLACK_KING;
                        tempJump.remove(posCalc+7);
                        posCalc=posCalc+7;
                    }
                    else if(tempJump.containsKey(posCalc-7))
                    {
                        int jump=posCalc-7;
                        int midPos=tempJump.get(posCalc-7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc-7]=BLACK_KING;
                        tempJump.remove(posCalc-7);
                        posCalc=posCalc-7;
                    }
                    else if(tempJump.containsKey(posCalc+9))
                    {
                        int jump=posCalc+9;
                        int midPos=tempJump.get(posCalc+9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc+9]=BLACK_KING;
                        tempJump.remove(posCalc+9);
                        posCalc=posCalc+9;
                    }
                    else if(tempJump.containsKey(posCalc-9))
                    {
                        int jump=posCalc-9;
                        int midPos=tempJump.get(posCalc-9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc-9]=BLACK_KING;
                        tempJump.remove(posCalc-9);
                        posCalc=posCalc-9;
                    }
                    else
                    {
                        set=true;
                        newBoard.setSeq(seq);
                        boardsReached.add(newBoard);
                        break;
                    }
                }
                if(!set && !jumpPosKing.isEmpty())
                {
                    newBoard.setSeq(seq);
                    boardsReached.add(newBoard);
                    jumpAvail=true;
                }
            }
        }
	
        for(int pos=1;pos<=32;pos++)
        {
            if("first".equals(player) && b.boardVal[pos]!=NO_PIECE  && b.boardVal[pos]==BLACK_PAWN && !jumpAvail)
            {
                newPos=getPawnMove(b, pos, player);
                for(int i=0;i<newPos.size();i++)
                {
                    ArrayList<Integer> seq=new ArrayList<>();
                    newBoard=new MyBoard(b.boardVal);
                    newBoard.boardVal[pos]=NO_PIECE;
                    if(newPos.get(i)==29 || newPos.get(i)==30 || newPos.get(i)==31 || newPos.get(i)==32)
                        newBoard.boardVal[newPos.get(i)]=BLACK_KING;
                    else
                        newBoard.boardVal[newPos.get(i)]=BLACK_PAWN;
                    seq.add(pos);
                    seq.add(newPos.get(i));
                    newBoard.setSeq(seq);
                    boardsReached.add(newBoard);
                }   
            }
	}
	
        for(int pos=1;pos<=32;pos++)
        {
            if("first".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==BLACK_KING && !jumpAvail)
            {
                newPosKing=getKingMove(b, pos);
                for(int i=0;i<newPosKing.size();i++)
                {
                    ArrayList<Integer> seq=new ArrayList<>();
                    newBoard=new MyBoard(b.boardVal);
                    newBoard.boardVal[pos]=NO_PIECE;
                    newBoard.boardVal[newPosKing.get(i)]=BLACK_KING;
                    seq.add(pos);
                    seq.add(newPosKing.get(i));
                    newBoard.setSeq(seq);
                    boardsReached.add(newBoard);
                }
            }
	}
	
        for(int pos=1;pos<=32;pos++)
        {		
            if("second".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==WHITE_PAWN)
            {
                jumpPos=getPawnJump(b, pos, player);
                
                HashMap<Integer,Integer> tempJump=new HashMap<>(jumpPos);
                newBoard=new MyBoard(b.boardVal);
                ArrayList<Integer> seq=new ArrayList<>();
                boolean set=false;
                int posCalc=pos;
                
                if(!jumpPos.isEmpty())
                    seq.add(posCalc);
                while(!tempJump.isEmpty())
                {
                    if(tempJump.containsKey(posCalc-7))
                    {
                        int jump=posCalc-7;
                        int midPos=tempJump.get(posCalc-7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        if(posCalc-7==29 || posCalc-7==30 || posCalc-7==31 || posCalc-7==32)
                            newBoard.boardVal[posCalc-7]=WHITE_KING;
                        else
                            newBoard.boardVal[posCalc-7]=WHITE_PAWN;

                        tempJump.remove(posCalc-7);
                        posCalc=posCalc-7;
                    }
                    else if(tempJump.containsKey(posCalc-9))
                    {
                        int jump=posCalc-9;
                        int midPos=tempJump.get(posCalc-9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        if(posCalc-9==29 || posCalc-9==30 || posCalc-9==31 || posCalc-9==32)
                            newBoard.boardVal[posCalc-9]=WHITE_KING;
                        else
                            newBoard.boardVal[posCalc-9]=WHITE_PAWN;

                        tempJump.remove(posCalc-9);
                        posCalc=posCalc-9;
                    }
                    else
                    {
                        set=true;
                        newBoard.setSeq(seq);
                        boardsReached.add(newBoard);
                        break;
                    }
                }
                if(!set && !jumpPos.isEmpty())
                {
                  newBoard.setSeq(seq);
                  boardsReached.add(newBoard);
                  jumpAvail=true;
                }
            }
	}
	
        for(int pos=1;pos<=32;pos++)
        {
            if("second".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==WHITE_KING)
            {
                jumpPosKing=getKingJump(b, pos, WHITE_KING);
                HashMap<Integer,Integer> tempJump=new HashMap<>(jumpPosKing);
                newBoard=new MyBoard(b.boardVal);
                ArrayList<Integer> seq=new ArrayList<>();
                boolean set=false;
                int posCalc=pos;
                
                if(!jumpPosKing.isEmpty())
                    seq.add(posCalc);
                while(!tempJump.isEmpty())
                {
                    if(tempJump.containsKey(posCalc+7))
                    {
                        int jump=posCalc+7;
                        int midPos=tempJump.get(posCalc+7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc+7]=WHITE_KING;
                        tempJump.remove(posCalc+7);
                        posCalc=posCalc+7;
                    }
                    else if(tempJump.containsKey(pos-7))
                    {
                        int jump=posCalc-7;
                        int midPos=tempJump.get(posCalc-7);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc-7]=WHITE_KING;
                        tempJump.remove(posCalc-7);
                        posCalc=posCalc-7;
                    }
                    else if(tempJump.containsKey(posCalc+9))
                    {
                        int jump=posCalc+9;
                        int midPos=tempJump.get(posCalc+9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc+9]=WHITE_KING;
                        tempJump.remove(posCalc+9);
                        posCalc=posCalc+9;
                    }
                    else if(tempJump.containsKey(posCalc-9))
                    {
                        int jump=posCalc-9;
                        int midPos=tempJump.get(posCalc-9);
                        seq.add(jump);
                        newBoard.boardVal[posCalc]=NO_PIECE;
                        newBoard.boardVal[midPos]=NO_PIECE;
                        newBoard.boardVal[posCalc-9]=WHITE_KING;
                        tempJump.remove(posCalc-9);
                        posCalc=posCalc-9;
                    }
                    else
                    {
                        set=true;
                        newBoard.setSeq(seq);
                        boardsReached.add(newBoard);
                        break;
                    }
                }
                if(!set && !jumpPosKing.isEmpty())
                {
                    newBoard.setSeq(seq);
                    boardsReached.add(newBoard);
                    jumpAvail=true;
                }
            }
	}
	
        for(int pos=1;pos<=32;pos++)
        {
            if("second".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==WHITE_PAWN && !jumpAvail)
            {  
                newPos=getPawnMove(b, pos, player);
                for(int i=0;i<newPos.size();i++)
                {
                    ArrayList<Integer> seq=new ArrayList<>();
                    newBoard=new MyBoard(b.boardVal);
                    newBoard.boardVal[pos]=NO_PIECE;
                    if(newPos.get(i)==1 || newPos.get(i)==2 || newPos.get(i)==3 || newPos.get(i)==4)
                        newBoard.boardVal[newPos.get(i)]=WHITE_KING;
                    else
                        newBoard.boardVal[newPos.get(i)]=WHITE_PAWN;
                    seq.add(pos);
                    seq.add(newPos.get(i));
                    newBoard.setSeq(seq);   
                    boardsReached.add(newBoard);
                }
            }
	}
	
        for(int pos=1;pos<=32;pos++)
        {		
            if("second".equals(player) && b.boardVal[pos]!=NO_PIECE && b.boardVal[pos]==WHITE_KING && !jumpAvail)
            {
                newPosKing=getKingMove(b, pos);
                for(int i=0;i<newPosKing.size();i++)
                {
                    ArrayList<Integer> seq=new ArrayList<>();
                    newBoard=new MyBoard(b.boardVal);
                    newBoard.boardVal[pos]=NO_PIECE;
                    newBoard.boardVal[newPosKing.get(i)]=WHITE_KING;
                    seq.add(pos);
                    seq.add(newPosKing.get(i));
                    newBoard.setSeq(seq);
                    boardsReached.add(newBoard);
                }
            }
        }
	
        return boardsReached;
    }
    
    public static void main (String[] args) 
    {
        if (args.length != 2 || (!args[0].equals("1") && !args[0].equals("2"))) 
        {
            System.err.println("Usage: java HumanPlayer X FOO, where X is 1 for registering the agent as 'first',\n"+
                         "  2 for registering it as 'second'.  The second argument (FOO)is the name of the agent.\n");
            System.exit(-1);
        }

        String playerName = args[1];
        String playerRegistration = (args[0].equals("1") ? "first" : "second");

        System.setSecurityManager(new RMISecurityManager());

        try 
        {
            HudaCheckersBot h = new HudaCheckersBot(playerName);
            Naming.rebind(playerRegistration, h);
            System.out.println("Player "+playerRegistration+"(named "+playerName+") is waiting for the referee");
        }
        catch (MalformedURLException ex) 
        {
            System.err.println("Bad URL for RMI server");
            System.err.println(ex);
        }
        catch (RemoteException ex) 
        {
            System.err.println(ex);
        }
    }
    
    /*public static void main(String[] args) 
    {
        // TODO code application logic here
        int[] board={5, BLACK_PAWN, BLACK_PAWN, WHITE_KING, BLACK_PAWN, 
                    BLACK_PAWN, BLACK_PAWN, NO_PIECE, NO_PIECE, 
                    NO_PIECE, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN,
                    NO_PIECE, WHITE_KING, NO_PIECE, NO_PIECE, 
                    NO_PIECE, BLACK_KING, NO_PIECE, NO_PIECE, 
                    WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, 
                    NO_PIECE, WHITE_PAWN, NO_PIECE, WHITE_PAWN, 
                    WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN};
        
        
        String player="first";
        int ply=0;
        int alpha=Integer.MIN_VALUE;
        int beta=Integer.MAX_VALUE;
        
        Board b=new Board(board);
        System.out.println("------- Parent Board------");
        System.out.println(boardToString(b.boardVal));
        System.out.println("--------------------------");
        LinkedList<Board> boardsReached=getBoards(b, player);
        
        for(int i = 0; i < boardsReached.size(); i++)    
        {
            String b2S=boardToString(boardsReached.get(i).boardVal);
            System.out.println(b2S);
            System.out.println(Arrays.toString((boardsReached.get(i)).getMoves()));
            System.out.println();
        }
        int maxIndex = 0;
        //boolean MAX=true;
        int maxValue = boardValue(boardsReached.get(0), player, MAX, ply, alpha, beta);
        
        for(int i = 1; i < boardsReached.size(); i++)    //find child with largest minmax value
        {
            int currentValue = boardValue(boardsReached.get(i), player, MAX, ply, alpha, beta);
            
            if(currentValue > maxValue)
            {
                maxIndex = i;
                maxValue = currentValue;
            }
        }
        
        System.out.println(maxValue);
        Board result = boardsReached.get(maxIndex);      //choose child as next move
        String resultString=boardToString(result.boardVal);
        System.out.println(resultString);
        System.out.println(Arrays.toString(result.getMoves()));
        //int maxIndex = 0;
        //int maxValue = boardValue(boardsReached.get(0), player, MAX, ply, alpha, beta);
        
    }*/
    
    private static int boardValue(MyBoard b, String player, int level, int ply, int alpha, int beta)
    { 
        if(ply>=MAX_PLY)
            return boardEval(b, player);
        else if(level==MAX)       //if board is at max level
        {
            int maxValue = Integer.MIN_VALUE;    
            LinkedList<MyBoard> boardsReached=getBoards(b,player);   //generate children of board
            
            for(int i = 0; i < boardsReached.size(); i++)
            {
                //find maximum of minmax values of children
                int currentValue = boardValue(boardsReached.get(i), player, MIN, ply+1, alpha, beta);
                
                if(currentValue > maxValue)
                    maxValue = currentValue;
                if(maxValue >= beta)    //if maximum exceeds beta stop
                    return maxValue;
                if(maxValue > alpha)    //if maximum exceeds alpha update alpha
                    alpha = maxValue;
            }
            return maxValue;            //return maximum value
        }
        else                        //if board is at min level
        {
            int minValue = Integer.MAX_VALUE;    
            LinkedList<MyBoard> boardsReached=getBoards(b,player);   //generate children of board
            
            for(int i = 0; i < boardsReached.size(); i++)
            {
                //find minimum of minmax values of children
                int currentValue =  boardValue(boardsReached.get(i), player, MAX, ply+1, alpha, beta);
                //minmax(boardsReached.get(i), player, MIN, ply+1, alpha, beta)
                
                if(currentValue < minValue)
                    minValue = currentValue;
                if(minValue <= alpha)   //if minimum is less than alpha stop
                    return minValue;
                if(minValue < beta)     //if minimum is less than beta update beta
                    beta = minValue;
            }
            
            return minValue;            //return minimum value
        }  
    }
    
    public static int boardEval(MyBoard b, String player)
    {
        //HashMap<int[], Integer> eval=new HashMap<int[], Integer>();
        int eval=0;
        int blackPoints=0;
        int whitePoints=0;
	HashMap<Integer, Integer> jumps;
		
        for(int i=1;i<=32;i++)
        {
            if(b.boardVal[i]==-1)
            {
                blackPoints=blackPoints+1;
		if(isCorner(i))
                    blackPoints=blackPoints+2;
                if(nextToCorner(i))
                    blackPoints=blackPoints+1;
                if(i>21)
                    blackPoints=blackPoints+1;
            }
            if(b.boardVal[i]==-2)
            {
                blackPoints=blackPoints+2;
		if(isCorner(i))
                    blackPoints=blackPoints+2;
                if(nextToCorner(i))
                    blackPoints=blackPoints+1;
            }
            if(b.boardVal[i]==1)
            {
                whitePoints=whitePoints+1;
		if(isCorner(i))
                    whitePoints=whitePoints+2;
                if(nextToCorner(i))
                    whitePoints=whitePoints+1;
                if(i<=12)
                    whitePoints=whitePoints+1;
            }
            if(b.boardVal[i]==2)
            {
                whitePoints=whitePoints+2;
		if(isCorner(i))
                    whitePoints=whitePoints+2;
                if(nextToCorner(i))
                    whitePoints=whitePoints+1;
            }
        }
        
        if("first".equals(player))
            eval=blackPoints-whitePoints;
        else
            eval=whitePoints-blackPoints;

        //System.out.println("----------- EVALUATION-----------"+eval);
        return eval;
    }
    public static boolean nextToCorner(int i)
    {
        if(i==6 || i==7 || i==8 || i==9 || i==16 || i==17 || i==24 || i==25 || i==26 || i==27)
            return true;
        else
            return false;
    }
    public static boolean isCorner(int i)
    {
      if(i==1 || i==2 || i==3 || i==4 || i==29 || i==30 || i==31 || i==32 || i==5 || i==13 || i==21 || i==12 || i==20 
           || i==28)
        return true;
      else
        return false;
    }
    public static String boardToString (int[] pieces) 
    {
        String result = "  ";
        for (int pos = 1; pos <= 32; pos++) 
        {
            if (pos % 4 == 1 && pos != 1) 
            {
                result += "\n";
                if (!isLeftRow(pos))
                {
                    result += "  ";
                }
            }
            String piece;
            switch (pieces[pos]) 
            {
                case BLACK_KING:
                    piece = "B ";
                    break;
                case BLACK_PAWN:
                    piece = "b ";
                    break;
                case NO_PIECE:
                    piece = Integer.toString(pos);
                    if (pos < 10) piece +=" ";  // All spaces should be two characters
                    break;
                case WHITE_PAWN:
                    piece = "w ";
                    break;
                case WHITE_KING:
                    piece = "W ";
                    break;
                default:
                    System.err.println("Illegal piece " + pieces[pos] + " at position " +
                        pos + "!!");
                    return "ERROR--BAD BOARD, Illegal piece " + pieces[pos] +
                        " at position " + pos + "!!";
            }
            result += piece+"  ";
        }
        return result;
    }
    
    public static boolean isLeftRow(int pos) 
    {
        return (pos - 1) % 8 > 3;
    }
}