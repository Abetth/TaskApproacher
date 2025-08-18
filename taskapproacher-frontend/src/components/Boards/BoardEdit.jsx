// src/components/Boards/BoardEdit.jsx
import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Form, FormGroup, Input, Label } from 'reactstrap';
import api from '../../services/api';
import { AuthContext } from '../../contexts/AuthContext';

function BoardEdit() {
  const { user } = useContext(AuthContext);
  const { boardId } = useParams();
  const navigate = useNavigate();
  const [board, setBoard] = useState({ title: '', isSorted: false });

  useEffect(() => {
    fetchBoard();
  }, [boardId]);

  const fetchBoard = async () => {
    try {
      const res = await api.get(`/boards/${boardId}`);
      setBoard({ title: res.data.title, isSorted: res.data.isSorted });
    } catch (error) {
      console.error('Error fetching board:', error);
    }
  };

  const updateBoard = async () => {
    try {
      await api.put('/boards', { id: boardId, title: board.title, isSorted: board.isSorted, user: { id: user.id } });
      navigate('/');
    } catch (error) {
      console.error('Error updating board:', error);
    }
  };

  return (
    <div>
      <h2>Edit Board</h2>
      <Form>
        <FormGroup>
          <Label>Title</Label>
          <Input
            type="text"
            value={board.title}
            onChange={(e) => setBoard({ ...board, title: e.target.value })}
            placeholder="Board title"
          />
        </FormGroup>
        <FormGroup>
          <Label>Sorting</Label>
          <Input
            type="select"
            value={board.isSorted}
            onChange={(e) => setBoard({ ...board, isSorted: e.target.value === 'true' })}
          >
            <option value={false}>Disabled</option>
            <option value={true}>Enabled</option>
          </Input>
        </FormGroup>
        <Button color="primary" onClick={updateBoard}>
          Update Board
        </Button>
        <Button color="secondary" onClick={() => navigate('/')} className="ms-2">
          Cancel
        </Button>
      </Form>
    </div>
  );
}

export default BoardEdit;