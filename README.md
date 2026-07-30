# 공공데이터를 활용한 EnjoyTrip 서비스

> Java 프로그램 구축

SSAFY 16기 서울 14반 양유진, 정지혁


---

## 개발 및 실행 환경 (Environment)

- **Language:** Java 8 / 11 이상
- **GUI Framework:** Java Swing
- **Data Format:** XML (SAX Parser)
- **External API:** OpenAI API (`gpt-image-2`)

---

## 패키지 구조 (Package Structure)
```
com.ssafy.trip
├── image/                             # AI 이미지 생성 모듈
│   ├── ImageGenerationDialog.java     # AI 합성 결과 출력 다이얼로그 (Swing)
│   └── OpenAiImageGenerationClient.java # OpenAI Image Edit API 통신 클라이언트
├── model/                             # 데이터 및 비즈니스 로직
│   ├── dao/                           # 데이터 접근 계층
│   │   ├── TripDao.java               # 관광지 데이터 DAO 인터페이스
│   │   └── TripDaoImpl.java           # 관광지 데이터 DAO 구현체
│   ├── dto/                           # 데이터 전달 객체
│   │   ├── TripDto.java               # 관광지 정보 DTO
│   │   └── TripSearchDto.java         # 관광지 검색 조건 DTO
│   └── service/                       # 비즈니스 로직 계층
│       ├── TripService.java           # 관광지 서비스 인터페이스
│       └── TripServiceImpl.java       # 관광지 서비스 구현체
├── util/                              # XML 파싱 Utility
│   ├── TouristDestinationSAXHandler.java # SAX 파싱 핸들러
│   └── TouristDestinationSAXParser.java  # SAX 파서 실행 클래스
└── view/                              # 메인 UI
    └── TripInfoView.java              # 메인 관광지 정보 화면
```
---

## 요구사항 명세

| 순번 | 분류 | 요구사항 명 | 요구사항 명세 | 우선 순위 |
|:---:|---|---|---|:---:|
| F101 | 여행 | 관광지 정보 조회 | 관광지 정보를 얻어와 화면에 목록으로 표시 | 필수 |
| F102 | 여행 | 관광지 정보 조건 검색 | 관광지명, 주소를 조건으로 해당하는 관광자 정보들을 조회 | 필수 |
| F103 | 여행 | 관광지 정보 상세 조회 | 관광지 목록 화면에 원하는 관광지 클릭 시, 왼쪽 화면에 상세 정보 표시 | 필수 |
| F104 | 기타 | AI 관광 시뮬레이션 | 관광지 사진과 인물 사진을 합성한 AI 이미지 화면에 표시 (좌: 정돈된 분위기, 우: 재미있고, 역동적인 분위기) | 심화 |

## 요구사항 상세

### 1. 관광지 정보 조회

- 요구 사항 번호: F101
- XML 로 제공된 관광지 정보를 Parsing 하여 자바의 객체 형태로 변경하고, Swing 을 사용하여 화면에 표시한다
<img width="1178" height="787" alt="image" src="https://github.com/user-attachments/assets/52abf961-509b-4446-874b-948885aade45" />


### 2. 관광지 정보 조건 검색

- 요구 사항 번호: F102
- 관광지 정보 목록화면의 상단에 검색 조건과 검색어를 입력한 뒤 검색버튼을 클릭하면 조건에 맞는 관광지 정보를 표시한다.
#### 관광지명 키워드로 검색 시
<img width="593" height="691" alt="image" src="https://github.com/user-attachments/assets/bc7d4f60-6a01-43d4-898f-fe3592a19810" />

#### 주소 키워드로 검색 시
<img width="596" height="584" alt="image" src="https://github.com/user-attachments/assets/382e8c3f-9e2c-4efc-a3df-a73abc60f28b" />


### 3. 관광지 정보 상세 조회

- 요구 사항 번호: F103
- 오른쪽 목록화면의 관광지를 클릭하면 왼쪽에 상세 조회 결과 화면이 출력된다.
<img width="1177" height="783" alt="image" src="https://github.com/user-attachments/assets/063b9b5f-5dc6-490e-a873-6639f084ebea" />


### 4. AI 관광 시뮬레이션

- 요구 사항 번호: F104
- F103 관광자 정보 상세 조회 화면에 배치된 AI 이미지 버튼을 클릭하면 사용자의 사진과 관광지 사진을 적절히 합성한 2장의 이미지가 생성된다.
- 사용 API: OpenAI gpt-image-2
<img width="961" height="704" alt="스크린샷 2026-07-30 151939" src="https://github.com/user-attachments/assets/5ea7667b-be10-4cdc-a68f-00e93df2ece7" />
<img width="1448" height="1086" alt="travel_normal_20260730_152128" src="https://github.com/user-attachments/assets/75af8d15-3dff-4f78-8134-b792b1b54af6" />
<img width="1448" height="1086" alt="travel_fun_20260730_152128" src="https://github.com/user-attachments/assets/74d131a5-229e-4514-b280-56b8a3788ff4" />

