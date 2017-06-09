import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
 
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
@ServerEndpoint("/echo") 
public class EchoServer
{
    
    private static final int MAX_PLAYERS = 4;
    private static Random xPosGen = new Random();
    private static Random yPosGen = new Random();
    private static List<Session> players = new ArrayList<>();
    private static int playersID = 0;
    
    @OnOpen
    public void onOpen(Session session) 
    {
        System.out.println(session.getId() + " has opened a connection");
        try 
        {
            SessionHandler.addSession(session);
            session.getBasicRemote().sendText("Connection Established"); 
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    } 

    @OnMessage
    public void onMessage(String message, Session session) 
    {
        handle(message, session);
    }
   
    private void handle(String csv, Session session) 
    {
        String[] protocol = csv.split(";"); 
        
        /* protocol:
            [0] = MainProtocol
            [1] = player
            [2] = player.x
            [3] = player.y             
        */    
        
        switch(protocol[0]) { 
                            
            case "0":   //start - oczekiwanie
                //przypisanie nowemu graczowi ID i pozycji
                int newX = xPosGen.nextInt(800);
                int newY = yPosGen.nextInt(600);  
                System.out.println("player got ID: " + playersID);
                session.getUserProperties().put("playerID", playersID); //zapisuje polozenie  gracza
                session.getUserProperties().put("xPos", newX); //zapisuje polozenie gracza
                session.getUserProperties().put("yPos", newY); //zapisuje polozenie gracza 
                    
                // wysłanie powyższego info do gracza
                session.getAsyncRemote().sendText(
                    makeUserResponse(
                        Protocol.START.header,
                        playersID + "",
                        newX + "",
                        newY + ""
                    )
                );
               
                players.add(session);
                playersID++;
                                
                if(players.size() >= MAX_PLAYERS) 
                {
                    //jesli jest czterech graczy, kazdy dostaje info o rozpoczęciu gry
                    for (int i = 0; i < players.size(); i++)
                    {
                        players.get(i).getAsyncRemote().sendText(Protocol.GAME.header);
                    }
                }
                break;
                
            case "1":  
                players.get(Integer.parseInt(protocol[1])).getUserProperties().put("xPos", protocol[2]);
                players.get(Integer.parseInt(protocol[1])).getUserProperties().put("yPos", protocol[3]);
               
                for (int i = 0; i < players.size(); i++)
                { 
                   session.getAsyncRemote().sendText(
                        makeUserResponse(
                            Protocol.GAME.header,
                            players.get(i).getUserProperties().get("playerID").toString(),
                            players.get(i).getUserProperties().get("xPos").toString(),
                            players.get(i).getUserProperties().get("yPos").toString()
                        )
                    );
                    
                    System.out.println(makeUserResponse(
                            Protocol.GAME.header,
                            players.get(i).getUserProperties().get("playerID").toString(),
                            players.get(i).getUserProperties().get("xPos").toString(),
                            players.get(i).getUserProperties().get("yPos").toString()
                        ));
                } 
                break;
        }
    }

    @OnClose
    public void onClose(Session session) 
    { 
        for (int i = 0; i < players.size(); i++)
        {
            players.remove(session);
        }
        SessionHandler.removeSession(session);
    }
    
    private String makeUserResponse(String proto, String ID, String xpos, String ypos)
    {
        return proto + ";" + ID + ";" + xpos + ";" + ypos;
    }
}

enum Protocol 
{
    START("0"), GAME("1"), END("2");
    
    public String header;
    
    Protocol(String protocolHeader)
    {
        header = protocolHeader;
    }
}