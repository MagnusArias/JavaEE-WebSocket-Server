import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
 
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
 
@ServerEndpoint("/echo") 
public class EchoServer {
    
    private static final int MAX_PLAYERS = 2;
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println(session.getId() + " has opened a connection");
        try {
            SessionHandler.addSession(session);
            
            session.getBasicRemote().sendText("Connection Established"); 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        handle(message, session);

        /*
        if(message.equals("jasiek")) {
            message = "kotek";
            SessionHandler.sendToAllConnectedSessionsInRoom(room, message);
        }
*/
    }
    private static Random xPosGen = new Random();
    private static Random yPosGen = new Random();
    private static List<Session> players = new ArrayList<>();
    private int playersID = 0;
    private void handle(String csv, Session session) {
        String[] protocol = csv.split(":");
        /* protocol:
            [0] = MainProtocol
            [1] = player
            [2] = player.x
            [3] = player.y
            [4] = player.rot
             
        */    
        
        switch(protocol[0]) { 
                            
            case "0":   //start - oczekiwanie
                    int newX = xPosGen.nextInt(800);
                    int newY = yPosGen.nextInt(600);  
                    System.out.println("player got ID: " + playersID);
                    session.getUserProperties().put("playerID", playersID); //zapisuje polozenie  gracza
                    session.getUserProperties().put("xPos", newX); //zapisuje polozenie gracza
                    session.getUserProperties().put("yPos", newY); //zapisuje polozenie gracza
                    session.getUserProperties().put("rot", 0); //zapisuje polozenie gracza
                    
                    //System.out.println("Response for player " + playersID + ": " + makeUserResponse(Protocol.START.header, playersID.toString(), newX.toString(), newY.toString(), 0));
                    //informuje klienta ktorym jest graczem
                    session.getAsyncRemote().sendText(
                                makeUserResponse(
                                            Protocol.START.header, 
                                            playersID + "", 
                                            newX+"", 
                                            newY+"", 
                                            0+""));
                    
                    players.add(session); 
                    playersID += 1; 
                      
                    if(players.size() >= MAX_PLAYERS) {
                        //jesli jest czterech przeciwnikow, kazdy dostaje info
                        for (int i = 0; i < players.size(); i++){
                            players.get(i).getAsyncRemote().sendText(Protocol.GAME.header);  
                        }
                        //session.getAsyncRemote().sendText(Protocol.GAME.header);
                    }  
                    break;                  
            case "1":
               
               players.get(Integer.parseInt(protocol[1])).getUserProperties().replace("xPos", protocol[2]);
               players.get(Integer.parseInt(protocol[1])).getUserProperties().replace("yPos", protocol[3]);
               players.get(Integer.parseInt(protocol[1])).getUserProperties().replace("rot", protocol[4]);
              /* System.out.println("Got it from user: " + makeUserResponse(   Protocol.GAME.header, 
                                                players.get(Integer.parseInt(protocol[1])).getUserProperties().get("playerID").toString(),
                                                players.get(Integer.parseInt(protocol[1])).getUserProperties().get("xPos").toString(),
                                                players.get(Integer.parseInt(protocol[1])).getUserProperties().get("yPos").toString(),
                                                players.get(Integer.parseInt(protocol[1])).getUserProperties().get("rot").toString()
                            ));*/
               
               for (int i = 0; i < players.size(); i++){
                    players.get(i).getAsyncRemote().sendText(
                            makeUserResponse(   Protocol.GAME.header, 
                                                players.get(i).getUserProperties().get("playerID").toString(),
                                                players.get(i).getUserProperties().get("xPos").toString(),
                                                players.get(i).getUserProperties().get("yPos").toString(),
                                                players.get(i).getUserProperties().get("rot").toString()
                            ) 
                    ); 
               } 
   
        }
        System.out.println(csv);
    }

    @OnClose
    public void onClose(Session session) { 
        SessionHandler.removeSession(session);
        
        //System.out.println("Session " + session.getId() + " has ended");
    }
    
    private String makeUserResponse(String proto, String ID, String xpos, String ypos, String rotation){
        return proto + ":" + ID + ":" + xpos + ":" + ypos + ":" + rotation;
    }
}

enum Protocol {
    START("0"), GAME("1"), END("2");
    
    public String header;
    
    Protocol(String protocolHeader) {
        header = protocolHeader;
    }
}