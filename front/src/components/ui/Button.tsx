// # 재사용 가능한 버튼 컴포넌트

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode; // 버튼 내부에 들어갈 내용 (텍스트, 아이콘 등)
  onClick?: React.MouseEventHandler<HTMLButtonElement>;
  disabled?: boolean;
}

const Button = ({ children, onClick, disabled = false, className = '', type = 'button', ...rest }: ButtonProps) => (
  <button
    type={type} // 기본 타입은 'button'으로 설정 (폼 제출 방지)
    onClick={onClick}
    disabled={disabled}
    className={`bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline ${disabled ? 'opacity-50 cursor-not-allowed' : ''} ${className}`}
    {...rest}
  >
    {children}
  </button>
);

export default Button;