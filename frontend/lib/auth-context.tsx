"use client";

import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { apiClient, ApiError } from "./api-client";

interface Employee {
  id: number;
  name: string;
  email: string;
  role: string;
  departmentId: number;
  departmentName: string;
  active: boolean;
}

interface AuthContextValue {
  user: Employee | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient<Employee>("/auth/me")
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const params = new URLSearchParams({ email, password });
    const res = await apiClient<{ employee: Employee }>(`/auth/login?${params}`);
    setUser(res.employee);
  }, []);

  const logout = useCallback(async () => {
    await apiClient("/auth/logout", { method: "POST" });
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
