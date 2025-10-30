import { motion } from "framer-motion";

const ProblemViewer = ({
  problem,
  onSelectProblem,
  problems,
  selectedProblemId,
}) => {
  if (!problem) {
    return (
      <div className="glass-card p-8 text-center text-gray-500">
        <div className="text-6xl mb-4">📝</div>
        <p>Select a problem from the list to start coding</p>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col space-y-3 min-h-0">
      {/* Problem Selector */}
      <div className="flex space-x-2 overflow-x-auto pb-2">
        {problems.map((p) => (
          <button
            key={p.id}
            onClick={() => onSelectProblem(p.id)}
            className={`px-4 py-2 rounded-lg font-medium transition-all whitespace-nowrap ${
              selectedProblemId === p.id
                ? "bg-blue-500 text-white"
                : "glass-card text-gray-300 hover:text-white hover:bg-glass"
            }`}
          >
            {p.title}
          </button>
        ))}
      </div>

      {/* Problem Content */}
      <motion.div
        key={problem.id}
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        className="glass-card flex-1 min-h-0 overflow-hidden flex flex-col"
      >
        {/* Header */}
        <div className="p-6 border-b border-glass-border">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-white mb-2">
                {problem.title}
              </h2>
              <div className="flex items-center space-x-4 text-sm">
                <span className="status-badge status-pending">
                  {problem.points} Points
                </span>
                <span className="text-gray-400">
                  Time Limit: {problem.timeLimit}s
                </span>
                <span className="text-gray-400">
                  Memory: {problem.memoryLimit}MB
                </span>
              </div>
            </div>
            <div className="flex flex-col items-end space-y-1 text-xs text-gray-500">
              <span>{problem.totalSubmissions || 0} submissions</span>
              <span className="text-green-400">
                {problem.acceptedSubmissions || 0} accepted
              </span>
            </div>
          </div>
        </div>

        {/* Problem Description */}
        <div className="p-6 space-y-6 flex-1 min-h-0 overflow-auto">
          <div>
            <h3 className="text-lg font-semibold text-white mb-3">
              Problem Description
            </h3>
            <div className="text-gray-300 whitespace-pre-wrap leading-relaxed">
              {problem.description}
            </div>
          </div>

          {/* Constraints */}
          {problem.constraints && (
            <div>
              <h3 className="text-lg font-semibold text-white mb-3">
                Constraints
              </h3>
              <div className="text-gray-300 whitespace-pre-wrap font-mono text-sm bg-dark-surface/50 p-4 rounded-lg">
                {problem.constraints}
              </div>
            </div>
          )}

          {/* Sample Input/Output */}
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <h3 className="text-lg font-semibold text-white mb-3">
                Sample Input
              </h3>
              <div className="bg-dark-surface/50 p-4 rounded-lg">
                <pre className="text-gray-300 font-mono text-sm overflow-x-auto">
                  {problem.sampleInput}
                </pre>
              </div>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-white mb-3">
                Sample Output
              </h3>
              <div className="bg-dark-surface/50 p-4 rounded-lg">
                <pre className="text-gray-300 font-mono text-sm overflow-x-auto">
                  {problem.sampleOutput}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default ProblemViewer;
