/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoMapComponent extends Component {
	
	private BufferedImage mapImage;

	private Collection<ControlCircle> controls;

	private Punch startPunch;
	
	public void loadMapImage(String filename) {
		try {
			mapImage = ImageIO.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public Dimension getPreferredSize() {
        return new Dimension(mapImage.getWidth(), mapImage.getHeight());
    }
	
	public Dimension getMinimumSize() {
		return new Dimension(800, 500);
	}
	
	public void setControls(Collection<ControlCircle> controls) {
		this.controls = controls;
		repaint();
	}
	
	public void showTrace(Punch punch) {
		this.startPunch = punch;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		g2.drawImage(mapImage, null, 0, 0);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 20));
		if( controls!=null ) {
			for (ControlCircle control : controls) {
				control.drawOn(g2);
			}
		}
		if( startPunch!=null ) {
			startPunch.drawOn(g2);
		}
	}
	
	
}
