import { useState, useCallback, useMemo } from 'react';
import { SignupFormData } from '@/types/member';

/**
 * @file useValidation Hook
 * @description
 * 폼 데이터의 유효성을 검사하고, 각 필드별 에러 메시지를 관리하는 커스텀 훅입니다.
 * 클라이언트 측 유효성 검사 규칙을 정의하고, 폼 제출 시 전체 폼의 유효성을 검사합니다.
 *
 * @param {SignupFormData} formData - 유효성을 검사할 폼 데이터 객체.
 * @returns {object} 유효성 검사 상태 및 함수들을 포함하는 객체.
 * - errors: 각 필드별 에러 메시지를 담는 객체.
 * - validateForm: 전체 폼의 유효성을 검사하는 함수.
 * - handleFieldChangeAndValidate: 일반 필드의 변경 및 유효성 검사를 처리하는 함수.
 * - setErrors: errors 상태를 직접 업데이트하는 함수.
 * - validateField: 특정 필드의 형식 유효성만 검사하는 함수.
 * - clearFormError: 전체 폼 에러 메시지를 초기화하는 함수.
 */

type FormErrors<T> = {
  [K in keyof T]?: string;
} & { form?: string; email?: string; phone?: string; userId?: string; };

// Validation 타입 정의
type ValidationRule = {
  required?: string;
  minLength?: { value: number; message: string; };
  maxLength?: { value: number; message: string; };
  pattern?: { value: RegExp; message: string; };
  // custom 함수는 추가 인자로 allFormData르 받음
  custom?: (value: string, allFormData: SignupFormData) => string | '';
};

// 모든 가능한 필드 이름(SignupFormData의 키 + 'email' + 'phone')을 포함하는 규칙 맵 타입
type ValidationRulesMap = {
  [K in (keyof SignupFormData | 'email' | 'phone')]?: ValidationRule;
};

/**
 * @function useValidation
 * @description
 * 폼 데이터와 현재 아이디/이메일 중복 확인 상태를 기반으로 클라이언트 측 유효성 검사를 수행하는 커스텀 훅.
 * 필드별 유효성 검사, 전체 폼 유효성 검사, 에러 메시지 관리 기능을 제공합니다.
 *
 * @param {SignupFormData} formData - 현재 폼 데이터 상태
 * @param {boolean | null} isUserIdAvailable - 아이디 중복 확인 결과 (null: 확인 전, true: 사용 가능, false: 사용 불가)
 * @param {boolean | null} isEmailAvailable - 이메일 중복 확인 결과 (null: 확인 전, true: 사용 가능, false: 사용 불가)
 * @returns {object}
 * - errors: 현재 폼의 유효성 검사 에러 객체
 * - validateForm: 전체 폼의 유효성을 검사하고 결과를 반환하는 함수
 * - handleFieldChangeAndValidate: 필드 변경 시 유효성 검사를 수행하는 핸들러
 * - setErrors: 에러 상태를 직접 설정하는 Dispatch 함수
 * - validateField: 특정 단일 필드의 유효성을 즉시 검사하는 함수
 * - clearFormError: 전체 폼 에러 메시지를 초기화하는 함수
 */
