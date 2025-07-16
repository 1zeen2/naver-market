// # 재사용 가능한 드롭다운 컴포넌트

interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  name: string;
  options: SelectOption[];
  error?: string;
  onBlur?: React.FocusEventHandler<HTMLSelectElement>; // onBlur 이벤트 핸들러 추가
}

const Select = ({ label, name, value, onChange, options, error, className = '', onBlur, ...rest }: SelectProps) => (
  <div className={`mb-4 ${className}`}>
    <label htmlFor={name} className="block text-gray-700 text-sm font-bold mb-2">{label}</label>
    <select
      id={name}
      name={name}
      value={value}
      onChange={onChange}
      onBlur={onBlur}
      className={`shadow border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${error ? 'border-red-500' : ''}`}
      {...rest}
    >
      <option value="">{label} 선택</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>{option.label}</option>
      ))}
    </select>
    {error && <p className="text-red-500 text-xs italic">{error}</p>}
  </div>
);

export default Select;