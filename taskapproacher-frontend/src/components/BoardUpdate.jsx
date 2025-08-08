import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Col, Input, Label, Row } from 'reactstrap';

function BoardUpdate({ fetchBoards }) {
  const { boardId } = useParams();
  const navigate = useNavigate();
  const [board, setBoard] = useState({ title: '', isSorted: false });

  useEffect(() => {
    fetchBoard();
  }, [boardId]);

  const fetchBoard = async () => {
    try {
      const response = await fetch(`/api/boards/${boardId}`);
      if (!response.ok) throw new Error('Failed to fetch board');
      const data = await response.json();
      setBoard({ title: data.title, isSorted: data.isSorted });
    } catch (error) {
      console.error('Error fetching board:', error);
    }
  };

  const updateBoard = async () => {
    try {
      const response = await fetch('/api/boards', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: boardId, title: board.title, isSorted: board.isSorted }),
      });
      if (!response.ok) throw new Error('Failed to update board');
      fetchBoards();
      navigate('/');
    } catch (error) {
      console.error('Error updating board:', error);
    }
  };

  return (
    <div>
      <h2>Update Board</h2>
      <Row className="mb-3">
        <Col md="6">
          <Label className="mt-2">Title</Label>
          <Input
            type="text"
            value={board.title}
            onChange={(e) => setBoard({ ...board, title: e.target.value })}
            placeholder="Board title"
          />
          <Label className="mt-2">Sorting</Label>
          <Input
            type="select"
            value={board.isSorted}
            onChange={(e) => setBoard({ ...board, isSorted: e.target.value === 'true' })}
          >
            <option value={true}>Enabled</option>
            <option value={false}>Disabled</option>
          </Input>
          <Button color="primary" onClick={updateBoard} className="mt-3">
            Update Board
          </Button>
          <Button color="secondary" onClick={() => navigate('/')} className="mt-3 ms-2">
            Cancel
          </Button>
        </Col>
      </Row>
    </div>
  );
}

export default BoardUpdate;