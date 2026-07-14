"use client";

import { useState, useEffect } from "react";

interface AttendanceFormProps {
  date: string;
  initial?: { clockIn: string; clockOut: string | null; breakMinutes: number; note: string | null };
  onSubmit: (data: { clockIn: string; clockOut: string | null; breakMinutes: number; note: string | null }) => Promise<void>;
  onClose: () => void;
}

export function AttendanceForm({ date, initial, onSubmit, onClose }: AttendanceFormProps) {
  const [clockIn, setClockIn] = useState(initial?.clockIn || "09:00");
  const [clockOut, setClockOut] = useState(initial?.clockOut || "");
  const [breakMinutes, setBreakMinutes] = useState(initial?.breakMinutes ?? 60);
  const [note, setNote] = useState(initial?.note || "");
  const [loading, setLoading] = useState(false);

  const workMinutes = clockIn && clockOut
    ? Math.max(0, diffMinutes(clockIn, clockOut) - breakMinutes)
    : 0;

  const overtimeMinutes = Math.max(0, workMinutes - 510);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit({ clockIn, clockOut: clockOut || null, breakMinutes, note: note || null });
      onClose();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-sm" onClick={(e) => e.stopPropagation()}>
        <h3 className="text-lg font-bold mb-4">{date}</h3>
        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-sm font-medium mb-1">出勤時刻</label>
            <input type="time" value={clockIn} onChange={(e) => setClockIn(e.target.value)}
              className="w-full border rounded px-3 py-2" required />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">退勤時刻</label>
            <input type="time" value={clockOut} onChange={(e) => setClockOut(e.target.value)}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">休憩（分）</label>
            <input type="number" value={breakMinutes} onChange={(e) => setBreakMinutes(Number(e.target.value))}
              className="w-full border rounded px-3 py-2" min={0} required />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">備考</label>
            <input type="text" value={note} onChange={(e) => setNote(e.target.value)}
              className="w-full border rounded px-3 py-2" />
          </div>
          <div className="text-sm text-gray-600 pt-2 border-t">
            <p>勤務時間: {formatMinutes(workMinutes)}</p>
            <p>残業: {formatMinutes(overtimeMinutes)}</p>
          </div>
          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 border rounded py-2">キャンセル</button>
            <button type="submit" disabled={loading}
              className="flex-1 bg-blue-600 text-white rounded py-2 disabled:opacity-50">
              {loading ? "保存中..." : "保存"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function diffMinutes(start: string, end: string): number {
  const [sh, sm] = start.split(":").map(Number);
  const [eh, em] = end.split(":").map(Number);
  let diff = (eh * 60 + em) - (sh * 60 + sm);
  if (diff < 0) diff += 24 * 60;
  return diff;
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${m.toString().padStart(2, "0")}m`;
}
