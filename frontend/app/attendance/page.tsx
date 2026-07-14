"use client";

import { useState, useEffect, useCallback } from "react";
import { AppLayout } from "@/components/AppLayout";
import { AttendanceForm } from "@/components/AttendanceForm";
import { SummaryCard } from "@/components/SummaryCard";
import { apiClient } from "@/lib/api-client";

interface AttendanceRecord {
  id: number;
  date: string;
  clockIn: string;
  clockOut: string | null;
  breakMinutes: number;
  note: string | null;
  workMinutes: number;
  overtimeMinutes: number;
  nightMinutes: number;
}

interface MonthlySummary {
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  totalNightMinutes: number;
  prescribedMinutes: number;
  balanceMinutes: number;
}

interface MonthlyResponse {
  year: number;
  month: number;
  records: AttendanceRecord[];
  summary: MonthlySummary;
}

export default function AttendancePage() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [data, setData] = useState<MonthlyResponse | null>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await apiClient<MonthlyResponse>(`/attendance?year=${year}&month=${month}`);
      setData(res);
    } catch {
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [year, month]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handlePrev = () => {
    if (month === 1) { setYear(year - 1); setMonth(12); }
    else setMonth(month - 1);
  };
  const handleNext = () => {
    if (month === 12) { setYear(year + 1); setMonth(1); }
    else setMonth(month + 1);
  };

  const daysInMonth = new Date(year, month, 0).getDate();
  const firstDow = new Date(year, month - 1, 1).getDay();
  const recordMap = new Map(data?.records.map(r => [r.date, r]) || []);

  const handleSubmit = async (formData: { clockIn: string; clockOut: string | null; breakMinutes: number; note: string | null }) => {
    if (!selectedDate) return;
    await apiClient(`/attendance/${selectedDate}`, {
      method: "PUT",
      body: JSON.stringify(formData),
    });
    await fetchData();
  };

  const handleExport = () => {
    const dir = window.location.pathname.replace(/\/[^/]*$/, "");
    window.location.href = `${dir}/api/export/attendance?year=${year}&month=${month}`;
  };

  const selectedRecord = selectedDate ? recordMap.get(selectedDate) : undefined;

  return (
    <AppLayout>
      <div className="max-w-5xl">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-4">
            <button onClick={handlePrev} className="px-3 py-1 border rounded">◀</button>
            <h2 className="text-xl font-bold">{year}年{month}月</h2>
            <button onClick={handleNext} className="px-3 py-1 border rounded">▶</button>
          </div>
          <button onClick={handleExport} className="px-4 py-2 bg-green-600 text-white rounded text-sm">
            CSV ダウンロード
          </button>
        </div>

        {data && <SummaryCard {...data.summary} />}

        {loading ? (
          <p className="mt-4 text-gray-500">読み込み中...</p>
        ) : (
          <div className="mt-4 bg-white rounded-lg shadow overflow-hidden">
            <div className="grid grid-cols-7 text-center text-sm font-medium bg-gray-100">
              {["日", "月", "火", "水", "木", "金", "土"].map(d => (
                <div key={d} className="py-2">{d}</div>
              ))}
            </div>
            <div className="grid grid-cols-7">
              {Array.from({ length: firstDow }, (_, i) => (
                <div key={`empty-${i}`} className="border-t border-l p-2 min-h-[80px]" />
              ))}
              {Array.from({ length: daysInMonth }, (_, i) => {
                const day = i + 1;
                const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
                const record = recordMap.get(dateStr);
                return (
                  <div
                    key={day}
                    className="border-t border-l p-2 min-h-[80px] cursor-pointer hover:bg-blue-50"
                    onClick={() => setSelectedDate(dateStr)}
                  >
                    <div className="text-xs font-medium">{day}</div>
                    {record && (
                      <div className="text-xs mt-1 space-y-0.5">
                        <div>{record.clockIn}-{record.clockOut || "?"}</div>
                        <div className="text-gray-500">{Math.floor(record.workMinutes / 60)}h{(record.workMinutes % 60).toString().padStart(2, "0")}m</div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {selectedDate && (
          <AttendanceForm
            date={selectedDate}
            initial={selectedRecord ? {
              clockIn: selectedRecord.clockIn,
              clockOut: selectedRecord.clockOut,
              breakMinutes: selectedRecord.breakMinutes,
              note: selectedRecord.note,
            } : undefined}
            onSubmit={handleSubmit}
            onClose={() => setSelectedDate(null)}
          />
        )}
      </div>
    </AppLayout>
  );
}
