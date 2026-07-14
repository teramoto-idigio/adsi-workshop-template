function getApiBase(): string {
  if (typeof window === "undefined") return "/api";
  const dir = window.location.pathname.replace(/\/[^/]*$/, "");
  return `${dir}/api`;
}

export function withBasePath(path: string): string {
  return path;
}

export async function apiClient<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const url = `${getApiBase()}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
    credentials: "include",
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "エラーが発生しました" }));
    throw new ApiError(res.status, error.message || "エラーが発生しました");
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json();
}

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
    this.name = "ApiError";
  }
}
