import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import toast from "react-hot-toast";
import { contestAPI } from "../services/api";
import ProblemViewer from "../components/ProblemViewer";
import CodeEditor from "../components/CodeEditor";
import Leaderboard from "../components/Leaderboard";

const ContestPage = () => {
  const { contestId } = useParams();
  const navigate = useNavigate();
  const [contest, setContest] = useState(null);
  const [selectedProblemId, setSelectedProblemId] = useState(null);
  const [activeTab, setActiveTab] = useState("problem");
  const [loading, setLoading] = useState(true);
  const [leaderboardKey, setLeaderboardKey] = useState(0);

  useEffect(() => {
    const username = localStorage.getItem("username");
    if (!username) {
      toast.error("Please join the contest first");
      navigate("/");
      return;
    }

    fetchContest();
    // Periodically refresh contest (problem stats and participants)
    const id = setInterval(fetchContest, 15000);
    return () => clearInterval(id);
  }, [contestId, navigate]);

  const fetchContest = async () => {
    try {
      const response = await contestAPI.getContest(contestId);
      const data = response.data;
      setContest(data);
      // preserve selection if valid; set default only when none/invalid
      setSelectedProblemId((prev) => {
        const list = data.problems || [];
        if (!prev || !list.some((p) => p.id === prev)) {
          return list.length ? list[0].id : null;
        }
        return prev;
      });
    } catch (error) {
      toast.error("Failed to load contest");
      navigate("/");
    } finally {
      setLoading(false);
    }
  };

  const handleLeave = () => {
    if (window.confirm("Are you sure you want to leave the contest?")) {
      localStorage.removeItem("username");
      localStorage.removeItem("contestId");
      navigate("/");
    }
  };

  const handleSubmissionUpdate = () => {
    // Refresh leaderboard after successful submission
    setLeaderboardKey((prev) => prev + 1);
    // Also refresh contest details to update submissions/accepted/participants
    fetchContest();
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  const selectedProblem = contest?.problems?.find(
    (p) => p.id === selectedProblemId
  );
  const username = localStorage.getItem("username");

  return (
    <div className="h-screen flex flex-col overflow-hidden pb-6">
      {/* Header */}
      <header className="glass-card border-b border-glass-border backdrop-blur-lg">
        <div className="container mx-auto px-6 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-6">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                  <span className="text-white font-bold">S</span>
                </div>
                <span className="text-xl font-bold text-gradient">
                  Shodh-a-Code
                </span>
              </div>

              <div className="hidden md:flex items-center space-x-4 text-sm">
                <span className="text-gray-400">Contest:</span>
                <span className="text-white font-semibold">
                  {contest?.title}
                </span>
                {contest?.isActive && (
                  <span className="px-2 py-1 bg-green-500/20 text-green-400 rounded-full text-xs font-semibold">
                    LIVE
                  </span>
                )}
              </div>
            </div>

            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-pink-600 rounded-full flex items-center justify-center">
                  <span className="text-white text-sm font-bold">
                    {username?.charAt(0).toUpperCase()}
                  </span>
                </div>
                <span className="text-white font-medium">{username}</span>
              </div>

              <button
                onClick={handleLeave}
                className="px-4 py-2 text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition-all"
              >
                Leave
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 container mx-auto px-6 py-4 overflow-hidden">
        <div className="grid lg:grid-cols-2 gap-3 h-full min-h-0">
          {/* Left Panel - Problem/Leaderboard */}
          <div className="flex flex-col space-y-3 h-full min-h-0">
            {/* Tab Switcher */}
            <div className="glass-card p-1 flex space-x-1">
              <button
                onClick={() => setActiveTab("problem")}
                className={`flex-1 px-4 py-2 rounded-lg font-medium transition-all ${
                  activeTab === "problem"
                    ? "bg-blue-500 text-white"
                    : "text-gray-400 hover:text-white hover:bg-glass"
                }`}
              >
                Problems
              </button>
              <button
                onClick={() => setActiveTab("leaderboard")}
                className={`flex-1 px-4 py-2 rounded-lg font-medium transition-all ${
                  activeTab === "leaderboard"
                    ? "bg-blue-500 text-white"
                    : "text-gray-400 hover:text-white hover:bg-glass"
                }`}
              >
                Leaderboard
              </button>
            </div>

            {/* Tab Content */}
            <div className="flex-1 overflow-hidden min-h-0">
              {activeTab === "problem" ? (
                <div className="h-full min-h-0">
                  <ProblemViewer
                    problem={selectedProblem}
                    problems={contest?.problems || []}
                    selectedProblemId={selectedProblemId}
                    onSelectProblem={setSelectedProblemId}
                  />
                </div>
              ) : (
                <div className="h-full overflow-hidden min-h-0">
                  <Leaderboard key={leaderboardKey} contestId={contestId} />
                </div>
              )}
            </div>
          </div>

          {/* Right Panel - Code Editor */}
          <div className="flex flex-col h-full overflow-hidden min-h-0">
            <CodeEditor
              problemId={selectedProblemId}
              contestId={contestId}
              problem={selectedProblem}
              onSubmissionUpdate={handleSubmissionUpdate}
            />
          </div>
        </div>
      </div>

      {/* Footer Status Bar */}
      <footer className="glass-card border-t border-glass-border">
        <div className="container mx-auto px-6 py-2">
          <div className="flex items-center justify-between text-xs text-gray-500">
            <div className="flex items-center space-x-4">
              <span>{contest?.problems?.length || 0} Problems</span>
              <span>{contest?.participantsCount || 0} Participants</span>
            </div>
            <div className="flex items-center space-x-4">
              <span className="flex items-center">
                <span className="inline-block w-2 h-2 bg-green-400 rounded-full mr-2 animate-pulse"></span>
                Connected
              </span>
              <span>{new Date().toLocaleTimeString()}</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default ContestPage;
