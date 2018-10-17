package dasspielv2;

import java.io.IOException;
import java.net.Socket;
import java.rmi.UnknownHostException;

public class Availability {

	public static boolean isHostAvailable(String ip, int port) {
		Socket s = null;
		boolean available = false;
//		System.out.println("in isHostAvailable");
		try {
//			System.out.println("im try bei isHostAvailable");
			s = new Socket(ip, port);
//			System.out.println("nach dem s = new socket im try");
			if (s.isConnected()) {
//				System.out.println("s.isConnected");
				s.close();
				available = true;
			}
		} catch (UnknownHostException e) { // unknown host
			available = false;
			s = null;
//			System.out.println("unknownhostexception");
		} catch (IOException e) { // io exception, service probably not running
			available = false;
			s = null;
//			System.out.println("ioexception");
		} catch (NullPointerException e) {
			available = false;
			s = null;
//			System.out.println("nullpointexception");
		}
//		System.out.println("available before return: " + available);
		return available;
	}

	public static boolean isJavaProgramRunning(String programName) {
		String osName = System.getProperty("os.name");
		String command = null;
		if(osName.contains("Windows")) {
			command = "cmd /c jps -l | findstr \"" + programName + "\"";
		}
		else if(osName.contains("Linux")) {
			command = "/bin/sh -c jps -l | grep \"" + programName + "\"";
		}
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			int exitVal = p.exitValue(); // Returns 0 if running, 1 if not
//			System.out.println("exitVal in isJavaProgramRunning: " + exitVal);
			if (exitVal == 0)
				return true;
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}
}
