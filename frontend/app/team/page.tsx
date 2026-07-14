"use client";

import { useState, useEffect } from "react";
import { AppLayout } from "@/components/AppLayout";
import { apiClient } from "@/lib/api-client";

interface MemberSummary {
  employeeId: number;
  employeeName: string;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  totalNightMinutes: number;
  workDays: number;
}

export default function TeamPage() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [members, setMembers] = useState<MemberSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    apiClient<MemberSummary[]>(`/manager/attendance?year=${year}&month=${month}`)
      .then(setMembers)
      .catch(() => setMembers([]))
      .finally(() => setLoading(false));
  }, [year, month]);

  const handleExport = () => {
    const dir = window.location.pathname.replace(/\/[^/]*$/, "");
    window.location.href = `${dir}/api/export/department-summary?year=${year}&month=${month}`;
  };

  return (
    <AppLayout>
      <div className="max-w-4xl">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold">チーム勤怠 — {year}年{month}月</h2>
          <button onClick={handleExport} className="px-4 py-2 bg-green-600 text-white rounded text-sm">
            CSV ダウンロード
          </button>
        </div>

        {loading ? (
          <p className="text-gray-500">読み込み中...</p>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-4 py-2 text-left">社員名</th>
                  <th className="px-4 py-2 text-right">勤務日数</th>
                  <th className="px-4 py-2 text-right">総労働</th>
                  <th className="px-4 py-2 text-right">残業</th>
                  <th className="px-4 py-2 text-right">深夜</th>
                </tr>
              </thead>
              <tbody>
                {members.map((m) => (
                  <tr key={m.employeeId} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-2">{m.employeeName}</td>
                    <td className="px-4 py-2 text-right">{m.workDays}日</td>
                    <td className="px-4 py-2 text-right">{fmt(m.totalWorkMinutes)}</td>
                    <td className="px-4 py-2 text-right text-orange-600">{fmt(m.totalOvertimeMinutes)}</td>
                    <td className="px-4 py-2 text-right text-purple-600">{fmt(m.totalNightMinutes)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AppLayout>
  );
}

function fmt(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${m.toString().padStart(2, "0")}m`;
}
