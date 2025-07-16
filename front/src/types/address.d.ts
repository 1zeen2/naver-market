// src/types/address.d.ts

// Daum 우편번호 서비스의 전역 객체 타입 선언 (이 파일에서만 존재해야 합니다!)
declare global {
  interface Window {
    daum?: {
      Postcode: new (options: {
        oncomplete: (data: DaumPostcodeData) => void;
        onresize?: (size: { width: number; height: number }) => void;
        onclose?: (state: string) => void;
        width?: string;
        height?: string;
      }) => {
        open: (options?: {
          left?: number;
          top?: number;
          popupTitle?: string;
          popupKey?: string;
        }) => void;
      };
    };
  }
}

// Daum Postcode API에서 반환하는 데이터 타입 (완전성을 높여 정의)
export interface DaumPostcodeData {
  address: string;          // 전체 주소 (예: 서울 강남구 테헤란로 42길 5)
  addressEnglish: string;   // 영문 전체 주소
  addressType: 'R' | 'J';   // R: 도로명, J: 지번
  apartment: 'Y' | 'N';     // 공동주택 여부
  autoJibunAddress: string; // 자동 생성된 지번 주소
  autoJibunAddressEnglish: string; // 자동 생성된 영문 지번 주소
  autoRoadAddress: string;  // 자동 생성된 도로명 주소
  autoRoadAddressEnglish: string; // 자동 생성된 영문 도로명 주소
  bname: string;            // 법정동/법정리 이름
  bname1: string;           // 법정동/법정리 이름 (상위)
  bname2: string;           // 법정동/법정리 이름 (하위)
  bnameEnglish: string;     // 영문 법정동/법정리 이름
  buildingCode: string;     // 건물 코드
  buildingName: string;     // 건물명
  hname: string;            // 행정동 이름
  jibunAddress: string;     // 지번 주소
  jibunAddressEnglish: string; // 영문 지번 주소
  noSuggest: 'Y' | 'N';     // 추천 주소 여부
  postcode: string;         // 구 우편번호 (6자리)
  postcode1: string;        // 구 우편번호 첫 3자리
  postcode2: string;        // 구 우편번호 마지막 3자리
  postcodeMR: string;       // 구 우편번호 MR
  query: string;            // 검색 쿼리
  roadAddress: string;      // 도로명 주소
  roadAddressEnglish: string; // 영문 도로명 주소
  roadname: string;         // 도로명
  roadnameCode: string;     // 도로명 코드
  roadnameEnglish: string;  // 영문 도로명
  sido: string;             // 시/도
  sidoEnglish: string;      // 영문 시/도
  sigungu: string;          // 시/군/구
  sigunguCode: string;      // 시/군/구 코드
  sigunguEnglish: string;   // 영문 시/군/구
  userLanguageType: 'K' | 'E'; // 사용자가 선택한 언어 타입
  userSelectedType: 'R' | 'J'; // 사용자가 선택한 주소 타입
  x: string;                // X 좌표
  y: string;                // Y 좌표
  zonecode: string;         // 새 우편번호 (5자리)
}

// 애플리케이션 내에서 주소 데이터를 처리하기 위한 인터페이스
// Daum API 응답을 가공하여 실제 폼 데이터 등으로 사용될 타입을 정의합니다.
export interface ProcessedAddressData { // 또는 AddressFormData 등
  zonecode: string;
  address: string;
  detailAddress: string; // 사용자로부터 직접 입력받을 상세 주소
}