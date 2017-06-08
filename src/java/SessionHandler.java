import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
 
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
/**
 *
 * @author student_10
 */
public class SessionHandler {
    private static final Set<Session> sessions = new HashSet<>();
    static Map<String, Session> openSessions;
   
    public static void addSession(Session session){
        sessions.add(session);
    }
   
    public static void removeSession(Session session){
        sessions.remove(session);
    }
   
    public static void sendToSession(Session session, String message){
       try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            sessions.remove(session);
            //Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    public static void sendToallConnectedSessions(String message){
         for (Session session : sessions) {
            sendToSession(session, message);
        }
    }
   
    public static void sendToallConnectedSessionsInRoom(String roomID, String message){
         for (Session session : sessions) {
            sendToSession(session, message);
        }
    }
}