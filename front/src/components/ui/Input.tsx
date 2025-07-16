/**
 * Input 컴포넌트의 props 타입을 정의합니다.
 * React의 input 요소가 받을 수 있는 모든 표준 HTML 속성을 포함하고,
 * 추가적인 커스텀 속성(label, error, onBlur 등)을 정의합니다.
 */

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string; // '?'을 사용하여 선택 사항으로 설정
  name: string;
  error?: string; // '?'을 사용하여 선택 사항으로 설정
  onBlur?: React.FocusEventHandler<HTMLInputElement>; // onBlur 이벤트 핸들러 추가
}

const Input = ({ label, name, type, value, onChange, error, onBlur, className = '', ...rest }: InputProps) => {
  // readOnly 속성을 rest 객체에서 직접 추출.
  const { readOnly, ...inputProps } = rest;

  return (
    <div className={`${className}`}>
      {/* label이 있을 때만 <label> 태그를 렌더링 */}
      {label && <label htmlFor={name} className="block text-gray-700 text-sm font-bold mb-2">{label}</label>}
      <input
        type={type}
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        readOnly={readOnly}
        className={`
          shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight 
          focus:outline-none focus:shadow-outline 
          ${error ? 'border-red-500' : ''}
          ${readOnly ? 'bg-gray-100 text-gray-700 cursor-not-allowed border-gray-300' : ''} 
        `}
        {...inputProps}
      />
      {/* 에러 메시지 공간은 항상 확보하는 div는 그대로 유지합니다. */}
      <div className="min-h-[1.25rem]"> 
        {error && <p className="text-red-500 text-xs italic text-red-500 mt-1">{error}</p>}
      </div>
    </div>
  );
};

export default Input;