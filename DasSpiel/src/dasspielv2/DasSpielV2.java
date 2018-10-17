package dasspielv2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.UnknownHostException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class DasSpielV2 extends Thread {

	private String ip, remoteIP;
	private int port, remotePort;
	private boolean serverDown;
	private double distToWall;
	private int zaPort;

	public DasSpielV2(String ip, int port) {
		this.ip = ip;
		this.port = port;
		System.out.println("in MyGame()");
		if (!Availability.isHostAvailable(ip, port)) {
			serverDown = true;
		} else {
			serverDown = false;
		}
		try {
			FileReader reader = new FileReader("za.txt");
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line;
			int endOfLine;
			while ((line = bufferedReader.readLine()) != null) {
				endOfLine = line.length();
				if (line.startsWith("zaPort:"))
					zaPort = Integer.parseInt(line.substring(7, endOfLine));
				else if (line.startsWith("remoteIP:"))
					remoteIP = line.substring(9, endOfLine);
				else if (line.startsWith("remotePort:"))
					remotePort = Integer.parseInt(line.substring(11, endOfLine));
			}
			reader.close();
//			System.out.println("zsPortOld: " + zaPort);
//			System.out.println("remoteIPOld: " + remoteIP);
//			System.out.println("remotePortOld: " + remotePort);
		} catch (FileNotFoundException e) {
//			System.out.println("filenotfoundexecption im konstruktor");
//			e.printStackTrace();
		} catch (IOException e) {
//			e.printStackTrace();
		}
		if (!Availability.isJavaProgramRunning("dasspielv2.Zwischenanwendung")) {
			System.out.println("Zwischenanwendung nicht verfügbar");
			compileAndRunZwischenanwendung();
		} else if (!ip.equalsIgnoreCase(remoteIP)
				|| !Integer.toString(port).equalsIgnoreCase(Integer.toString(remotePort))) {
			System.out.println("Neue Verbindungsdaten gegeben");
			disconnect();
			compileAndRunZwischenanwendung();
		}
		else {
			System.out.println("Zwischenanwendung läuft");
		}
	}

	private void compileAndRunZwischenanwendung() {
//		System.out.println("in compile and run zwischenanwedung");
		String fileToCompile = "src" + File.separator + "dasspielv2" + File.separator + "Zwischenanwendung.java";
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int compilationResult = compiler.run(null, null, null, fileToCompile);
		if (compilationResult == 0) {
			try {
				System.out.println("Compilation of Zwischenanwendung successful");
					
				Runtime.getRuntime().exec("java -cp bin dasspielv2.Zwischenanwendung " + ip + " " + port);
			
				BufferedReader bufferedReader;
				int zaPortNew = zaPort;				
				do {
					try {
					bufferedReader = new BufferedReader(new FileReader("za.txt"));
					String lineNew = bufferedReader.readLine();
					if(lineNew != null) {
//						System.out.println("line: " + lineNew);
						int endOfLineNew = lineNew.length();
						zaPortNew = Integer.parseInt(lineNew.substring(7, endOfLineNew));
//						System.out.println("zsPortNew in compile: " + zaPortNew);
					}
					else {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					bufferedReader.close();				
					} catch(FileNotFoundException fne) {
//						System.out.println("filenotfoundexception in compileandrunza");
					}
				} while (zaPortNew == zaPort);
				zaPort = zaPortNew;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("Compilation of Zwischenanwendung failed");
		}

	}

	public void sendMessage(String message) {
		System.out.println(message);
		if (!message.equals("DISCONNECT;")) {
			if (serverDown) {
				System.out.println("Failed to connect; Wrong IP-address or wrong port number or server not up");
				return;
			}
		}

		Socket s = null;
		OutputStreamWriter out = null;
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			System.out.println("im try-block von sendmessage");
			s = new Socket();
			s.setSoTimeout(3000);
			System.out.println("zaPort: " + zaPort);
			s.connect(new InetSocketAddress("localhost", zaPort));
			
			System.out.println("nach init von socket in sendmessage");
			out = new OutputStreamWriter(s.getOutputStream());
			writer = new BufferedWriter(out);
			writer.write(message);
			writer.flush();
			
			System.out.println("message sent");
			InputStreamReader isr = new InputStreamReader(s.getInputStream());
			reader = new BufferedReader(isr);
			if (message.contains("STATUS;") || message.contains("DIST;")) {
				System.out.println("Message contains STATUS oder DIST");
				StringBuffer standardOutput = new StringBuffer();
				String line = null;
				try {
					System.out.println("im try vor der while von contains status");
					while ((line = reader.readLine()) != null) {
						standardOutput.append(line);
					}
					standardOutput = standardOutput.deleteCharAt(standardOutput.length() - 1);
					if(message.contains("DIST;")) {
						try {
							distToWall = Double.parseDouble(standardOutput.toString());
						} catch (NumberFormatException e) {
						}
					}
					System.out.println("Message from server: " + standardOutput);
				} catch (SocketTimeoutException e) {
					System.out.println("Server antwortet nicht");
//					e.printStackTrace();
				} catch (IOException e) {
//					e.printStackTrace();
				}
							
			}

			writer.close();
			reader.close();
			s.close();
		} catch (SocketTimeoutException e) {
			System.out.println("Server antwortet nicht");
//			e.printStackTrace();
		} catch (UnknownHostException e) {
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
//					System.out.println("s closed in finally");
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
		sendMessage("STATUS;\n");
	}

	public double getDistToWall() {
		sendMessage("DIST;\n");
		return distToWall;
	}

}
