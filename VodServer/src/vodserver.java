import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

// Each Client Connection will be managed in a dedicated Thread
public class vodserver implements Runnable{ 
	
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "src/index.html";
	static final String FILE_NOT_FOUND = "src/404.html";
	static final String METHOD_NOT_SUPPORTED = "src/not_supported.html";
	// port to listen connection
	static int PORT = 8080;
	
	// verbose mode
	static final boolean verbose = true;
	
	// Client Connection via Socket Class
	private Socket connect;
	
	public vodserver(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
            if (args.length > 0)
            {
                System.out.println("The Port number for connecting is : "+args[0]); 
                PORT = Integer.parseInt(args[0]);
            }
			ServerSocket serverConnect = new ServerSocket(PORT);
			//serverConnect.setSoTimeout(10000);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			// we listen until user halts server execution
			while (true) {
				vodserver myServer = new vodserver(serverConnect.accept());
				
				if (verbose) {
					System.out.println("Connection opened. (" + new Date() + ")");
				}
				
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	public void prettyprint(String content, int fileLength, File file)
	{
		System.out.println("HTTP/1.1 200 OK");
		System.out.println("Server: ON");
		System.out.println("Date: " + new Date());
		System.out.println("Content-Type: " + content);
		System.out.println("Content-Length: " + fileLength);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		System.out.println("Last-Modified" +  sdf.format(file.lastModified()));
		System.out.println(); // blank line between headers and content, very important !
	}
	
	public void prettyprint_206(String content, int fileLength, File file, String str, int i1)
	{
		System.out.println("HTTP/1.1 206 Partial Content");
		System.out.println("Server: ON ");
		System.out.println("Date: " + new Date());
		System.out.println("Content-Type: " + content);
		System.out.println("Content-Range: "+ str +"/"+ fileLength);
		System.out.println("Content-Length: "+ i1);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		System.out.println("Last-Modified" +  sdf.format(file.lastModified()));
		System.out.println(); // blank line between headers and content, very important !
	}
	
	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			System.out.println(in);
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();
			
			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				
				// we return the not supported file to the client
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//read content to return to client
				byte[] fileData = readFileData(file, fileLength);

				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: ON");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println("Connection: Keep-Alive");
				//out.println("Keep-Alive: timeout=50, max=10000");
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
				
			} else {
					// GET or HEAD method
					if (fileRequested.contains("range")) 
					{
						String str = null;
						int i1 = 0;

						//System.out.println("File requested " + fileRequested);
						str = fileRequested.substring(fileRequested.lastIndexOf("%20") + 3,fileRequested.lastIndexOf("%22"));
						String str1 = str.substring(str.lastIndexOf("bytes")+6);
						System.out.println("File requested range: " + str1);
						String str2 = str1.substring(str1.lastIndexOf("-")+1);
						String str3 = str1.substring(0, str1.lastIndexOf("-"));
						int i3 = Integer.parseInt(str3);
						int i2 = Integer.parseInt(str2);
						i1 = (i2-i3)+1;
						
						fileRequested = fileRequested.substring(0, fileRequested.indexOf("%20"));

						//fileRequested += DEFAULT_FILE;
						System.out.println("File requested " + fileRequested);

						if (fileRequested.endsWith("/"))
						{
							fileRequested += DEFAULT_FILE;
						}
						else
						{
							fileRequested = "Content"+fileRequested;
						}
						
						File file = new File(WEB_ROOT, fileRequested);
						int fileLength = (int) file.length();
						String content = getContentType(fileRequested);
						System.out.println("content type " + content);

					
						if (method.equals("GET")) 
						{ 
							// GET method so we return content
							byte[] fileData = readFileData(file, fileLength);
							prettyprint_206(content, fileLength, file, str, i1);
							// send HTTP Headers
							out.println("HTTP/1.1 206 Partial Content");
							out.println("Server: ON ");
							out.println("Date: " + new Date());
							out.println("Content-Type: " + content);
							out.println("Content-Range: "+ str +"/"+ fileLength);
							out.println("Content-Length: "+ i1);
							out.println("Accept-Ranges: bytes");
							SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
							out.println("Last-Modified" +  sdf.format(file.lastModified()));

							out.println(); // blank line between headers and content, very important !
							out.flush(); // flush character output stream buffer
						
							dataOut.write(fileData, 0, fileLength);
							dataOut.flush();
						}
						if (verbose) 
						{
							System.out.println("File " + fileRequested + " of type " + content + " returned");
						}
						
					}
					else
					{
						// GET or HEAD method
						if (fileRequested.endsWith("/"))
						{
							fileRequested += DEFAULT_FILE;
						}
						else
						{
							fileRequested = "Content"+fileRequested;
						}
						File file = new File(WEB_ROOT, fileRequested);
						int fileLength = (int) file.length();
						String content = getContentType(fileRequested);
						
						if (method.equals("GET")) 
						{ // GET method so we return content
							byte[] fileData = readFileData(file, fileLength);
							prettyprint(content, fileLength, file);					
							// send HTTP Headers
							out.println("HTTP/1.1 200 OK");
							out.println("Server: ON");
							out.println("Connection: Keep-Alive");
							out.println("Content-Length: " + fileLength);
							out.println("Date: " + new Date());
							out.println("Content-Type: " + content);
							out.println("Accept-Ranges: bytes");
							SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
							out.println("Last-Modified" +  sdf.format(file.lastModified()));
		
							//out.println("Keep-Alive: timeout=50, max=100000");
							out.println(); // blank line between headers and content, very important !
							out.flush(); // flush character output stream buffer
							
							dataOut.write(fileData, 0, fileLength);
							dataOut.flush();
						}
						
						if (verbose) {
							System.out.println("File " + fileRequested + " of type " + content + " returned");
						}
						
				}
			}
		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
			
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
		
		
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";
		else if (fileRequested.endsWith(".css"))
			return "text/css";
		else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg") )
			return "image/jpeg";
		else if (fileRequested.endsWith(".png"))
			return "image/png";
		else if (fileRequested.endsWith(".gif"))
			return "image/gif";
		else if (fileRequested.endsWith(".mpeg"))
			return "audio/mpeg";
		else if (fileRequested.endsWith(".webm"))
			return "video/webm";
		else if (fileRequested.endsWith(".ogg"))
			return "video/ogg";
		else if (fileRequested.endsWith(".wav"))
			return "audio/wav";
		else if (fileRequested.endsWith(".txt"))
			return "text/plain";
		else if (fileRequested.endsWith(".css"))
			return "application/javascript";
		else if (fileRequested.endsWith(".mp4"))
			return "video/mp4";
		else
			return "application/octet-stream";
	}
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from Sharath : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if (verbose) {
			System.out.println("File " + fileRequested + " not found");
		}
	}
	
}