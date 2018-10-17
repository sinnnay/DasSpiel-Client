package dasspielv1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.UnknownHostException;

public class DasSpielV1 extends Thread {

	private String ip;
	private int port;
	private double distToWall;

	public DasSpielV1(String ip, int port) {
		this.ip = ip;
		this.port = port;

	}

	public void sendMessage(String message) {
//		System.out.println(message);

		Socket s = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader bufferedReader = null;
		try {
//			System.out.println("im try-block von sendmessage");
			s = new Socket();
			s.setSoTimeout(3000);
//			System.out.println("socketPort: " + s.getLocalPort());
			s.connect(new InetSocketAddress(ip, port));
//			System.out.println("nach init von socket in sendmessage");
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bufferedWriter.write(message);
			bufferedWriter.flush();

//			System.out.println("message sent");
			bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			if (message.contains("STATUS;") || message.contains("DIST;")) {
//				System.out.println("Message contains STATUS");
				String line;
				StringBuffer standardOutput = new StringBuffer();
				try {
					do {
//						logger.info("im do von readfromserver");
						line = bufferedReader.readLine();
//						logger.info("lineFromServer: " + lineFromServer);
						standardOutput.append(line);
					}
					while(!line.contains(";"));
					if (message.contains("DIST;")) {
						try {
							distToWall = Double.parseDouble(standardOutput.toString()) / 100000;
						} catch (NumberFormatException e) {
						}
					}
					System.out.println("Message from server: " + standardOutput.toString());
				} catch (SocketTimeoutException e) {
					System.out.println("Server antwortet nicht");
//					e.printStackTrace();
				} catch (IOException e) {
//					e.printStackTrace();
				}
				
			}

			bufferedWriter.close();
			bufferedReader.close();
			s.close();
		
		} catch (SocketTimeoutException e) {
			System.out.println("Server antwortet nicht");
//			e.printStackTrace();
		}catch (UnknownHostException e) {
			System.out.println("Failed to connect; Wrong IP-address or wrong port number or server not up");
//			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to connect; Wrong IP-address or wrong port number or server not up");
//			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("Failed to connect; Wrong IP-address or wrong port number or server not up");
//			e.printStackTrace();
		} finally {
//			System.out.println("im finally");
			if (s != null && !s.isClosed()) {
				try {
					s.close();
				} catch (IOException e1) {
//					e1.printStackTrace();

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

	public void rotatePlayer(int angle) {
		sendMessage("ROTATE|" + angle + ";");
	}

	public void movePlayer(int speed) {
		sendMessage("MOVE|" + speed + ";");
	}

	public void shootBullet() {
		sendMessage("SHOOT;");
	}

	public void startDrawing(String color) {
		sendMessage("DRAW|" + color + ";");
	}

	public void stopAndClearDrawing() {
		sendMessage("STOP_DRAWING;");
	}

	public void getStatus() {
		sendMessage("STATUS;");
	}
	
	public double getDistToWall() {
		sendMessage("DIST;");
		return distToWall;
	}

}
