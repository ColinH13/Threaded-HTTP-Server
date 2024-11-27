import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class WebServer {
    public static void main(String[] args) {

        HTTPServer.main(args);

    }
}

class HTTPServer {
    public static void main(String[] args) {

        String keystore;
        String password;

        if (args.length == 2) {
            keystore = args[0];
            password = args[1];

        // Set system properties to specify the keystore file and password.
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        }

        

        //String keystore = "keystore.jks";
        //String password = "your password";



        // Set the port number
        int port = 80;
        int Httpsport = 443;

        // Start both HTTP and HTTPS
        if (args.length != 2) {
            new Thread(() -> startHttpServer(port)).start();
        }
        else if (args.length == 2) {
            new Thread(() -> startHttpServer(port)).start();
            new Thread(() -> startHttpsServer(Httpsport)).start();
        }
    }

    private static void startHttpServer(int port) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP Server running on port " + port);

            // Infinite loop to listen for incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Create a new thread to handle the request
                HttpRequestHandler requestHandler = new HttpRequestHandler(clientSocket);
                Thread thread = new Thread(requestHandler);
                thread.start();

                //System.out.println("Thread Name = " + thread.getName()); // Test print to display number of threads
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }

}


    private static void startHttpsServer(int Httpsport) {
        // Modify to work for HTTPS
        try {

            System.out.println("HTTPS Server running on port " + Httpsport);

            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(Httpsport);

            // Infinite loop to listen for incoming connections
            while (true) {

                SSLSocket SSLSocket = (SSLSocket)sslServerSocket.accept(); // SSLSocket?
                
                // Create a new thread to handle the request
                HttpsRequestHandler HTTPSrequestHandler = new HttpsRequestHandler(SSLSocket);
                Thread SSLthread = new Thread(HTTPSrequestHandler);
                SSLthread.start();

                //System.out.println("Thread Name = " + thread.getName()); // Test print to display number of threads
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        } 


    }
}

class HttpsRequestHandler extends HttpRequestHandler {

    private final String ROOT_DIRECTORY = ".";
    private SSLSocket socket; // SSLSocket?

    public HttpsRequestHandler(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream is = socket.getInputStream();
             DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                //System.out.println();

            // Read the request line
            String requestLine = br.readLine();

            // Split the request line into different portions
            if (requestLine == null) { System.err.println("Client sent no data or closed the connection."); return;}
            String[] line = requestLine.split(" ");

            //System.out.println(requestLine);

            if (line.length != 3) {
                // System.out.println("request is less than 3"); // Test print
            }
            
            String HTTPMethod = line[0];
            //System.out.println("HTTPMethod = " + HTTPMethod);
            if (!line[0].equals("GET")) {
            os.writeBytes("HTTP/1.1 405 Method Not Allowed\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" + 
            "HTTP Method Not Supported.");}

            String filePath = line[1];
            String sanitizedFilePath = sanitizeFilePath(filePath);

            String fileName = sanitizedFilePath;

            // Extract the requested file path from the HTTP request line and sanitize file path
            
            File requestedFile = new File(fileName);
            //System.out.println("requestedFile = " + requestedFile);


            //System.out.println("request parts:");
            for (String part : line) {
                //System.out.println(part);
            }
            // HTTP Method | req info | http version

            //System.out.println("fileName = " + fileName);
            
            // Determine content type of the file
            String MIMEType = contentType(fileName);
            //MIMEType = "text/html";

            //System.out.println("Received request: " + requestLine);
            //System.out.println("MIME = " + MIMEType);
            
            // Send a basic HTTP response TODO: With File
            String response;


            if (requestedFile.exists()) {
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:" + MIMEType + "\r\n";
            }
            // Send 404 Error
            else {response = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" + 
            "The requested file was not found.";
            }
            os.writeBytes(response);
            if (MIMEType == "image/jpeg" || MIMEType == "image/gif") {os.writeBytes("Content-Legth: " + requestedFile.length() + "\r\n" + "\r\n");}
            else {os.writeBytes("\r\n");}
            sendFileContents(os, requestedFile, MIMEType); // Output file contents
            


            // Close input and output streams
            //is.close();
            os.close();
            
        } catch (IOException e) {
            System.err.println("Request handling exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Socket closing exception: " + e.getMessage());
            }
        }
    }

}

