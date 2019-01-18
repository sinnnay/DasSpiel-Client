package flowermeadow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.UnknownHostException;

public class FlowerMeadow {

	private String ip;
	private int port;
	private double distToWall;
	private String messageFromServer;
	private static FlowerMeadow instance;
	private long lastTimeMessageSent, timePassedSinceLastMsgSent, currentTimeMs;
	
	private FlowerMeadow(String ip, int port) {
		this.ip = ip;
		this.port = port;
		lastTimeMessageSent = System.currentTimeMillis();
	}

	public static FlowerMeadow getInstance (String ip, int port) {
	    if (FlowerMeadow.instance == null) {
	    	FlowerMeadow.instance = new FlowerMeadow(ip, port);
	    }
	    return FlowerMeadow.instance;
	  }
	
	private synchronized void sendMessage(String message) {
		currentTimeMs = System.currentTimeMillis();
		if((timePassedSinceLastMsgSent = currentTimeMs - lastTimeMessageSent) < 500) {
			try {
				Thread.sleep(500 - timePassedSinceLastMsgSent);
			} catch (InterruptedException e2) {
				System.out.println("InterruptedException in sendMessage() ausgelöst");
			}
		}
		Socket s = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader bufferedReader = null;
		try {
			s = new Socket();
			s.setSoTimeout(3000);
			s.connect(new InetSocketAddress(ip, port));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bufferedWriter.write(message);
			bufferedWriter.flush();
			bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			if (message.contains("STATUS;") || message.contains("DIST;") || message.contains("SURR_PLAYERS") 
					|| message.contains("SURR_PICKUPS") || message.contains("DIR_VECTOR") || message.contains("CONNECT")
					|| message.contains("WALL;") || message.contains("ANGLE;") || message.contains("TIPP;")) {
				String line;
				StringBuffer standardOutput = new StringBuffer();
				try {
					do {
						line = bufferedReader.readLine();
						standardOutput.append(line);
					}
					while(!line.contains(";"));
					String serverMsg = standardOutput.toString().substring(0, standardOutput.toString().length() - 1);  // Entfernt das Semikolon am Ende
					if (message.contains("DIST;")) {
						try {
							distToWall = Double.parseDouble(serverMsg); 
						} catch (NumberFormatException e) {
							
						}
					}
					else if (message.contains("CONNECT")) {
						System.out.println(serverMsg);
					}
					else{
						messageFromServer = serverMsg;
					}
				} catch (SocketTimeoutException e) {
					System.out.println("Server antwortet nicht");
				} catch (IOException e) {
					
				}
				
			}

			bufferedWriter.close();
			bufferedReader.close();
			s.close();
			lastTimeMessageSent = System.currentTimeMillis();
		} catch (SocketTimeoutException e) {
			System.out.println("Server antwortet nicht");

		} catch (UnknownHostException e) {
			System.out.println("Verbindung fehlgeschlagen! Bitte überprüfe deine Eingaben für IP-Adresse und Portnummer.\nDer Server läuft vielleicht noch nicht, oder er ist überlastet.");
//			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Verbindung fehlgeschlagen! Bitte überprüfe deine Eingaben für IP-Adresse und Portnummer.\nDer Server läuft vielleicht noch nicht, oder er ist überlastet.");
//			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("Verbindung fehlgeschlagen! Bitte überprüfe deine Eingaben für IP-Adresse und Portnummer.\nDer Server läuft vielleicht noch nicht, oder er ist überlastet.");
//			e.printStackTrace();
		} finally {
			if (s != null && !s.isClosed()) {
				try {
					s.close();
				} catch (IOException e1) {

				}

			}
		}

	}

	public void connect(String username) {
		sendMessage("CONNECT|" + username + ";");

	}

	public void disconnect() {
		sendMessage("DISCONNECT;");
	}

	public void spawnPlayer() {
		sendMessage("SPAWN;");
	}

	public void deletePlayer() {
		sendMessage("DELETE;");
	}

	public void rotatePlayer(double angle) {
		sendMessage("ROTATE|" + angle + ";");
	}

	public void movePlayer(int speed) {
		if(speed > 500 || speed < -500) {
			System.out.println("Speed muss im Bereich von -500 und 500 sein");
			return;
		}
		sendMessage("MOVE|" + speed + ";");
	}

	public void shoot() {
		sendMessage("SHOOT;");
	}

	public void startDrawing(String color) {
		sendMessage("DRAW|" + color + ";");
	}

	public void startMowing() {
		sendMessage("MOW;");
	}
	
	public void stopMowing() {
		sendMessage("STOP_MOWING;");
	}
	
	public void stopAndClearDrawing() {
		sendMessage("STOP_DRAWING;");
	}

	public String getStatus() {
		sendMessage("STATUS;");
		return messageFromServer;
	}
	
	public double getDistToWall() {
		sendMessage("DIST;");
		return distToWall;
	}
	
	public String getPlayersInRadius(int radius) {
		if(radius > 250 || radius < 0) {
			System.out.println("Radius muss im Bereich von 0 und 250 sein");
			return null;
		}
		sendMessage("SURR_PLAYERS|" + radius + ";");
		return messageFromServer;
	}
	
	public String getPickUpsInRadius(int radius) {
		if(radius > 250 || radius < 0) {
			System.out.println("Radius muss im Bereich von 0 und 250 sein");
			return null;
		}
		sendMessage("SURR_PICKUPS|" + radius + ";");
		return messageFromServer;
	}
	
	public String getDirectionVector() {
		sendMessage("DIR_VECTOR;");
		return messageFromServer;
	}
	
	public String getAngleWall() {
		sendMessage("ANGLE;");
		return messageFromServer;
	}
	
	public String getTipp() {
		sendMessage("TIPP;");
		return messageFromServer;
	}
	
	public String getWallDistance() {
		sendMessage("WALL;");
		return messageFromServer;
	}
}
