import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { contestAPI } from "../services/api";

const Leaderboard = ({ contestId }) => {
  const [leaderboard, setLeaderboard] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchLeaderboard = async () => {
    try {
      const response = await contestAPI.getLeaderboard(contestId);
      setLeaderboard(response.data);
    } catch (error) {
      console.error("Error fetching leaderboard:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLeaderboard();
    // Poll every 15 seconds
    const interval = setInterval(fetchLeaderboard, 15000);
    return () => clearInterval(interval);
  }, [contestId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  const currentUsername = localStorage.getItem("username");

  return (
    <div className="h-full flex flex-col space-y-3 min-h-0">
      {/* Header Stats */}
      <div className="grid grid-cols-3 gap-3">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass-card p-4 text-center"
        >
          <div className="text-3xl font-bold text-blue-400">
            {leaderboard?.entries?.length || 0}
          </div>
          <div className="text-sm text-gray-400 mt-1">Total Participants</div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="glass-card p-4 text-center"
        >
          <div className="text-3xl font-bold text-green-400">
            {leaderboard?.entries?.[0]?.problemsSolved || 0}
          </div>
          <div className="text-sm text-gray-400 mt-1">Max Problems Solved</div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="glass-card p-4 text-center"
        >
          <div className="text-3xl font-bold text-purple-400">
            {new Date(leaderboard?.lastUpdated).toLocaleTimeString()}
          </div>
          <div className="text-sm text-gray-400 mt-1">Last Updated</div>
        </motion.div>
      </div>

      {/* Leaderboard Table */}
      <div className="glass-card overflow-hidden flex-1 min-h-0">
        <div className="p-4 border-b border-glass-border">
          <h3 className="text-xl font-bold text-white flex items-center">
            <span className="mr-2">🏆</span> Live Leaderboard
          </h3>
        </div>

        {leaderboard?.entries?.length > 0 ? (
          <div className="overflow-x-auto overflow-y-auto max-h-full">
            <table className="w-full">
              <thead>
                <tr className="border-b border-glass-border">
                  <th className="text-left p-4 text-gray-400 font-medium">
                    Rank
                  </th>
                  <th className="text-left p-4 text-gray-400 font-medium">
                    Username
                  </th>
                  <th className="text-center p-4 text-gray-400 font-medium">
                    Problems
                  </th>
                  <th className="text-center p-4 text-gray-400 font-medium">
                    Points
                  </th>
                  <th className="text-right p-4 text-gray-400 font-medium">
                    Time
                  </th>
                </tr>
              </thead>
              <tbody>
                {leaderboard.entries.map((entry, index) => {
                  const isCurrentUser = entry.username === currentUsername;
                  return (
                    <motion.tr
                      key={entry.username}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.05 }}
                      className={`border-b border-glass-border hover:bg-glass transition-colors ${
                        isCurrentUser ? "bg-blue-500/10" : ""
                      }`}
                    >
                      <td className="p-4">
                        <div className="flex items-center">
                          {entry.rank === 1 && (
                            <span className="mr-2 text-2xl">🥇</span>
                          )}
                          {entry.rank === 2 && (
                            <span className="mr-2 text-2xl">🥈</span>
                          )}
                          {entry.rank === 3 && (
                            <span className="mr-2 text-2xl">🥉</span>
                          )}
                          {entry.rank > 3 && (
                            <span className="text-gray-400 font-mono">
                              #{entry.rank}
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center">
                          <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center mr-3">
                            <span className="text-white text-sm font-bold">
                              {entry.username.charAt(0).toUpperCase()}
                            </span>
                          </div>
                          <span
                            className={`font-medium ${
                              isCurrentUser ? "text-blue-400" : "text-white"
                            }`}
                          >
                            {entry.username}
                            {isCurrentUser && (
                              <span className="ml-2 text-xs">(You)</span>
                            )}
                          </span>
                        </div>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-green-400 font-semibold">
                          {entry.problemsSolved}
                        </span>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-yellow-400 font-semibold">
                          {entry.totalPoints}
                        </span>
                      </td>
                      <td className="p-4 text-right text-gray-300 font-mono text-sm">
                        {Math.floor(entry.totalTime / 60000)}:
                        {String(
                          Math.floor((entry.totalTime % 60000) / 1000)
                        ).padStart(2, "0")}
                      </td>
                    </motion.tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-12 text-center text-gray-500">
            <div className="text-6xl mb-4">🎯</div>
            <p>No submissions yet. Be the first to solve a problem!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Leaderboard;
