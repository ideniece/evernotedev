package info.justtech.oauth;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.userstore.Constants;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;

public class EvernoteDemo {
	/**
	 * developer token
	 */
	 private static final String authToken ="your auth token";
	 /**
	  * evernote host
	  */
	 private static final String evernoteHost = "sandbox.evernote.com";
	 /**
	  * storeUrl
	  */
	 private static final String userStoreUrl = "https://" + evernoteHost + "/edam/user";
	 private static final String userAgent = "Evernote/EvernoteAuth (Java) " + 
              Constants.EDAM_VERSION_MAJOR + "." + 
              Constants.EDAM_VERSION_MINOR;
	 private NoteStore.Client noteStore;
	 private String newNoteGuid;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		EvernoteDemo everAuth = new EvernoteDemo();
		boolean flag =everAuth.initialize(); 
		if(flag){
			System.out.println("initalized succeed!");
			everAuth.listNotes();
//			everAuth.createNote();
		}else{
			System.out.println("initalized failed!");
		}
	}
	/**
	 * inti
	 * @return
	 * @throws Exception
	 */
	private boolean initialize() throws Exception {
		// Set up the UserStore client and check that we can speak to the server
		THttpClient userStoreTrans = new THttpClient(userStoreUrl);
		userStoreTrans.setCustomHeader("User-Agent", userAgent);
		TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
		UserStore.Client userStore = new UserStore.Client(userStoreProt,
				userStoreProt);

		boolean versionOk = userStore.checkVersion("Evernote EvernoteAuth (Java)",
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
		if (!versionOk) {
			System.err.println("Incomatible Evernote client protocol version");
			return false;
		}
		String notestoreUrl = userStore.getNoteStoreUrl(authToken);
	    
	    // Set up the NoteStore client 
	    THttpClient noteStoreTrans = new THttpClient(notestoreUrl);
	    noteStoreTrans.setCustomHeader("User-Agent", userAgent);
	    TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
	    noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
		return versionOk;
	}

	private void listNotes() throws Exception {
		// List the notes in the user's account
		System.out.println("Listing notes:");

		// First, get a list of all notebooks
		List<Notebook> notebooks = noteStore.listNotebooks(authToken);

		for (Notebook notebook : notebooks) {
			System.out.println("Notebook: " + notebook.getName());

			// Next, search for the first 100 notes in this notebook, ordering
			// by creation date
			NoteFilter filter = new NoteFilter();
			filter.setNotebookGuid(notebook.getGuid());
			filter.setOrder(NoteSortOrder.CREATED.getValue());
			filter.setAscending(true);

			NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
			List<Note> notes = noteList.getNotes();
			for (Note note : notes) {
				System.out.println(" * " + note.getTitle());
				System.out.println(" * " + note.getContent());
			}
		}
		System.out.println();
	}

	private void createNote() throws Exception {
		// To create a new note, simply create a new Note object and fill in
		// attributes such as the note's title.
		Note note = new Note();
		note.setTitle("html test2"); 
		String contents = "<hr/><h1>这个是什么呢？？？？？？？？？？？？？？？</h1><hr/><br/>"; 
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
				+ "<en-note>"
//				+ "<span style=\"color:green;\">"+contents+"</span><br/>"
				+ "<html><body><font size='3' color='blue'>这个能成功么？？？</font></body></html>"
				+ "</en-note>";
		note.setContent(content); 
		Note createdNote = noteStore.createNote(authToken, note);
		newNoteGuid = createdNote.getGuid();

		System.out.println("Successfully created a new note with GUID: "
				+ newNoteGuid);
		System.out.println();
	}

	private static Data readFileAsData(String fileName) throws Exception {

		// Read the full binary contents of the file
		FileInputStream in = new FileInputStream(fileName);
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		byte[] block = new byte[10240];
		int len;
		while ((len = in.read(block)) >= 0) {
			byteOut.write(block, 0, len);
		}
		in.close();
		byte[] body = byteOut.toByteArray();

		// Create a new Data object to contain the file contents
		Data data = new Data();
		data.setSize(body.length);
		data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
		data.setBody(body);

		return data;
	}

	/**
	 * Helper method to convert a byte array to a hexadecimal string.
	 */
	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte hashByte : bytes) {
			int intVal = 0xff & hashByte;
			if (intVal < 0x10) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(intVal));
		}
		return sb.toString();
	}
}
