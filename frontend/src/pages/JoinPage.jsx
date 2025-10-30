import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import toast from "react-hot-toast";
import { contestAPI } from "../services/api";

const JoinPage = () => {
  const navigate = useNavigate();
  const [contestId, setContestId] = useState("");
  const [username, setUsername] = useState("");
  const [loading, setLoading] = useState(false);

  const handleJoin = async (e) => {
    e.preventDefault();

    if (!contestId || !username) {
      toast.error("Please fill in all fields");
      return;
    }

    setLoading(true);
    try {
      // Verify contest exists
      await contestAPI.getContest(contestId);

      // Store user info in localStorage
      localStorage.setItem("username", username);
      localStorage.setItem("contestId", contestId);

      toast.success("Joining contest...");
      navigate(`/contest/${contestId}`);
    } catch (error) {
      toast.error("Invalid contest ID or contest not found");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="glass-card border-b border-glass-border backdrop-blur-lg">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              className="flex items-center space-x-2"
            >
              <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center">
                <span className="text-white font-bold text-xl">S</span>
              </div>
              <span className="text-2xl font-bold text-gradient">
                Shodh-a-Code
              </span>
            </motion.div>

            <nav className="hidden md:flex items-center space-x-8">
              <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
              >
                Home
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
              >
                About
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
              >
                Contact
              </a>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center p-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="w-full max-w-md"
        >
          <div className="glass-card p-8 space-y-8">
            {/* Title */}
            <div className="text-center space-y-2">
              <h1 className="text-4xl font-bold text-white">Join Contest</h1>
              <p className="text-gray-400">
                Enter your details to start coding
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleJoin} className="space-y-6">
              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-300">
                  Contest ID
                </label>
                <input
                  type="text"
                  value={contestId}
                  onChange={(e) => setContestId(e.target.value)}
                  className="input-field"
                  placeholder="Enter contest ID (e.g., 1)"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-300">
                  Username
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="input-field"
                  placeholder="Choose your username"
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-6 btn-glow border border-accent-blue rounded-xl text-white font-semibold
                         hover:bg-accent-blue/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
              >
                {loading ? (
                  <span className="flex items-center justify-center">
                    <svg
                      className="animate-spin h-5 w-5 mr-3"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                        fill="none"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    Joining...
                  </span>
                ) : (
                  "Join Contest"
                )}
              </button>
            </form>

            {/* Features */}
            <div className="grid grid-cols-3 gap-4 pt-6 border-t border-glass-border">
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-400">Live</div>
                <div className="text-xs text-gray-500">Judging</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-400">
                  Real-time
                </div>
                <div className="text-xs text-gray-500">Leaderboard</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-purple-400">
                  Instant
                </div>
                <div className="text-xs text-gray-500">Feedback</div>
              </div>
            </div>
          </div>

          {/* Demo Info */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
            className="mt-6 text-center text-sm text-gray-500"
          >
            <p>
              Try Contest ID:{" "}
              <span className="text-white font-mono bg-dark-surface px-2 py-1 rounded">
                1
              </span>{" "}
              for demo
            </p>
          </motion.div>
        </motion.div>
      </div>

      {/* Footer */}
      <footer className="glass-card border-t border-glass-border backdrop-blur-lg">
        <div className="container mx-auto px-6 py-4">
          <div className="text-center text-sm text-gray-500">
            © 2024 Shodh-a-Code. Built with React & Spring Boot.
          </div>
        </div>
      </footer>
    </div>
  );
};

export default JoinPage;

