"use client";

interface SummaryCardProps {
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  totalNightMinutes: number;
  prescribedMinutes: number;
  balanceMinutes: number;
}

export function SummaryCard(props: SummaryCardProps) {
  return (
    <div className="bg-white rounded-lg shadow p-4 grid grid-cols-2 md:grid-cols-5 gap-4 text-sm">
      <div>
        <p className="text-gray-500">総労働</p>
        <p className="text-lg font-semibold">{fmt(props.totalWorkMinutes)}</p>
      </div>
      <div>
        <p className="text-gray-500">残業</p>
        <p className="text-lg font-semibold text-orange-600">{fmt(props.totalOvertimeMinutes)}</p>
      </div>
      <div>
        <p className="text-gray-500">深夜</p>
        <p className="text-lg font-semibold text-purple-600">{fmt(props.totalNightMinutes)}</p>
      </div>
      <div>
        <p className="text-gray-500">所定</p>
        <p className="text-lg font-semibold">{fmt(props.prescribedMinutes)}</p>
      </div>
      <div>
        <p className="text-gray-500">過不足</p>
        <p className={`text-lg font-semibold ${props.balanceMinutes < 0 ? "text-red-600" : "text-green-600"}`}>
          {props.balanceMinutes >= 0 ? "+" : ""}{fmt(props.balanceMinutes)}
        </p>
      </div>
    </div>
  );
}

function fmt(minutes: number): string {
  const sign = minutes < 0 ? "-" : "";
  const abs = Math.abs(minutes);
  const h = Math.floor(abs / 60);
  const m = abs % 60;
  return `${sign}${h}h${m.toString().padStart(2, "0")}m`;
}
