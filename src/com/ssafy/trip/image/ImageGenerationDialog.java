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
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/** 관광지 이미지와 사용자의 첨부 이미지를 이용해 AI 이미지를 표시하는 다이얼로그. */
public class ImageGenerationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final File destinationImageFile;
	private static final String NORMAL_PROMPT = "Use the first image as the tourist destination background. "
			+ "Place the person from the second image naturally in that destination. "
			+ "Preserve the person's facial features and clothing as much as possible. "
			+ "Create a realistic travel souvenir photo with natural lighting, shadows, and perspective.";
	private static final String FUN_PROMPT = "Use the first image as the tourist destination background and the second image as the person reference. "
			+ "Create a playful, funny travel souvenir photo. Keep the person recognizable, but give them an exaggerated joyful pose "
			+ "and make them interact humorously with a large, harmless landmark-related object in the scene. "
			+ "Keep it family-friendly, colorful, natural-looking, and clearly set in the destination.";

	private final JLabel destinationImageLabel = createImageLabel("관광지 이미지");
	private final JLabel userImageLabel = createImageLabel("사용자 이미지를 첨부하세요");
	private final JLabel normalResultImageLabel = createImageLabel("일반 AI 생성 이미지");
	private final JLabel funResultImageLabel = createImageLabel("재미있는 AI 생성 이미지");
	private final JButton attachButton = new JButton("이미지 첨부");
	private final JButton generateButton = new JButton("AI 생성 시작");
	private final JLabel waitingLabel = new JLabel(" ");
	private final OpenAiImageGenerationClient imageClient = new OpenAiImageGenerationClient();

	private File userImageFile;
	private int waitingSeconds;
	private Timer waitingTimer;
	
	
	public ImageGenerationDialog(Window owner) {
		this(owner, new File("img", "image01.jpg"));
	}

	public ImageGenerationDialog(Window owner, File destinationImageFile) {
		super(owner, "AI 여행 이미지 생성", ModalityType.APPLICATION_MODAL);
		this.destinationImageFile = destinationImageFile;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout(10, 10));
		setSize(980, 720);
		setLocationRelativeTo(owner);

		add(createImagePanels(), BorderLayout.CENTER);
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

	private JPanel createImagePanels() {
		JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		panel.add(createImageRow(destinationImageLabel, userImageLabel));
		panel.add(createImageRow(normalResultImageLabel, funResultImageLabel));
		return panel;
	}

	private JPanel createImageRow(JLabel leftImageLabel, JLabel rightImageLabel) {
		JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
		row.add(leftImageLabel);
		row.add(rightImageLabel);
		return row;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(attachButton);
		panel.add(generateButton);
		panel.add(waitingLabel);
		return panel;
	}

	private JLabel createImageLabel(String text) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(430, 240));
		label.setBorder(BorderFactory.createEtchedBorder());
		return label;
	}

	private void showDestinationImage() {
        if (destinationImageFile == null || !destinationImageFile.isFile()) {
            destinationImageLabel.setText("관광지 이미지를 찾지 못했습니다.");
            return;
        }
        destinationImageLabel.setIcon(
                new ImageIcon(scaleImage(new ImageIcon(destinationImageFile.getPath()).getImage(), 400, 220)));
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
        if (destinationImageFile == null || !destinationImageFile.isFile()) {
            JOptionPane.showMessageDialog(this, "관광지 이미지를 찾지 못했습니다.", "이미지 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

		generateButton.setEnabled(false);
		attachButton.setEnabled(false);
		normalResultImageLabel.setIcon(null);
		funResultImageLabel.setIcon(null);
		normalResultImageLabel.setText("일반 여행 사진을 생성하고 있습니다...");
		funResultImageLabel.setText("재미있는 여행 사진을 생성하고 있습니다...");
		startWaitingTimer();

		new SwingWorker<BufferedImage[], Void>() {
			@Override
			protected BufferedImage[] doInBackground() throws Exception {
				BufferedImage normalImage = imageClient.generate(destinationImageFile, userImageFile, NORMAL_PROMPT);
				BufferedImage funImage = imageClient.generate(destinationImageFile, userImageFile, FUN_PROMPT);
				return new BufferedImage[] { normalImage, funImage };
			}

			@Override
			protected void done() {
				stopWaitingTimer();
				generateButton.setEnabled(true);
				attachButton.setEnabled(true);
				try {
					BufferedImage[] results = get();
					normalResultImageLabel.setIcon(new ImageIcon(scaleImage(results[0], 400, 220)));
					normalResultImageLabel.setText("");
					funResultImageLabel.setIcon(new ImageIcon(scaleImage(results[1], 400, 220)));
					funResultImageLabel.setText("");
					waitingLabel.setText("생성 완료: " + waitingSeconds + "초");
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					showGenerationError(exception);
				} catch (ExecutionException exception) {
					showGenerationError(exception.getCause());
				}
			}
		}.execute();
	}

	private void startWaitingTimer() {
		waitingSeconds = 0;
		waitingLabel.setText("AI 이미지 생성 중... 0초");
		waitingTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				waitingSeconds++;
				waitingLabel.setText("AI 이미지 생성 중... " + waitingSeconds + "초");
			}
		});
		waitingTimer.start();
	}

	private void stopWaitingTimer() {
		if (waitingTimer != null) {
			waitingTimer.stop();
		}
	}

	private void showGenerationError(Throwable exception) {
		normalResultImageLabel.setIcon(null);
		funResultImageLabel.setIcon(null);
		normalResultImageLabel.setText("이미지 생성에 실패했습니다.");
		funResultImageLabel.setText("이미지 생성에 실패했습니다.");
		waitingLabel.setText("생성 실패: " + waitingSeconds + "초");
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
