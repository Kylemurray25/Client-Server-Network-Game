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
import java.io.Reader;
import java.io.InputStream;
import java.io.FileInputStream;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import java.security.SecureRandom;

import java.util.HashMap;
import java.util.Iterator;
/**
 * A multi-client simple chat server that will accept incoming connections, and which
 * will broadcast anything received from one client to all other clients.
 */
public class ScoreboardServer {
    private ArrayList<ScoreBoardClient> handlers = new ArrayList<ScoreBoardClient>();
    public ArrayList<ChallengeResponseGame> games = new ArrayList<ChallengeResponseGame>();

    public ScoreboardServer(ArrayList<ChallengeResponseGame> g){
        for(int i = 0; i < g.size(); i++){
            games.add(g.get(i));
        }
    }

    /**
     * Start the server and handle incoming connections
     * @param port TCP port to listen on
     */
    public void startServer(int port, String key, String pass) {
        // create new socket server

        BufferedReader in = null;
        PrintWriter out = null;
        DataInputStream is = null;
        DataOutputStream os = null;
        Socket cli = null;
        SSLServerSocket server = null;
        try
        {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksIs = new FileInputStream(key);
            ks.load(ksIs, pass.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pass.toCharArray());

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(kmf.getKeyManagers(), null, new SecureRandom());

            server = (SSLServerSocket)
            sc.getServerSocketFactory().createServerSocket(port);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        while (true) {
            // WRITE ME: accept connection, extract streams and start thread

            try 
            {
                cli = server.accept();
                is = new DataInputStream(cli.getInputStream());
                os = new DataOutputStream(cli.getOutputStream());
            }
            catch(IOException e)
            {
                System.err.println(e);
            }
            Reader reader = new InputStreamReader(is);
            in = new BufferedReader(reader);
            out = new PrintWriter(os,true);
            ScoreBoardClient client = new ScoreBoardClient(in, out);
            Thread t = new Thread(client);
            t.start();

            // register callback
            client.registerCallback(this);
            handlers.add(client);

        }
    }

    /** 
     * Send chat message to all clients, except originator
     * @param text Text to send
     * @param except Client to exclude
     */
    public void sendToAll(String text) {
        // WRITE ME: send message to all channels, except to sender
        for(ScoreBoardClient e: handlers)
        {
            e.send(text);
        }
    }

    public void whatGames()
    {
        for(ChallengeResponseGame game:games)
        {
            sendToAll(game.getId());
        }
    }

    /**
     * Called when a client disconnects.
     * @param c Disconnecting client
     */
    public void leave(ScoreBoardClient c) {
        int i = handlers.indexOf(c);
        synchronized(System.out) {
            if (i != -1) {
                System.out.print("Client disconnected. ");
                handlers.remove(c);
            }
            System.out.println(handlers.size()+" clients remaining.");
        }
    }

    public boolean sendChoice(String c, String un){
        for(int i = 0; i < games.size(); i++){
            if(games.get(i).equals(c) == true){
                games.get(i).addPlayer(un);
                return true;
            }
        }
        return false;
    }

    public void sendScore(String un){
        HashMap<String, Integer> s;
        for(ChallengeResponseGame game : games){
            s = game.getScores();
            if(s.containsKey(un) == true){
                Iterator<String> itr = s.keySet().iterator();
                while(itr.hasNext()){
                    sendToAll(un + " " + s.get(itr.next()));
                }
            }
        }
    }

    public void printQs(String un){
        HashMap<String, Integer> s;
        for(ChallengeResponseGame game : games){
            s = game.getScores();
            if(s.containsKey(un) == true){
                ArrayList<Question> qs = game.getQuestions();
                for(Question q : qs){
                    sendToAll("ID: " + q.getId() + ", Question: " + q.getQuestion() + ", Points Worth: " + q.getPoints());
                }
            }
        }
    }

    public void sendQA(String q, String a, String un){
        HashMap<String, Integer> s;
        for(ChallengeResponseGame game : games){
            s = game.getScores();
            if(s.containsKey(un) == true){
                ArrayList<Question> qs = game.getQuestions();
                for(Question quest : qs){
                    if(quest.equals(q)){
                        boolean result = quest.answer(un, a);
                        if(result == false){
                            System.out.print("Error, wrong answer.");
                        }

                        if(result == true){
                            System.out.print("Correct answer");
                        }
                    }
                }
            }
        }
    }
}
