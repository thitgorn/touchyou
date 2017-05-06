package touchyou.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import touchyou.util.GuiUtil;

public class Mobile extends JPanel {
	private final String Image_URL = "/images/phone.png";
	private Image img;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Mobile(int width, int height) {
		// now 489 * 599
		setPreferredSize(new Dimension(width, height));

		setBorder(GuiUtil.getBorder());
		initcomponent();
	}

	private void initcomponent() {
		URL url = this.getClass().getResource(Image_URL);
		img = new ImageIcon(url).getImage();
		setMobileSize(300, 500);
		JLabel mobile = new JLabel(new ImageIcon(img));
		add(mobile);
	}

	public void setMobileSize(int width, int height) {
		img = getScaledImage(img, width, height);
	}
	
	private Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}
}
