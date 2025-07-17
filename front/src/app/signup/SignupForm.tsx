'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useDispatch } from 'react-redux';
import { showNotification } from '@/features/notification/notificationSlice';

// --- 커스텀 훅 임포트 ---
import { useForm } from '@/hooks/useForm';                 // 폼 데이터 상태 관리 커스텀 훅
import { useValidation } from '@/hooks/useValidation';     // 폼 유효성 검사 로직 커스텀 훅
import { useAuthQueries } from '@/hooks/useAuthQueries';   // TanStack Query 기반 인증 API 호출 훅

// --- 타입 임포트 ---
import { SignupFormData, SignupApiRequest } from '@/types/member'; // 회원가입 폼 데이터 타입 정의
import { DaumPostcodeData } from '@/types/address'; // Daum Postcode API 데이터 타입 정의

// --- UI 컴포넌트 임포트 ---
import Input from '@/components/ui/Input';               // 재사용 가능한 Input 컴포넌트
import Button from '@/components/ui/Button';             // 재사용 가능한 Button 컴포넌트
import Select from '@/components/ui/Select';             // 재사용 가능한 Select 컴포넌트
import AddressSearchButton from '@/components/common/AddressSearchButton'; // 주소 검색 버튼 컴포넌트

// 전화번호 앞자리 옵션은 컴포넌트 외부에서 정의하여 불필요한 재생성 방지
const PHONE_PREFIX_OPTIONS = [
  { value: '010', label: '010' }, { value: '011', label: '011' }, { value: '012', label: '012' },
  { value: '013', label: '013' }, { value: '014', label: '014' }, { value: '015', label: '015' },
  { value: '016', label: '016' }, { value: '017', label: '017' }, { value: '018', label: '018' },
  { value: '019', label: '019' }
];


/**
 * @file 회원가입 폼 컴포넌트
 * @description
 * 이 컴포넌트는 사용자 회원가입을 위한 UI와 클라이언트 측 로직을 담당합니다.
 * 폼 데이터 관리, 실시간 유효성 검사, 아이디/이메일 중복 확인, 주소 검색 연동,
 * 최종 회원가입 API 호출 등 회원가입 플로우의 핵심 기능을 통합하여 제공합니다.
 * TanStack Query를 사용하여 비동기 데이터 페칭 및 상태 관리를 최적화합니다.
 *
 * 🎯 **주요 목표 (Goals):**
 * 1. 사용자에게 직관적이고 피드백이 명확한 회원가입 경험 제공.
 * 2. 외부 훅(useForm, useValidation, useAuthQueries)을 활용하여 관심사를 분리하고 코드 재사용성을 높임.
 * 3. 아이디/이메일 중복 확인 결과 메시지(성공/실패)를 사용자에게 지속적으로 표시하여 편의성 증대.
 * 4. 주소 검색 플로우를 간소화하여 사용자 경험을 최적화.
 *
 * 🛠️ **설계 원칙 (Design Principles):**
 * - **모듈화 (Modularity):** 핵심 폼 로직을 외부 훅으로 분리하여 SignupForm 컴포넌트의 복잡도를 낮춤.
 * - **관심사 분리 (Separation of Concerns):** UI 렌더링, 폼 데이터 관리, 유효성 검사, API 통신 등 각 기능을 전담하는 훅과 컴포넌트 사용.
 * - **상태 관리 명확성 (Clear State Management):** TanStack Query를 활용하여 비동기 상태(로딩, 에러, 데이터), 동기화된 로컬 상태를 효율적으로 관리.
 * - **사용자 경험 (User Experience):** 실시간 유효성 피드백, 지속적인 중복 확인 메시지, 간소화된 주소 검색 제공.
 *
 * 🔗 **관련 파일/컴포넌트 (Related Files/Components):**
 * - `src/app/signup/page.tsx`: 이 컴포넌트를 렌더링하는 상위 페이지.
 * - `src/hooks/useForm.ts`: 폼 데이터 상태 및 handleChange 로직.
 * - `src/hooks/useValidation.ts`: 클라이언트 측 폼 유효성 검사 로직.
 * - `src/hooks/useAuthQueries.ts`: TanStack Query 기반 인증 관련 API 호출 훅.
 * - `src/api/auth.ts`: 실제 인증 API 호출을 위한 순수 함수들.
 * - `src/components/common/AddressSearchButton.tsx`: Daum Postcode API 연동.
 * - `src/components/ui/*.tsx`: 재사용 가능한 UI 컴포넌트 (Input, Select, Button).
 * - `src/types/member.ts`: 회원 관련 데이터 타입 정의.
 * - `src/types/address.d.ts`: Daum Postcode API 관련 타입 정의.
 * - `src/app/layout.tsx`: TanStack Query `QueryClientProvider` 설정.
 */

