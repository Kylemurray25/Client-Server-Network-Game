import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.security.KeyStore;
import java.io.InputStream;
import java.io.FileInputStream;

public class ScoreBoardClient implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private ScoreboardServer master;
    private String userName;

    /**
     * Constructor
     * @param in Input stream
     * @param out Output stream
     */
    public ScoreBoardClient(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Register chat server handler for callbacks
     */
    protected void registerCallback(ScoreboardServer c) {
        this.master = c;
    }

    /**
     * Display text to client
     * @param text Text to be displayed
     */
    protected void send(String text) {
        out.printf("%s\r\n", text);
    }

    /**
     * Display score to client
     * @param text Text to be displayed
     */
    protected void printScore(String score) {
        out.printf("%s\r\n", score);
    }

    /**
     * Display games to client
     * @param games Array of games available to be displayed
     */
    protected void printGames(String[] games) {
        for(int i = 0; i < games.length; i++){
            out.printf("%s\r\n", games[i]);
        }
    }

    /**
     * Display qs to client
     * @param qs Array of qs to be displayed
     */
    protected void printQs(String[] qs) {
        for(int i = 0; i < qs.length; i++){
            out.printf("%s\r\n", qs[i]);
        }
    }

    @Override
    /**
     * Thread logic
     */
    public void run() {
        /* WRITE ME: read line-by-line. After each line read, callback to
         * server app to send input to all.
         *
         * OPTIONAL: the word QUIT will disconnect a client.
         */ 
        boolean quit = true;
        boolean join = true;
        try{
            //start up
            this.send("Please enter your username:");                
            userName = in.readLine();

            //join block
            while(join){
                this.send("Please enter the name of the game you want to join:");
                master.whatGames();
                String line = in.readLine();
                
                if(master.sendChoice(line, userName) == true){//sends choice to master telling it which game client wants
                    this.send("Game joined successfully!");
                    join = false;
                }
                
                else{
                    this.send("Error joining game, please try again.");
                }
            }

            //quit block
            //main loop where client can quit out of after they join a game
            while(quit){
                String line = in.readLine();
                if(line.equals("QUIT")){
                    in.close();
                    out.close();
                    master.leave(this);
                    quit = false;
                    continue;
                }

                else if(line.equals("SHOW_SCORE")){
                    master.sendScore(userName);
                }

                else if(line.equals("LIST_QS")){
                    master.printQs(userName);//question array
                }

                else if(line.equals("SEND_A")){
                    this.send("Please enter the question id you want to answer.");
                    String q = in.readLine();

                    this.send("Please enter the answer you would like to submit.");
                    String a = in.readLine();

                    master.sendQA(q, a, userName);//sends q num and answer to master 
                }
                else//catch
                {
                    this.send("Not a valid option");
                    this.send("QUIT to quit.");
                    this.send("SHOW_SCORE to show score.");
                    this.send("LIST_QS to list the questions.");
                    this.send("SEND_A to answer a question.");
                }
            }
        }
        catch(Exception e){
            System.err.println("client error");
        }
    }
}