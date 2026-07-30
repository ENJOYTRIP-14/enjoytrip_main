package com.ssafy.trip.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.ssafy.trip.image.ImageGenerationDialog;
import com.ssafy.trip.model.dto.TripDto;
import com.ssafy.trip.model.dto.TripSearchDto;
import com.ssafy.trip.model.service.TripService;
import com.ssafy.trip.model.service.TripServiceImpl;

public class TripInfoView {

	/** model들 */
	private TripService tripService;

	/** main 화면 */
	private JFrame frame;

	/** 관광지 이미지 표시 Panel */
	private JLabel imgL;
	private JLabel[] tripInfoL;

	/** 조회 조건 */
	private JComboBox<String> findC;
	private JTextField wordTf;
	private JButton searchBt;

	/** 조회 내용 표시할 table */
	private DefaultTableModel tripModel;
	private JTable tripTable;
	private JScrollPane tripPan;
	private String[] title = { "번호", "관광지명", "도로명주소", "지번주소", "전화번호" };

	/** 검색 조건 */
	private String key;
	private String[] choice = { "검색조건선택", "관광지명", "주소" };
	/** 검색할 단어 */
	private String word;

	/** 화면에 표시하고 있는 주택 */
	private TripDto curTrip;
	
	/** 심화 기능: 이미지 생성 기능**/
	private JButton generateImageBt;

	public TripInfoView() {
		/* Service들 생성 */
		tripService = new TripServiceImpl();

		/* 메인 화면 설정 */
		frame = new JFrame("Enjoy! Trip - 즐거운 여행");
//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e){
//				frame.dispose();
//			}
//		});

		setMain();

		frame.setSize(1200, 800);
		frame.setResizable(true);
		frame.setVisible(true);
		showTripInfo(0);
	}

	private void showTripInfo(int num) {
		curTrip = tripService.search(num);
		
		if(curTrip == null) {
			return;
		}

		tripInfoL[0].setText("");
		tripInfoL[1].setText("");
		tripInfoL[2].setText(curTrip.getTouristDestination());
		tripInfoL[3].setText(curTrip.getStreetAddress());
		tripInfoL[4].setText(curTrip.getLotAddress());
		tripInfoL[5].setText(curTrip.getLat() + "");
		tripInfoL[6].setText(curTrip.getLng() + "");
		tripInfoL[7].setText(curTrip.getTel());
		tripInfoL[8].setText(curTrip.getInfo());
		tripInfoL[9].setText("");

		String img = curTrip.getImg();
	    
	    // DB의 img 값이 비어있거나 실제 파일이 img 폴더에 없는 경우
	    if (img == null || img.trim().length() == 0 || !new File("img", img).exists()) {
	        int randomNum = (int)(Math.random() * 11) + 1;
	        img = String.format("image%02d.jpg", randomNum);
	        
	        // ★ 핵심: curTrip DTO의 img 필드에도 매핑된 실제 이미지 파일명을 세팅해줍니다!
	        curTrip.setImg(img); 
	    }

	    // 셋팅된 이미지를 화면 Label에 배치
	    ImageIcon icon = new ImageIcon("img/" + img);
	    Image image = icon.getImage();
	    Image changeImage = image.getScaledInstance(570, 470, Image.SCALE_SMOOTH);
	    ImageIcon changeIcon = new ImageIcon(changeImage);
	    imgL.setIcon(changeIcon);
	}

