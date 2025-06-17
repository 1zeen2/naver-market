"use client";

export default function SearchBar() {
    return (
        <div className="flex justify-center py-2">
            <input
                type="text"
                placeholder="검색어를 입력하세요."
                className="w-1/2 p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 placeholder-center text-lg"
            />
        </div>
    )
}