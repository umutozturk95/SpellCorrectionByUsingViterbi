/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Umut Ozturk
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // the numbers of arguments are checked whether they equal to the correct number(2) .
       
        if(args.length!=2){
           
           System.out.println("Invalid argument!");
           return;
       }

        //The spelling correction is starting by using args arguments.
        SpellCorrection sc=new SpellCorrection();
        sc.startSpellCorrection(args[0],args[1]);
         
    }
    
}