	/** 메인 화면인 관광지 목록을 위한 화면 셋팅하는 메서드 */
	public void setMain() {

		/* 왼쪽 화면을 위한 설정 */
		JPanel left = new JPanel(new BorderLayout());
		JPanel leftCenter = new JPanel(new BorderLayout(0, 10));
		JPanel leftR = new JPanel(new GridLayout(10, 2));
		leftR.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		String[] info = { "", "", "관광지명", "도로명주소", "지번주소", "위도", "경도", "전화번호", "관광지정보", "" };
		int size = info.length;
		JLabel infoL[] = new JLabel[size];
		tripInfoL = new JLabel[size];
		for (int i = 0; i < size; i++) {
			infoL[i] = new JLabel(info[i]);
			tripInfoL[i] = new JLabel("");
			leftR.add(infoL[i]);
			leftR.add(tripInfoL[i]);
		}
		imgL = new JLabel();
		
		generateImageBt = new JButton("AI 이미지 생성");
		
		JPanel imgPanel = new JPanel(new BorderLayout(0,5));
		imgPanel.add(imgL, "Center");
		imgPanel.add(generateImageBt, "South");
				
		leftCenter.add(imgPanel, "Center");
		leftCenter.add(leftR, "South");

		left.add(new JLabel("관광지 정보", JLabel.CENTER), "North");
		left.add(leftCenter, "Center");

		/* 오른쪽 화면을 위한 설정 */
		JPanel right = new JPanel(new BorderLayout());
		JPanel rightTop = new JPanel(new GridLayout(4, 2));

		JPanel rightTop2 = new JPanel(new GridLayout(1, 3));
		String[] item = { "검색조건선택", "관광지명", "주소" };
		findC = new JComboBox<String>(item);
		wordTf = new JTextField();
		searchBt = new JButton("검색");

		rightTop2.add(findC);
		rightTop2.add(wordTf);
		rightTop2.add(searchBt);

		rightTop.add(new Label(""));
		rightTop.add(new Label(""));
		rightTop.add(rightTop2);
		rightTop.add(new Label(""));

		JPanel rightCenter = new JPanel(new BorderLayout());
		tripModel = new DefaultTableModel(title, 20);
		tripTable = new JTable(tripModel);
		tripPan = new JScrollPane(tripTable);
		tripTable.setColumnSelectionAllowed(true);
		rightCenter.add(new JLabel("광광지 정보", JLabel.CENTER), "North");
		rightCenter.add(tripPan, "Center");

		right.add(rightTop, "North");
		right.add(rightCenter, "Center");

		JPanel mainP = new JPanel(new GridLayout(1, 2));

		mainP.add(left);
		mainP.add(right);

		mainP.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		frame.add(mainP, "Center");

		tripTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int row = tripTable.getSelectedRow();
				if(row == -1)return;
				
				int code = Integer.parseInt(((String) tripModel.getValueAt(row, 0)).trim());
				
				if(curTrip == null || curTrip.getNum() != code) {					
					showTripInfo(code);
				}
			}
		});

		// complete code #01
		 searchBt.addActionListener(e-> searchTrips());
		 wordTf.addActionListener(e -> searchTrips());
		 
		 generateImageBt.addActionListener(e -> {
			    // 1. 선택된 관광지가 없는 경우 예외 처리
			    if (curTrip == null) {
			        JOptionPane.showMessageDialog(frame, "관광지를 먼저 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
			        return;
			    }

			    // 2. showTripInfo()에서 이미 검증 완료된 이미지 파일 가져오기
			    File destinationImgFile = new File("img", curTrip.getImg());

			    // 3. 다이얼로그 호출 (화면에 보이는 것과 100% 동일한 파일이 전달됨)
			    ImageGenerationDialog dialog = new ImageGenerationDialog(frame, destinationImgFile);
			    dialog.setVisible(true);
			});

		showTrips();
	}

	/** 검색 조건에 맞는 관광지 검색 */
	private void searchTrips() {
		word = wordTf.getText().trim();
		key = choice[findC.getSelectedIndex()];
		showTrips();
	}

	/**
	 * 관광지 목록을 갱신하기 위한 메서드
	 */
	public void showTrips() {
		TripSearchDto tripSearchDto = new TripSearchDto();
		if (key != null) {
			if (key.equals("관광지명")) {
				tripSearchDto.setTouristDestination(word);
			} else if (key.equals("주소")) {
				tripSearchDto.setSido(word);
			}
		}

		if (word == null || word.trim().length() == 0)
			findC.setSelectedIndex(0);

		List<TripDto> trips = tripService.searchAll(tripSearchDto);
		if (trips != null) {
			int i = 0;
			String[][] data = new String[trips.size()][5];
			for (TripDto trip : trips) {
				data[i][0] = "" + trip.getNum();
				data[i][1] = trip.getTouristDestination();
				data[i][2] = trip.getStreetAddress();
				data[i][3] = trip.getLotAddress();
				data[i++][4] = trip.getTel();
			}
			tripModel.setDataVector(data, title);
		}
	}

	public static void main(String[] args) {
		new TripInfoView();
	}
}
