"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-context";

const NAV_ITEMS = [
  { href: "/", label: "ダッシュボード", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/attendance", label: "勤怠", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/leave", label: "有給申請", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/overtime", label: "残業申請", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/approvals", label: "承認", roles: ["MANAGER", "ADMIN"] },
  { href: "/team", label: "チーム勤怠", roles: ["MANAGER", "ADMIN"] },
  { href: "/admin/employees", label: "社員管理", roles: ["ADMIN"] },
  { href: "/admin/departments", label: "部署管理", roles: ["ADMIN"] },
  { href: "/admin/leave", label: "有給付与", roles: ["ADMIN"] },
];

export function Sidebar() {
  const { user } = useAuth();
  if (!user) return null;

  const visibleItems = NAV_ITEMS.filter((item) =>
    item.roles.includes(user.role)
  );

  return (
    <aside className="w-56 bg-gray-900 text-white min-h-screen p-4">
      <nav className="space-y-1">
        {visibleItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className="block px-3 py-2 rounded hover:bg-gray-700 text-sm"
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
