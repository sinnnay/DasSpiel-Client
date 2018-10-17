package dasspielv2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Zwischenanwendung {

	private static Thread t1;
	private static ServerSocket ss;
	public static int remotePort, ssPort;
	public static String remoteIPAddress;
	private static boolean running;
	
	static Logger logger;
	static FileHandler fh;
	static FileWriter writer;

	private static void openGateway() {
//		logger.info("In openGateway");
		t1 = new Thread() {
			@Override
			public void run() {

				ss = null;
				OutputStreamWriter writerToServer = null, writerToClient = null;
				BufferedWriter bufferedWriterToServer = null, bufferedWriterToClient = null;
				Socket sToServer = null;
				Socket sToClient = null;
				InputStreamReader readerFromServer = null, readerFromClient = null;
				BufferedReader bufferedReaderFromServer = null, bufferedReaderFromClient = null;
				try {
					ss = new ServerSocket(0);
					ssPort = ss.getLocalPort();
					writer.write("zaPort:" + ssPort);
					writer.write("\r\n");
					writer.write("remoteIP:" + remoteIPAddress);
					writer.write("\r\n");
					writer.write("remotePort:" + remotePort);
					writer.close();
//					logger.info("ssPort: " + ssPort + " ss.getInetAddress(): " + ss.getInetAddress());
//					logger.info("im try-block");
					ss.setSoTimeout(600000); // nach 10 Minuten ohne eingehende Verbindung, schließt der Server
					
					while (running) {
//						logger.info("running ist true");
						if (sToServer == null || sToServer.isClosed()) {
//							logger.info("sToServer ist null");
							if (Availability.isHostAvailable(remoteIPAddress, remotePort)) {
//								logger.info("Spielserver ist available");
								sToServer = new Socket();
								sToServer.setSoTimeout(3000);
								sToServer.connect(new InetSocketAddress(remoteIPAddress, remotePort));
								writerToServer = new OutputStreamWriter(sToServer.getOutputStream());
								bufferedWriterToServer = new BufferedWriter(writerToServer);
								readerFromServer = new InputStreamReader(sToServer.getInputStream());
								bufferedReaderFromServer = new BufferedReader(readerFromServer);
							
							} else {
//								logger.info("Spielserver nicht available");
							}
						}

//						logger.info("Before ss.accept");
						sToClient = ss.accept();
						sToClient.setSoTimeout(3000);
//						logger.info("s.hashcode nach accept: " + sToClient.hashCode());
//						logger.info("After ss.accept");

						readerFromClient = new InputStreamReader(sToClient.getInputStream());
//						logger.info("nach readerFromClient init");
						bufferedReaderFromClient = new BufferedReader(readerFromClient);
//						logger.info("nach bufferedReaderFromClient init");
//						int n1;
//						char[] c1 = new char[1024];
//						StringBuffer standardOutputFromClient = new StringBuffer();
						String lineFromClient;
//						logger.info("nach lineFromClient deklaration");
						try {
							logger.info("im try block vor der ehemaligen while - jetzt nur readline");
//							while ((n1 = isr.read(c1)) > 0) {
//							while((lineFromClient = bufferedReaderFromClient.readLine()) != null){
								lineFromClient = bufferedReaderFromClient.readLine();
//								standardOutputFromClient.append(lineFromClient);
							
//								logger.info("in while-loop fürs readen zum " + counter + ". mal");
//								standardOutput.append(c1, 0, n1);
//								String messageFromClient = standardOutputFromClient.toString();

								logger.info("messageFromClient: " + lineFromClient);
								// logger.info(sToServer.toString());
//								logger.info("vor der Abfrage auf contains disconnect");
								if (lineFromClient.contains("DISCONNECT;")) {
//									logger.info("message contains disconnect");
									running = false;
									
								}
								if (sToServer != null && !sToServer.isClosed()) {
//									logger.info("im if block von sToServer != null && not closed");
//									out.write(message);
									bufferedWriterToServer.write(lineFromClient);
//									logger.info("nach out.write");
									bufferedWriterToServer.flush();
//									out.flush();
									logger.info("message sent out");
								}
//							    logger.info("vor der abfrage auf message.contains status");
								if (lineFromClient.contains("STATUS;") || lineFromClient.contains("DIST;")) {

									logger.info("Message contains STATUS oder DIST");
//									int n12;
//									char[] c12 = new char[1024];
									String lineFromServer;
									StringBuffer standardOutputFromServer = new StringBuffer();
									try {
										do {
											logger.info("im do von readfromserver");
											lineFromServer = bufferedReaderFromServer.readLine();
//										logger.info("lineFromServer: " + lineFromServer);
											standardOutputFromServer.append(lineFromServer);
										}
										while(!lineFromServer.contains(";"));
//										logger.info("lineFromServer contains ;");
//										
//										n12 = isr2.read(c12);
										// while ((n12 = isr2.read(c12)) > 0) {
//										logger.info("im status try block");
//										standardOutput2.append(c12, 0, n12);
//										String messageFromServer = standardOutputFromServer.toString();
										if (sToClient != null && !sToClient.isClosed()) {
//											logger.info("s.hashcode im s != null && not closed: " + sToClient.hashCode());
											writerToClient = new OutputStreamWriter(sToClient.getOutputStream());
											bufferedWriterToClient = new BufferedWriter(writerToClient);
											bufferedWriterToClient.write(standardOutputFromServer.toString());
//											out2.write(message2);
											bufferedWriterToClient.flush();
//											out2.flush();
											bufferedWriterToClient.close();
//											out2.close();
//											logger.info("message " + lineFromServer + " sent out to client");
										}

										// }
									} catch (SocketTimeoutException e) {
										logger.info(e.toString() + " in innerster while von message contains status");
									} catch (IOException e) {
//										logger.info(e.toString() + " in innerster while von message contains status");
									}

								}
//								logger.info("nach dem if-block zu contains status or dist");

//								for (int i = 0; i < c1.length; i++) {
//									if (c1[i] != '\u0000')
//										logger.info(i + "  " + c1[i]);
//								}
//							}
//							}
						} catch (IOException e) {
//							logger.info(e.toString() + " in mittlereren try");
						}

						// isr.close();
						// out.close();

					}
//					logger.info("running ist nicht mehr true");
					if (sToClient != null && !sToClient.isClosed()) {
//						logger.info("s closed");
						sToClient.close();
					}
					if (ss != null && !ss.isClosed()) {
						ss.close();
//						logger.info("ss closed");
					}
					if (sToServer != null && !sToServer.isClosed()) {
						sToServer.close();
//						logger.info("sToServer closed");
					}
					

				} catch (SocketTimeoutException exc) {
//					logger.info(exc.toString());
				} catch (IOException e) {
//					logger.info(e.toString() + " in äußerstem try");		
				} finally {
					try {
						if (sToClient != null && !sToClient.isClosed()) {
//							logger.info("s closed");
							sToClient.close();
						}
						if (ss != null && !ss.isClosed()) {
							ss.close();
//							logger.info("ss closed");
						}
						if (sToServer != null && !sToServer.isClosed()) {
							sToServer.close();
//							logger.info("sToServer closed");
						}
						
					} catch (IOException ex) {
//						logger.info(ex.toString() + " im catch vom finally im äußersten try");
					}
				}
			}
		};
		t1.start();
	}

	public static void main(String[] args) {
		try {
			writer = new FileWriter("za.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger = Logger.getLogger("MyLog");
//
		try {

			// This block configures the logger with handler and formatter
			fh = new FileHandler("zaLog.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		remoteIPAddress = args[0];
		remotePort = Integer.parseInt(args[1]);
		//// remoteIPAddress = "localhost";
		//// remotePort = 12345;
		running = true;
		logger.info("In Main von Zwischenanwendung");
		openGateway();
	
	}
}
