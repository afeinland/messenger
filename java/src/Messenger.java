/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		// System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   public int getNextSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select nextval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         System.out.println(user);
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("6. *DEV* Auto log in Norma");
            System.out.println("0. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 0: keepon = false; break;
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 6: authorisedUser = LogInNorma(esql); break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.format("\nWelcome %s!\nMAIN MENU\n", authorisedUser);
                System.out.println("---------");
                System.out.println("1. View contacts");
                System.out.println("2. Add contact");
                System.out.println("3. Remove contact");
                System.out.println("4. View blocked users");
                System.out.println("5. Block user");
                System.out.println("6. Unblock user");
				System.out.println("7. Chat Menu");
                System.out.println("8. Write a new message");
                System.out.println("9. Delete your account");
                System.out.println(".........................");
                System.out.println("0. Log out");
                switch (readChoice()){
                   case 0:
                     usermenu = false;
                     break;
                   case 1:
                     ListContacts(esql, authorisedUser);
                     break;
                   case 2:
                     AddToContact(esql, authorisedUser);
                     break;
                   case 3:
                     DeleteFromContact(esql, authorisedUser);
                     break;
                   case 4:
                     ListBlocked(esql, authorisedUser);
                     break;
                   case 5:
                     AddToBlocked(esql, authorisedUser);
                     break;
                   case 6:
                     DeleteFromBlocked(esql, authorisedUser);
                     break;
				   case 7:
                     ChatMenu(esql,authorisedUser);
                     break;
                   case 8:
                     NewMessage(esql);
                     break;
                   case 9:
                      if(DeleteUser(esql, authorisedUser) > 0){
                         usermenu = false;
                      }
                      break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

	 //Creating empty contact\block lists for a user
	 esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
	 int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
	 int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
	 String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /* Checks if the user is the owner of any chats, or TBD. */
   public static boolean linkedInfo(Messenger esql, String user){
      try{
          String query = String.format("SELECT * FROM chat WHERE init_sender='%s'", user);
          int hasChat = esql.executeQuery(query);
          return hasChat > 0;
      }catch (Exception e){
         // ignored.
      }
      return false;
   }// end
   
   public static int DeleteUser(Messenger esql, String user){    
      int userDeleted = 0;
      try{
         System.out.print("Are you sure you want to delete your account? (Y/N): ");
         String answer = in.readLine();
         // TODO Require user to enter password.
         System.out.println(answer);
         switch (answer) {
            case "Y":
               if (linkedInfo(esql, user)) {
                  System.out.println("Your account is the owner of a chat and cannot be deleted.");
                  userDeleted = 0; // User cannot be deleted.
                  break;
               }
               String query = String.format("DELETE FROM usr WHERE login='%s'", user);
               esql.executeUpdate(query);
               System.out.println("Your account was deleted.");
               userDeleted = 1;
               break;
            case "N":
               userDeleted = 0; // User not deleted.
               break;
            default:
               System.out.println("Invalid option. Your account will not be deleted.");
               userDeleted = 0; // User not deleted.
               break;
         }
      }catch (Exception e){
         // ignored.
      }// end try
      return userDeleted; // User successfully deleted.
   }// end DeletUser

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
        return login;
     }
         System.out.format("User '%s' not found\n\n", login);
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   /* So I don't have to type in a login each time I run the program. HACK*/
   public static String LogInNorma(Messenger esql){
      try{
         String login = new String("Norma");
         String password = new String("8c0bb848dc6691e9e8580f1b5eff110880d3");
         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0){
            return login;
         }
        return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void AddToContact(Messenger esql, String user){
      try{
         System.out.print("\tEnter user to add: ");
         String userToAdd = in.readLine();
         String query = String.format("INSERT INTO user_list_contains VALUES ((SELECT contact_list FROM usr WHERE login='%s'), '%s')", user, userToAdd);
         esql.executeUpdate(query);
         System.out.format("Successfully added %s to your contacts\n", userToAdd);
      }catch(Exception e){
         //ignore.
      }
   }//end

   public static void DeleteFromContact(Messenger esql, String user){
      try{
         System.out.print("\tEnter user to remove: ");
         String userToDelete = in.readLine();
         String query = String.format("DELETE FROM user_list_contains WHERE list_id=(SELECT contact_list FROM usr WHERE login='%s') AND list_member='%s'", user, userToDelete);
         esql.executeUpdate(query);
         System.out.format("Successfully removed %s from your contacts\n", userToDelete);
      }catch(Exception e){
         //ignore.
      }
   }//end

   public static void ListContacts(Messenger esql, String user){
      System.out.println("\nYour Contacts:");
      String query = String.format("SELECT list_member FROM user_list_contains WHERE list_id=(SELECT contact_list FROM usr WHERE login='%s')", user);
      try{
         esql.executeQueryAndPrintResult(query);
      }catch(Exception e){
         //ignore.
      }
   }//end

   public static void AddToBlocked(Messenger esql, String user){
      try{
         System.out.print("\tEnter user to block: ");
         String userToAdd = in.readLine();
         String query = String.format("INSERT INTO user_list_contains VALUES ((SELECT block_list FROM usr WHERE login='%s'), '%s')", user, userToAdd);
         esql.executeUpdate(query);
         System.out.format("Successfully blocked %s\n", userToAdd);
      }catch(Exception e){
         //ignore.
      }
   }//end

   public static void DeleteFromBlocked(Messenger esql, String user){
      try{
         System.out.print("\tEnter user to unblock: ");
         String userToDelete = in.readLine();
         String query = String.format("DELETE FROM user_list_contains WHERE list_id=(SELECT block_list FROM usr WHERE login='%s') AND list_member='%s'", user, userToDelete);
         esql.executeUpdate(query);
         System.out.format("Successfully unblocked %s\n", userToDelete);
      }catch(Exception e){
         //ignore.
      }
   }//end

   /* Print out this user's blocked list. */
   public static void ListBlocked(Messenger esql, String user){
      System.out.println("\nYour Blocked Users:");
      String query = String.format("SELECT list_member FROM user_list_contains WHERE list_id=(SELECT block_list FROM usr WHERE login='%s')", user);
      try{
         esql.executeQueryAndPrintResult(query);
      }catch(Exception e){
         //ignore.
      }
   }// end

   public static void NewMessage(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end 


   public static void Query6(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

	public static void ChatMenu(Messenger esql, String user){
      try{
              boolean chatmenu = true;
			  
              while(chatmenu) {
                System.out.println("\nCHAT MENU");
                System.out.println("---------");
                System.out.println("1. Message Options");
                System.out.println("2. Add Member to Chat");
                System.out.println("3. Delete Member from Chat");
                System.out.println("4. Create new Chat");
                System.out.println("5. Delete Whole Chat");
                System.out.println(".........................");
                System.out.println("0. Previous Menu");
                switch (readChoice()){
                   case 1: DisplayChats(esql,user); break;
				   case 2: DisplayChatInit(esql,user); break;
				   case 3: DisplayChatDele(esql,user); break;
				   case 4: CreateChat(esql, user); break; 
				   case 5: WholeChatDele(esql, user); break; 
                   case 0: chatmenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   public static void CreateChat(Messenger esql, String user){
      try{
         String query;
         List<String> users = new ArrayList<String>();
         int numUsers = 1;
         users.add(user);

         System.out.println("Enter users to chat with. Type 'DONE' when done.");
         System.out.format("\tEnter user %d: ", numUsers);
         String login = in.readLine();
         while(!login.equals("DONE")){
            users.add(login);
            numUsers += 1;
            System.out.format("\tEnter user %d: ", numUsers);
            login = in.readLine();
         }

         if(numUsers <= 2){
            query = String.format("INSERT INTO chat (chat_type, init_sender) VALUES ('private', '%s')", user);
         }
         else{
            query = String.format("INSERT INTO chat (chat_type, init_sender) VALUES ('public', '%s')", user);
         }
         esql.executeUpdate(query);

         query = String.format("SELECT chat_id FROM chat ORDER BY chat_id DESC LIMIT 1");
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         int chat_id = Integer.valueOf(result.get(0).get(0));
         System.out.format("chat_id: %d", chat_id);

         for(int i = 0; i < numUsers; ++i){
            System.out.format("Adding %s to chat_list\n", users.get(i));
            query = String.format("INSERT INTO chat_list VALUES (%d, '%s')", chat_id, users.get(i));
            esql.executeUpdate(query);
         }

            return;

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   public static void MessageMenu(Messenger esql, String chat_id, String user){
      try{
              boolean MessageMenu = true;
			  int limit = 10;
              while(MessageMenu) {
                System.out.println("\nMessage Menu");
                System.out.println("-------------");
                System.out.println("1. Add Message");
                System.out.println("2. Edit Message");
                System.out.println("3. Delete Message");
                System.out.println("4. Display Chat Messages");
                System.out.println("5. Load More Chat Messages");
                System.out.println(".........................");
                System.out.println("0. Previous Menu");
                switch (readChoice()){
                   case 1: AddMessage( esql, chat_id, user); break;
				   case 2: Edit_My_Messages(esql, chat_id, limit, user); break;
				   case 3: Delete_My_Messages(esql, chat_id, limit, user); break;
				   case 4: DisplayMessage(esql, chat_id, limit); break;
				   case 5: limit = increase_limit(limit); break;
                   case 0: MessageMenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end


   public static void DisplayChats(Messenger esql, String user){
	try{	
			String query =  String.format("select chat_id from CHAT_LIST where member= '%s'", user);
			//int rs = esql.executeQueryAndPrintResult(query);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			//DisplayMessage(esql, user, 1899); 
			
			for(int i = 0; i < result.size(); i++)
			{

				List<String> temp = result.get(i);
				//System.out.println(temp.get(0));
				String id = temp.get(0);
				//DisplayMessage(esql, id);
			}

			DisplayOwner(esql, user);
			

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

  public static void DisplayMessage( Messenger esql, String chat_id, int limit ){
	try{	
			String query = String.format("select sender_login AS Author, msg_timestamp AS CreationDate, msg_text AS Text from Message where chat_id= '%s' Order By msg_timestamp DESC limit %d", chat_id, limit );
			//int rs = esql.executeQueryAndPrintResult(query);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);

			for(int i = 0; i < result.size(); i++)
			{

				List<String> temp = result.get(i);
				String author = temp.get(0);
				String time = temp.get(1);
				String message = temp.get(2);
				
				System.out.print("By: ");
				System.out.println(author);
				System.out.print("On: ");
				System.out.println(time);
				System.out.println("MESSAGE");
				System.out.println(message);
			}
			

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }  
  }

	  public static void DisplayOwner( Messenger esql, String user){
		try{				
			
				boolean MessageMenu = true;
            	while(MessageMenu) {
				
 				System.out.println("\nYOUR CHATS ");
				System.out.println("----------");
				String query = String.format("select init_sender, chat_id from CHAT where chat_id= ANY (select chat_id from CHAT_LIST where member= '%s')",user);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". Chat Started By ");
					System.out.print(temp.get(0));
					System.out.println();
				}
				System.out.println(".........................");
				System.out.println("Which chat would you like to access? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
						String chat_id = result.get(val - 1).get(1);
						//System.out.println(chat_id); 
						MessageMenu(esql, chat_id, user);
				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }


		}catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }

   public static int increase_limit (int limit)
   {
		System.out.println("Current Message Limit: "+limit);
		limit = limit + 10;
		System.out.println("New Limit: "+limit);

		return limit;
   }

   public static void Delete_My_Messages(Messenger esql, String chat_id, int limit, String user)
   {	
		try{	
				boolean MessageMenu = true;
				while(MessageMenu)
				{

			String query = String.format("select msg_timestamp, msg_text, msg_id from Message where chat_id= '%s' AND sender_login= '%s' Order By msg_timestamp DESC limit %d",
	 							 chat_id, user, limit );
				
 				System.out.println("\nYOUR MESSAGES ");
				System.out.println("-----------");
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0)
				{
						System.out.println("You Have No Messages!");
						MessageMenu=false;
						break;
				}
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". On: ");
					System.out.println(temp.get(0));
					System.out.print((i+1) + ". Message: ");
					System.out.println(temp.get(1));
				}
				System.out.println(".........................");
				System.out.println("Which message would you like to delete? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
					String query1 = String.format("DELETE FROM Message WHERE msg_id= '%s'", result.get(val-1).get(2));
					esql.executeUpdate(query1);

				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }

		}
		catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }

 public static void Edit_My_Messages(Messenger esql, String chat_id, int limit, String user)
   {	
		try{	
				boolean MessageMenu = true;
				while(MessageMenu)
				{

			String query = String.format("select msg_timestamp, msg_text, msg_id from Message where chat_id= '%s' AND sender_login= '%s' Order By msg_timestamp DESC limit %d",
	 							 chat_id, user, limit );
				
 				System.out.println("\nYOUR MESSAGES ");
				System.out.println("----------");
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0)
				{
						System.out.println("You Have No Messages!");
						MessageMenu=false;
						break;
				}
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". On: ");
					System.out.println(temp.get(0));
					System.out.print((i+1) + ". Message: ");
					System.out.println(temp.get(1));
				}
				System.out.println(".........................");
				System.out.println("Which message would you like to edit? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
					System.out.println("Enter New Message.");
					String message = in.readLine();	
					String query1 = String.format("UPDATE Message SET msg_text= '%s' WHERE msg_id= '%s'", message, result.get(val-1).get(2));
					esql.executeUpdate(query1);

				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }

		}
		catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }

 	public static void DisplayChatInit( Messenger esql, String user){
		try{				
			
				boolean MessageMenu = true;
            	while(MessageMenu) {
				
 				System.out.println("\nMANAGEABLE CHATS ");
				System.out.println("-------------------");
				String query = String.format("select init_sender, chat_id from CHAT where init_sender= '%s'", user);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0)
				{
						System.out.println("You Are Not The Leader of Any Chats!");
						MessageMenu=false;
						break;
				}
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". Chat By: ");
					String query1 = String.format("select member from CHAT_LIST where chat_id= '%s'", temp.get(1));
					List<List<String>> result1 = esql.executeQueryAndReturnResult(query1);
					System.out.print(temp.get(0));
					System.out.println();
					System.out.println("With:");

					for(int j = 0; j < result1.size(); j++)
					{
						System.out.println(result1.get(j).get(0));
					}
						System.out.println("----------------------------");

					
				}
				System.out.println(".........................");
				System.out.println("To which chat would you like to add a member? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
						String chat_id = result.get(val - 1).get(1);
						System.out.println("Enter New Member Name.");
						String member = in.readLine();	
						String query2 = String.format("INSERT INTO CHAT_LIST VALUES('%s','%s')",chat_id, member);
						esql.executeUpdate(query2);

				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }


		}catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }

	public static void DisplayChatDele( Messenger esql, String user){
		try{				
			
				boolean MessageMenu = true;
            	while(MessageMenu) {
				
 				System.out.println("\nMANAGEABLE CHATS ");
				System.out.println("-------------------");
				String query = String.format("select init_sender, chat_id from CHAT where init_sender= '%s'", user);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0)
				{
						System.out.println("You Are Not The Leader of Any Chats!");
						MessageMenu=false;
						break;
				}
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". Chat By: ");
					String query1 = String.format("select member from CHAT_LIST where chat_id= '%s'", temp.get(1));
					List<List<String>> result1 = esql.executeQueryAndReturnResult(query1);
					System.out.print(temp.get(0));
					System.out.println();
					System.out.println("With:");

					for(int j = 0; j < result1.size(); j++)
					{
						System.out.println(result1.get(j).get(0));
					}
						System.out.println("----------------------------");

					
				}
				System.out.println(".........................");
				System.out.println("To which chat would you like to delete a member? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
						String chat_id = result.get(val - 1).get(1);
						System.out.println("Enter Member Name.");
						String member = in.readLine();	
						String query2 = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s' AND member='%s'",chat_id, member);
						esql.executeUpdate(query2);

				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }


		}catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }


	public static void WholeChatDele( Messenger esql, String user){
		try{				
			
				boolean MessageMenu = true;
            	while(MessageMenu) {
				
 				System.out.println("\nMANAGEABLE CHATS ");
				System.out.println("-------------------");
				String query = String.format("select init_sender, chat_id from CHAT where init_sender= '%s'", user);
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0)
				{
						System.out.println("You Are Not The Leader of Any Chats!");
						MessageMenu=false;
						break;
				}
				for(int i = 0; i < result.size(); i++)
				{

					List<String> temp = result.get(i);
					System.out.print((i+1) + ". Chat By: ");
					String query1 = String.format("select member from CHAT_LIST where chat_id= '%s'", temp.get(1));
					List<List<String>> result1 = esql.executeQueryAndReturnResult(query1);
					System.out.print(temp.get(0));
					System.out.println();
					System.out.println("With:");

					for(int j = 0; j < result1.size(); j++)
					{
						System.out.println(result1.get(j).get(0));
					}
						System.out.println("----------------------------");

					
				}
				System.out.println(".........................");
				System.out.println("Which chat would you like to delete? Press 0 to go back!");
 				
				int val = readChoice();
				if(val == 0)
				{
						MessageMenu = false; 
				}
				else if (val <= result.size())
				{
						String chat_id = result.get(val - 1).get(1);
						String query4 = String.format("DELETE FROM Message WHERE chat_id='%s'",chat_id);
						esql.executeUpdate(query4);
						String query2 = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s'",chat_id);
						esql.executeUpdate(query2);
						String query3 = String.format("DELETE FROM CHAT WHERE chat_id='%s'",chat_id);
						esql.executeUpdate(query3);

				}
				else
				{
                   System.out.println("Unrecognized choice!"); 
                }
              }


		}catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }

 public static void AddMessage( Messenger esql, String chat_id, String user){
	try{	


			System.out.println("Enter Message.");
			String message = in.readLine();	
			String query2 = String.format("INSERT INTO Message (msg_text, msg_timestamp,  sender_login, chat_id) VALUES('%s', CURRENT_TIMESTAMP,'%s','%s')", message, user, chat_id);
			esql.executeUpdate(query2);



      }catch(Exception e){
         System.err.println (e.getMessage ());
      }  
  }

}//end Messenger
