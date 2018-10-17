package testing;

import dasspielv2.DasSpielV2;
import dasspielv1.DasSpielV1;

public class Play {

	public static void main(String[] args) {
		
//		DasSpielV1 g = new DasSpielV1("192.168.33.1", 57854);
		DasSpielV2 g = new DasSpielV2("192.168.33.1", 49763);
		
		g.connect("Yannis");
//
		g.spawnPlayer();

//		g.rotatePlayer(180);
//		g.getStatus();
		g.movePlayer(5);

//		while(g.getDistToWall() > 6) {
//			try {
//				Thread.sleep(20);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		g.movePlayer(0);
//		g.rotatePlayer(180);
		System.out.println(g.getDistToWall());
		g.startDrawing("Green");
//		g.getStatus();
//		g.movePlayer(15);
//		for (int i = 0; i < 30; i++) {
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			g.rotatePlayer(30);
//			g.shootBullet();
//		}
//		g.rotatePlayer(90);
//		g.stopAndClearDrawing();
		g.getStatus();
//
		g.deletePlayer();
		g.disconnect();

	}

}
