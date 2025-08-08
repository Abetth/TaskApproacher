import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { Container } from 'reactstrap';
import BoardList from './components/BoardList';
import TaskList from './components/TaskList';
import BoardUpdate from './components/BoardUpdate';
import Navbar from './components/Navbar';
import 'bootstrap/dist/css/bootstrap.min.css';

function AppContent() {
  const [boards, setBoards] = useState([]);
  const location = useLocation();

  useEffect(() => {
    fetchBoards();
  }, [location.pathname]);

  const fetchBoards = async () => {
    try {
      const response = await fetch('/api/boards');
      if (!response.ok) throw new Error('Failed to fetch boards');
      const data = await response.json();
      setBoards(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching boards:', error);
    }
  };

  return (
    <Container className="mt-4">
      <Routes>
        <Route path="/" element={<BoardList boards={boards} fetchBoards={fetchBoards} />} />
        <Route path="/board/:boardId" element={<TaskList />} />
        <Route path="/board/:boardId/update" element={<BoardUpdate fetchBoards={fetchBoards} />} />
      </Routes>
    </Container>
  );
}

function App() {
  return (
    <Router>
      <Navbar />
      <AppContent />
    </Router>
  );
}

export default App;