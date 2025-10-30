import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

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

