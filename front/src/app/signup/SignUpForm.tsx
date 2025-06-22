'use client';
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
 * - **상태 관리 명확성 (Clear State Management):** TanStack Query를 활용하여 비동기 상태(로딩, 에러, 데이터)를 효율적으로 관리하고, 필요한 로컬 상태와 동기화.
 * - **사용자 경험 (User Experience):** 아이디/이메일 중복 확인 메시지 지속성, 실시간 유효성 피드백, 간소화된 주소 검색 제공.
 *
 * 🔗 **관련 파일/컴포넌트 (Related Files/Components):**
 * - `src/app/signup/page.tsx`: 이 컴포넌트를 렌더링하는 상위 페이지.
 * - `src/hooks/useForm.ts`: 폼 데이터 상태 및 `handleChange` 로직.
 * - `src/hooks/useValidation.ts`: 클라이언트 측 폼 유효성 검사 로직.
 * - `src/hooks/useAuthQueries.ts`: TanStack Query 기반의 인증 관련 API 호출 훅.
 * - `src/api/auth.ts`: 실제 인증 API 호출을 위한 순수 함수들. (새롭게 분리됨)
 * - `src/components/common/AddressSearchButton.tsx`: Daum Postcode API 연동 및 주소 검색 버튼 컴포넌트.
 * - `src/components/ui/*.tsx`: 재사용 가능한 UI 컴포넌트 (Input, Select, Button).
 * - `src/types/member.ts`: 회원 관련 데이터 타입 정의.
 * - `src/types/address.d.ts`: Daum Postcode API 관련 타입 정의.
 * - `src/app/layout.tsx`: TanStack Query `QueryClientProvider` 설정 및 Daum Postcode 스크립트 로드.
 */

