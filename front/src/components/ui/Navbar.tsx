"use client";

import Link from "next/link";

const menuItems = [
    { name: "판매 상품", path: "/products" },
    { name: "커뮤니티", path: "/community" },
    { name: "문의", path: "/contact" },
    { name: "이벤트", path: "/events" },
    { name: "고객센터", path: "/support" }
];

export default function Navbar() {
  return (
    <nav className="py-3">
      <ul className="flex justify-center gap-6">
          {menuItems.map((item) => (
            <li key={item.name}>
              <Link href={item.path} className="text-gray-700 hover:text-blue-500 font-medium">
                {item.name}
              </Link>
            </li>
          ))}
      </ul>
    </nav>
  );
}