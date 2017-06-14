import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
    private static List projectiles = new ArrayList();
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
   
    private static  boolean gameStarted = false;
    private void handle(String csv, Session session) 
    {
        String[] protocol = csv.split(";"); 
        String response = "";
        /* protocol:
            [0] = MainProtocol
            [1] = player
            [2] = player.x
            [3] = player.y             
        */    
        
        switch(protocol[0]) { 
                            
            case "0":   //start - oczekiwanie
                if (!gameStarted){
                    response = String.format("%s;%s", 
                                Protocol.START.header,
                                addPlayer(session));
                    sendToPlayer(session, response);             
                    if(isFull()) {
                            gameStarted = true;
                            startGame();
                        }
                }
                break;
                
            // informowanie kazdego gracza o pozycji innych graczy
            case "1":  
                response = String.format("%s;%s;%s;%s",
                            Protocol.MOVE.header,
                            protocol[1],
                            protocol[2],
                            protocol[3]);
                    
                    sendToAllPlayers(response);
                break; 
                
            case "2":
               response = String.format("%s;%s;%s;%s",
                            Protocol.HIT.header,
                            protocol[1],
                            protocol[2], 
                            protocol[3]); 
                    
                    sendToAllPlayers(response); 
                break; 
                
            case "3":  
                response = String.format("%s;%s",
                            Protocol.POINT.header,
                            protocol[1]);
                    System.out.println("Gracz " + session.getId() + " o indexie " + session.getUserProperties().get("index") + " zdobyl punkt");
                    sendToAllPlayers(response);
                break;  
                  
            case "4": 
                response = String.format("%s", Protocol.END.header);
                
                sendToAllPlayers(response);
                 
                gameStarted = false;
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
        
    private void sendToPlayer(Session session, String response) {
       session.getAsyncRemote().sendText(response);
    }
    
    private void startGame() {
        sendToAllPlayers(Protocol.START.header);
    }
    private static int HOW_MANY_PLAYERS_IN_ROOM = 4;
    
    private boolean isFull() {
        return players.size() == HOW_MANY_PLAYERS_IN_ROOM;
    }
     private void sendToAllPlayers(String request) {
        Iterator<Session> iter = players.iterator();
        while (iter.hasNext()) {
            Session str = iter.next();
            
            str.getAsyncRemote().sendText(request);
        }
        /*
        for(Session player: players) {
            player.getAsyncRemote().sendText(request);
        }
*/
    }
     
     
      private int addPlayer(Session player) {
        int index = firstFreeIndex();
        player.getUserProperties().put("index", index);
         
        players.add(player);
        
        System.out.println("Player " + player.getId() + " have index: " + index);
        
        return index;//players.indexOf(player);
    }

    private int firstFreeIndex() {
        List<Integer> availableIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        for(Session p: players) {
            int otherIndex = (int) p.getUserProperties().get("index");
            if(availableIndexes.contains(otherIndex)) {
                System.out.println("Zawiera: " + otherIndex);
                availableIndexes.remove(new Integer(otherIndex));
                for(Integer i : availableIndexes) {
                    System.out.println("Zostaly - " + i);
                }
            }
        }
        return !availableIndexes.isEmpty() ? availableIndexes.get(0) : -1;
    }
}  

enum Protocol 
{
    START("0"), MOVE("1"), HIT("2"), POINT("3"), END("4");
    
    public String header;
    
    Protocol(String protocolHeader)
    {
        header = protocolHeader;
    } 
}