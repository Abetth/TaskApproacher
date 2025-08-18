import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container } from 'reactstrap';
import Navbar from './components/Navbar/Navbar';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import BoardList from './components/Boards/BoardList';
import BoardEdit from './components/Boards/BoardEdit';
import TaskList from './components/Tasks/TaskList';
import UserProfile from './components/Auth/UserProfile';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider } from './contexts/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Container className="mt-4">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<ProtectedRoute><BoardList /></ProtectedRoute>} />
            <Route path="/board/:boardId" element={<ProtectedRoute><TaskList /></ProtectedRoute>} />
            <Route path="/board/:boardId/edit" element={<ProtectedRoute><BoardEdit /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><UserProfile /></ProtectedRoute>} />
          </Routes>
        </Container>
      </AuthProvider>
    </Router>
  );
}

export default App;