import axios from "axios";

// Use VITE_API_URL if provided (e.g., in Vercel env), fallback to localhost for dev
const API_BASE_URL =
  typeof import.meta !== "undefined" &&
  import.meta.env &&
  import.meta.env.VITE_API_URL
    ? import.meta.env.VITE_API_URL
    : "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Contest API
export const contestAPI = {
  getContest: (contestId) => api.get(`/contests/${contestId}`),
  getLeaderboard: (contestId) => api.get(`/contests/${contestId}/leaderboard`),
};

// Submission API
export const submissionAPI = {
  submitCode: (data) => api.post("/submissions", data),
  getSubmission: (submissionId) => api.get(`/submissions/${submissionId}`),
};

// Health check
export const healthCheck = () => api.get("/health");

export default api;
