/**
 * @file 실제 회원가입 폼을 렌더링하는 컴포넌트
 * @description
 * 이 컴포넌트는 사용자 회원가입을 위한 UI와 클라이언트 측 로직을 담당합니다.
 * 폼 데이터 관리, 실시간 유효성 검사, 아이디 중복 확인, 주소 검색 연동, 최종 회원가입 API 호출 등
 * 회원가입 플로우의 핵심 기능을 통합하여 제공합니다.
 *
 * 🎯 **주요 목표 (Goals):**
 * 1. 사용자에게 직관적이고 피드백이 명확한 회원가입 경험 제공.
 * 2. 외부 훅(useForm, useValidation, useAuthApi, useAddressSearch)을 활용하여 관심사를 분리하고 코드 재사용성을 높임.
 * 3. 아이디 중복 확인 결과 메시지(성공/실패)를 사용자에게 지속적으로 표시하여 편의성 증대.
 *
 * 🛠️ **설계 원칙 (Design Principles):**
 * - **모듈화 (Modularity):** 핵심 폼 로직을 외부 훅으로 분리하여 SignUpForm 컴포넌트의 복잡도를 낮춤.
 * - **관심사 분리 (Separation of Concerns):** UI 렌더링, 폼 데이터 관리, 유효성 검사, API 통신 등 각 기능을 전담하는 훅과 컴포넌트 사용.
 * - **상태 관리 명확성 (Clear State Management):** 아이디 중복 확인 결과, API 에러/성공 메시지 등을 명확히 분리하여 혼동 방지.
 * - **사용자 경험 (User Experience):** 아이디 중복 확인 메시지 지속성, 실시간 유효성 피드백 제공.
 *
 * 🔗 **관련 파일/컴포넌트 (Related Files/Components):**
 * - `src/app/signup/page.tsx`: 이 컴포넌트를 렌더링하는 상위 페이지.
 * - `src/hooks/useForm.ts`: 폼 데이터 상태 및 `handleChange` 로직.
 * - `src/hooks/useValidation.ts`: 클라이언트 측 폼 유효성 검사 로직.
 * - `src/hooks/useAuthApi.ts`: 백엔드 인증 관련 API(아이디 중복 확인, 회원가입) 호출 및 결과 상태 관리.
 * - `src/hooks/useAddressSearch.ts`: Daum Postcode API 연동 및 주소 검색 로직.
 * - `src/components/ui/*.tsx`: 재사용 가능한 UI 컴포넌트 (Input, Select, Button).
 * - `src/types/member.ts`: 회원 관련 데이터 타입 정의.
 */

'use client'; // Next.js 클라이언트 컴포넌트