import React, { useState, useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';

// --- 커스텀 훅 임포트 ---
import { useForm } from '@/hooks/useForm';              // 폼 데이터 상태 관리 커스텀 훅
import { useValidation } from '@/hooks/useValidation';  // 폼 유효성 검사 로직 커스텀 훅
import { useAuthQueries } from '@/hooks/useAuthQueries';

// --- 타입 임포트 ---
import { SignupFormData, SignupApiRequest } from '@/types/member'; // 회원가입 폼 데이터 타입 정의
import { DaumPostcodeData } from '@/types/address';

// --- UI 컴포넌트 임포트 ---
import Input from '@/components/ui/Input';              // 재사용 가능한 Input 컴포넌트
import Button from '@/components/ui/Button';            // 재사용 가능한 Button 컴포넌트
import Select from '@/components/ui/Select';            // 재사용 가능한 Select 컴포넌트
import AddressSearchButton from '@/components/common/AddressSearchButton';

// 전화번호 앞자리 옵션은 컴포넌트 외부에서 정의하여 불필요한 재생성 방지
const PHONE_PREFIX_OPTIONS = [
  { value: '010', label: '010' }, { value: '011', label: '011' }, { value: '012', label: '012' },
  { value: '013', label: '013' }, { value: '014', label: '014' }, { value: '015', label: '015' },
  { value: '016', label: '016' }, { value: '017', label: '017' }, { value: '018', label: '018' },
  { value: '019', label: '019' }
];

/**
 * @function SignUpForm
 * @description 회원가입 폼을 렌더링하고 사용자 입력을 처리하며, 유효성 검사 및 API 호출을 담당합니다.
 * TanStack Query를 활용하여 비동기 상태 관리를 최적화합니다.
 */
function SignupForm() {
  const router = useRouter();

  // --- 폼 데이터 관리 ---
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

  // TanStack Query 훅에서 API 호출 함수 및 상태를 가져옵니다.
  const { checkUserIdMutation, checkEmailMutation, signupMutation } = useAuthQueries();

  // 아이디 중복 확인 결과 상태 (TanStack Query 결과를 로컬 상태로 반영)
  // 이는 `useValidation` 훅에 `isUserIdAvailable`을 전달하기 위함입니다.
  const [isUserIdAvailable, setIsUserIdAvailable] = useState<boolean | null>(null);
  const [lastCheckedUserId, setLastCheckedUserId] = useState<string | null>(null);

  // 이메일 중복 확인 결과 상태 (TanStack Query 결과를 로컬 상태로 반영)
  const [isEmailAvailable, setIsEmailAvailable] = useState<boolean | null>(null);
  const [lastCheckedEmail, setLastCheckedEmail] = useState<string | null>(null);

  // 클라이언트 측 유효성 검사
  const {
    errors,
    validateForm,
    handleFieldChangeAndValidate,
    setErrors,
    validateField,
    clearFormError,
  } = useValidation(formData, isUserIdAvailable, isEmailAvailable);

  // --- useEffect: Tanstack Query 결과 -> 로컬 상태 동기화 및 유효성 메시지 처리 ---
  // 아이디 중복 확인 결과 동기화
  useEffect(() => {
    if (checkUserIdMutation.isSuccess) {
      setIsUserIdAvailable(checkUserIdMutation.data.isAvailable);
      setLastCheckedUserId(formData.userId); // 마지막으로 성공적으로 확인된 아이디
    } else if (checkUserIdMutation.isError) {
      setIsUserIdAvailable(false); // API 에러 시 사용 불가로 간주
      setLastCheckedUserId(formData.userId) // 에러 발생 시에도 마지막 확인 아이디 기록 (재확인 방지)
    }
    // userId가 변경되면 isUserIdAvailable을 null로 초기화하는 로직은 아래에서 처리
  }, [checkUserIdMutation.isSuccess, checkUserIdMutation.isError, checkUserIdMutation.data, formData.userId])

  // 이메일 중복 확인 결과 동기화
  useEffect(() => {
    if (checkEmailMutation.isSuccess) {
      setIsEmailAvailable(checkEmailMutation.data.isAvailable);
      setLastCheckedEmail(`${formData.emailLocalPart}@${formData.emailDomain}`);
    } else if (checkEmailMutation.isError) {
      setIsEmailAvailable(false);
      setLastCheckedEmail(`${formData.emailLocalPart}@${formData.emailDomain}`);
    }
  }, [checkEmailMutation.isSuccess, checkEmailMutation.isError, checkEmailMutation.data, formData.emailLocalPart, formData.emailDomain]);

  // --- useEffect: 폼 데이터 변경 시 중복 확인 상태 초기화 ---
  // 이 로직은 `useAuthQueries`에서 QueryClient.removeQueries를 사용하지 않고,
  // 로컬 상태 `isUserIdAvailable`, `isEmailAvailable`을 관리하기 위해 필요합니다.
  useEffect(() => {
    if (lastCheckedUserId !== null && formData.userId !== lastCheckedUserId) {
      setIsUserIdAvailable(null);
      setErrors(prev => ({ ...prev, userId: '' }));
      // NOTE: TanStack Query를 사용하는 경우, 특정 쿼리의 캐시를 무효화하는 것도 고려할 수 있습니다.
      // 예: queryClient.invalidateQueries(['checkUserId', lastCheckedUserId]);
      // 하지만 이 경우 UI의 isUserIdAvailable 상태는 직접 관리하는 것이 더 직관적일 수 있습니다.
    }
  }, [formData.userId, lastCheckedUserId, setErrors])

  useEffect(() => {
    const currentFullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;
    if (lastCheckedEmail !== null && currentFullEmail !== lastCheckedEmail) {
      setIsEmailAvailable(null);
      setErrors(prev => ({ ...prev, email: '' }));
    }
  }, [formData.emailLocalPart, formData.emailDomain, lastCheckedEmail, setErrors])

  // --- 콜백 함수 (이벤트 핸들러) ---

  const handleCompleteAddressSearch = useCallback((data: DaumPostcodeData) => {
    setFormData(prev => ({
      ...prev,
      address: data.roadAddress || data.jibunAddress,
      detailAddress: '',
      zonecode: data.zonecode,
    }));

    setErrors(prev => ({ ...prev, address: '' }));
    const addressError = validateField('address', data.roadAddress || data.jibunAddress);
    if (addressError) {
      setErrors(prev => ({ ...prev, address: addressError }));
    }
  }, [setFormData, setErrors, validateField]);

  /**
   * @function handleCheckUserId
   * @description 아이디 중복 확인 API를 호출합니다.
   * API 호출 전 클라이언트 측 유효성 검사를 수행합니다.
   */
  const handleCheckUserId = useCallback(async () => {
    const userIdError = validateField('userId', formData.userId);

    if (userIdError) {
      setErrors(prevErrors => ({ ...prevErrors, userId: userIdError }));
      setIsUserIdAvailable(null); // 클라이언트 유효성 에러 시 상태 초기화
      checkUserIdMutation.reset(); // TanStack Query 뮤테이션 상태도 리셋하여 메시지 초기화
      return;
    }

    setErrors(prev => ({ ...prev, userId: '' })); // 기존 클라이언트 유효성 에러 제거
    setIsUserIdAvailable(null); // 중복 확인 상태 초기화 (API 호출 전)
    checkUserIdMutation.reset(); // 새로운 호출 시작 전 이전 메시지/상태 리셋

    try {
      // TanStack Query의 mutateAsync를 사용하여 비동기 호출
      await checkUserIdMutation.mutateAsync(formData.userId);
      // isUserIdAvailable 및 lastCheckedUserId는 useEffect에서 처리됩니다.
    } catch (error) {
      // 에러는 useMutation onError에서 처리되며, UI에 error.message가 반영됩니다.
    }
  }, [formData.userId, validateField, setErrors, checkUserIdMutation]);

   /**
   * @function handleCheckEmail
   * @description 이메일 중복 확인 API를 호출합니다.
   * API 호출 전 클라이언트 측 유효성 검사를 수행합니다.
   */
  const handleCheckEmail = useCallback(async () => {
    const fullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;

    const emailError = validateField('email', fullEmail);

    if (emailError) {
      setErrors(prevErrors => ({ ...prevErrors, email: emailError }));
      setIsEmailAvailable(null);
      checkEmailMutation.reset();
      return;
    }

    setErrors(prev => ({ ...prev, email: '' }));
    setIsEmailAvailable(null);
    checkEmailMutation.reset();

    try {
      await checkEmailMutation.mutateAsync(fullEmail);
    } catch (error) {
      // 에러는 useMutation onError에서 처리됩니다.
    }
  },
    [
      formData.emailLocalPart, formData.emailDomain,
      validateField, setErrors, checkEmailMutation,
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
    signupMutation.reset(); // 회원가입 뮤테이션 상태 리셋

    // 1. 전체 폼 유효성 검사 실행
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
      await signupMutation.mutateAsync(signupData);
      alert('회원 가입에 성공했습니다!');
      resetForm(); // 폼 데이터 초기화
      router.push('/'); // 성공 시 메인 페이지로 이동
    } catch (error) {
      // 에러는 useMutation onError에서 처리되지만, 여기에서 추가적인 UI 피드백을 줄 수 있습니다.
      setErrors(prev => ({ ...prev, form: error instanceof Error ? error.message : '회원가입 중 알 수 없는 오류가 발생했습니다.' }));
    }
  }, [formData, validateForm, signupMutation, router, setErrors, resetForm, clearFormError]);


  /**
   * @function handleBlur
   * @description 입력 필드에서 포커스 아웃 시 (onBlur 이벤트) 유효성 검사를 실행합니다.
   * @param {React.FocusEvent<HTMLInputElement | HTMLSelectElement>} e - 포커스 이벤트 객체
   */
  const handleBlur = useCallback((e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => {
    handleFieldChangeAndValidate(e as React.ChangeEvent<HTMLInputElement | HTMLSelectElement>);
  }, [handleFieldChangeAndValidate]);

  // 아이디 중복 확인 버튼 활성화/비활성화 로직
  // userId 필드의 클라이언트 측 유효성 검사 에러가 없을 때 활성화
  const isUserIdCheckButtonDisabled =
    checkUserIdMutation.isPending || // API 로딩 중
    !formData.userId.trim() || // userId가 비어있을 때
    !!validateField('userId', formData.userId, true); // `true`를 전달하여 `custom` 규칙을 무시하고 필드 자체의 유효성만 검사

  // 이메일 중복 확인 버튼 활성화/비활성화 로직
  const isEmailCheckButtonDisabled =
    checkEmailMutation.isPending || // API 로딩 중
    !formData.emailLocalPart.trim() || // 이메일 로컬파트가 비어있을 때
    !formData.emailDomain.trim() || // 이메일 도메인이 비어있을 때
    !!validateField('email', `${formData.emailLocalPart}@${formData.emailDomain}`, true); // `true`를 전달하여 `custom` 규칙을 무시

  // JSX 렌더링 시작
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold mb-6 text-center">회원가입</h2>
        <form onSubmit={handleSubmit} className="space-y-4">

          {/* 전체 폼 제출 관련 API 에러 메시지 (아이디/이메일 중복 확인 메시지와는 다름) */}
          {signupMutation.isError && (
            <p className="text-red-500 text-center">
              {signupMutation.error?.message || '회원가입 중 알 수 없는 오류가 발생했습니다.'}
            </p>
          )}
          {signupMutation.isSuccess && (
            <p className="text-green-600 text-center">회원가입에 성공했습니다!</p>
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
                error={errors.userId} // errors.userId를 직접 전달
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
              {/* 클라이언트 유효성 에러가 있다면 최우선 표시 */}
              {errors.userId && <p className="text-red-500">{errors.userId}</p>}
              {/* 클라이언트 에러가 없고, API 요청이 완료되었을 때만 API 메시지 표시 */}
              {!errors.userId && checkUserIdMutation.isSuccess && (
                <p className={checkUserIdMutation.data?.isAvailable ? "text-green-600" : "text-red-500"}>
                  {checkUserIdMutation.data?.message}
                </p>
              )}
              {/* API 에러 메시지 (API 호출 실패 시) */}
              {!errors.userId && checkUserIdMutation.isError && (
                <p className="text-red-500">
                  {checkUserIdMutation.error?.message || '아이디 중복 확인 중 오류가 발생했습니다.'}
                </p>
              )}
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
                error={undefined}
                className="flex-grow mr-2"
              />
              <span className="mr-2 text-gray-500 mt-[-22px]">@</span>
              <Input
                name="emailDomain"
                type="text"
                value={formData.emailDomain}
                onChange={handleChange}
                onBlur={handleBlur}
                error={undefined}
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
              {errors.email && <p className="text-red-500">{errors.email}</p>}
              {!errors.email && checkEmailMutation.isSuccess && (
                <p className={checkEmailMutation.data?.isAvailable ? "text-green-600" : "text-red-500"}>
                  {checkEmailMutation.data?.message}
                </p>
              )}
              {!errors.email && checkEmailMutation.isError && (
                <p className="text-red-500">
                  {checkEmailMutation.error?.message || '이메일 중복 확인 중 오류가 발생했습니다.'}
                </p>
              )}
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
            options={[{ value: '', label: '선택' }, { value: 'M', label: '남성' }, { value: 'F', label: '여성' }, { value: 'O', label: '기타' }]}
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
              <span className="mr-2 text-gray-500">-</span>
              <Input
                name="phoneMiddle"
                type="text"
                value={formData.phoneMiddle}
                onChange={handleChange}
                onBlur={handleBlur}
                maxLength={4}
                error={undefined}
                className="w-1/3 mr-2"
              />
              <span className="mr-2 text-gray-500">-</span>
              <Input
                name="phoneLast"
                type="text"
                value={formData.phoneLast}
                onChange={handleChange}
                onBlur={handleBlur}
                maxLength={4}
                error={undefined}
                className="w-1/3"
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