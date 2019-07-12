/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myBot;
import java.util.*;
/**
 *
 * @author Huda
 */
public class MyBoard {
    
    public int[] boardVal=new int[32];
    public int[] moves=new int[2];
    public ArrayList<Integer> seq;
    
    public MyBoard(int[] boardVal)
    {
        this.boardVal=Arrays.copyOf(boardVal,33);
        
    }
    public void setSeq(ArrayList<Integer> seq)
    {
      this.seq=new ArrayList<>(seq);
    }
    public ArrayList<Integer> getSeq()
    {
      return seq;
    }
}
