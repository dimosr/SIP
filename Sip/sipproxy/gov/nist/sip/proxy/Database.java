/*
 * 
 * 	Raptis Dimos - Dimitrios (dimosrap@yahoo.gr) - 03109770
 *  Lazos Philippos (plazos@gmail.com) - 03109082
 * 	Omada 29
 * 
 */

package gov.nist.sip.proxy;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

public class Database {
    
private static String driver;
private static String url;
private static String user ;
private static String password ;
private static Connection conn ;

    Database(){
        
        driver = "com.mysql.jdbc.Driver";
        url = "jdbc:mysql://localhost:3306/proxy";
        user = "root";
        password = "";
        conn = null;
        
    }

    public static Connection getConnection() {
        try {
            if (conn == null) {
                Class.forName(driver);
                Properties connectionProps = new Properties();
                connectionProps.put("user", user);
                connectionProps.put("password", password);
                conn = DriverManager.getConnection(url, connectionProps);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    
    public boolean userExists(String username){

        String selectQuery = "SELECT * FROM  users WHERE  name =?";
        int rowsCount = 0;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next())
            {
                rowsCount = rowsCount + 1;
            }
            if(rowsCount > 0){
                ProxyDebug.println("There are " + rowsCount + "users with the given username,password. ");
                return true ;
            }
            else{
                ProxyDebug.println("There are no users with the given username,password. ");
                return false ;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean InsertUser(String username, String passwd, String address, String email){

        String insertQuery = "INSERT INTO users (user_id, name, password, address, email) VALUES (null, ?, ?, ?, ?) ";

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, passwd);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, email);
            preparedStatement.executeUpdate();

            return true ; 

        }
        catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean DeleteUser(String username){

        String deleteQuery = "DELETE FROM users WHERE name = ?";

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(deleteQuery);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

            return true ; 

        }
        catch (SQLException e) {
            e.printStackTrace();
            
            return false;
        }
    }



    public boolean UserIsBlockedBy(String callee_username, String caller_username){

        String selectQuery = "SELECT user_id FROM users WHERE name = ?";
        ResultSet rs;
        int u_id1,u_id2;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, caller_username);
            rs = preparedStatement.executeQuery();
            rs.next();
            u_id1 = rs.getInt("user_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The caller is not registered in the database!!");
            return false;
        }

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, callee_username);
            rs = preparedStatement.executeQuery();
            rs.next();
            u_id2 = rs.getInt("user_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The callee is not registered in the database!!");
            return false;
        }

        selectQuery = "SELECT * FROM blocks WHERE blocker = ? AND blockee = ?";
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, u_id1);
            preparedStatement.setInt(2, u_id2);
            rs = preparedStatement.executeQuery();
            
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an SQL Exception!!");
            return false;
        }

        int rowsCount = 0;
        try {
            while( rs.next() )
            {
                rowsCount = rowsCount + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(rowsCount > 0){
            System.out.println("The " + callee_username + " is blocked by " + caller_username + "!!!!");
            return true ;
        }
        else{
            System.out.println("The " + callee_username + " is not blocked by " + caller_username + "!!!!");
            return false;
        }
    }

    public boolean userForwards(String forwarder_username){
        String selectQuery = "SELECT user_id FROM users WHERE name = ?";
        ResultSet rs;
        int u_id1,u_id2;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, forwarder_username);
            rs = preparedStatement.executeQuery();
            rs.next();
            u_id1 = rs.getInt("user_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The user is not registered in the databaase!!");
            return false;
        }

        selectQuery = "SELECT * FROM forwards WHERE forwarder = ?";
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, u_id1);
            rs = preparedStatement.executeQuery();
            
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an SQL Exception!!");
            return false;
        }

        int rowsCount = 0;
        try {
            while( rs.next() )
            {
                rowsCount = rowsCount + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(rowsCount > 0){
            System.out.println("The user " + forwarder_username + " has chosen to forward the calls !!!!");
            return true ;
        }
        else{
            System.out.println("The user " + forwarder_username + " has not chosen to forward the calls !!!!");
            return false;
        }

    }

    public String ForwardsTo(String forwarder_username){
        String selectQuery = "SELECT user_id FROM users WHERE name = ?";
        ResultSet rs;
        int forwarder_id, forwardee_id;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, forwarder_username);
            rs = preparedStatement.executeQuery();
            rs.next();
            forwarder_id = rs.getInt("user_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The user is not registered in the databaase!!");
            return null;
        }
        
        selectQuery = "SELECT forwardee FROM forwards WHERE forwarder = ?";
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, forwarder_id);
            rs = preparedStatement.executeQuery();
            rs.next();
            forwardee_id = rs.getInt("forwardee");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The user is not forwarding to anyone!!");
            return null;
        }
        
        selectQuery = "SELECT name FROM users WHERE user_id = ?";
        String forwardee_username;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, forwardee_id);
            rs = preparedStatement.executeQuery();
            rs.next();
            forwardee_username = rs.getString("name");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The forwardee is not registered in the databaase!!");
            return null;
        }
        
        return forwardee_username;
    }
    
    public int getUserID(String username){
        String selectQuery = "SELECT user_id FROM users WHERE name = ?";
        ResultSet rs;
        int u_id;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            rs = preparedStatement.executeQuery();
            rs.next();
            u_id = rs.getInt("user_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an SQL Exception!!");
            return -1;
        }
        return u_id;
    }
    
    public String getUserPassword(String username){
        String selectQuery = "SELECT password FROM users WHERE name = ?";
        ResultSet rs;
        String password;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            rs = preparedStatement.executeQuery();
            rs.next();
            password = rs.getString("password");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an SQL Exception!!");
            return null;
        }
        return password;
    }
    
    public boolean createFriendship(String friender_username, String friendee_username){
    	boolean flag1,flag2;
    	flag1 = insertFriendPair(friender_username, friendee_username);
    	flag2 = insertFriendPair(friendee_username, friender_username);
    	
    	if( ( flag1 == false ) || ( flag2 == false)){
    		System.out.println("Something was wrong and the friendship was not created!");
    		return false;
    	}
    	else{
    		return true;
    	}
    }
    
    public boolean deleteFriendship(String friender_username, String friendee_username){
    	boolean flag1,flag2;
    	flag1 = removeFriendPair(friender_username, friendee_username);
    	flag2 = removeFriendPair(friendee_username, friender_username);
    	
    	if( ( flag1 == false ) || ( flag2 == false)){
    		System.out.println("Something was wrong and the friendship was not created!");
    		return false;
    	}
    	else{
    		return true;
    	}
    }
    
    public boolean insertFriendPair(String friender_username, String friendee_username){
        int friender_ID,friendee_ID;

        friender_ID = getUserID(friender_username);
        friendee_ID = getUserID(friendee_username);
        if(friender_ID == -1){
            System.out.println("Friends pair could not be inserted in the database. Friender is not registered!");
            return false;
        }
        else if(friendee_ID == -1 ){
            System.out.println("Friend pair could not be inserted in the database. Friendee is not registered!");
            return false;
        }
        else{
            String insertQuery = "INSERT INTO friends (friender, friendee) VALUES (?, ?) ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, friender_ID);
                preparedStatement.setInt(2, friendee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public boolean removeFriendPair(String friender_username, String friendee_username){
        int friender_ID,friendee_ID;

        friender_ID = getUserID(friender_username);
        friendee_ID = getUserID(friendee_username);
        if(friender_ID == -1){
            System.out.println("Friend pair could not be inserted in the database. Friender is not registered!");
            return false;
        }
        else if(friendee_ID == -1 ){
            System.out.println("Friend pair could not be inserted in the database. Friendee is not registered!");
            return false;
        }
        else{
            String insertQuery = "DELETE FROM friends WHERE friender = ? AND friendee = ? ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, friender_ID);
                preparedStatement.setInt(2, friendee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean insertBlockingPair(String blocker_username, String blockee_username){
        int blocker_ID,blockee_ID;

        blocker_ID = getUserID(blocker_username);
        blockee_ID = getUserID(blockee_username);
        if(blocker_ID == -1){
            System.out.println("Blocking pair could not be inserted in the database. Blocker is not registered!");
            return false;
        }
        else if(blockee_ID == -1 ){
            System.out.println("Blocking pair could not be inserted in the database. Blockee is not registered!");
            return false;
        }
        else{
            String insertQuery = "INSERT INTO blocks (blocker, blockee) VALUES (?, ?) ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, blocker_ID);
                preparedStatement.setInt(2, blockee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public boolean removeBlockingPair(String blocker_username, String blockee_username){
        int blocker_ID,blockee_ID;

        blocker_ID = getUserID(blocker_username);
        blockee_ID = getUserID(blockee_username);
        if(blocker_ID == -1){
            System.out.println("Blocking pair could not be inserted in the database. Blocker is not registered!");
            return false;
        }
        else if(blockee_ID == -1 ){
            System.out.println("Blocking pair could not be inserted in the database. Blockee is not registered!");
            return false;
        }
        else{
            String insertQuery = "DELETE FROM blocks WHERE blocker = ? AND blockee = ? ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, blocker_ID);
                preparedStatement.setInt(2, blockee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public boolean removeForwardingPairs(String forwarder_username){
        int forwarder_ID;

        forwarder_ID = getUserID(forwarder_username);
        if(forwarder_ID == -1){
            System.out.println("There is no user with the name of the forwardee in the database");
            return false;
        }
        else{
            String deleteQuery = "DELETE FROM forwards WHERE forwarder = ? ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, forwarder_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    

    public boolean insertForwardingPair(String forwarder_username, String forwardee_username){
        int forwarder_ID,forwardee_ID;

        forwarder_ID = getUserID(forwarder_username);
        forwardee_ID = getUserID(forwardee_username);
        if(forwarder_ID == -1){
            System.out.println("Forwarding pair could not be inserted in the database. Forwarder is not registered!");
            return false;
        }
        else if(forwardee_ID == -1 ){
            System.out.println("Forwarding pair could not be inserted in the database. Forwardee is not registered!");
            return false;
        }
        else{
            String insertQuery = "INSERT INTO forwards (forwarder, forwardee) VALUES (?, ?) ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, forwarder_ID);
                preparedStatement.setInt(2, forwardee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public boolean insertCall(String call_id, String caller_username, String callee_username, long start_timestamp ){
        int caller_ID,callee_ID;

        caller_ID = getUserID(caller_username);
        callee_ID = getUserID(callee_username);
        if(caller_ID == -1){
            System.out.println("Call could not be inserted in the database. Caller is not registered!");
            return false;
        }
        else if(callee_ID == -1 ){
            System.out.println("Call could not be inserted in the database. Callee is not registered!");
            return false;
        }
        else{
            String insertQuery = "INSERT INTO callhistory (caller, callee, start, duration, cost, call_id) VALUES (?, ?, ?, null, null, ?) ";

            try {
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, caller_ID);
                preparedStatement.setInt(2, callee_ID);
                preparedStatement.setLong(3, start_timestamp);
                preparedStatement.setString(4, call_id);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    public boolean updateFinishedCall(String call_id, String caller_username, String callee_username, long end_timestamp, long fixed_cost_per_sec){
        int caller_ID,callee_ID;
        long start_timestamp;

        caller_ID = getUserID(caller_username);
        callee_ID = getUserID(callee_username);
        if(caller_ID == -1){
            System.out.println("Call could not be inserted in the database. Caller is not registered!");
            return false;
        }
        else if(callee_ID == -1 ){
            System.out.println("Call could not be inserted in the database. Callee is not registered!");
            return false;
        }
        else{
            String updateQuery = "UPDATE callhistory SET duration = ?, cost = ? where caller = ? AND callee = ? AND duration IS NULL";

            try {
                
                String selectQuery = "SELECT start FROM callhistory WHERE caller = ? AND callee = ? AND duration IS NULL";
                ResultSet rs;
                try {
                    PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
                    preparedStatement.setInt(1, caller_ID);
                    preparedStatement.setInt(2, callee_ID);
                    rs = preparedStatement.executeQuery();
                    if( !rs.next() ){
                    	return false;
                    }
                    start_timestamp = rs.getLong("start");
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("The call is not registered in the database!!");
                    return false;
                }
                
                long duration =  end_timestamp - start_timestamp;
                long cost;
                if( duration < 300 ){			//calls smaller than 5 minutes
                	cost = duration*fixed_cost_per_sec;
                }
                else{
                	cost = duration*(fixed_cost_per_sec + 2); 
                }
                
                PreparedStatement preparedStatement = this.getConnection().prepareStatement(updateQuery);
                preparedStatement.setLong(1, duration);
                preparedStatement.setLong(2, cost);
                preparedStatement.setLong(3, caller_ID);
                preparedStatement.setLong(4, callee_ID);
                preparedStatement.executeUpdate();
                return true ; 
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
    }
    
    public String getTheLastForwardeeFrom(String first_forwarder_username, String caller){
        boolean flag = userForwards(first_forwarder_username);
        String next_step,forwarder_temporary;
        HashSet<String> passed_users = new HashSet<String>();
        passed_users.add(first_forwarder_username);
        passed_users.add(caller);
        
        if( flag == true){
            forwarder_temporary = first_forwarder_username;
            while( userForwards(forwarder_temporary) == true ){
                next_step = ForwardsTo(forwarder_temporary);
                if( passed_users.contains(next_step) ){
                	return null;
                    //break;
                }
                else{
                    forwarder_temporary = next_step;
                    passed_users.add(forwarder_temporary);
                }
            }
            return forwarder_temporary;
        }
        else
            return first_forwarder_username;
    }
    
    public boolean existsCall(String call_id){
    	String selectQuery = "SELECT * FROM  callhistory WHERE  call_id =? ";
        int rowsCount = 0;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, call_id);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next())
            {
                rowsCount = rowsCount + 1;
            }
            if(rowsCount == 0){
                ProxyDebug.println("There are " + rowsCount + "open calls with call_id :" + call_id);
                return false ;
            }
            else if(rowsCount == 1){
                ProxyDebug.println("There is 1 open call with call_id : " + call_id);
                return true ;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
		return false;
    }
    
    public String getUserName(int user_id){
        String selectQuery = "SELECT name FROM users WHERE user_id = ?";
        ResultSet rs;
        String username;

        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, user_id);
            rs = preparedStatement.executeQuery();
            rs.next();
            username = rs.getString("name");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an SQL Exception!!");
            return null;
        }
        return username;
    }
    
    public String[] getAllBlockedUsers(String blocker_username){
    	int blocker_ID = getUserID(blocker_username);
    	int user_id;
    	
    	String selectQuery = "SELECT * FROM  blocks WHERE  blocker =? ";
    	String[] blocked_usernames;
    	ArrayList<String> temporary_names = new ArrayList<String>();
        int blocked_count = 0;
        
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, blocker_ID);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next())
            {
                blocked_count = blocked_count + 1;
                user_id = rs.getInt("blockee");   
                temporary_names.add( getUserName(user_id) );
            }
            blocked_usernames = new String[blocked_count];
            for(int i=0;i < blocked_count;i++){
            	blocked_usernames[i] = temporary_names.get(i);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return blocked_usernames;
    }

    public String getTotalCost(String caller_username){
        int caller_id = getUserID(caller_username);
        int cost;
        String final_cost;
        
        String selectQuery = "SELECT SUM(cost) as total_cost FROM  callhistory WHERE  caller =? ";
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, caller_id);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();

            cost = rs.getInt("total_cost");
            
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        if( cost == 0 ){
            return "none";
        }
        else{
            int euros = cost / 100;
            int cents = cost % 100;
            final_cost = Integer.toString(euros) + " Euros , " + Integer.toString(cents) + "cents";
            return final_cost;
        }
        
    }
    
    public String[] getAllFriends(String friender_username){
    	int friender_ID = getUserID(friender_username);
    	int user_id;
    	
    	String selectQuery = "SELECT * FROM  friends WHERE  friender =? ";
    	String[] friends_usernames;
    	ArrayList<String> temporary_names = new ArrayList<String>();
        int friends_count = 0;
        
        try {
            PreparedStatement preparedStatement = this.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, friender_ID);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next())
            {
            	friends_count = friends_count + 1;
                user_id = rs.getInt("friendee");   
                temporary_names.add( getUserName(user_id) );
            }
            friends_usernames = new String[friends_count];
            for(int i=0;i < friends_count;i++){
            	friends_usernames[i] = temporary_names.get(i);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return friends_usernames;
    }
    
    public static void main(String args[]){
        
        Database    test = new Database();
        String username = new String("dimos");
        String passwd = new String("dimos_password");
        String address = new String("athens");
        String email = new String("dimosrap@yahoo.gr");
        boolean query_flag;
        
        System.out.println("Main function is beginning!!");
        
        test.getConnection();
        
        System.out.println("Connection with the database done!!");
        
        test.createFriendship("jimmy","plazos");
        System.out.println("Friendship was created between jimmy and plazos");
              
    }
    
    
    
}
