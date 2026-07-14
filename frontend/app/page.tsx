"use client";

import { AppLayout } from "@/components/AppLayout";
import { useAuth } from "@/lib/auth-context";

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <AppLayout>
      <div className="max-w-4xl">
        <h2 className="text-2xl font-bold mb-6">ダッシュボード</h2>
        {user && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">ようこそ</p>
              <p className="text-xl font-semibold mt-1">{user.name}</p>
              <p className="text-sm text-gray-500 mt-1">{user.departmentName}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">ロール</p>
              <p className="text-xl font-semibold mt-1">
                {user.role === "ADMIN" && "管理者"}
                {user.role === "MANAGER" && "マネージャー"}
                {user.role === "EMPLOYEE" && "一般社員"}
              </p>
            </div>
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">今日の勤怠</p>
              <p className="text-xl font-semibold mt-1 text-gray-400">未入力</p>
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  );
}
