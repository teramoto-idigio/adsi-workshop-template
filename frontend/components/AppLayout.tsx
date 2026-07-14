"use client";

import { useAuth } from "@/lib/auth-context";
import { Sidebar } from "./Sidebar";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export function AppLayout({ children }: { children: React.ReactNode }) {
  const { user, loading, logout } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) {
      const dir = window.location.pathname.replace(/\/[^/]*$/, "");
      window.location.href = `${dir}/login`;
    }
  }, [loading, user]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">読み込み中...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <header className="h-14 border-b flex items-center justify-between px-6 bg-white">
          <h1 className="font-semibold text-lg">勤怠管理</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {user.name}（{user.departmentName}）
            </span>
            <button
              onClick={() => logout().then(() => router.push("/login"))}
              className="text-sm text-red-600 hover:underline"
            >
              ログアウト
            </button>
          </div>
        </header>
        <main className="flex-1 p-6 bg-gray-50">{children}</main>
      </div>
    </div>
  );
}