export const useValidation = (
  formData: SignupFormData,
  isUserIdAvailable: boolean | null, // SignupForm에서 전달받는 아이디 중복 확인 상태
  isEmailAvailable: boolean | null // SignupForm에서 전달받는 이메일 중복 확인 상태
) => {
  const [errors, setErrors] = useState<FormErrors<SignupFormData>>({});
  
  // 유효성 검사 규칙 (Rules) 정의
  // useMemo를 사용하여 규칙이 변경될 때만 다시 생성되도록 최적화
  const validationRules: ValidationRulesMap = useMemo(() => ({
    userId: {
      required: '아이디는 필수 항목입니다.',
      minLength: { value: 3, message: '아이디는 3자리 이상이어야 합니다.' },
      maxLength: { value: 20, message: '아이디는 20자리 이하여야 합니다.' },
      pattern: { value: /^[a-z0-9]+$/, message: '아이디는 영문 소문자, 숫자만 가능합니다.' },
    },
    userPwd: {
      required: '비밀번호는 필수 항목입니다.',
      minLength: { value: 8, message: '비밀번호는 8자리 이상이어야 합니다.' },
      pattern: { value: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+])[A-Za-z\d!@#$%^&*()_+]{8,}$/, message: '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.' },
    },
    confirmUserPwd: {
      required: '비밀번호 확인은 필수 항목입니다.',
      custom: (value: string, allFormData: SignupFormData) => {
        if (value !== allFormData.userPwd) return '비밀번호가 일치하지 않습니다.';
        return '';
      },
    },
    email: {
      required: '이메일은 필수 항목입니다.',
      pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '유효한 이메일 형식이 아닙니다.' },
    },
    userName: {
      required: '이름은 필수 항목입니다.',
      minLength: { value: 2, message: '이름은 2자리 이상이어야 합니다.' },
      pattern: { value: /^\S*$/, message: '이름에는 공백을 포함할 수 없습니다.' },
    },
    dateOfBirth: {
      required: '생년월일은 필수 항목입니다.',
      custom: (value: string) => {
        if (!value) return ''; // required에서 처리
        const birthDate = new Date(value);
        const today = new Date();
        if (birthDate > today) return '미래의 날짜는 입력할 수 없습니다.';
        return '';
      },
    },
    gender: {
      required: '성별은 필수 항목입니다.',
    },
    address: {
      required: '주소는 필수 항목입니다.',
      custom: (value: string, allFormData: SignupFormData) => {
        if (!value || !allFormData.zonecode) return '주소 검색을 통해 주소를 입력해주세요.';
        return '';
      },
    },
    detailAddress: {
      required: '상세 주소는 필수 항목입니다.',
      minLength: { value: 2, message: '상세 주소는 2자리 이상이어야 합니다.' },
    },
    phone: { // 전화번호 3파트 전체를 합쳐서 검사
      required: '전화번호는 필수 항목입니다.',
      pattern: { value: /^01[0-9]{8,9}$/, message: '유효한 전화번호 형식이 아닙니다.' },
    },
  }), [isUserIdAvailable, isEmailAvailable]);

  /**
   * @function validateField
   * @description
   * 단일 필드의 유효성을 검사하고 에러 메시지를 반환합니다.
   * @param {keyof SignupFormData | 'email' | 'phone'} fieldName - 검사할 필드 이름
   * @param {string} value - 검사할 필드의 값
   * @param {boolean} [skipCustomValidation=false] - custom 유효성 검사를 건너뛸지 여부 (기본값: false).
   * 주로 버튼 활성화 로직처럼 형식만 검사할 때 사용됩니다.
   * @returns {string} 에러 메시지 또는 빈 문자열
   */
  const validateField = useCallback((
    fieldName: keyof SignupFormData | 'email' | 'phone',
    value: string,
    skipCustomValidation: boolean = false
  ): string => {
    const rules = validationRules[fieldName]; // fieldName에 해당하는 규칙 가져오기

    if (!rules) return ''; // 해당 필드에 규칙이 없으면 빈 문자열 반환 (에러 없음)

    // 1. required 검사
    if (rules.required && !value.trim()) {
      return rules.required;
    }

    // 2. minLength 검사
    if (rules.minLength && value.trim().length < rules.minLength.value) {
      return rules.minLength.message;
    }

    // 3. maxLength 검사
    if (rules.maxLength && value.trim().length > rules.maxLength.value) {
      return rules.maxLength.message;
    }

    // 4. pattern 검사
    if (rules.pattern && !rules.pattern.value.test(value)) {
      return rules.pattern.message;
    }
    
    // 5. custom 검사 (skipCustomValidation이 true면 건너김)
    if (rules.custom && !skipCustomValidation) { 
      const customError = rules.custom(value, formData);
      if (customError) return customError;
    }

    return ''; // 모든 검사 통과
  }, [formData, validationRules]);

  /**
   * @function handleFieldChangeAndValidate
   * @description
   * Input 또는 Select 컴포넌트의 onChange/onBlur 이벤트에 연결되어
   * 필드 값을 업데이트하고 해당 필드의 유효성을 즉시 검사합니다.
   * 이 함수는 아이디/이메일 필드와 같이 복잡한 로직이 필요한 필드 외에 사용됩니다.
   *
   * @param {React.ChangeEvent<HTMLInputElement | HTMLSelectElement>} e - 이벤트 객체.
   */
  const handleFieldChangeAndValidate = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;

    let fieldToValidate: keyof SignupFormData | 'email' | 'phone' = name as keyof SignupFormData;
    let valueToValidate = value;

    // 중복 확인 필드(userId, email)와 address 필드는 onChange/onBlur 시 custom validation을 스킵합니다.
    // 이는 '아이디 중복 확인이 필요합니다.'와 같은 메시지가 handleChange에서 관리되기 위함입니다.
    // userName의 pattern(공백 검사)은 항상 적용되어야 하므로 shouldSkipCustomValidation에 포함되지 않습니다.
    const shouldSkipCustomValidation = (fieldToValidate === 'userId' || fieldToValidate === 'email' || fieldToValidate === 'address');

    if (name === 'emailLocalPart' || name === 'emailDomain') {
      fieldToValidate = 'email';
      const currentEmailLocalPart = name === 'emailLocalPart' ? value : formData.emailLocalPart;
      const currentEmailDomain = name === 'emailDomain' ? value : formData.emailDomain;
      valueToValidate = `${currentEmailLocalPart}@${currentEmailDomain}`;

    } else if (name === 'phonePrefix' || name === 'phoneMiddle' || name === 'phoneLast') {
      fieldToValidate = 'phone';
      const currentPhonePrefix = name === 'phonePrefix' ? value : formData.phonePrefix;
      const currentPhoneMiddle = name === 'phoneMiddle' ? value : formData.phoneMiddle;
      const currentPhoneLast = name === 'phoneLast' ? value : formData.phoneLast;
      valueToValidate = `${currentPhonePrefix}${currentPhoneMiddle}${currentPhoneLast}`;
    }

    // validateField 호출 시 shouldSkipCustomValidation을 전달
    const error = validateField(fieldToValidate, valueToValidate, shouldSkipCustomValidation);

    setErrors(prevErrors => ({
      ...prevErrors,
      [fieldToValidate]: error,
      form: '', // 필드 에러 발생 시 전체 폼 에러는 지웁니다.
    }));
  }, [formData, validateField, setErrors]);

  /**
   * @function validateForm
   * @description
   * 전체 폼의 유효성을 검사합니다. 모든 필드가 유효하면 true를 반환하고,
   * 그렇지 않으면 false를 반환하며 에러 메시지를 `errors` 상태에 업데이트합니다.
   * 이 함수는 폼 제출 시 호출되며, 모든 유효성 규칙(custom 포함)을 적용합니다.
   *
   * @returns {boolean} 폼의 유효성 여부
   */
  const validateForm = useCallback(() => {
    const newErrors: FormErrors<SignupFormData> = {};
    let isValid = true;

    // 모든 필드에 대해 유효성 검사
    (Object.keys(formData) as Array<keyof SignupFormData>).forEach(field => {
      let error: string | undefined;
      let value = formData[field] as string; // 기본값

      if (field === 'emailLocalPart' || field === 'emailDomain') {
        const fullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;
        error = validateField('email', fullEmail, false); // 최종 검사이므로 custom validation 스킵 안 함
        if (error) {
          newErrors.email = error;
          isValid = false;
        } else if (isEmailAvailable === null) { // 이메일 중복 확인이 아직 안 된 경우
          newErrors.email = '이메일 중복 확인이 필요합니다.';
          isValid = false;
        } else if (isEmailAvailable === false) { // 이메일이 이미 사용 중인 경우
          newErrors.email = '이미 사용 중인 이메일입니다.';
          isValid = false;
        }
      } else if (field === 'phonePrefix' || field === 'phoneMiddle' || field === 'phoneLast') {
        const fullPhone = `${formData.phonePrefix}${formData.phoneMiddle}${formData.phoneLast}`;
        error = validateField('phone', fullPhone, false); // 최종 검사이므로 custom validation 스킵 안 함
        if (error) {
          newErrors.phone = error;
          isValid = false;
        }
      } else if (field === 'userId') {
        error = validateField('userId', formData.userId, false); // 최종 검사이므로 custom validation 스킵 안 함
        if (error) {
          newErrors.userId = error;
          isValid = false;
        } else if (isUserIdAvailable === null) { // 아이디 중복 확인이 아직 안 된 경우
          newErrors.userId = '아이디 중복 확인이 필요합니다.';
          isValid = false;
        } else if (isUserIdAvailable === false) { // 아이디가 이미 사용 중인 경우
          newErrors.userId = '이미 사용 중인 아이디입니다.';
          isValid = false;
        }
      } else {
        // 일반 필드 검사 (userName 포함)
        error = validateField(field, value, false); // 최종 검사이므로 custom validation 스킵 안 함
        if (error) {
          newErrors[field] = error;
          isValid = false;
        }
      }
    });

    // 비밀번호 확인은 모든 필드 검사 후 별도로 한번 더 확인 (formData.userPwd 필요)
    // validateField('confirmUserPwd', formData.confirmUserPwd, false) 호출을 통해 처리 가능
    // 하지만 현재 custom 로직이 이미 잘 작동하므로, 명시적으로 한번 더 체크하는 것도 좋습니다.
    const confirmPwdError = validateField('confirmUserPwd', formData.confirmUserPwd, false);
    if (confirmPwdError) {
        newErrors.confirmUserPwd = confirmPwdError;
        isValid = false;
    }
    // 추가: 비밀번호 확인 필드가 비어있는 경우
    if (!formData.confirmUserPwd.trim()) {
      newErrors.confirmUserPwd = '비밀번호 확인은 필수 입력 항목입니다.';
      isValid = false;
    }

    setErrors(newErrors); // 모든 에러를 한 번에 업데이트
    return isValid;
  }, [formData, validateField, isUserIdAvailable, isEmailAvailable]); // isUserIdAvailable, isEmailAvailable을 의존성에 추가


  /**
   * @function clearFormError
   * @description 전체 폼 에러 메시지(`errors.form`)를 초기화합니다.
   */
  const clearFormError = useCallback(() => {
    setErrors(prev => ({ ...prev, form: '' })); // 'form' 에러만 초기화
  }, [setErrors]);

  return {
    errors,
    validateForm,
    handleFieldChangeAndValidate,
    setErrors,
    validateField,
    clearFormError,
  };
};