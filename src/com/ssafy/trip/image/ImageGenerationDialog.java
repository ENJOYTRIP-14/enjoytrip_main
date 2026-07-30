package com.ssafy.trip.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/** 관광지 이미지와 사용자의 첨부 이미지를 이용해 AI 이미지를 표시하는 다이얼로그. */
public class ImageGenerationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final File DESTINATION_IMAGE_FILE = new File("img", "image01.jpg");

	private final JLabel destinationImageLabel = createImageLabel("관광지 이미지");
	private final JLabel userImageLabel = createImageLabel("사용자 이미지를 첨부하세요");
	private final JLabel resultImageLabel = createImageLabel("생성 결과가 여기에 표시됩니다");
	private final JButton attachButton = new JButton("이미지 첨부");
	private final JButton generateButton = new JButton("AI 생성 시작");
	private final OpenAiImageGenerationClient imageClient = new OpenAiImageGenerationClient();

	private File userImageFile;

	public ImageGenerationDialog(Window owner) {
		super(owner, "AI 여행 이미지 생성", ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout(10, 10));
		setSize(980, 650);
		setLocationRelativeTo(owner);

		add(createInputPanel(), BorderLayout.NORTH);
		add(resultImageLabel, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		showDestinationImage();
		attachButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectUserImage();
			}
		});
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				generateImage();
			}
		});
	}

	private JPanel createInputPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		panel.add(destinationImageLabel);
		panel.add(userImageLabel);
		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(attachButton);
		panel.add(generateButton);
		return panel;
	}

	private JLabel createImageLabel(String text) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(430, 240));
		label.setBorder(BorderFactory.createEtchedBorder());
		return label;
	}

	private void showDestinationImage() {
		if (!DESTINATION_IMAGE_FILE.isFile()) {
			destinationImageLabel.setText("임시 관광지 이미지를 찾지 못했습니다: img/image01.jpg");
			return;
		}
		destinationImageLabel.setIcon(
				new ImageIcon(scaleImage(new ImageIcon(DESTINATION_IMAGE_FILE.getPath()).getImage(), 400, 220)));
		destinationImageLabel.setText("");
	}

	private void selectUserImage() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "jpeg", "png", "webp"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		userImageFile = chooser.getSelectedFile();
		Image selectedImage = new ImageIcon(userImageFile.getPath()).getImage();
		userImageLabel.setIcon(new ImageIcon(scaleImage(selectedImage, 400, 220)));
		userImageLabel.setText("");
	}

	private void generateImage() {
		if (userImageFile == null) {
			JOptionPane.showMessageDialog(this, "먼저 사용자 이미지를 첨부하세요.", "이미지 필요", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!DESTINATION_IMAGE_FILE.isFile()) {
			JOptionPane.showMessageDialog(this, "임시 관광지 이미지(img/image01.jpg)를 찾지 못했습니다.", "이미지 필요",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		generateButton.setEnabled(false);
		attachButton.setEnabled(false);
		resultImageLabel.setIcon(null);
		resultImageLabel.setText("AI 이미지를 생성하고 있습니다...");

		new SwingWorker<BufferedImage, Void>() {
			@Override
			protected BufferedImage doInBackground() throws Exception {
				return imageClient.generate(DESTINATION_IMAGE_FILE, userImageFile);
			}

			@Override
			protected void done() {
				generateButton.setEnabled(true);
				attachButton.setEnabled(true);
				try {
					BufferedImage result = get();
					resultImageLabel.setIcon(new ImageIcon(scaleImage(result, 850, 340)));
					resultImageLabel.setText("");
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					showGenerationError(exception);
				} catch (ExecutionException exception) {
					showGenerationError(exception.getCause());
				}
			}
		}.execute();
	}

	private void showGenerationError(Throwable exception) {
		resultImageLabel.setIcon(null);
		resultImageLabel.setText("이미지 생성에 실패했습니다.");
		String message = exception.getMessage() == null ? "알 수 없는 오류" : exception.getMessage();
		JOptionPane.showMessageDialog(this, message, "이미지 생성 실패", JOptionPane.ERROR_MESSAGE);
	}

	private Image scaleImage(Image image, int maxWidth, int maxHeight) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (width <= 0 || height <= 0) {
			return image;
		}
		double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
		return image.getScaledInstance((int) (width * ratio), (int) (height * ratio), Image.SCALE_SMOOTH);
	}

	/** TripInfoView 연동 전 단독 화면 확인용 실행 메서드. */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ImageGenerationDialog(new JFrame()).setVisible(true);
			}
		});
	}
}
