import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import JoinPage from "./pages/JoinPage";
import ContestPage from "./pages/ContestPage";

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-dark-bg">
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: "#1a1a1a",
              color: "#fff",
              border: "1px solid rgba(255, 255, 255, 0.1)",
            },
          }}
        />

        <Routes>
          <Route path="/" element={<JoinPage />} />
          <Route path="/contest/:contestId" element={<ContestPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