function SignupForm() {
  const router = useRouter();
  const dispatch = useDispatch();

  // --- 폼 데이터 관리 ---
  const { formData, handleChange: baseHandleChange, setFormData, resetForm } = useForm<SignupFormData>({
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

  // TanStack Query 훅에서 API 호출 함수 및 상태를 가져옵니다.
  const { checkUserIdMutation, checkEmailMutation, signupMutation } = useAuthQueries();

  // 중복 확인 결과 상태 및 마지막 확인 값 추적 (API 응답 기반)
  // 이 상태들은 오직 API의 '최종' 확인 결과를 저장하며, 입력값 변경 시 초기화됨
  const [isUserIdAvailable, setIsUserIdAvailable] = useState<boolean | null>(null);
  const [lastCheckedUserId, setLastCheckedUserId] = useState<string | null>(null);

  const [isEmailAvailable, setIsEmailAvailable] = useState<boolean | null>(null);
  const [lastCheckedEmail, setLastCheckedEmail] = useState<string | null>(null);

  // 클라이언트 측 유효성 검사 훅
  // useValidation은 이제 isUserIdAvailable, isEmailAvailable을 직접 받지 않고,
  // validateForm 호출 시 SignupForm 내부에서 이 상태들을 활용하여 전체 유효성을 검사합니다.
  const {
    errors,
    validateForm, // 이 함수는 최종 폼 제출 시 모든 유효성 (형식, 중복 확인 여부)을 검사
    handleFieldChangeAndValidate, // 일반 필드의 유효성 검사용
    setErrors,
    validateField, // 순수 클라이언트 측 형식 유효성만 검사 (예: 길이, 문자 종류 등)
    clearFormError,
  } = useValidation(formData, isUserIdAvailable, isEmailAvailable);

  // --- useEffects: TanStack Query 결과를 로컬 상태 및 유효성 메시지에 동기화 ---

  // 아이디 중복 확인 API 응답을 받아 로컬 상태 (isUserIdAvailable, errors.userId) 업데이트
  useEffect(() => {
    if (checkUserIdMutation.isSuccess) {
      setIsUserIdAvailable(checkUserIdMutation.data.isAvailable);
      setLastCheckedUserId(formData.userId); // 현재 검사된 ID 저장 (이 값이 API 응답과 매칭됨)
      setErrors(prev => ({ ...prev, userId: checkUserIdMutation.data.message })); // 백엔드 메시지 설정
    } else if (checkUserIdMutation.isError) {
      setIsUserIdAvailable(false); // API 오류 시 사용 불가로 간주
      setLastCheckedUserId(formData.userId); // 에러 발생 ID 저장
      setErrors(prev => ({ ...prev, userId: checkUserIdMutation.error?.message || '아이디 중복 확인 중 오류가 발생했습니다.'}));
    }
  }, [checkUserIdMutation.isSuccess, checkUserIdMutation.isError, checkUserIdMutation.data, checkUserIdMutation.error, formData.userId, setErrors]);

  // 이메일 중복 확인 API 응답을 받아 로컬 상태 (isEmailAvailable, errors.email) 업데이트
  useEffect(() => {
    if (checkEmailMutation.isSuccess) {
      setIsEmailAvailable(checkEmailMutation.data.isAvailable);
      setLastCheckedEmail(`${formData.emailLocalPart}@${formData.emailDomain}`); // 현재 검사된 이메일 저장
      setErrors(prev => ({ ...prev, email: checkEmailMutation.data.message })); // 백엔드 메시지 설정
    } else if (checkEmailMutation.isError) {
      setIsEmailAvailable(false); // API 오류 시 사용 불가로 간주
      setLastCheckedEmail(`${formData.emailLocalPart}@${formData.emailDomain}`); // 에러 발생 이메일 저장
      setErrors(prev => ({ ...prev, email: checkEmailMutation.error?.message || '이메일 중복 확인 중 오류가 발생했습니다.' }));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checkEmailMutation.isSuccess, checkEmailMutation.isError, checkEmailMutation.data, checkEmailMutation.error, setErrors]);


  // --- 커스텀 handleChange 함수 (실시간 유효성 검사 및 메시지, 상태 초기화) ---
  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    baseHandleChange(e); // 기본 폼 데이터 업데이트

    // --- 아이디 필드 로직 ---
    if (name === 'userId') {
      const currentUserId = value;
      const formatError = validateField('userId', currentUserId); // 클라이언트 측 형식 유효성만 검사

      // 현재 입력된 아이디가 마지막으로 확인된 아이디와 다를 때만 API 관련 상태 초기화
      // 사용자가 값을 변경했을 때만 재확인을 유도하고, 동일한 값일 때는 이전 API 메시지를 유지합니다.
      if (currentUserId !== lastCheckedUserId) {
        setIsUserIdAvailable(null); // 중복 확인 상태 초기화 (다시 확인해야 함)
        setLastCheckedUserId(null); // 마지막으로 확인된 ID도 초기화 (이전 결과 무효화)
        checkUserIdMutation.reset(); // TanStack Query의 이전 API 호출 결과 및 상태 리셋

        if (formatError) {
          setErrors(prev => ({ ...prev, userId: formatError })); // 형식 위반 시 메시지 출력
        } else if (currentUserId.trim() === '') {
          setErrors(prev => ({ ...prev, userId: '' })); // userId가 비어있으면 메시지 제거
        } else {
          setErrors(prev => ({ ...prev, userId: '아이디 중복 확인이 필요합니다.' })); // 형식이 유효하고 비어있지 않은 경우: 중복 확인 필요 메시지 출력
        }
      } else {
        // 현재 입력된 아이디가 lastCheckedUserId와 동일한 경우
        // 이전에 API를 통해 설정된 메시지(사용 가능/사용 불가)를 유지합니다.
        // 단, 형식 오류가 새로 발생했다면 그 오류를 우선적으로 표시합니다.
        if (formatError) {
          setErrors(prev => ({ ...prev, userId: formatError }));
        }
        // else: errors.userId는 useEffect에 의해 설정된 API 응답 메시지를 그대로 유지
      }
    } 
    // --- 이메일 필드 로직 (아이디와 동일하게 동작) ---
    else if (name === 'emailLocalPart' || name === 'emailDomain') {
      const currentEmailLocalPart = name === 'emailLocalPart' ? value : formData.emailLocalPart;
      const currentEmailDomain = name === 'emailDomain' ? value : formData.emailDomain;
      const currentFullEmail = `${currentEmailLocalPart}@${currentEmailDomain}`;
      const formatError = validateField('email', currentFullEmail);

      // 현재 입력된 이메일이 마지막으로 확인된 이메일과 다를 때만 API 관련 상태 초기화
      if (currentFullEmail !== lastCheckedEmail) {
        setIsEmailAvailable(null);
        setLastCheckedEmail(null);
        checkEmailMutation.reset();

        if (formatError) {
          setErrors(prev => ({ ...prev, email: formatError }));
        } else if (currentFullEmail.trim() === '') {
          setErrors(prev => ({ ...prev, email: '' }));
        } else {
          setErrors(prev => ({ ...prev, email: '이메일 중복 확인이 필요합니다.' }));
        }
      } else {
        // 현재 입력된 이메일이 lastCheckedEmail과 동일한 경우
        if (formatError) {
          setErrors(prev => ({ ...prev, email: formatError }));
        }
        // else: errors.email은 useEffect에 의해 설정된 API 응답 메시지를 그대로 유지
      }
    }
    // --- 기타 일반 필드 로직 ---
    else {
      // 아이디/이메일이 아닌 다른 필드는 일반 유효성 검사 수행
      handleFieldChangeAndValidate(e); 
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    baseHandleChange, formData.emailLocalPart, formData.emailDomain, 
    isUserIdAvailable, lastCheckedUserId, setIsUserIdAvailable, setLastCheckedUserId, checkUserIdMutation, setErrors, validateField,
    isEmailAvailable, lastCheckedEmail, setIsEmailAvailable, setLastCheckedEmail, checkEmailMutation, handleFieldChangeAndValidate
  ]);
  
  // --- 콜백 함수 (이벤트 핸들러) ---

  const handleCompleteAddressSearch = useCallback((data: DaumPostcodeData) => {
    setFormData(prev => ({
      ...prev,
      address: data.roadAddress || data.jibunAddress,
      detailAddress: '',
      zonecode: data.zonecode,
    }));
    setErrors(prev => ({ ...prev, address: '' }));
  }, [setFormData, setErrors]);

  /**
   * @function handleCheckUserId
   * @description 아이디 중복 확인 API를 호출합니다.
   * 버튼 클릭 시 필수 및 형식 유효성 검사를 통과하면 항상 API 호출을 시도합니다.
   */
  const handleCheckUserId = useCallback(async () => {
    const currentUserId = formData.userId.trim();
    const formatError = validateField('userId', currentUserId);

    // 1. 클라이언트 측 필수 입력 검사
    if (!currentUserId) {
      setErrors(prev => ({ ...prev, userId: '아이디는 필수 항목입니다.' }));
      return; 
    }
    // 2. 클라이언트 측 형식 유효성 검사
    if (formatError) {
      setErrors(prev => ({ ...prev, userId: formatError }));
      return; 
    }

    // 3. 모든 클라이언트 측 유효성 검사 통과 시 API 호출 시도
    //    이전의 API 결과 상태 초기화 및 TanStack Query 리셋은 이미 handleChange에서 처리됩니다.
    //    여기서는 API 호출을 시작하기 위한 최소한의 준비만 합니다.
    setErrors(prev => ({ ...prev, userId: '' })); // 메시지를 초기화하여 '확인 중...'이 표시되도록 준비 (1-3 시나리오)

    try {
      await checkUserIdMutation.mutateAsync(currentUserId);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
      // 에러는 useMutation onError 및 useEffect에서 처리되므로 여기서는 추가 로직 불필요
    }
  }, [formData.userId, validateField, setErrors, checkUserIdMutation]);


  /**
   * @function handleCheckEmail
   * @description 이메일 중복 확인 API를 호출합니다. (아이디와 동일하게 동작)
   */
  const handleCheckEmail = useCallback(async () => {
    const fullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;
    const formatError = validateField('email', fullEmail);

    if (!formData.emailLocalPart.trim() || !formData.emailDomain.trim()) {
      setErrors(prev => ({ ...prev, email: '이메일은 필수 항목입니다.' }));
      return;
    }
    if (formatError) {
      setErrors(prev => ({ ...prev, email: formatError }));
      return;
    }

    setErrors(prev => ({ ...prev, email: '' }));
    setIsEmailAvailable(null);
    setLastCheckedEmail(null);
    checkEmailMutation.reset();

    try {
      await checkEmailMutation.mutateAsync(fullEmail);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (_error) {
      // 에러는 useMutation onError 및 useEffect에서 처리되므로 여기서는 추가 로직 불필요
    }
  },
    [
      formData.emailLocalPart, formData.emailDomain, validateField, 
      setErrors, checkEmailMutation
    ]
  );

  /**
   * @function handleSubmit
   * @description 최종 폼 제출 핸들러. 모든 유효성 검사를 통과하면 회원가입 API를 호출합니다.
   * @param {React.FormEvent<HTMLFormElement>} e - 폼 이벤트 객체
   */
  const handleSubmit = useCallback(async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    clearFormError(); // 폼 제출 시 전체 폼 에러 메시지 초기화
    signupMutation.reset();

    // 1. 전체 폼 유효성 검사 실행 (여기서 isUserIdAvailable/isEmailAvailable도 함께 검사됨)
    // useValidation 훅의 validateForm이 isUserIdAvailable, isEmailAvailable 상태를 사용하도록 업데이트 필요
    const isValid = validateForm(); 
    if (!isValid) {
      setErrors(prev => ({ ...prev, form: '입력된 정보를 확인해주세요.' }));
      return;
    }

    // 2. 백엔드 API 요청 데이터 준비
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

    // 3. 회원가입 API 호출 (TanStack Query의 mutateAsync 사용)
    try {
      await signupMutation.mutateAsync(signupData, {
        onSuccess: (data) => {
          resetForm(); // 폼 데이터 초기화
          // Redux 액션을 dispatch하여 성공 알림 메시지를 전역 상태에 저장
          dispatch(showNotification({ message: data.message || '회원 가입이 성공적으로 완료되었습니다!', type: 'success'}));
          router.push('/'); // 성공 시 메인 페이지로 이동
        },
        onError: (error) => {
          // 에러는 useMutation onError에서 처리되지만, 여기서 추가 UI 피드백 가능
          // Redux 액션을 디스패치하여 에러 알림 메시지를 전역 상태에 저장
          dispatch(showNotification({ message: error.message || '회원 가입 중 알 수 없는 오류가 발생하였습니다.', type: 'error'}));
          setErrors(prev => ({ ...prev, form: error.message || '회원 가입 중 알 수 없는 오류가 발생하였습니다.'}));
        }
      });
    } catch (error) {
      // 이 블럭은 mutateAsync의 onError 콜백에서 이미 처리되므로 일반적으로는 추가적인 에러 처리가 필요 없음.
      // 하지만 혹시 모를 상황에 대비해 fallback 로직을 유지할 수 있음
      console.error("Signup submission error: ", error)
    }
  }, [formData, validateForm, signupMutation, router, setErrors, resetForm, clearFormError, dispatch]);


  /**
   * @function handleBlur
   * @description 입력 필드에서 포커스 아웃 시 (onBlur 이벤트) 유효성 검사를 실행합니다.
   * `handleChange`에서 실시간 유효성 검사를 처리하지만, `onBlur` 시점에 최종 유효성 검사 결과를 반영합니다.
   * @param {React.FocusEvent<HTMLInputElement | HTMLSelectElement>} e - 포커스 이벤트 객체
   */
  const handleBlur = useCallback((e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => {
    // `handleChange` 로직을 그대로 호출하여 `onBlur` 시점에도 실시간 유효성 검사와 동일한 메시징 로직을 따르도록 합니다.
    handleChange(e as React.ChangeEvent<HTMLInputElement | HTMLSelectElement>);
  }, [handleChange]); // handleChange를 의존성에 포함

  // 아이디 중복 확인 버튼 활성화/비활성화 로직 (1-2, 1-3 시나리오 처리)
  // 버튼은 다음 조건 중 하나라도 만족하면 비활성화됩니다.
  // 1. API 호출 중 (`checkUserIdMutation.isPending`)
  // 2. 아이디 입력 필드가 비어있음 (`!formData.userId.trim()`)
  // 3. 아이디 형식 오류가 있음 (`!!validateField('userId', formData.userId)`가 true를 반환)
  // 4. 현재 입력된 아이디가 'lastCheckedUserId'와 일치하고, API를 통해 '사용 가능' 또는 '이미 가입됨'으로 확정되었을 때
  const isUserIdCheckButtonDisabled =
    checkUserIdMutation.isPending || 
    !formData.userId.trim() || 
    !!validateField('userId', formData.userId) || // 형식 오류가 있다면 비활성화
    (isUserIdAvailable !== null && formData.userId === lastCheckedUserId); // API 확인이 완료되었고 (true/false), 현재 값이 그 확인된 값과 동일하다면 비활성화

    
  // 이메일 중복 확인 버튼 활성화/비활성화 로직 (아이디와 동일한 로직 적용)
  const isEmailCheckButtonDisabled =
    checkEmailMutation.isPending || 
    !formData.emailLocalPart.trim() || 
    !formData.emailDomain.trim() || 
    !!validateField('email', `${formData.emailLocalPart}@${formData.emailDomain}`) || // 형식 오류가 있다면 비활성화
    (isEmailAvailable !== null && `${formData.emailLocalPart}@${formData.emailDomain}` === lastCheckedEmail); // API 확인이 완료되었고, 현재 값이 그 확인된 값과 동일하다면 비활성화

  // JSX 렌더링 시작
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold mb-6 text-center">회원가입</h2>
        <form onSubmit={handleSubmit} className="space-y-4">

          {/* 전체 폼 제출 관련 API 에러 메시지 */}
          {signupMutation.isError && (
            <p className="text-red-500 text-center">
              {signupMutation.error?.message || '회원가입 중 알 수 없는 오류가 발생했습니다.'}
            </p>
          )}
          {signupMutation.isSuccess && signupMutation.data && (
            <p className="text-green-600 text-center">
              {signupMutation.data.message}
            </p>
          )}
          {errors.form && <p className="text-red-500 text-center">{errors.form}</p>}

          {/* --- 아이디 입력 필드와 중복 확인 버튼 섹션 --- */}
          <div>
            <div className="flex items-center">
              <Input
                label="아이디"
                name="userId"
                type="text"
                value={formData.userId}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined} // 오류 메시지는 아래 통합 영역에서 표시
                className="flex-grow mr-2"
              />
              <Button
                onClick={handleCheckUserId}
                type="button"
                disabled={isUserIdCheckButtonDisabled}
                className="shrink-0 mt-[10px]"
              >
                {checkUserIdMutation.isPending ? '확인 중...' : '중복 확인'}
              </Button>
            </div>
            {/* 아이디 관련 모든 메시지를 표시할 통합 영역 */}
            <div className="min-h-[1.25rem] text-xs italic mt-1">
              {checkUserIdMutation.isPending ? (
                <p className="text-gray-500">확인 중...</p>
              ) : errors.userId ? ( // errors.userId에 값이 있으면 우선적으로 표시
                <p className={errors.userId.includes('사용 가능') ? "text-green-600" : "text-red-500"}>
                  {errors.userId}
                </p>
              ) : null /* 메시지가 없으면 아무것도 표시 안함 */
              }
            </div>
          </div>

          {/* 비밀번호 섹션 */}
          <Input
            label="비밀번호"
            name="userPwd"
            type="password"
            value={formData.userPwd}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.userPwd}
          />
          <Input
            label="비밀번호 확인"
            name="confirmUserPwd"
            type="password"
            value={formData.confirmUserPwd}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.confirmUserPwd}
          />

          {/* 이메일 섹션 */}
          <div>
            <label className="block text-gray-700 text-sm font-bold mb-2">이메일</label>
            <div className="flex items-center">
              <Input
                name="emailLocalPart"
                type="text"
                value={formData.emailLocalPart}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined} // 이메일 필드에 대한 개별 에러는 통합 'email' 에러로 표시되므로 undefined
                className="flex-grow mr-2"
              />
              <span className="mr-2 text-gray-500 mt-[-22px]">@</span>
              <Input
                name="emailDomain"
                type="text"
                value={formData.emailDomain}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined} // 이메일 필드에 대한 개별 에러는 통합 'email' 에러로 표시되므로 undefined
                className="flex-grow mr-2"
              />
              <Button
                onClick={handleCheckEmail}
                type="button"
                disabled={isEmailCheckButtonDisabled}
                className="shrink-0 mt-[-19px]"
              >
                {checkEmailMutation.isPending ? '확인 중...' : '중복 확인'}
              </Button>
            </div>
            {/* 이메일 관련 모든 메시지를 표시할 통합 영역 */}
            <div className="min-h-[1.25rem] text-xs italic mt-1">
              {checkEmailMutation.isPending ? (
                <p className="text-gray-500">확인 중...</p>
              ) : errors.email ? ( // errors.email에 값이 있으면 우선적으로 표시
                <p className={errors.email.includes('사용 가능') ? "text-green-600" : "text-red-500"}>
                  {errors.email}
                </p>
              ) : null /* 메시지가 없으면 아무것도 표시 안함 */
              }
            </div>
          </div>
          
          {/* 이름 섹션 */}
          <Input
            label="이름"
            name="userName"
            type="text"
            value={formData.userName}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.userName}
          />

          {/* 생년월일 섹션 */}
          <Input
            label="생년월일"
            name="dateOfBirth"
            type="date"
            value={formData.dateOfBirth}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.dateOfBirth}
          />

          {/* 성별 섹션 */}
          <Select
            label="성별"
            name="gender"
            value={formData.gender}
            onChange={handleChange}
            onBlur={handleBlur}
            options={[{ value: 'M', label: '남성' }, { value: 'F', label: '여성' }, { value: 'O', label: '기타' }]}
            error={errors.gender}
          />

          {/* 주소 섹션 */}
          <div>
            <div className="flex items-center">
              <Input
                label="우편번호"
                name="zonecode"
                type="text"
                value={formData.zonecode}
                readOnly
                error={undefined}
                className="flex-grow mr-2"
              />
              <AddressSearchButton
                onComplete={handleCompleteAddressSearch}
                disabled={signupMutation.isPending}
                className="shrink-0 mt-[10px]"
              />
            </div>
            <Input
              label="기본 주소"
              name="address"
              type="text"
              value={formData.address}
              readOnly
              error={errors.address}
              className="mt-4"
            />
            <Input
              label="상세 주소"
              name="detailAddress"
              type="text"
              value={formData.detailAddress}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.detailAddress}
              className="mt-4"
            />
          </div>

          {/* 전화번호 섹션 */}
          <div>
            <label htmlFor="phonePrefix" className="block text-gray-700 text-sm font-bold mb-2">
              전화번호
            </label>
            <div className="flex items-center">
              <Select
                name="phonePrefix"
                value={formData.phonePrefix}
                onChange={handleChange}
                options={PHONE_PREFIX_OPTIONS}
                className="w-1/4 mr-2"
                onBlur={handleBlur}
                error={undefined}
              />
              <span className="mr-2 text-gray-500 mb-2">-</span>
              <Input
                name="phoneMiddle"
                type="text"
                value={formData.phoneMiddle}
                onChange={handleChange}
                onBlur={handleBlur}
                maxLength={4}
                error={undefined}
                className="w-1/3 mr-2 mt-3"
              />
              <span className="mr-2 text-gray-500 mb-2">-</span>
              <Input
                name="phoneLast"
                type="text"
                value={formData.phoneLast}
                onChange={handleChange}
                onBlur={handleBlur}
                maxLength={4}
                error={undefined}
                className="w-1/3 mt-3"
              />
            </div>
            {errors.phone && <p className="text-red-500 text-xs italic mt-1">{errors.phone}</p>}
          </div>

          <Button type="submit" disabled={signupMutation.isPending} className="w-full mt-6">
            {signupMutation.isPending ? '가입 중...' : '회원가입'}
          </Button>
        </form>
      </div>
    </div>
  );
}

export default SignupForm;