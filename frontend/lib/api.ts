export const API_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function fetchAPI(
  endpoint: string,
  options: RequestInit = {} //method, headers, body, etc.
) {
  const res = await fetch(`${API_URL}${endpoint}`, options);
  if (!res.ok) {
    throw new Error(`Failed to fetch ${endpoint}: ${res.statusText}`);
  }
  return res.json();
}