import React, { useState, useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';

import { useForm } from '@/hooks/useForm';              // 폼 데이터 상태 관리 커스텀 훅
import { useValidation } from '@/hooks/useValidation';  // 폼 유효성 검사 로직 커스텀 훅
import { useAuthApi } from '@/hooks/useAuthApi';        // 인증 관련 API 호출 커스텀 훅 (TanStack Query 대신 일반 fetch 훅이라고 가정)

import Input from '@/components/ui/Input';              // 재사용 가능한 Input 컴포넌트
import Button from '@/components/ui/Button';            // 재사용 가능한 Button 컴포넌트
import Select from '@/components/ui/Select';            // 재사용 가능한 Select 컴포넌트
import AddressSearchModal from '@/components/member/AddressSearchModal'; // 주소 검색 모달 컴포넌트

import { SignupFormData, SignupApiRequest } from '@/types/member'; // 회원가입 폼 데이터 타입 정의

/**
 * @typedef {object} APIResponse
 * @property {boolean} isAvailable - API 응답의 가용성 여부 (아이디/이메일 중복 확인 시)
 * @property {string} [message] - API 응답 메시지 (성공 또는 실패 이유)
 */

/**
 * @function SignUpForm
 * @description 회원가입 폼을 렌더링하고 사용자 입력을 처리하며, 유효성 검사 및 API 호출을 담당합니다.
 * Next.js 15.1.7 (App Router) 환경에 최적화된 함수형 컴포넌트입니다.
 */

function SignUpForm() {
  const router = useRouter();

  // 폼 데이터 관리
  const { formData, handleChange, setFormData, resetForm } = useForm<SignupFormData>({
    userId: '',
    userPwd: '', confirmUserPwd: '',
    emailLocalPart: '', emailDomain: '',
    phonePrefix: '010', phoneMiddle: '', phoneLast: '',
    userName: '',
    dateOfBirth: '',
    gender: '',
    address: '', detailAddress: '',
    zonecode: '',
  });

  // API 호출 관련 상태 및 함수 (인증 관련)
  const { 
    isLoading,
    error: apiError, 
    successMessage, 
    checkUserIdAvailability, 
    checkEmailAvailability: checkEmailAvailabilityApi, 
    signupUser, 
    setError: setApiError, 
    setSuccessMessage 
  } = useAuthApi();

  // 아이디 중복 확인 결과 상태
  const [isUserIdAvailable, setIsUserIdAvailable] = useState<boolean | null>(null); // null: 확인 전, true: 사용 가능, false: 사용 불가
  const [lastCheckedUserId, setLastCheckedUserId] = useState<string | null>(null); // 마지막으로 중복 확인한 아이디
  
  // 이메일 중복 확인 결과 상태
  const [isEmailAvailable, setIsEmailAvailable] = useState<boolean | null>(null); // null: 확인 전, true: 사용 가능, false: 사용 불가
  const [lastCheckedEmail, setLastCheckedEmail] = useState<string | null>(null); // 마지막으로 중복 확인한 아이디

  // 주소 검색 모달의 열림/닫힘 상태 관리
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);

  // 클라이언트 측 유효성 검사
  const { 
    errors, 
    validateForm, 
    handleFieldChangeAndValidate, // Input의 onBlur, onChange에 직접 연결
    setErrors,
    validateField, // 개별 필드 유효성 검사 (아이디/이메일 중복 확인 버튼 클릭 시)
    clearFormError 
  } = useValidation(formData, isUserIdAvailable, isEmailAvailable);

  /**
   * @function handleOpenAddressModal
   * @description 주소 검색 모달을 엽니다.
   */
  const handleOpenAddressModal = useCallback(() => {
    setIsAddressModalOpen(true)
  }, []);

  /**
   * @function handleCloseAddressModal
   * @description 주소 검색 모달을 닫습니다.
   */
  const handleCloseAddressModal = useCallback(() => {
    setIsAddressModalOpen(false);
  }, []);

  /**
   * @function handleCompleteAddressSearch
   * @description AddressSearchModal에서 주소 선택이 완료되었을 때 호출되는 콜백 함수입니다.
   * 선택된 주소와 우편번호를 formData에 업데이트하고 모달을 닫습니다.
   * @param {object} data - 주소 검색 결과 객체. { address: string; zonecode: string }
   */
  const handleCompleteAddressSearch = useCallback((data: { address: string; zonecode: string }) => {
    setFormData(prev => ({
      ...prev,
      address: data.address,
      zonecode: data.zonecode,
    }));
    // 주소 필드 유효성 검사 에러 초기화 및 유효성 검사 재실행 (필요하다면)
    setErrors(prev => ({ ...prev, address: ''}));
    handleFieldChangeAndValidate({ target: {name: 'address', value: data.address } } as React.ChangeEvent<HTMLInputElement>);

    handleCloseAddressModal(); // 주소 선택 처리 후 모달 닫기
  }, [setFormData, setErrors, handleCloseAddressModal, handleFieldChangeAndValidate]);

  // userId 변경 시 중복 확인 상태 초기화 (useForm의 handleChange와 분리, lastCheckedUserId를 사용하여 사용자 경험 개선)
  useEffect(() => {
    // 현재 입력된 userId가 마지막으로 확인한 userId와 다르고, 이전에 확인한 기록이 있다면
    if (formData.userId !== lastCheckedUserId && lastCheckedUserId !== null) {
      setIsUserIdAvailable(null); // 중복 확인 상태 초기화
      setErrors(prev => ({ ...prev, userId: ''}));
      setApiError(null); // Api 에러 메시지 초기화 (useAuthApi에서 온 전체 에러)
      setSuccessMessage(null); // Api 성공 메시지 초기화 (useAuthApi에서 온 전체 성공 메시지)
    }
  }, [formData.userId, lastCheckedUserId, setIsUserIdAvailable, setErrors, setApiError, setSuccessMessage]);

  // 이메일 중복 확인 상태 초기화
  useEffect(() => {
    const currentFullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;
    // 이메일 파트 중 하나라도 바뀌었거나, 마지막으로 확인한 이메일과 다르고, 이전에 확인한 기록이 있다면
    if (currentFullEmail !== lastCheckedEmail && lastCheckedEmail !== null) {
      setIsEmailAvailable(null); // 이메일 중복 확인 상태 초기화
      setErrors(prev => ({ ...prev, email: '' })); // 클라이언트 측 이메일 유효성 에러 메시지 초기화
      // 이메일 API 관련 에러/성공 메시지는 useAuthApi에서 전체적으로 관리하므로
      // 여기서는 특정 이메일 에러만 초기화하기 어려울 수 있음.
      // 필요에 따라 useAuthApi에 특정 메시지를 초기화하는 함수를 추가할 수도 있음.
    }
  }, [formData.emailLocalPart, formData.emailDomain, lastCheckedEmail, setIsEmailAvailable, setErrors]);

  // 아이디 중복 확인 API 호출
  const handleCheckUserId = useCallback(async () => {
    // API 호출 전 클라이언트 측 validation
    const userIdError = validateField('userId', formData.userId);

    if (userIdError) {
        setErrors(prevErrors => ({ ...prevErrors, userId: userIdError }));
        setIsUserIdAvailable(null); // 에러가 있으면 중복 확인 상태 초기화
        return;
    }

    // 1. 기존 아이디 관련 메시지 모두 초기화 (새로운 확인 시작 전)
    setErrors(prev => ({ ...prev, userId: '' })); // 클라이언트 측 에러 초기화
    setApiError(null);                             // API 에러 초기화
    setSuccessMessage(null);                       // API 성공 메시지 초기화
    setIsUserIdAvailable(null);                    // 중복 확인 상태도 초기화

    // 2. API 훅을 통해 중복 확인 요청
    const { isAvailable, message } = await checkUserIdAvailability(formData.userId);

    // 3. 마지막으로 확인한 아이디와 그 결과를 저장
    setLastCheckedUserId(formData.userId);
    setIsUserIdAvailable(isAvailable); // checkUserIdAvailability에서 반환된 결과 (true/false/null)

    // 4. 사용자에게 피드백 메시지 표시
    if (isAvailable) {// 'message'가 undefined인 경우 'null'로 변환
      setSuccessMessage(message ?? null); 
    } else {
      setApiError(message ?? null);
    }
  }, [formData.userId, checkUserIdAvailability, setErrors, setApiError, setSuccessMessage, setLastCheckedUserId, validateField]);

  // 이메일 중복 확인 핸들러
  const handleCheckEmail = useCallback(async () => {
    const fullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;

    setErrors(prev => ({ ...prev, email: '' }));
    setApiError(null);
    setSuccessMessage(null);
    setIsEmailAvailable(null);

    // 1. 클라이언트 측 이메일 유효성 검사
    const emailError = validateField('email', fullEmail); // 이메일 전체 주소를 validateField에 전달

    if (emailError) {
      setErrors(prevErrors =>({ ...prevErrors, email: emailError}));
      setIsEmailAvailable(null);
      return;
    }

    // 2. API 훅을 통해 중복 확인 요청
    const { isAvailable, message } = await checkEmailAvailabilityApi(fullEmail); // useAuthApi에서 { isAvailable, message } 반환 가정

    // 3. 마지막으로 확인한 이메일과 그 결과를 저장
    setLastCheckedEmail(fullEmail);
    setIsEmailAvailable(isAvailable);

    if (isAvailable) {
      setSuccessMessage(message ?? null);
    } else {
      setApiError(message ?? null);
    }}, 
    [
      formData.emailLocalPart,
      formData.emailDomain,
      checkEmailAvailabilityApi,
      setErrors, 
      setApiError, 
      setSuccessMessage, 
      setLastCheckedEmail,
      validateField
    ]
  );

  // 최종 폼 제출 핸들러
  const handleSubmit = useCallback(async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // 1. 전체 폼 유효성 검사 실행
    const isValid = validateForm();
    if (!isValid) {
      setErrors(prev => ({ ...prev, form: '입력된 정보를 확인해주세요.' }));
      return;
    }

    // 2. 백엔드 API 요청 데이터 준비 (백엔드 SignupApiRequest 형태)
    const signupData: SignupApiRequest = {
      userId: formData.userId,
      userPwd: formData.userPwd,
      email: `${formData.emailLocalPart}@${formData.emailDomain}`,
      phone: `${formData.phonePrefix}${formData.phoneMiddle}${formData.phoneLast}`,
      userName: formData.userName,
      dateOfBirth: formData.dateOfBirth,
      gender: formData.gender,
      address: formData.address,
      detailAddress: formData.detailAddress,
      zonecode: formData.zonecode,
    };

    // 3. 회원가입 API 호출
    const success = await signupUser(signupData);

    // 4. API 호출 결과에 따른 후처리
    if (success) {
      alert('회원 가입에 성공했습니다!');
      router.push('/'); // 성공 시 메인 페이지로 이동
    } else {
      // API 훅에서 이미 에러 메시지가 설정되었으므로, 여기서는 추가 작업 불필요
    }
  }, [formData, validateForm, signupUser, router, setErrors]);


  // 입력 필드 포커스 아웃 시 유효성 검사 실행 (handleChange와 통합하지 않고 별도 관리)
  const handleBlur = useCallback((e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => {
    handleFieldChangeAndValidate(e as React.ChangeEvent<HTMLInputElement | HTMLSelectElement>);
  }, [handleFieldChangeAndValidate]);

  // JSX 렌더링 시작
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold mb-6 text-center">회원가입</h2>
        <form onSubmit={handleSubmit}>
          {/* 전체 폼 제출 관련 API 에러/성공 메시지 (아이디 중복 확인 메시지와는 다름) */}
          {apiError && !apiError.includes('아이디') && <p className="text-red-500 text-center mb-4">{apiError}</p>}
          {successMessage && !apiError && !successMessage.includes('아이디') && <p className="text-green-600 text-center mb-4">{successMessage}</p>}
          {errors.form && <p className="text-red-500 text-center mb-4">{errors.form}</p>}

          {/* --- 아이디 입력 필드와 중복 확인 버튼 섹션 --- */}
          <div className="flex items-center mb-4">
            <Input
              label="아이디"
              name="userId"
              type="text"
              value={formData.userId}
              onChange={handleChange} // onChange 시에는 단순히 값만 업데이트
              onBlur={handleBlur}     // onBlur 시에만 유효성 검사 및 메시지 표시
                                      // error prop을 null로 설정하여여 모든 아이디 관련 메시지를 아래 별도 div에서 처리.
              error={undefined} 
              className="flex-grow mr-2" 
            />
            <Button
              onClick={handleCheckUserId}
              type="button"
              // `errors.userId`가 비어있지 않고, 그 값이 '아이디는 3자리 이상이어야 합니다.'가 아닐 때만 비활성화
              disabled={
                isLoading || 
                !formData.userId.trim() || 
                formData.userId.trim().length < 3 || 
                (!!errors.userId && errors.userId !== '아이디는 3자리 이상이어야 합니다.')
              }
              className="shrink-0 mt-2"
            >
              {isLoading ? '확인 중...' : '중복 확인'} 
            </Button>
          </div>

          {/* ✨ 아이디 관련 모든 메시지를 표시할 통합 영역 ✨ */}
          {/* 이 div는 아이디 입력 필드 바로 아래에 위치하며, 항상 동일한 높이를 가집니다. */}
          <div className="min-h-[1.25rem] -mt-8 mb-4">
            {successMessage && successMessage.includes('아이디') && !errors.userId && !apiError ? (
              // 성공 메시지가 있을 때 (초록색)
              <p className="text-green-600 text-xs italic">{successMessage}</p>
            ) : apiError && apiError.includes('아이디') && !errors.userId ? (
              // 서버 에러 메시지가 있을 때 (빨간색)
              <p className="text-red-500 text-xs italic">{apiError}</p>
            ) : errors.userId ? (
              // 클라이언트 유효성 에러 메시지가 있을 때 (빨간색)
              <p className="text-red-500 text-xs italic">{errors.userId}</p>
            ) : (
              null 
            )}
          </div>

          <Input
            label="비밀번호"
            name="userPwd"
            type="password"
            value={formData.userPwd}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.userPwd}
            className="mb-4"
          />
          <Input
            label="비밀번호 확인"
            name="confirmUserPwd"
            type="password"
            value={formData.confirmUserPwd}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.confirmUserPwd}
            className="mb-4"
          />

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">이메일</label>
            <div className="flex items-center">
              <Input
                name="emailLocalPart"
                type="text"
                value={formData.emailLocalPart}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined} // 이메일 에러는 통합 메시지에서 관리
                className="flex-grow mr-2"
              />
              <span className="mr-2 text-gray-500 mb-6">@</span>
              <Input
                name="emailDomain"
                type="text"
                value={formData.emailDomain}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined} // 이메일 에러는 통합 메시지에서 관리
                className="flex-grow mr-2"
              />
              <Button
                onClick={handleCheckEmail}
                type="button"
                disabled={
                  isLoading ||
                  !formData.emailLocalPart.trim() ||
                  !formData.emailDomain.trim()
                }
                className="shrink-0 mb-5"
              >
                {isLoading ? '확인 중...' : '중복 확인'}
              </Button>
            </div>
            {/* ✅ 이메일 관련 모든 메시지를 표시할 통합 영역 */}
            <div className="min-h-[1.25rem] mt-1"> {/* `min-h`로 공간 확보 */}
              {successMessage && successMessage.includes('이메일') && !errors.email && !apiError ? (
                <p className="text-green-600 text-xs italic">{successMessage}</p>
              ) : apiError && apiError.includes('이메일') && !errors.email ? (
                <p className="text-red-500 text-xs italic">{apiError}</p>
              ) : errors.email ? ( // 클라이언트 측 이메일 유효성 에러
                <p className="text-red-500 text-xs italic">{errors.email}</p>
              ) : (
                null
              )}
            </div>
          </div>

          {/* 전화번호 섹션 */}
          <div className="mb-4">
            <label htmlFor="phonePrefix" className="block text-gray-700 text-sm font-bold mb-2">
              전화번호
            </label>
            <div className="flex items-center">
              <Select
                name="phonePrefix"
                value={formData.phonePrefix}
                onChange={handleChange}
                options={[
                  { value: '010', label: '010' }, { value: '011', label: '011' }, { value: '012', label: '012' },
                  { value: '013', label: '013' }, { value: '014', label: '014' }, { value: '015', label: '015' },
                  { value: '016', label: '016' }, { value: '017', label: '017' }, { value: '018', label: '018' },
                  { value: '019', label: '019' }
                ]}
                className="w-1/4 mr-2 h-10" 
              />
              <span className="mr-2 text-gray-500">-</span>
              <Input
                name="phoneMiddle"
                type="text"
                value={formData.phoneMiddle}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.phoneMiddle}
                className="w-1/3 mr-2 py-2 h-10 mb-4" 
              />
              <span className="mr-2 text-gray-500">-</span>
              <Input
                name="phoneLast"
                type="text"
                value={formData.phoneLast}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.phoneLast}
                className="w-1/3 py-2 h-10 mb-4"
              />
            </div>
          </div>

          <Input
            label="이름"
            name="userName"
            type="text"
            value={formData.userName}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.userName}
            className="mb-4" 
          />
          <Input
            label="생년월일"
            name="dateOfBirth"
            type="date"
            value={formData.dateOfBirth}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.dateOfBirth}
            className="mb-4" 
          />
          <Select
            label="성별"
            name="gender"
            value={formData.gender}
            onChange={handleChange}
            onBlur={handleBlur}
            options={[{ value: 'M', label: '남성' }, { value: 'F', label: '여성' }, { value: 'O', label: '기타' }]}
            error={errors.gender}
            className="mb-4" 
          />

          <div className="flex items-center mb-4">
            <Input
              label="주소"
              name="address"
              type="text"
              value={formData.address}
              readOnly
              onBlur={handleBlur}
              error={errors.address}
              className="flex-grow mr-2"
            />
            <Button
              type="button"
              onClick={openPostcodePopup}
              disabled={!isScriptLoaded}
              className="shrink-0 mt-[8px]"
            >
              주소 검색
            </Button>
          </div>

          <Input
            label="상세 주소"
            name="detailAddress"
            type="text"
            value={formData.detailAddress}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.detailAddress}
            className="mb-4"
          />

          <Button type="submit" disabled={isLoading} className="w-full">
            {isLoading ? '가입 중...' : '회원가입'}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default SignUpForm;