class HttpRequestHandler implements Runnable {
    private final String ROOT_DIRECTORY = ".";
    private Socket socket;

    public HttpRequestHandler() {
        
    }

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream is = socket.getInputStream();
             DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            // Read the request line
            String requestLine = br.readLine();

            // Split the request line into different portions
            if (requestLine == null) { System.err.println("Client sent no data or closed the connection."); return;}
            String[] line = requestLine.split(" ");

            //System.out.println(requestLine);

            if (line.length != 3) {
                // System.out.println("request is less than 3"); // Test print
            }
            
            String HTTPMethod = line[0];
            //System.out.println("HTTPMethod = " + HTTPMethod);
            if (!line[0].equals("GET")) {
            os.writeBytes("HTTP/1.1 405 Method Not Allowed\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" + 
            "HTTP Method Not Supported.");}

            String filePath = line[1];
            String sanitizedFilePath = sanitizeFilePath(filePath);

            String fileName = sanitizedFilePath;

            // Extract the requested file path from the HTTP request line and sanitize file path
            
            File requestedFile = new File(fileName);
            //System.out.println("requestedFile = " + requestedFile);


            //System.out.println("request parts:");
            for (String part : line) {
                //System.out.println(part);
            }
            // HTTP Method | req info | http version

            //System.out.println("fileName = " + fileName);
            
            // Determine content type of the file
            String MIMEType = contentType(fileName);
            //MIMEType = "text/html";

            //System.out.println("Received request: " + requestLine);
            //System.out.println("MIME = " + MIMEType);
            
            String response;


            if (requestedFile.exists()) {
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:" + MIMEType + "\r\n";
            }
            // Send 404 Error
            else {response = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" + 
            "The requested file was not found.";
            }
            os.writeBytes(response);
            if (MIMEType == "image/jpeg" || MIMEType == "image/gif") {os.writeBytes("Content-Legth: " + requestedFile.length() + "\r\n" + "\r\n");}
            else {os.writeBytes("\r\n");}
            sendFileContents(os, requestedFile, MIMEType); // Output file contents
            


            // Close input and output streams
            is.close();
            os.close();
            
        } catch (IOException e) {
            System.err.println("Request handling exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Socket closing exception: " + e.getMessage());
            }
        }
    }

    public String contentType(String fileName) {
        String[] splitFile = fileName.split("\\."); 

        for (String part : splitFile) {
            //System.out.println(part);
        }

        //System.out.println("Size: " + splitFile.length);
        //System.out.println("fileName = " + fileName);

        String fileType = splitFile[1];
        String MIMEType = "";

        switch(fileType) {
            case "html":
                MIMEType = "text/html";
                break;
            case "txt":
                MIMEType = "text/plain";
                break;
            case "jpeg":
                MIMEType = "image/jpeg";
                break;
                case "jpg":
                MIMEType = "image/jpeg";
                break;
            case "gif":
                MIMEType = "image/gif";
                break;
            case "json":
                MIMEType = "application/json";
                break;
        }
        return MIMEType;
    }


    public String sanitizeFilePath(String requestedPath) throws IOException{
        File requestedFile = new File(ROOT_DIRECTORY, requestedPath).getCanonicalFile();
        File rootDirectory = new File(ROOT_DIRECTORY).getCanonicalFile();

        if (!requestedFile.getPath().startsWith(rootDirectory.getPath())) {
            System.err.println("File not in root directory.");
        }

        return requestedFile.getPath();
    }



    public void sendFileContents(DataOutputStream os, File file, String MIMEType) throws IOException{
        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            int data;
            byte[] buffer = new byte[4096];
            String stringData;

            if (MIMEType == "image/jpeg" || MIMEType == "image/gif") {
            while ((data = fis.read(buffer)) != -1) {
                    try {os.write(buffer, 0, data);}
                    catch (SocketException e) { System.err.println("Connection reset by client: " + e.getMessage()); break;}
                    
                    //os.write(data);
            }
            
        } else {
            while((stringData = reader.readLine()) != null) {
                //stringData += "\r\n";
                os.writeBytes(stringData);
                if (MIMEType == "text/html") {os.writeBytes("<br>\r\n");}
                else {os.writeBytes("\r\n");}
            }
            
            fis.close();
        }
        os.flush();
    }
    
}
}